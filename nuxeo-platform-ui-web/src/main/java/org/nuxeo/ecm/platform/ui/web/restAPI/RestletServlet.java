/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thierry Delprat
 *     Florent Guillaume
 */

package org.nuxeo.ecm.platform.ui.web.restAPI;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.ui.web.restAPI.service.PluggableRestletService;
import org.nuxeo.ecm.platform.ui.web.restAPI.service.RestletPluginDescriptor;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;

import com.noelios.restlet.ext.servlet.ServletConverter;

/**
 * Servlet used to run a Restlet inside Nuxeo.
 * <p>
 * Setup Seam Restlet filter if needed.
 * <p>
 * Ensures a transaction is started/committed.
 */
public class RestletServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(RestletServlet.class);

    private static final long serialVersionUID = 1764653653643L;

    protected ServletConverter converter;

    protected PluggableRestletService service;

    @Override
    public synchronized void init() throws ServletException {
        super.init();

        if (converter != null) {
            log.error("RestletServlet initialized several times");
            return;
        }
        converter = new ServletConverter(getServletContext());

        // init the router
        Router restletRouter = new Router();

        // get the service
        service = (PluggableRestletService) Framework.getRuntime().getComponent(
                PluggableRestletService.NAME);
        if (service == null) {
            log.error("Unable to get Service " + PluggableRestletService.NAME);
            throw new ServletException(
                    "Can't initialize Nuxeo Pluggable Restlet Service");
        }

        for (String restletName : service.getContributedRestletNames()) {
            RestletPluginDescriptor plugin = service.getContributedRestletDescriptor(restletName);

            Restlet restletToAdd;
            if (plugin.getUseSeam()) {
                Filter seamFilter = new SeamRestletFilter(plugin.getUseConversation());

                Restlet seamRestlet = service.getContributedRestletByName(
                        restletName);

                seamFilter.setNext(seamRestlet);

                restletToAdd = seamFilter;
            } else {

                if (plugin.isSingleton()) {
                    restletToAdd = service.getContributedRestletByName(restletName);
                } else {
                    Filter threadSafeRestletFilter = new ThreadSafeRestletFilter();

                    Restlet restlet = service.getContributedRestletByName(restletName);

                    threadSafeRestletFilter.setNext(restlet);
                    restletToAdd = threadSafeRestletFilter;
                }
            }

            // force regexp init
            for (String urlPattern : plugin.getUrlPatterns()) {
                log.debug("Pre-compiling restlet pattern " + urlPattern);
                Route route =  restletRouter.attach(urlPattern, restletToAdd);
                route.getTemplate().match("");
            }
        }

        converter.setTarget(restletRouter);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        boolean tx = false;
        if (!TransactionHelper.isTransactionActive()) {
            tx = TransactionHelper.startTransaction();
        }
        try {
            converter.service(req, res);
        } catch (ServletException e) {
            if (tx) {
                TransactionHelper.setTransactionRollbackOnly();
            }
            throw e;
        } catch (IOException e) {
            if (tx) {
                TransactionHelper.setTransactionRollbackOnly();
            }
            throw e;
        } finally {
            if (tx) {
                if (TransactionHelper.isTransactionActive()) {
                    // SeamRestletFilter might have done an early commit to
                    // avoid race condition on the core session on restlets
                    // who rely upon the conversation lock to fetch it
                    // thread-safely
                    TransactionHelper.commitOrRollbackTransaction();
                }
            }
        }
    }

}
