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

    @Override
    protected int doRender(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
        if(isCollapsedState()) {
            return renderCollapsed(gc, taskLineXStart, taskLineYStart, rc);
        } else {
            return renderNotCollapsed(gc, taskLineXStart, taskLineYStart, rc);
        }
    }

    private int renderCollapsed(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
        // Render the line in the task panel
        int taskLineHeight = rc.getLineRowHeight();
        gc.setStroke(rc.getPanelBorderColor());
        gc.setFill(rc.getPanelBackground());
        gc.fillRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, taskLineHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, taskLineHeight);
        // If collapsible, render the symbol
        int textOffset = 0;
        if(isCollapsible()) {
            int squareSize = rc.getTextHeight();
            // Draw a full square
            gc.setFill(rc.getPanelBorderColor());
            gc.fillRect(taskLineXStart, taskLineYStart,
                    squareSize, squareSize);
            textOffset = squareSize + (int) rc.getTextPadding();
            // Remember square location for event processing
            setCollapseButtonBoundingBox(new BoundingBox(taskLineXStart, taskLineYStart, squareSize, squareSize));
        } else {
            // Reset square location
            setCollapseButtonBoundingBox(null);
        }
        // Render text
        gc.setStroke(rc.getPanelForegroundColor());
        gc.strokeText(getName(), taskLineXStart + textOffset + rc.getTextPadding(),
                (int) Math.round(taskLineYStart + rc.getLineRowHeight()/2.0 + rc.getTextHeight()/2.0),
                rc.getTaskPanelWidth() - 2 * rc.getTextPadding() - taskLineXStart);
        // Render task bottom line
        gc.setStroke(rc.getPanelBorderColor());
        gc.strokeLine(rc.getTaskPanelWidth(), taskLineYStart + rc.getLineRowHeight(), rc.getImageAreaWidth(), taskLineYStart + rc.getLineRowHeight());
        // Task projection
        if(rc.getTaskProjectionHint() == TaskItemProjection.ALWAYS || rc.getTaskProjectionHint() == TaskItemProjection.COLLAPSE) {
            drawProjectedTasks(gc, taskLineYStart, rc);
        }
        return taskLineHeight;
    }

    private int renderNotCollapsed(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
        int nbLines = getNbOfLines();
        int groupBoxWidth = rc.getLineRowHeight();
        // Render the sub lines
        int i = 0;
        for(ITaskLine line : getItems()) {
            line.render(gc, taskLineXStart + groupBoxWidth, taskLineYStart + i * rc.getLineRowHeight(), rc);
            i += line.getNbOfLines();
        }
        int groupBoxHeight = getNbOfLines() * rc.getLineRowHeight();
        // Draw the group box
        gc.setFill(rc.getPanelBackground());
        gc.setStroke(rc.getPanelBorderColor());
        gc.fillRect(taskLineXStart, taskLineYStart, groupBoxWidth, groupBoxHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, groupBoxWidth, groupBoxHeight);
        // If collapsible, render the symbol
        if(isCollapsible()) {
            int squareSize = rc.getTextHeight();
            // Draw an empty square
            gc.strokeRect(taskLineXStart, taskLineYStart,
                    squareSize, squareSize);
            // Remember square location for event processing
            setCollapseButtonBoundingBox(new BoundingBox(taskLineXStart, taskLineYStart, squareSize, squareSize));
        } else {
            // Reset square location
            setCollapseButtonBoundingBox(null);
        }
        // Render text
        gc.setStroke(rc.getPanelForegroundColor());
        gc.save();
        gc.translate(taskLineXStart, taskLineYStart);
        gc.rotate(-90);
        // Render in the middle
        int textWidth = rc.getTextWidth(gc, getName());
        double offset = nbLines * rc.getLineRowHeight()/2.0;
        gc.strokeText(getName(), (int) Math.round(- offset - textWidth/2.0), (int) Math.round(groupBoxWidth/2.0 + rc.getTextHeight()/2.0));
        gc.restore();
        // If not collapsed, the task projection cannot be rendered in any case
        // Return the box height
        return groupBoxHeight;
    }

    @Override
    public void renderLineBackground(GraphicsContext gc, int taskLineXStart, int taskLineYStart, int renderedLines, IRenderingContext rc) {
        // Render the background of the sub-lines
        if(!isCollapsedState()) {
            int i = 0;
            for(ITaskLine line : getItems()) {
                line.renderLineBackground(gc, taskLineXStart, taskLineYStart + i * rc.getLineRowHeight(), renderedLines + i, rc);
                i += line.getNbOfLines();
            }
        } else {
            // Render the background of the hierarchical line
            Color bgColor = renderedLines % 2 == 0 ? rc.getBackgroundColor() : ColorUtil.computeOddColor(rc.getBackgroundColor());
            gc.setFill(rc.isHighlightLine() ? ColorUtil.percentageUpdate(rc.getBackgroundColor(), -0.15) : bgColor);
            gc.fillRect(taskLineXStart, taskLineYStart, rc.getImageAreaWidth() - taskLineXStart, rc.getLineRowHeight());
        }
    }
}
