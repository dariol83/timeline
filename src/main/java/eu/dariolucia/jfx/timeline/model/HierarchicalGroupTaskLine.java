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

    @Override
    public int getNbOfLines() {
        int nbLines = 1;
        if(!isCollapsedState()) {
            for(ITaskLine tl : getItems()) {
                nbLines += tl.getNbOfLines();
            }
        }
        return nbLines;
    }

    @Override
    protected int doRender(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
        int nbLines = getNbOfLines();
        int groupBoxHeight = nbLines * rc.getLineRowHeight();
        // Draw the group box
        gc.setFill(rc.getPanelBackground());
        gc.setStroke(rc.getPanelBorderColor());
        gc.fillRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, groupBoxHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, groupBoxHeight);
        // Render the bottom line
        gc.strokeLine(rc.getTaskPanelWidth(), taskLineYStart + rc.getLineRowHeight(), rc.getImageAreaWidth(), taskLineYStart + rc.getLineRowHeight());
        // If collapsible, render the symbol
        int textOffset = 0;
        if(isCollapsible()) {
            int squareSize = rc.getTextHeight();
            textOffset = squareSize + (int) rc.getTextPadding();
            // Draw a square
            if(isCollapsed()) {
                gc.setFill(rc.getPanelBorderColor());
                gc.fillRect(taskLineXStart, taskLineYStart, squareSize, squareSize);
            } else {
                gc.setStroke(rc.getPanelBorderColor());
                gc.strokeRect(taskLineXStart, taskLineYStart, squareSize, squareSize);
            }
            // Remember square location for event processing
            setCollapseButtonBoundingBox(new BoundingBox(taskLineXStart, taskLineYStart, squareSize, squareSize));
        } else {
            // Reset square location
            setCollapseButtonBoundingBox(null);
        }
        // Render text
        gc.setStroke(rc.getPanelForegroundColor());
        gc.strokeText(getName(), taskLineXStart + rc.getTextPadding() + textOffset,
                (int) Math.round(taskLineYStart + rc.getLineRowHeight()/2.0 + rc.getTextHeight()/2.0),
                rc.getTaskPanelWidth() - 2 * rc.getTextPadding() - taskLineXStart);
        // Render the sub lines
        if(!isCollapsedState()) {
            int i = 1;
            for (ITaskLine line : getItems()) {
                line.render(gc, taskLineXStart + getIndentSpace(), taskLineYStart + i * rc.getLineRowHeight(), rc);
                i += line.getNbOfLines();
            }
        }
        // Task projection
        if(rc.getTaskProjectionHint() == TaskItemProjection.ALWAYS || (rc.getTaskProjectionHint() == TaskItemProjection.COLLAPSE && isCollapsedState())) {
            drawProjectedTasks(gc, taskLineYStart, rc);
        }
        // Remember box
        return groupBoxHeight;
    }

    @Override
    public void renderLineBackground(GraphicsContext gc, int taskLineXStart, int taskLineYStart, int renderedLines, IRenderingContext rc) {
        // Render the background of the hierarchical line
        Color bgColor = renderedLines % 2 == 0 ? rc.getBackgroundColor() : ColorUtil.computeOddColor(rc.getBackgroundColor());
        gc.setFill(rc.isHighlightLine() ? ColorUtil.percentageUpdate(rc.getBackgroundColor(), -0.15) : bgColor);
        gc.fillRect(taskLineXStart, taskLineYStart, rc.getImageAreaWidth() - taskLineXStart, rc.getLineRowHeight());
        // Render the background of the sub-lines
        if(!isCollapsedState()) {
            int i = 1;
            for (ITaskLine line : getItems()) {
                line.renderLineBackground(gc, taskLineXStart, taskLineYStart + i * rc.getLineRowHeight(), renderedLines + i, rc);
                i += line.getNbOfLines();
            }
        }
    }

    public int getIndentSpace() {
        return indentSpace.get();
    }

    public SimpleIntegerProperty indentSpaceProperty() {
        return indentSpace;
    }

    public void setIndentSpace(int indentSpace) {
        this.indentSpace.set(indentSpace);
    }

    @Override
    public Observable[] getObservableProperties() {
        List<Observable> observableList = new ArrayList<>(Arrays.asList(super.getObservableProperties()));
        observableList.add(indentSpaceProperty());
        return observableList.toArray(new Observable[0]);
    }
}
