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
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: WebLayoutManagerImpl.java 28510 2008-01-06 10:21:44Z sfermigier $
 */

package org.nuxeo.ecm.platform.forms.layout.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.forms.layout.api.BuiltinModes;
import org.nuxeo.ecm.platform.forms.layout.api.BuiltinWidgetModes;
import org.nuxeo.ecm.platform.forms.layout.api.FieldDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.Layout;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRow;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRowDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetSelectOption;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetType;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetTypeDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.exceptions.LayoutException;
import org.nuxeo.ecm.platform.forms.layout.api.exceptions.WidgetException;
import org.nuxeo.ecm.platform.forms.layout.api.impl.LayoutImpl;
import org.nuxeo.ecm.platform.forms.layout.api.impl.LayoutRowComparator;
import org.nuxeo.ecm.platform.forms.layout.api.impl.LayoutRowImpl;
import org.nuxeo.ecm.platform.forms.layout.api.impl.WidgetDefinitionImpl;
import org.nuxeo.ecm.platform.forms.layout.api.impl.WidgetImpl;
import org.nuxeo.ecm.platform.forms.layout.api.service.LayoutManager;
import org.nuxeo.ecm.platform.forms.layout.facelets.RenderVariables;
import org.nuxeo.ecm.platform.forms.layout.facelets.WidgetTypeHandler;
import org.nuxeo.ecm.platform.ui.web.util.ComponentTagUtils;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.el.VariableMapperWrapper;
import com.sun.facelets.tag.TagConfig;

