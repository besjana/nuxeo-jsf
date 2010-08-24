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
package org.nuxeo.ecm.platform.ui.web.contentview.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.nuxeo.ecm.platform.ui.web.contentview.ContentView;
import org.nuxeo.ecm.platform.ui.web.contentview.ContentViewLayout;
import org.nuxeo.ecm.platform.ui.web.contentview.ContentViewService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.NXRuntimeTestCase;

/**
 * @author Anahide Tchertchian
 */
public class TestContentViewService extends NXRuntimeTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        deployContrib("org.nuxeo.ecm.platform.ui",
                "OSGI-INF/contentview-framework.xml");
        deployContrib("org.nuxeo.ecm.platform.ui.test",
                "test-contentview-contrib.xml");
    }

    public void testRegistration() throws Exception {
        ContentViewService service = Framework.getService(ContentViewService.class);
        assertNotNull(service);

        assertNull(service.getContentView("foo"));

        ContentView contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN");
        assertNotNull(contentView);
        // check content view attributes
        assertEquals("CURRENT_DOCUMENT_CHILDREN", contentView.getName());
        assertEquals("current document children", contentView.getTitle());
        assertFalse(contentView.getTranslateTitle());
        assertEquals("/icons/document_listing_icon.png",
                contentView.getIconPath());
        assertEquals("CURRENT_SELECTION_LIST",
                contentView.getActionsCategories().get(0));
        assertEquals("simple", contentView.getPagination());

        List<ContentViewLayout> resultLayouts = contentView.getResultLayouts();
        assertNotNull(resultLayouts);
        assertEquals(1, resultLayouts.size());
        assertEquals("document_listing", resultLayouts.get(0).getName());
        assertEquals("label.document_listing.layout",
                resultLayouts.get(0).getTitle());
        assertTrue(resultLayouts.get(0).getTranslateTitle());
        assertEquals("/icons/myicon.png", resultLayouts.get(0).getIconPath());

        assertEquals("search_layout", contentView.getSearchLayout().getName());
        assertNull(contentView.getSearchLayout().getTitle());
        assertFalse(contentView.getSearchLayout().getTranslateTitle());
        assertNull(contentView.getSearchLayout().getIconPath());

        assertEquals("CURRENT_SELECTION", contentView.getSelectionListName());
        List<String> eventNames = contentView.getRefreshEventNames();
        assertNotNull(eventNames);
        assertEquals(1, eventNames.size());
        assertEquals("documentChildrenChanged", eventNames.get(0));
        assertFalse(contentView.getUseGlobalPageSize());

        List<String> flags = contentView.getFlags();
        assertNotNull(flags);
        assertEquals(2, flags.size());
        assertEquals("foo", flags.get(0));
        assertEquals("bar", flags.get(1));

    }

    public void testOverride() throws Exception {
        deployContrib("org.nuxeo.ecm.platform.ui.test",
                "test-contentview-override-contrib.xml");

        ContentViewService service = Framework.getService(ContentViewService.class);
        assertNotNull(service);

        assertNull(service.getContentView("foo"));

        ContentView contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN");
        assertNotNull(contentView);
        // check content view attributes
        assertEquals("CURRENT_DOCUMENT_CHILDREN", contentView.getName());
        assertEquals("current document children overriden",
                contentView.getTitle());
        assertFalse(contentView.getTranslateTitle());
        assertEquals("/icons/document_listing_icon.png",
                contentView.getIconPath());
        assertEquals("CURRENT_SELECTION_LIST_2",
                contentView.getActionsCategories().get(0));
        assertEquals("simple_2", contentView.getPagination());

        List<ContentViewLayout> resultLayouts = contentView.getResultLayouts();
        assertNotNull(resultLayouts);
        assertEquals(2, resultLayouts.size());
        assertEquals("document_listing", resultLayouts.get(0).getName());
        assertEquals("label.document_listing.layout",
                resultLayouts.get(0).getTitle());
        assertTrue(resultLayouts.get(0).getTranslateTitle());
        assertEquals("/icons/myicon.png", resultLayouts.get(0).getIconPath());
        assertEquals("document_listing_2", resultLayouts.get(1).getName());
        assertEquals("label.document_listing.layout_2",
                resultLayouts.get(1).getTitle());
        assertTrue(resultLayouts.get(1).getTranslateTitle());
        assertNull(resultLayouts.get(1).getIconPath());

        assertEquals("search_layout_2", contentView.getSearchLayout().getName());
        assertNull(contentView.getSearchLayout().getTitle());
        assertFalse(contentView.getSearchLayout().getTranslateTitle());
        assertNull(contentView.getSearchLayout().getIconPath());

        assertEquals("CURRENT_SELECTION_2", contentView.getSelectionListName());
        List<String> eventNames = contentView.getRefreshEventNames();
        assertNotNull(eventNames);
        assertEquals(1, eventNames.size());
        assertEquals("documentChildrenChanged", eventNames.get(0));
        assertTrue(contentView.getUseGlobalPageSize());

        List<String> flags = contentView.getFlags();
        assertNotNull(flags);
        assertEquals(1, flags.size());
        assertEquals("foo2", flags.get(0));

    }

    public void testGetContentViewNames() throws Exception {
        ContentViewService service = Framework.getService(ContentViewService.class);
        assertNotNull(service);

        Set<String> names = service.getContentViewNames();
        assertNotNull(names);
        assertEquals(3, names.size());
        List<String> orderedNames = new ArrayList<String>();
        orderedNames.addAll(names);
        Collections.sort(orderedNames);
        assertEquals("CURRENT_DOCUMENT_CHILDREN", orderedNames.get(0));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_FETCH", orderedNames.get(1));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_WITH_SEARCH_DOCUMENT",
                orderedNames.get(2));

    }

    public void testGetContentViewByFlag() throws Exception {
        ContentViewService service = Framework.getService(ContentViewService.class);
        assertNotNull(service);

        Set<String> names = service.getContentViews("foo");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("CURRENT_DOCUMENT_CHILDREN", names.iterator().next());

        names = service.getContentViews("foo2");
        assertNotNull(names);
        assertEquals(0, names.size());

        names = service.getContentViews("bar");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("CURRENT_DOCUMENT_CHILDREN", names.iterator().next());

        names = service.getContentViews("not_set");
        assertNotNull(names);
        assertEquals(0, names.size());

        // check after override too
        deployContrib("org.nuxeo.ecm.platform.ui.test",
                "test-contentview-override-contrib.xml");

        names = service.getContentViews("foo");
        assertNotNull(names);
        assertEquals(0, names.size());

        names = service.getContentViews("foo2");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("CURRENT_DOCUMENT_CHILDREN", names.iterator().next());

        names = service.getContentViews("bar");
        assertNotNull(names);
        assertEquals(0, names.size());

        names = service.getContentViews("not_set");
        assertNotNull(names);
        assertEquals(0, names.size());
    }

}
