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

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A flat group represents a fixed group of {@link ITaskLine} in a timeline, grouped together with a vertical task label.
 * This class can be subclassed and the doRender() method can be overwritten.
 */
public class FlatGroupTaskLine extends CompositeTaskLine {

    /**
     * Class constructor with no description.
     * @param name the name of the task group
     */
    public FlatGroupTaskLine(String name) {
        this(name, null);
    }

    /**
     * Class constructor with name and description.
     * @param name the name of the task group
     * @param description the description of the task group
     */
    public FlatGroupTaskLine(String name, String description) {
        super(name, description);
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    @Override
    protected int doRender(GraphicsContext gc, int groupXStart, int groupYStart, IRenderingContext rc) {
        if(isCollapsedState()) {
            return renderCollapsedGroup(gc, groupXStart, groupYStart, rc);
        } else {
            return renderExpandedGroup(gc, groupXStart, groupYStart, rc);
        }
    }

    /**
     * Render the flat group in a collapsed state.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the start X of the group in Canvas coordinates
     * @param groupYStart the start Y of the group in Canvas coordinates
     * @param rc the {@link IRenderingContext}
     * @return the complete height of the group
     */
    protected int renderCollapsedGroup(GraphicsContext gc, int groupXStart, int groupYStart, IRenderingContext rc) {
        // Render the line in the task panel
        int taskLineHeight = rc.getLineRowHeight();
        drawCollapsedGroupPanelBox(gc, groupXStart, groupYStart, (int) Math.round(rc.getTaskPanelWidth() - groupXStart), taskLineHeight, rc);
        // If collapsible, render the symbol
        int textOffset = 0;
        if(isCollapsible()) {
            int squareSize = rc.getTextHeight();
            // Draw a full square
            drawCollapsedToggleButton(gc, groupXStart, groupYStart, squareSize, rc);
            textOffset = squareSize + (int) rc.getTextPadding();
            // Remember square location for event processing
            setCollapseButtonBoundingBox(new BoundingBox(groupXStart, groupYStart, squareSize, squareSize));
        } else {
            // Reset square location
            setCollapseButtonBoundingBox(null);
        }
        // Render name
        drawCollapsedGroupName(gc, groupXStart, groupYStart, textOffset, rc);
        // Render task bottom line
        gc.setStroke(rc.getPanelBorderColor());
        gc.strokeLine(rc.getTaskPanelWidth(), groupYStart + rc.getLineRowHeight(), rc.getImageAreaWidth(), groupYStart + rc.getLineRowHeight());
        // Task projection
        if(rc.getTaskProjectionHint() == TaskItemProjection.ALWAYS || rc.getTaskProjectionHint() == TaskItemProjection.COLLAPSE) {
            drawProjectedTasks(gc, groupYStart, rc);
        }
        return taskLineHeight;
    }

    /**
     * Draw the name of the group in collapsed state.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param textOffset the offset to be added to the group start, on top of the text padding
     * @param rc the {@link IRenderingContext}
     */
    protected void drawCollapsedGroupName(GraphicsContext gc, int groupXStart, int groupYStart, int textOffset, IRenderingContext rc) {
        gc.setStroke(rc.getPanelForegroundColor());
        gc.strokeText(getName(), groupXStart + textOffset + rc.getTextPadding(),
                (int) Math.round(groupYStart + rc.getLineRowHeight()/2.0 + rc.getTextHeight()/2.0),
                rc.getTaskPanelWidth() - 2 * rc.getTextPadding() - groupXStart);
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

    /**
     * Draw the box of the group in the task panel in collapsed state.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param groupBoxWidth the width of the group box
     * @param groupBoxHeight the height of the group box
     * @param rc the {@link IRenderingContext}
     */
    protected void drawCollapsedGroupPanelBox(GraphicsContext gc, int groupXStart, int groupYStart, int groupBoxWidth, int groupBoxHeight, IRenderingContext rc) {
        gc.setStroke(rc.getPanelBorderColor());
        gc.setFill(rc.getPanelBackground());
        gc.fillRect(groupXStart, groupYStart, groupBoxWidth, groupBoxHeight);
        gc.strokeRect(groupXStart, groupYStart, groupBoxWidth, groupBoxHeight);
    }

    /**
     * Draw the group in expanded state.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param rc the {@link IRenderingContext}
     * @return the height of the group as rendered, in pixels
     */
    protected int renderExpandedGroup(GraphicsContext gc, int groupXStart, int groupYStart, IRenderingContext rc) {
        int groupBoxWidth = rc.getLineRowHeight();
        // Render the sub lines
        int i = 0;
        for(ITaskLine line : getItems()) {
            line.render(gc, groupXStart + groupBoxWidth, groupYStart + i * rc.getLineRowHeight(), rc);
            i += line.getNbOfLines();
        }
        int groupBoxHeight = getNbOfLines() * rc.getLineRowHeight();
        // Draw the group box
        drawExpandedGroupPanelBox(gc, groupXStart, groupYStart, groupBoxWidth, groupBoxHeight, rc);
        // If collapsible, render the symbol
        if(isCollapsible()) {
            int squareSize = rc.getTextHeight();
            // Draw an empty square
            drawExpandedToggleButton(gc, groupXStart, groupYStart, squareSize, rc);
            // Remember square location for event processing
            setCollapseButtonBoundingBox(new BoundingBox(groupXStart, groupYStart, squareSize, squareSize));
        } else {
            // Reset square location
            setCollapseButtonBoundingBox(null);
        }
        // Render text
        drawExpandedGroupName(gc, groupXStart, groupYStart, groupBoxWidth, groupBoxHeight, rc);
        // If not collapsed, the task projection cannot be rendered in any case
        // Return the box height
        return groupBoxHeight;
    }

    /**
     * Draw the name of the group in the task panel in expanded state.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param groupBoxWidth the width of the group box
     * @param groupBoxHeight the height of the group box
     * @param rc the {@link IRenderingContext}
     */
    protected void drawExpandedGroupName(GraphicsContext gc, int groupXStart, int groupYStart, int groupBoxWidth, int groupBoxHeight, IRenderingContext rc) {
        gc.setStroke(rc.getPanelForegroundColor());
        gc.save();
        gc.translate(groupXStart, groupYStart);
        gc.rotate(-90);
        // Render in the middle
        int textWidth = rc.getTextWidth(gc, getName());
        double offset = groupBoxHeight/2.0;
        gc.strokeText(getName(), (int) Math.round(-offset - textWidth/2.0), (int) Math.round(groupBoxWidth/2.0 + rc.getTextHeight()/2.0));
        gc.restore();
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
        gc.setFill(rc.getPanelBorderColor());
        gc.strokeRect(groupXStart, groupYStart, squareSize, squareSize);
    }

    /**
     * Draw the box of the group in the task panel in expanded state.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param groupBoxWidth the width of the group box
     * @param groupBoxHeight the height of the group box
     * @param rc the {@link IRenderingContext}
     */
    protected void drawExpandedGroupPanelBox(GraphicsContext gc, int groupXStart, int groupYStart, int groupBoxWidth, int groupBoxHeight, IRenderingContext rc) {
        gc.setFill(rc.getPanelBackground());
        gc.setStroke(rc.getPanelBorderColor());
        gc.fillRect(groupXStart, groupYStart, groupBoxWidth, groupBoxHeight);
        gc.strokeRect(groupXStart, groupYStart, groupBoxWidth, groupBoxHeight);
    }

    @Override
    public void renderLineBackground(GraphicsContext gc, int groupXStart, int groupYStart, int renderedLines, IRenderingContext rc) {
        // Render the background of the sub-lines in expanded state
        if(!isCollapsedState()) {
            int i = 0;
            for(ITaskLine line : getItems()) {
                line.renderLineBackground(gc, groupXStart, groupYStart + i * rc.getLineRowHeight(), renderedLines + i, rc);
                i += line.getNbOfLines();
            }
        } else {
            // Render the background of the flat group line in collapsed state
            drawCollapsedLineBackground(gc, groupXStart, groupYStart, renderedLines, rc);
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
    protected void drawCollapsedLineBackground(GraphicsContext gc, int groupXStart, int groupYStart, int renderedLines, IRenderingContext rc) {
        Color bgColor = renderedLines % 2 == 0 ? rc.getBackgroundColor() : ColorUtil.computeOddColor(rc.getBackgroundColor());
        gc.setFill(rc.isHighlightLine() ? ColorUtil.percentageUpdate(rc.getBackgroundColor(), -0.15) : bgColor);
        gc.fillRect(groupXStart, groupYStart, rc.getImageAreaWidth() - groupXStart, rc.getLineRowHeight());
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    @Override
    public int getNbOfLines() {
        if(!isCollapsedState()) {
            int nbLines = 0;
            for (ITaskLine tl : getItems()) {
                nbLines += tl.getNbOfLines();
            }
            return nbLines;
        } else {
            // Collapsed: 1 line
            return 1;
        }
    }
}