/**
 * Layout service implementation.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class WebLayoutManagerImpl extends DefaultComponent implements
        WebLayoutManager {

    public static final ComponentName NAME = new ComponentName(
            WebLayoutManagerImpl.class.getName());

    private static final Log log = LogFactory.getLog(WebLayoutManagerImpl.class);

    private static final long serialVersionUID = 1L;

    public static final String WIDGET_TYPES_EP_NAME = "widgettypes";

    public static final String WIDGETS_EP_NAME = "widgets";

    public static final String LAYOUTS_EP_NAME = "layouts";

    // runtime component API

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        if (extensionPoint.equals(WIDGET_TYPES_EP_NAME)) {
            registerWidgetType((WidgetTypeDefinition) contribution);
        } else if (extensionPoint.equals(LAYOUTS_EP_NAME)) {
            registerLayout((LayoutDefinition) contribution);
        } else if (extensionPoint.equals(WIDGETS_EP_NAME)) {
            registerWidget((WidgetDefinition) contribution);
        } else {
            log.error(String.format(
                    "Unknown extension point %s, can't register !",
                    extensionPoint));
        }
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        if (extensionPoint.equals(WIDGET_TYPES_EP_NAME)) {
            unregisterWidgetType((WidgetTypeDefinition) contribution);
        } else if (extensionPoint.equals(LAYOUTS_EP_NAME)) {
            unregisterLayout((LayoutDefinition) contribution);
        } else if (extensionPoint.equals(WIDGETS_EP_NAME)) {
            unregisterWidget((WidgetDefinition) contribution);
        } else {
            log.error(String.format(
                    "Unknown extension point %s, can't unregister !",
                    extensionPoint));
        }
    }

    // Core service api

    @Override
    public void registerWidgetType(WidgetTypeDefinition desc) {
        getCoreLayoutManager().registerWidgetType(desc);
    }

    @Override
    public void unregisterWidgetType(WidgetTypeDefinition desc) {
        getCoreLayoutManager().unregisterWidgetType(desc);
    }

    @Override
    public void registerLayout(LayoutDefinition layoutDef) {
        getCoreLayoutManager().registerLayout(layoutDef);
    }

    @Override
    public void unregisterLayout(LayoutDefinition layoutDef) {
        getCoreLayoutManager().unregisterLayout(layoutDef);
    }

    @Override
    public void registerWidget(WidgetDefinition widgetDef) {
        getCoreLayoutManager().registerWidget(widgetDef);
    }

    @Override
    public void unregisterWidget(WidgetDefinition widgetDef) {
        getCoreLayoutManager().unregisterWidget(widgetDef);
    }

    protected LayoutManager getCoreLayoutManager() {
        LayoutManager lm = null;
        try {
            lm = Framework.getLocalService(LayoutManager.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (lm == null) {
            throw new RuntimeException("Missing service for LayoutManager");
        }
        return lm;
    }

    @Override
    public WidgetType getWidgetType(String typeName) {
        return getCoreLayoutManager().getWidgetType(typeName);
    }

    @Override
    public WidgetTypeDefinition getWidgetTypeDefinition(String typeName) {
        return getCoreLayoutManager().getWidgetTypeDefinition(typeName);
    }

    @Override
    public List<WidgetTypeDefinition> getWidgetTypeDefinitions() {
        return getCoreLayoutManager().getWidgetTypeDefinitions();
    }

    @Override
    public LayoutDefinition getLayoutDefinition(String layoutName) {
        return getCoreLayoutManager().getLayoutDefinition(layoutName);
    }

    @Override
    public List<String> getLayoutDefinitionNames() {
        return getCoreLayoutManager().getLayoutDefinitionNames();
    }

    @Override
    public WidgetDefinition getWidgetDefinition(String widgetName) {
        return getCoreLayoutManager().getWidgetDefinition(widgetName);
    }

    // specific API (depends on JSF impl)

    public WidgetTypeHandler getWidgetTypeHandler(String typeName)
            throws WidgetException {
        WidgetType type = getWidgetType(typeName);
        if (type == null) {
            return null;
        }
        WidgetTypeHandler handler;
        try {
            // Thread context loader is not working in isolated EARs
            handler = (WidgetTypeHandler) type.getWidgetTypeClass().newInstance();
        } catch (Exception e) {
            log.error("Caught error when instanciating widget type handler", e);
            return null;
        }
        // set properties
        handler.setProperties(type.getProperties());
        return handler;
    }

    /**
     * Evaluates an EL expression in given context.
     * <p>
     * If the expression resolves to an EL expression, evaluate it again this
     * is useful when retrieving the expression from a configuration file.
     * <p>
     * If given context is null, do no try to evaluate it and return the
     * expression itself.
     *
     * @param context the facelet context.
     * @param expression the string expression.
     */
    protected static Object evaluateExpression(FaceletContext context,
            String expression) {
        if (expression == null) {
            return null;
        }
        if (context == null) {
            return expression;
        }
        Object value = ComponentTagUtils.resolveElExpression(context,
                expression);
        if (value != null && value instanceof String) {
            // evaluate a second time in case it's another EL expression
            value = ComponentTagUtils.resolveElExpression(context,
                    (String) value);
        }
        return value;
    }

    /**
     * Evaluates an expression to a boolean value.
     */
    protected static Boolean getBooleanValue(FaceletContext context,
            String expression) {
        Object value = evaluateExpression(context, expression);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value == null || value instanceof String) {
            return Boolean.valueOf((String) value);
        } else {
            log.error("Could not get boolean value for " + value);
            return Boolean.FALSE;
        }
    }

    /**
     * Evaluates an expression to a string value.
     */
    protected static String getStringValue(FaceletContext context,
            String expression) {
        Object value = evaluateExpression(context, expression);
        if (value == null || value instanceof String) {
            return (String) value;
        } else {
            log.error("Could not get string value for " + value);
            return null;
        }
    }

    protected static String getModeFromLayoutMode(FaceletContext context,
            WidgetDefinition wDef, String layoutMode) {
        String wMode = getStringValue(context, wDef.getMode(layoutMode));
        if (wMode == null) {
            wMode = BuiltinModes.getWidgetModeFromLayoutMode(layoutMode);
        }
        return wMode;
    }

    /**
     * Computes a widget from a definition for a mode in a given context.
     * <p>
     * If the widget is configured not to be rendered in the given mode,
     * returns null.
     * <p>
     * Sub widgets are also computed recursively.
     */
    protected Widget getWidget(FaceletContext context, LayoutDefinition lDef,
            WidgetDefinition wDef, String layoutMode, String valueName,
            int level) {
        VariableMapper orig = null;
        // avoid variable mapper changes if context is null for tests
        if (context != null) {
            // expose widget mode so that it can be used in a mode el
            // expression
            orig = context.getVariableMapper();
            VariableMapper vm = new VariableMapperWrapper(orig);
            context.setVariableMapper(vm);
            ExpressionFactory eFactory = context.getExpressionFactory();
            ValueExpression modeVe = eFactory.createValueExpression(layoutMode,
                    String.class);
            vm.setVariable(RenderVariables.globalVariables.mode.name(), modeVe);
        }
        String wMode = getModeFromLayoutMode(context, wDef, layoutMode);
        if (context != null) {
            context.setVariableMapper(orig);
        }

        if (BuiltinWidgetModes.HIDDEN.equals(wMode)) {
            return null;
        }
        List<Widget> subWidgets = new ArrayList<Widget>();
        for (WidgetDefinition swDef : wDef.getSubWidgetDefinitions()) {
            Widget subWidget = getWidget(context, lDef, swDef, wMode,
                    valueName, level + 1);
            if (subWidget != null) {
                subWidgets.add(subWidget);
            }
        }

        boolean required = getBooleanValue(context,
                wDef.getRequired(layoutMode, wMode)).booleanValue();
        Widget widget = new WidgetImpl(lDef.getName(), wDef.getName(), wMode,
                wDef.getType(), valueName, wDef.getFieldDefinitions(),
                wDef.getLabel(layoutMode), wDef.getHelpLabel(layoutMode),
                wDef.isTranslated(), wDef.getProperties(layoutMode, wMode),
                required, subWidgets.toArray(new Widget[] {}), level,
                wDef.getSelectOptions(), computeWidgetDefinitionId(wDef));
        return widget;
    }

    public Layout getLayout(FaceletContext ctx, String layoutName, String mode,
            String valueName) throws LayoutException {
        return getLayout(ctx, layoutName, mode, valueName, null, false);
    }

    public Layout getLayout(FaceletContext ctx, String layoutName, String mode,
            String valueName, List<String> selectedRows,
            boolean selectAllRowsByDefault) {
        LayoutDefinition layoutDef = getLayoutDefinition(layoutName);
        if (layoutDef == null) {
            log.debug(String.format("Layout %s not found", layoutName));
            return null;
        }
        return getLayout(ctx, layoutDef, mode, valueName, selectedRows,
                selectAllRowsByDefault);
    }

    public Layout getLayout(FaceletContext ctx, LayoutDefinition layoutDef,
            String mode, String valueName, List<String> selectedRows,
            boolean selectAllRowsByDefault) {
        if (layoutDef == null) {
            log.debug("Layout definition is null");
            return null;
        }
        String layoutName = layoutDef.getName();
        if (ctx == null) {
            log.warn("Layout creation computed in a null facelet context: expressions "
                    + "found in the layout definition will not be evaluated");
        }
        LayoutRowDefinition[] rowsDef = layoutDef.getRows();
        List<LayoutRow> rows = new ArrayList<LayoutRow>();
        Set<String> foundRowNames = new HashSet<String>();
        int rowIndex = -1;
        for (LayoutRowDefinition rowDef : rowsDef) {
            rowIndex++;
            String rowName = rowDef.getName();
            if (rowName == null) {
                rowName = "layout_row_" + rowIndex;
                if (selectedRows != null) {
                    log.debug(String.format("Generating default name '%s' in "
                            + "layout '%s' for row or column at index %s",
                            rowName, layoutName, Integer.valueOf(rowIndex)));
                }
            }
            boolean emptyRow = true;
            if (selectedRows != null && !selectedRows.contains(rowName)
                    && !rowDef.isAlwaysSelected()) {
                continue;
            }
            if (selectedRows == null && !selectAllRowsByDefault
                    && !rowDef.isSelectedByDefault()
                    && !rowDef.isAlwaysSelected()) {
                continue;
            }
            List<Widget> widgets = new ArrayList<Widget>();
            for (String widgetName : rowDef.getWidgets()) {
                if (widgetName == null || widgetName.length() == 0) {
                    // no widget at this place
                    widgets.add(null);
                    continue;
                }
                WidgetDefinition wDef = layoutDef.getWidgetDefinition(widgetName);
                if (wDef == null) {
                    // try in global registry
                    wDef = getWidgetDefinition(widgetName);
                }
                if (wDef == null) {
                    log.error(String.format("Widget %s not found in layout %s",
                            widgetName, layoutName));
                    widgets.add(null);
                    continue;
                }
                Widget widget = getWidget(ctx, layoutDef, wDef, mode,
                        valueName, 0);
                if (widget != null) {
                    emptyRow = false;
                }
                widgets.add(widget);
            }
            if (!emptyRow) {
                rows.add(new LayoutRowImpl(rowName,
                        rowDef.isSelectedByDefault(),
                        rowDef.isAlwaysSelected(), widgets,
                        rowDef.getProperties(mode),
                        computeLayoutRowDefinitionId(rowDef)));
            }
            foundRowNames.add(rowName);
        }
        if (selectedRows != null) {
            Collections.sort(rows, new LayoutRowComparator(selectedRows));
            for (String selectedRow : selectedRows) {
                if (!foundRowNames.contains(selectedRow)) {
                    log.debug(String.format(
                            "Selected row or column named '%s' "
                                    + "was not found in layout '%s'",
                            selectedRow, layoutName));
                }
            }
        }
        int columns = layoutDef.getColumns();
        Layout layout = new LayoutImpl(layoutDef.getName(), mode,
                layoutDef.getTemplate(mode), rows, columns,
                layoutDef.getProperties(mode),
                computeLayoutDefinitionId(layoutDef));
        return layout;
    }

    public FaceletHandler getFaceletHandler(FaceletContext ctx,
            TagConfig config, Widget widget) {
        String widgetTypeName = widget.getType();
        WidgetTypeHandler handler = getWidgetTypeHandler(widgetTypeName);
        if (handler == null) {
            log.error("No widget handler found for type " + widgetTypeName);
        } else {
            FaceletHandler[] subHandlers = null;
            Widget[] subWidgets = widget.getSubWidgets();
            if (subWidgets != null) {
                List<FaceletHandler> subHandlersList = new ArrayList<FaceletHandler>();
                for (Widget subWidget : subWidgets) {
                    subHandlersList.add(getFaceletHandler(ctx, config,
                            subWidget));
                }
                subHandlers = subHandlersList.toArray(new FaceletHandler[] {});
            }
            FaceletHandler fHandler = handler.getFaceletHandler(ctx, config,
                    widget, subHandlers);
            return fHandler;
        }
        return null;
    }

    @Override
    public Widget createWidget(FaceletContext ctx, String type, String mode,
            String valueName, Map<String, Serializable> properties,
            Widget[] subWidgets) {
        return createWidget(ctx, type, mode, valueName, null, null, null, null,
                properties, subWidgets);
    }

    @Override
    public Widget createWidget(FaceletContext ctx, String type, String mode,
            String valueName, List<FieldDefinition> fieldDefinitions,
            String label, String helpLabel, Boolean translated,
            Map<String, Serializable> properties, Widget[] subWidgets) {
        Serializable requiredProp = properties.get(WidgetDefinition.REQUIRED_PROPERTY_NAME);
        boolean required = false;
        if (requiredProp != null) {
            if (requiredProp instanceof Boolean) {
                required = ((Boolean) requiredProp).booleanValue();
            } else if (requiredProp instanceof String) {
                required = getBooleanValue(ctx, (String) requiredProp).booleanValue();
            } else {
                log.error(String.format(
                        "Invalid property \"%s\" on widget: %s",
                        WidgetDefinition.REQUIRED_PROPERTY_NAME, requiredProp));
            }
        }
        WidgetDefinitionImpl wDef = new WidgetDefinitionImpl("widget", type,
                label, helpLabel, Boolean.TRUE.equals(translated), null,
                fieldDefinitions, properties, null);
        Widget widget = new WidgetImpl("layout", wDef.getName(), mode,
                wDef.getType(), valueName, wDef.getFieldDefinitions(), label,
                helpLabel, wDef.isTranslated(), properties, required,
                subWidgets, 0, null, computeWidgetDefinitionId(wDef));
        return widget;
    }

    // helpers

    /**
     * Returns an identifier computed from this definition so that an identical
     * definition will have the same id.
     *
     * @since 5.4.3
     */
    protected String computeLayoutDefinitionId(LayoutDefinition layoutDef) {
        StringBuilder builder = new StringBuilder();
        builder.append(layoutDef.getName()).append(";");
        Map<String, String> templates = layoutDef.getTemplates();
        if (templates != null) {
            builder.append(templates.toString());
        }
        builder.append(";");
        LayoutRowDefinition[] rows = layoutDef.getRows();
        if (rows != null) {
            for (LayoutRowDefinition row : rows) {
                if (row != null) {
                    builder.append(computeLayoutRowDefinitionId(row)).append(
                            ",");
                }
            }
        }
        builder.append(";");
        Map<String, Map<String, Serializable>> properties = layoutDef.getProperties();
        if (properties != null) {
            builder.append(properties.toString());
        }
        builder.append(";");

        Integer intValue = new Integer(builder.toString().hashCode());
        return intValue.toString();
    }

    /**
     * Returns an identifier computed from this definition so that an identical
     * definition will have the same id.
     *
     * @since 5.4.3
     */
    protected String computeLayoutRowDefinitionId(
            LayoutRowDefinition layoutRowDef) {
        StringBuffer builder = new StringBuffer();
        builder.append(layoutRowDef.getName()).append(";");
        builder.append(layoutRowDef.isSelectedByDefault()).append(";");
        builder.append(layoutRowDef.isAlwaysSelected()).append(";");
        String[] widgets = layoutRowDef.getWidgets();
        if (widgets != null) {
            for (String widget : widgets) {
                if (widget != null) {
                    builder.append(widget).append(",");
                }
            }
        }
        builder.append(";");

        Map<String, Map<String, Serializable>> properties = layoutRowDef.getProperties();
        if (properties != null) {
            builder.append(properties.toString());
        }
        builder.append(";");

        Integer intValue = new Integer(builder.toString().hashCode());
        return intValue.toString();

    }

    /**
     * Returns an identifier computed from this definition so that an identical
     * definition will have the same id.
     *
     * @since 5.4.3
     */
    protected String computeWidgetDefinitionId(WidgetDefinition widgetDef) {
        StringBuffer builder = new StringBuffer();
        builder.append(widgetDef.getName()).append(";");
        builder.append(widgetDef.getType()).append(";");

        FieldDefinition[] fieldDefinitions = widgetDef.getFieldDefinitions();
        if (fieldDefinitions != null) {
            for (FieldDefinition fieldDef : fieldDefinitions) {
                builder.append(fieldDef.getPropertyName() + ",");
            }
        }
        builder.append(";");

        Map<String, String> labels = widgetDef.getLabels();
        if (labels != null) {
            builder.append(labels.toString());
        }
        builder.append(";");
        Map<String, String> helpLabels = widgetDef.getHelpLabels();
        if (helpLabels != null) {
            builder.append(helpLabels.toString());
        }
        builder.append(";");

        WidgetDefinition[] subWidgets = widgetDef.getSubWidgetDefinitions();
        if (subWidgets != null) {
            for (WidgetDefinition widget : subWidgets) {
                if (widget != null) {
                    builder.append(computeWidgetDefinitionId(widget)).append(
                            ",");
                }
            }
        }
        builder.append(";");

        Map<String, Map<String, Serializable>> properties = widgetDef.getProperties();
        if (properties != null) {
            builder.append(properties.toString());
        }
        builder.append(";");
        Map<String, Map<String, Serializable>> widgetModeProperties = widgetDef.getWidgetModeProperties();
        if (widgetModeProperties != null) {
            builder.append(widgetModeProperties.toString());
        }
        builder.append(";");

        builder.append(widgetDef.isTranslated()).append(";");

        Map<String, String> modes = widgetDef.getModes();
        if (modes != null) {
            builder.append(modes.toString());
        }
        builder.append(";");

        WidgetSelectOption[] selectOptions = widgetDef.getSelectOptions();
        if (selectOptions != null) {
            for (WidgetSelectOption option : selectOptions) {
                if (option != null) {
                    builder.append(option.getTagConfigId()).append(",");
                }
            }
        }
        builder.append(";");

        Integer intValue = new Integer(builder.toString().hashCode());
        return intValue.toString();
    }

}
