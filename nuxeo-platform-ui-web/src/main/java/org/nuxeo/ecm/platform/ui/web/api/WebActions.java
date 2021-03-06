/*
 * (C) Copyright 2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * $Id: WebActions.java 25545 2007-09-28 15:03:26Z btatar $
 */

package org.nuxeo.ecm.platform.ui.web.api;

import java.util.List;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.actions.Action;
import org.nuxeo.ecm.platform.actions.ActionContext;
import org.nuxeo.ecm.platform.actions.ejb.ActionManager;

/**
 * Component that handles actions retrieval as well as current tab(s)
 * selection.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public interface WebActions {

    public static final String NULL_TAB_ID = "";

    /**
     * The category of actions for default tabs
     */
    public static final String DEFAULT_TABS_CATEGORY = "VIEW_ACTION_LIST";

    /**
     * The category of actions for main tabs
     *
     * @since 5.5
     */
    public static final String MAIN_TABS_CATEGORY = "MAIN_TABS";

    public static final String SUBTAB_CATEGORY_SUFFIX = "_sub_tab";

    /**
     * Identifier of main tab for the "Documents management" area
     *
     * @since 5.5
     */
    public static final String DOCUMENTS_MAIN_TAB_ID = "documents";

    /**
     * Request parameter used for tab ids settings
     *
     * @since 5.5
     */
    public static final String TAB_IDS_PARAMETER = "tabIds";

    /**
     * Request parameter used for main tab id settings
     *
     * @since 5.5
     */
    public static final String MAIN_TAB_ID_PARAMETER = "mainTabId";

    /**
     * Event raised when the current tab has changed, with 2 parameters: first
     * parameter is a String representing the tab category, and second
     * parameter is a String representing the new tab id (or null if current
     * tab is reset for this category).
     *
     * @since 5.4.2
     */
    public static final String CURRENT_TAB_CHANGED_EVENT = "currentTabChanged";

    /**
     * Event raised when the current tab is selected, with 2 parameters: first
     * parameter is a String representing the tab category, and second
     * parameter is a String representing the new tab id (or null if current
     * tab is reset for this category).
     * <p>
     * This event is sent also when current tab did not change.
     *
     * @since 5.6
     */
    public static final String CURRENT_TAB_SELECTED_EVENT = "currentTabSelected";

    /**
     * Returns all filtered actions for a given category and given resolution
     * context.
     * <p>
     * Actions are filtered according to filters set on the actions
     * definitions.
     */
    List<Action> getActionsList(String category, ActionContext context);

    /**
     * Returns all filtered actions for a given category and given resolution
     * context, creating a new context for the filters resolution.
     * <p>
     * Actions are filtered according to filters set on the actions
     * definitions.
     *
     * @since 5.7
     */
    List<Action> getActionsList(String category, Boolean hideUnavailableAction);

    /**
     * Returns all filtered actions for a given category and given resolution
     * context.
     * <p>
     * Actions are filtered according to filters set on the actions
     * definitions.
     *
     * @since 5.7
     */
    List<Action> getActionsList(String category, ActionContext context,
            boolean hideUnavailableAction);

    /**
     * Returns all filtered actions for a given category, creating a new
     * context for the filters resolution.
     *
     * @see #getActionsList(String, ActionContext)
     */
    List<Action> getActionsList(String category);

    /**
     * Returns all actions for a given category and given resolution context.
     * <p>
     * Actions are not filtered according to filters set on the actions
     * definitions: actions that should have been removed are just marked as
     * non-available.
     *
     * @deprecated since 5.7, use
     *             {@link #getActionsList(String, ActionContext, boolean)}
     */
    @Deprecated
    List<Action> getUnfiltredActionsList(String category, ActionContext context);

    /**
     * Returns all actions for a given category, creating a new context for the
     * filters resolution.
     *
     * @see #getUnfiltredActionsList(String, ActionContext)
     * @deprecated since 5.7, use
     *             {@link #getActionsList(String, ActionContext, boolean)}
     */
    @Deprecated
    List<Action> getUnfiltredActionsList(String category);

    /**
     * Returns all actions for a given category, without filtering.
     */
    List<Action> getAllActions(String category);

    /**
     * Returns filtered actions for the category {@link #DEFAULT_TABS_CATEGORY}
     */
    List<Action> getTabsList();

    /**
     * Returns filtered actions for a category computed from the current tab
     * action id and the suffix {@link #SUBTAB_CATEGORY_SUFFIX}.
     */
    List<Action> getSubTabsList();

    /**
     * Returns the current action for category {@link #DEFAULT_TABS_CATEGORY}
     */
    Action getCurrentTabAction();

    /**
     * Sets the current action for category {@link #DEFAULT_TABS_CATEGORY}
     */
    void setCurrentTabAction(Action tabAction);

    /**
     * Returns the current sub tab for a category computed from the current tab
     * action id and the suffix {@link #SUBTAB_CATEGORY_SUFFIX}.
     */
    Action getCurrentSubTabAction();

    /**
     * Sets the current sub tab for a category computed from the current tab
     * action id and the suffix {@link #SUBTAB_CATEGORY_SUFFIX}.
     */
    void setCurrentSubTabAction(Action tabAction);

    /**
     * Returns the current action id for category
     * {@link #DEFAULT_TABS_CATEGORY}
     */
    String getCurrentTabId();

    /**
     * Sets the current action id for category {@link #DEFAULT_TABS_CATEGORY}.
     * <p>
     * Does nothing if tabId is null, but resets current tab for this category
     * when using an empty string instead.
     */
    void setCurrentTabId(String tabId);

    /**
     * Returns the current sub tab id for a category computed from the current
     * tab action id and the suffix {@link #SUBTAB_CATEGORY_SUFFIX}.
     */
    String getCurrentSubTabId();

    /**
     * Sets the current sub tab id for a category computed from the current tab
     * action id and the suffix {@link #SUBTAB_CATEGORY_SUFFIX}.
     * <p>
     * Does nothing if sub tab id is null, but resets current tab for this
     * category when using an empty string instead.
     */
    void setCurrentSubTabId(String tabId);

    /**
     * Resets actions resolved for category {@link #DEFAULT_TABS_CATEGORY} so
     * that they're recomputed. Also calls {@link #resetCurrentTab()}
     */
    void resetTabList();

    /**
     * Resets current tab information (includes sub tab information) for
     * category {@link #DEFAULT_TABS_CATEGORY}.
     */
    void resetCurrentTab();

    /**
     * Returns the current action for given category.
     */
    Action getCurrentTabAction(String category);

    /**
     * Returns the current sub tab action for given parent action, computing
     * the category from parent action id with suffix
     * {@link #SUBTAB_CATEGORY_SUFFIX}.
     */
    Action getCurrentSubTabAction(String parentActionId);

    /**
     * Sets the current action for given category.
     * <p>
     * If given action is null, it resets the current action for this category.
     */
    void setCurrentTabAction(String category, Action tabAction);

    /**
     * Returns the current action id for given category
     */
    String getCurrentTabId(String category);

    /**
     * Indicates if the current tab id is set for given category
     *
     * @since 5.5
     */
    boolean hasCurrentTabId(String category);

    /**
     * Sets the current action for given category, with additional sub tabs.
     */
    void setCurrentTabId(String category, String tabId, String... subTabIds);

    /**
     * Returns current tab ids as a string, encoded as is:
     * CATEGORY_1:ACTION_ID_1,CATEGORY_2:ACTION_ID_2,...
     *
     * @since 5.4.2
     */
    String getCurrentTabIds();

    /**
     * Sets current tab ids as a String, splitting on commas ',' and parsing
     * each action information as is:
     * CATEGORY:ACTION_ID[:OPTIONAL_SUB_ACTION_ID[:OPTIONAL_SUB_ACTION_ID]...]
     * <p>
     * If category is omitted or empty, the category
     * {@link #DEFAULT_TABS_CATEGORY} will be used (if there is no subtab
     * information).
     * <p>
     * The resulting string looks like:
     * CATEGORY_1:ACTION_ID_1,CATEGORY_2:ACTION_ID_2_SUB_ACTION_ID_2,...
     *
     * @since 5.4.2
     */
    void setCurrentTabIds(String tabIds);

    /**
     * Resets all current tabs information.
     *
     * @since 5.4.2
     */
    void resetCurrentTabs();

    /**
     * Resets current tabs for given category, taking subtabs into account by
     * resetting actions in categories computed from reset actions id with
     * suffix {@link #SUBTAB_CATEGORY_SUFFIX}.
     */
    void resetCurrentTabs(String category);

    /**
     * Calls {@link #setCurrentTabAndNavigate(DocumentModel, String)} for the
     * current document.
     * <p>
     * Given action should hold the category {@link #DEFAULT_TABS_CATEGORY}
     *
     * @see NavigationContext#getCurrentDocument()
     */
    String setCurrentTabAndNavigate(String currentTabActionId);

    /**
     * Navigate to the given document and opens the view page of the given
     * document selecting the given tab.
     * <p>
     * Given action should hold the category {@link #DEFAULT_TABS_CATEGORY}
     *
     * @param document to document which will be shown in the view page
     * @param currentTabActionId the tab that will be selected in the view page
     * @return the JSF view for the given document.
     */
    String setCurrentTabAndNavigate(DocumentModel document,
            String currentTabActionId);

    /**
     * @since 5.6
     * @see ActionManager#checkFilter(String, ActionContext)
     */
    boolean checkFilter(String filterId);

    /**
     * @since 5.7
     * @see ActionManager#getAction(String, ActionContext, boolean)
     */
    Action getAction(String actionId, boolean hideUnavailableAction);

    /**
     * @since 5.6
     * @see ActionManager#getAction(String, ActionContext, boolean)
     */
    Action getAction(String actionId, ActionContext context,
            boolean hideUnavailableAction);

    @Deprecated
    List<Action> getSubViewActionsList();

    /**
     * @deprecated use {@link #setCurrentTabId()} or
     *             {@link #setCurrentTabAction(String, Action)}
     */
    @Deprecated
    void setCurrentTabAction(String currentTabActionId);

    /**
     * @deprecated since 5.4: useless, and does nothing
     */
    @Deprecated
    void selectTabAction();

    /**
     * @deprecated should be handled by a workflow related (or at least
     *             document) action listener.
     */
    @Deprecated
    String getCurrentLifeCycleState() throws Exception;

    /**
     * @deprecated since 5.4.2: useless
     */
    @Deprecated
    void setTabsList(List<Action> tabsList);

    /**
     * @deprecated since 5.4.2: useless
     */
    @Deprecated
    void setSubTabsList(List<Action> tabsList);

}
