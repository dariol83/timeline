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
 * A task group represents a fixed group of {@link ITaskLine} in a timeline, grouped together with a vertical task label.
 * This class can be subclassed and the doRender() method can be overwritten.
 */
public class GroupTaskLine extends CompositeTaskLine {

    /**
     * Class constructor with no description.
     * @param name the name of the task group
     */
    public GroupTaskLine(String name) {
        this(name, null);
    }

    /**
     * Class constructor with name and description.
     * @param name the name of the task group
     * @param description the description of the task group
     */
    public GroupTaskLine(String name, String description) {
        super(name, description);
    }

    @Override
    public int getNbOfLines() {
        int nbLines = 0;
        for(ITaskLine tl : getItems()) {
            nbLines += tl.getNbOfLines();
        }
        return nbLines;
    }

    @Override
    protected int doRender(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
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
        gc.setFill(rc.getPanelBackgroundColor());
        gc.setStroke(rc.getPanelBorderColor());
        gc.fillRect(taskLineXStart, taskLineYStart, groupBoxWidth, groupBoxHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, groupBoxWidth, groupBoxHeight);
        gc.setStroke(rc.getPanelForegroundColor());

        gc.save();
        gc.translate(taskLineXStart, taskLineYStart);
        gc.rotate(-90);
        // Render in the middle
        int textWidth = rc.getTextWidth(gc, getName());
        double offset = nbLines * rc.getLineRowHeight()/2.0;
        gc.strokeText(getName(), (int) Math.round(- offset - textWidth/2.0), (int) Math.round(groupBoxWidth/2.0 + rc.getTextHeight()/2.0));
        gc.restore();
        // Return the box height
        return groupBoxHeight;
    }

    @Override
    public void renderLineBackground(GraphicsContext gc, int taskLineXStart, int taskLineYStart, int renderedLines, IRenderingContext rc) {
        // Render the background of the sub-lines
        int i = 0;
        for(ITaskLine line : getItems()) {
            line.renderLineBackground(gc, taskLineXStart, taskLineYStart + i * rc.getLineRowHeight(), renderedLines + i, rc);
            i += line.getNbOfLines();
        }
    }
}
