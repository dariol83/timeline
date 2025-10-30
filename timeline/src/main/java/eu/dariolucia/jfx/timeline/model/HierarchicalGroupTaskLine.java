/*
 * Copyright (c) 2024 Dario Lucia (https://www.dariolucia.eu)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package eu.dariolucia.jfx.timeline.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A hierarchical group represents a group of {@link ITaskLine} in a timeline with hierarchical representation.
 * This class can be subclassed and the doRender() method can be overwritten.
 */
public class HierarchicalGroupTaskLine extends CompositeTaskLine {

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/

    /**
     * Width of the indentation space, in pixels.
     */
    private final SimpleIntegerProperty indentSpace = new SimpleIntegerProperty(20);

    /**
     * Class constructor with no description.
     * @param name the name of the task group
     */
    public HierarchicalGroupTaskLine(String name) {
        this(name, null);
    }

    /**
     * Class constructor with name and description.
     * @param name the name of the task group
     * @param description the description of the task group
     */
    public HierarchicalGroupTaskLine(String name, String description) {
        super(name, description);
    }

    /* *****************************************************************************************
     * Property Accessors
     * *****************************************************************************************/

    public int getIndentSpace() {
        return indentSpace.get();
    }

    public SimpleIntegerProperty indentSpaceProperty() {
        return indentSpace;
    }

    public void setIndentSpace(int indentSpace) {
        this.indentSpace.set(indentSpace);
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    @Override
    protected int doRender(GraphicsContext gc, int groupXStart, int groupYStart, IRenderingContext rc) {
        int groupBoxHeight = getHeight(rc);
        int groupBoxWidth = (int) rc.getTaskPanelWidth() - groupXStart;
        // Draw the group box
        drawGroupPanelBox(gc, groupXStart, groupYStart, groupBoxWidth, groupBoxHeight, rc);
        // Render the bottom line
        gc.strokeLine(rc.getTaskPanelWidth(), groupYStart + rc.getLineRowHeight(), rc.getImageAreaWidth(), groupYStart + rc.getLineRowHeight());
        // If collapsible, render the symbol
        int textOffset = 0;
        if(isCollapsible()) {
            int squareSize = rc.getTextHeight();
            textOffset = squareSize + (int) rc.getTextPadding();
            // Draw a square
            if(isCollapsed()) {
                drawCollapsedToggleButton(gc, groupXStart, groupYStart, squareSize, rc);
            } else {
                drawExpandedToggleButton(gc, groupXStart, groupYStart, squareSize, rc);
            }
            // Remember square location for event processing
            setCollapseButtonBoundingBox(new BoundingBox(groupXStart, groupYStart, squareSize, squareSize));
        } else {
            // Reset square location
            setCollapseButtonBoundingBox(null);
        }
        // Render name
        drawGroupName(gc, groupXStart, groupYStart, textOffset, rc);
        // Render the sub lines
        if(!isCollapsedState()) {
            int i = 1;
            for (ITaskLine line : getItems()) {
                line.render(gc, groupXStart + getIndentSpace(), groupYStart + i * rc.getLineRowHeight(), rc);
                i += line.getNbOfLines();
            }
        }
        // Task projection
        if(rc.getTaskProjectionHint() == TaskItemProjection.ALWAYS || (rc.getTaskProjectionHint() == TaskItemProjection.COLLAPSE && isCollapsedState())) {
            drawProjectedTasks(gc, groupYStart, rc);
        }
        // Remember box
        return groupBoxHeight;
    }

    /**
     * Draw the box of the group in the task panel.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param groupBoxWidth the width of the box in the task panel
     * @param groupBoxHeight the height of the entire taskline
     * @param rc the {@link IRenderingContext}
     */
    protected void drawGroupPanelBox(GraphicsContext gc, int groupXStart, int groupYStart, int groupBoxWidth, int groupBoxHeight, IRenderingContext rc) {
        gc.setFill(rc.getPanelBackground());
        gc.setStroke(rc.getPanelBorderColor());
        gc.fillRect(groupXStart, groupYStart, groupBoxWidth, groupBoxHeight);
        gc.strokeRect(groupXStart, groupYStart, groupBoxWidth, groupBoxHeight);
    }

    /**
     * Draw the name of the group.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param textOffset the offset to be added to the group start, on top of the text padding
     * @param rc the {@link IRenderingContext}
     */
    protected void drawGroupName(GraphicsContext gc, int groupXStart, int groupYStart, int textOffset, IRenderingContext rc) {
        gc.setStroke(rc.getPanelForegroundColor());
        gc.strokeText(getName(), groupXStart + textOffset + rc.getTextPadding(),
                (int) Math.round(groupYStart + rc.getLineRowHeight()/2.0 + rc.getTextHeight()/2.0),
                rc.getTaskPanelWidth() - 2 * rc.getTextPadding() - groupXStart);
    }

    /**
     * Draw the toggle button of the group in expanded state.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param squareSize the size of the button
     * @param rc the {@link IRenderingContext}
     */
    protected void drawExpandedToggleButton(GraphicsContext gc, int groupXStart, int groupYStart, int squareSize, IRenderingContext rc) {
        gc.setStroke(rc.getPanelBorderColor());
        gc.strokeRect(groupXStart, groupYStart, squareSize, squareSize);
    }

    /**
     * Draw the toggle button of the group in collapsed state.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param squareSize the size of the button
     * @param rc the {@link IRenderingContext}
     */
    protected void drawCollapsedToggleButton(GraphicsContext gc, int groupXStart, int groupYStart, int squareSize, IRenderingContext rc) {
        gc.setFill(rc.getPanelBorderColor());
        gc.fillRect(groupXStart, groupYStart, squareSize, squareSize);
    }

    @Override
    public void renderLineBackground(GraphicsContext gc, int groupXStart, int groupYStart, int renderedLines, IRenderingContext rc) {
        // Render the background of the hierarchical line
        drawLineBackground(gc, groupXStart, groupYStart, renderedLines, rc);
        // Render the background of the sub-lines
        if(!isCollapsedState()) {
            int i = 1;
            for (ITaskLine line : getItems()) {
                line.renderLineBackground(gc, groupXStart, groupYStart + i * rc.getLineRowHeight(), renderedLines + i, rc);
                i += line.getNbOfLines();
            }
        }
    }

    /**
     * Draw the background of the group line.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param renderedLines the number of rendered lines before this line
     * @param rc the {@link IRenderingContext}
     */
    protected void drawLineBackground(GraphicsContext gc, int groupXStart, int groupYStart, int renderedLines, IRenderingContext rc) {
        Color bgColor = renderedLines % 2 == 0 ? rc.getBackgroundColor() : ColorUtil.computeOddColor(rc.getBackgroundColor());
        gc.setFill(rc.isHighlightLine() ? ColorUtil.percentageUpdate(rc.getBackgroundColor(), -0.15) : bgColor);
        gc.fillRect(groupXStart, groupYStart, rc.getImageAreaWidth() - groupXStart, rc.getLineRowHeight());
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    @Override
    public Observable[] getObservableProperties() {
        List<Observable> observableList = new ArrayList<>(Arrays.asList(super.getObservableProperties()));
        observableList.add(indentSpaceProperty());
        return observableList.toArray(new Observable[0]);
    }

    @Override
    public int getNbOfLines()
    {
        //If task group is not collapsed, then one row is added to store the group name.
        return super.getNbOfLines() + (!isCollapsedState() ? 1:0);
    }
}
