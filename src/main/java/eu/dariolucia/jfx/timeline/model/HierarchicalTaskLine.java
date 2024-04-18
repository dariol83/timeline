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

import javafx.scene.canvas.GraphicsContext;

/**
 * A hierarchical task line represents a group of {@link ITaskLine} in a timeline with hierarchical representation.
 * This class can be subclassed and the doRender() method can be overwritten.
 */
public class HierarchicalTaskLine extends CompositeTaskLine {

    /**
     * Class constructor with no description.
     * @param name the name of the task group
     */
    public HierarchicalTaskLine(String name) {
        this(name, null);
    }

    /**
     * Class constructor with name and description.
     * @param name the name of the task group
     * @param description the description of the task group
     */
    public HierarchicalTaskLine(String name, String description) {
        super(name, description);
    }

    @Override
    public int getNbOfLines() {
        int nbLines = 0;
        for(ITaskLine tl : getItems()) {
            nbLines += tl.getNbOfLines();
        }
        return nbLines + 1;
    }

    @Override
    protected int doRender(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
        int nbLines = getNbOfLines();
        int groupBoxHeight = nbLines * rc.getLineRowHeight();
        // Draw the group box
        gc.setFill(rc.getPanelBackgroundColor());
        gc.setStroke(rc.getPanelBorderColor());
        gc.fillRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, groupBoxHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, groupBoxHeight);
        // Render the bottom line
        gc.strokeLine(rc.getTaskPanelWidth(), taskLineYStart + rc.getLineRowHeight(), rc.getImageAreaWidth(), taskLineYStart + rc.getLineRowHeight());
        // Render text
        gc.setStroke(rc.getPanelForegroundColor());
        gc.strokeText(getName(), taskLineXStart + rc.getTextPadding(),
                (int) Math.round(taskLineYStart + rc.getLineRowHeight()/2.0 + rc.getTextHeight()/2.0),
                rc.getTaskPanelWidth() - taskLineXStart - rc.getTextPadding());
        // Render the sub lines
        int i = 1;
        for(ITaskLine line : getItems()) {
            line.render(gc, taskLineXStart + rc.getTextPadding(), taskLineYStart + i * rc.getLineRowHeight(), rc);
            i += line.getNbOfLines();
        }
        // Remember box
        return groupBoxHeight;
    }

    @Override
    public void renderLineBackground(GraphicsContext gc, int taskLineXStart, int taskLineYStart, int renderedLines, IRenderingContext rc) {
        // Render the background of the tree line
        gc.setFill(renderedLines % 2 == 0 ? rc.getBackgroundColor() : TaskLine.computeOddColor(rc.getBackgroundColor()));
        gc.fillRect(taskLineXStart, taskLineYStart, rc.getImageAreaWidth() - taskLineXStart, rc.getLineRowHeight());
        // Render the background of the sub-lines
        int i = 1;
        for(ITaskLine line : getItems()) {
            line.renderLineBackground(gc, taskLineXStart, taskLineYStart + i * rc.getLineRowHeight(), renderedLines + i, rc);
            i += line.getNbOfLines();
        }
    }
}
