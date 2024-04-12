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

import eu.dariolucia.jfx.timeline.Timeline;
import javafx.beans.Observable;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * This interface is used to abstract the access to the lines of a timeline (group or single).
 */
public interface ITaskLine {

    /**
     * Return the name of the task line, which is display in the task panel.
     * @return the name of the task line
     */
    String getName();

    /**
     * Return the description of the task line.
     * @return the name of the task line
     */
    String getDescription();

    /**
     * Return the number of lines rendered by this task line. This number changes depending on the type and number of
     * nested task lines and whether there are time overlaps between {@link TaskItem} of the same {@link TaskLine}.
     * @return the number of lines rendered by this object
     */
    int getNbOfLines();

    /**
     * Ask the item to recompute the rendering structure and return whether there was a structural change (more/fewer lines
     * to be drawn). The {@link ITaskLine} implementation shall use this opportunity to recompute the rendering structure.
     * This method is not supposed to be called by external class users.
     * @return true if the rendering structure changes, otherwise false
     */
    boolean computeRenderingStructure();

    /**
     * This method requests the rendering of this task line, in line with the information provided as method's arguments.
     * This method is not supposed to be called by external class users.
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the X offset where the task line has to start: this value is 0 for top-level task lines
     * @param taskLineYStart the Y offset where the task line has to start
     * @param rc the {@link IRenderingContext}
     */
    void render(GraphicsContext gc, double taskLineXStart, double taskLineYStart, IRenderingContext rc);

    /**
     * Inform this task line that there is a rendering cycle but the line won't be rendered because it is not in the viewport.
     * This method is not supposed to be called by external class users.
     */
    void noRender();

    /**
     * Return true if the task line is rendered (completely or partially), otherwise false.
     * @return true if the task line is rendered (completely or partially), otherwise false
     */
    boolean isRendered();

    /**
     * Return whether x and y, in Canvas area coordinates, fall inside the rendering boundaries of the task line.
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if x,y is within the rendering boundaries of the task line, otherwise false
     */
    boolean contains(double x, double y);

    /**
     * Return the list of the observable properties, used to monitor the inner list of task lines or task items.
     * This method is not supposed to be called by external class users.
     * @return the observable properties
     */
    Observable[] getObservableProperties();

    /**
     * Set the parent task line of this task line. This is null if the task line is a top level task line.
     * This method is not supposed to be called by external class users.
     * @param parent the parent task line
     */
    void setParent(ITaskLine parent);

    /**
     * Get the parent task line of this task line. This is null if the task line is a top level task line.
     * This method is not supposed to be called by external class users.
     * @return the parent task line, or null if this is a top level task line
     */
    ITaskLine getParent();

    /**
     * Return all the {@link TaskItem} contained in this task line. This method computes recursively all such items.
     * @return all the {@link TaskItem} contained in this task line
     */
    List<TaskItem> getTaskItems();

    /**
     * Set the owning timeline of this task line. This is null if the task line is not in a timeline.
     * This method is not supposed to be called by external class users.
     * @param timeline the owning timeline
     */
    void setTimeline(Timeline timeline);

    /**
     * Get the owning {@link Timeline} of this task line. This is null if the task line is not in a {@link Timeline}.
     * This method is not supposed to be called by external class users.
     * @return the owning {@link Timeline} of this task line, or null if this is not part of a {@link Timeline}
     */
    Timeline getTimeline();
}
