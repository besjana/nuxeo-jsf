/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Contributors:
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.webapp.security;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.platform.ui.web.api.WebActions.CURRENT_TAB_CHANGED_EVENT;
import static org.nuxeo.ecm.platform.ui.web.api.WebActions.CURRENT_TAB_SELECTED_EVENT;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.ecm.platform.usermanager.exceptions.GroupAlreadyExistsException;

/**
 * /** Handles users management related web actions.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.2
 */
@Name("groupManagementActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class GroupManagementActions extends AbstractUserGroupManagement
        implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(GroupManagementActions.class);

    public static final String GROUPS_TAB = USER_CENTER_CATEGORY + ":"
            + USERS_GROUPS_HOME + ":" + "GroupsHome";

    public static final String GROUPS_LISTING_CHANGED = "groupsListingChanged";

    protected Boolean canEditGroups;

    protected DocumentModel selectedGroup;

    protected DocumentModel newGroup;

    @Override
    protected String computeListingMode() throws ClientException {
        return userManager.getGroupListingMode();
    }

    public DocumentModel getSelectedGroup() {
        shouldResetStateOnTabChange = true;
        return selectedGroup;
    }

    public void setSelectedGroup(String groupName) throws ClientException {
        selectedGroup = refreshGroup(groupName);
    }

    // refresh to get references
    protected DocumentModel refreshGroup(String groupName)
            throws ClientException {
        return userManager.getGroupModel(groupName);
    }

    public DocumentModel getNewGroup() throws ClientException {
        if (newGroup == null) {
            newGroup = userManager.getBareGroupModel();
        }
        return newGroup;
    }

    public void clearSearch() {
        searchString = null;
        fireSeamEvent(GROUPS_LISTING_CHANGED);
    }

    public void createGroup() throws ClientException {
        createGroup(false);
    }

    public void createGroup(boolean createAnotherGroup) throws ClientException {
        try {
            selectedGroup = userManager.createGroup(newGroup);
            newGroup = null;
            facesMessages.add(
                    StatusMessage.Severity.INFO,
                    resourcesAccessor.getMessages().get(
                            "info.groupManager.groupCreated"));
            if (createAnotherGroup) {
                showCreateForm = true;
            } else {
                showCreateForm = false;
                showUserOrGroup = true;
                detailsMode = null;
            }
            fireSeamEvent(GROUPS_LISTING_CHANGED);
        } catch (GroupAlreadyExistsException e) {
            String message = resourcesAccessor.getMessages().get(
                    "error.groupManager.groupAlreadyExists");
            facesMessages.addToControl("groupName",
                    StatusMessage.Severity.ERROR, message);
        }
    }

    public void updateGroup() throws ClientException {
        try {
            userManager.updateGroup(selectedGroup);
            detailsMode = DETAILS_VIEW_MODE;
            fireSeamEvent(GROUPS_LISTING_CHANGED);
        } catch (Throwable t) {
            throw ClientException.wrap(t);
        }
    }

    public void deleteGroup() throws ClientException {
        try {
            userManager.deleteGroup(selectedGroup);
            selectedGroup = null;
            showUserOrGroup = false;
            fireSeamEvent(GROUPS_LISTING_CHANGED);
        } catch (Throwable t) {
            throw ClientException.wrap(t);
        }
    }

    public boolean getAllowCreateGroup() throws ClientException {
        return getCanEditGroups();
    }

    protected boolean getCanEditGroups() throws ClientException {
        if (canEditGroups == null) {
            canEditGroups = false;
            if (!userManager.areGroupsReadOnly()
                    && currentUser instanceof NuxeoPrincipal) {
                if (webActions.checkFilter(USERS_GROUPS_MANAGEMENT_ACCESS_FILTER)) {
                    canEditGroups = true;
                }
            }
        }
        return canEditGroups;
    }

    public boolean getAllowDeleteGroup() throws ClientException {
        return getCanEditGroups()
                && !BaseSession.isReadOnlyEntry(selectedGroup);
    }

    public boolean getAllowEditGroup() throws ClientException {
        // Changing administrator group is only given to administrators (not
        // powerusers)
        // NXP-10584
        if (userManager.getAdministratorsGroups().contains(
                selectedGroup.getId())) {
            return ((NuxeoPrincipal) currentUser).isAdministrator();
        }

        return getCanEditGroups()
                && !BaseSession.isReadOnlyEntry(selectedGroup);
    }

    public void validateGroupName(FacesContext context, UIComponent component,
            Object value) {
        if (!(value instanceof String)
                || !StringUtils.containsOnly((String) value, VALID_CHARS)) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
                            context, "label.groupManager.wrongGroupName"), null);
            ((EditableValueHolder) component).setValid(false);
            context.addMessage(component.getClientId(context), message);
            // also add global message
            context.addMessage(null, message);
        }
    }

    public String viewGroup() throws ClientException {
        if (selectedGroup != null) {
            return viewGroup(selectedGroup.getId());
        } else {
            return null;
        }
    }

    public String viewGroup(String groupName) throws ClientException {
        webActions.setCurrentTabIds(MAIN_TAB_HOME + "," + GROUPS_TAB);
        setSelectedGroup(groupName);
        showUserOrGroup = true;
        // do not reset the state before actually viewing the group
        shouldResetStateOnTabChange = false;
        return VIEW_HOME;
    }

    protected void fireSeamEvent(String eventName) {
        Events evtManager = Events.instance();
        evtManager.raiseEvent(eventName);
    }

    @Observer(value = { GROUPS_LISTING_CHANGED })
    public void onUsersListingChanged() {
        contentViewActions.refreshOnSeamEvent(GROUPS_LISTING_CHANGED);
        contentViewActions.resetPageProviderOnSeamEvent(GROUPS_LISTING_CHANGED);
    }

    @Observer(value = { CURRENT_TAB_CHANGED_EVENT + "_" + MAIN_TABS_CATEGORY,
            CURRENT_TAB_CHANGED_EVENT + "_" + NUXEO_ADMIN_CATEGORY,
            CURRENT_TAB_CHANGED_EVENT + "_" + USER_CENTER_CATEGORY,
            CURRENT_TAB_CHANGED_EVENT + "_" + USERS_GROUPS_MANAGER_SUB_TAB,
            CURRENT_TAB_CHANGED_EVENT + "_" + USERS_GROUPS_HOME_SUB_TAB,
            CURRENT_TAB_SELECTED_EVENT + "_" + MAIN_TABS_CATEGORY,
            CURRENT_TAB_SELECTED_EVENT + "_" + NUXEO_ADMIN_CATEGORY,
            CURRENT_TAB_SELECTED_EVENT + "_" + USER_CENTER_CATEGORY,
            CURRENT_TAB_SELECTED_EVENT + "_" + USERS_GROUPS_MANAGER_SUB_TAB,
            CURRENT_TAB_SELECTED_EVENT + "_" + USERS_GROUPS_HOME_SUB_TAB })
    public void resetState() {
        if (shouldResetStateOnTabChange) {
            newGroup = null;
            showUserOrGroup = false;
            showCreateForm = false;
            detailsMode = DETAILS_VIEW_MODE;
        }
    }

}
