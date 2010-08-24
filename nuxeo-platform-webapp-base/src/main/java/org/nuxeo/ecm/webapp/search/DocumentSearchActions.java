/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.webapp.search;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.nuxeo.ecm.core.query.sql.model.Literal;
import org.nuxeo.ecm.core.query.sql.model.StringLiteral;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.ecm.webapp.helpers.EventNames;

/**
 * Handles simple, advanced and administrator searches using content views with
 * defined names.
 *
 * @author Anahide Tchertchian
 */
@Name("documentSearchActions")
@Scope(ScopeType.CONVERSATION)
public class DocumentSearchActions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Query parameter string used to perform full text search on all documents
     */
    protected String simpleSearchKeywords = "";

    protected String nxqlQuery = "";

    public String getSimpleSearchKeywords() {
        return simpleSearchKeywords;
    }

    public void setSimpleSearchKeywords(String simpleSearchKeywords) {
        this.simpleSearchKeywords = simpleSearchKeywords;
    }

    public void validateSimpleSearchKeywords(FacesContext context,
            UIComponent component, Object value) {
        if (!(value instanceof String) || StringUtils.isEmpty((String) value)) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
                            context, "feedback.search.noKeywords"), null);
            throw new ValidatorException(message);
        }
        String[] keywords = ((String) value).split(" ");
        for (String keyword : keywords) {
            if (keyword.startsWith("*")) {
                // Can't begin search with * character
                FacesMessage message = new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
                                context, "feedback.search.star"), null);
                throw new ValidatorException(message);
            }
        }
    }

    public String getNxqlQuery() {
        return nxqlQuery;
    }

    public void setNxqlQuery(String nxqlQuery) {
        this.nxqlQuery = nxqlQuery;
    }

    public Literal getNxqlQueryAsLiteral() {
        if (nxqlQuery == null) {
            return new StringLiteral("");
        } else {
            return new StringLiteral(nxqlQuery);
        }
    }

    @Observer(value = EventNames.USER_ALL_DOCUMENT_TYPES_SELECTION_CHANGED, create = false)
    @BypassInterceptors
    public void resetSearches() {
        simpleSearchKeywords = "";
        nxqlQuery = "";
    }

}