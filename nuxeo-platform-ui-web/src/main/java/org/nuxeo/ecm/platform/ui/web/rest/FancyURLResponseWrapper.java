/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id: FancyURLResponseWrapper.java 28924 2008-01-10 14:04:05Z sfermigier $
 */

package org.nuxeo.ecm.platform.ui.web.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.runtime.api.Framework;

/**
 * Used in post methods to rewrite the url with needed get params. Encapsulates
 * response into a wrapper.
 */
public class FancyURLResponseWrapper extends HttpServletResponseWrapper {

    private static final Log log = LogFactory.getLog(FancyURLResponseWrapper.class);

    protected HttpServletRequest request = null;

    public FancyURLResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /**
     * @deprecated since 5.5: use constructor without the
     *             {@link StaticNavigationHandler} that is now wrapped into the
     *             {@link URLPolicyService}
     */
    @Deprecated
    public FancyURLResponseWrapper(HttpServletResponse response,
            HttpServletRequest request,
            StaticNavigationHandler navigationHandler) {
        this(response, request);
    }

    public FancyURLResponseWrapper(HttpServletResponse response,
            HttpServletRequest request) {
        super(response);
        this.request = request;
    }

    protected String getViewId(String url, HttpServletRequest request) {
        return null;
    }

    /**
     * Rewrites url for given document view but extracts view id from the
     * original url.
     */
    protected String rewriteUrl(String url, DocumentView docView,
            URLPolicyService service) {
        // try to get outcome that was saved in request by
        // FancyNavigationHandler
        String newViewId = (String) request.getAttribute(URLPolicyService.POST_OUTCOME_REQUEST_KEY);
        if (newViewId != null) {
            docView.setViewId(newViewId);
        } else {
            String jsfOutcome = service.getOutcomeFromUrl(url, request);
            docView.setViewId(jsfOutcome);
        }

        int index = url.indexOf('?');
        if (index != -1) {
            String uriString = url.substring(index + 1);
            Map<String, String> parameters = URIUtils.getRequestParameters(uriString);
            if (parameters != null) {
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    docView.addParameter(entry.getKey(), entry.getValue());
                }
            }
        }
        String baseUrl = BaseURL.getBaseURL(request);
        url = service.getUrlFromDocumentView(docView, baseUrl);
        return url;
    }

    @Override
    public void sendRedirect(String url) throws IOException {
        if (request != null) {
            try {
                URLPolicyService pservice = Framework.getService(URLPolicyService.class);
                if (pservice.isCandidateForEncoding(request)) {
                    DocumentView docView = pservice.getDocumentViewFromRequest(request);
                    if (docView != null) {
                        url = rewriteUrl(url, docView, pservice);
                    }
                }
            } catch (Exception e) {
                log.error("Could not redirect", e);
            }
        }
        super.sendRedirect(url);
    }
}
