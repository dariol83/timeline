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
import javafx.event.Event;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * This interface is used to abstract the access to the lines of a timeline (group or single).
 */
public interface ITaskLine extends ILineElement {

    /**
     * Return the height of task line in pixels
     * @param rc {@link IRenderingContext}
     * @return Return the height of task line in pixels
     */
    default int getHeight(IRenderingContext rc)
    {
        return rc.getLineRowHeight() * getNbOfLines();
    }

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
     * Draw the box of the task line in the task panel. Subclasses can override.
     * Composite task also uses this
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the start X in Canvas coordinates of the task line
     * @param taskLineYStart the start Y in Canvas coordinates of the task line
     * @param taskLinePanelBoxWidth the width of the task line box in the task panel
     * @param taskLineHeight the full height of the task line, i.e. including all rendering lines heights
     * @param rc the {@link IRenderingContext}
     */
    default void drawTaskLineBox(GraphicsContext gc, int taskLineXStart, int taskLineYStart, double taskLinePanelBoxWidth, int taskLineHeight, IRenderingContext rc)
    {
        gc.setStroke(rc.getPanelBorderColor());
        gc.setFill(rc.getPanelBackground());
        gc.fillRect(0, taskLineYStart, taskLineXStart+taskLinePanelBoxWidth, taskLineHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, taskLinePanelBoxWidth, taskLineHeight);
    }

    /**
     * Draw the name of the task line in the task panel box. Subclasses can override.
     * Composite task also uses this
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the start X in Canvas coordinates of the task line
     * @param taskLineYStart the start Y in Canvas coordinates of the task line
     * @param taskLinePanelBoxWidth the width of the task line box in the task panel
     * @param taskLineHeight the full height of the task line, i.e. including all rendering lines heights
     * @param rc the {@link IRenderingContext}
     * @param textOffset the offset to be added to the group/line start, on top of the text padding
     */
    default void drawTaskLineName(GraphicsContext gc, int taskLineXStart, int taskLineYStart, double taskLinePanelBoxWidth, int taskLineHeight, int textOffset, IRenderingContext rc)
    {
        gc.setStroke(rc.getPanelForegroundColor());
        gc.strokeText(getName(), taskLineXStart + textOffset + rc.getTextPadding(),
            taskLineYStart + (int) Math.round(taskLineHeight/2.0 + rc.getTextHeight()/2.0),
            taskLinePanelBoxWidth - 2 * rc.getTextPadding() - taskLineXStart);
    }

    /**
     * Draw the box of the task line in the additional panel. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the start X in Canvas coordinates of the task line
     * @param taskLineYStart the start Y in Canvas coordinates of the task line
     * @param taskLineAdditionalBoxWidth the width of the task line box in the additional panel
     * @param taskLineHeight the full height of the task line, i.e. including all rendering lines heights
     * @param rc the {@link IRenderingContext}
     */
    default void drawAdditionalTaskLineBox(GraphicsContext gc, int taskLineXStart, int taskLineYStart, double taskLineAdditionalBoxWidth, int taskLineHeight, IRenderingContext rc)
    {
        gc.setStroke(rc.getPanelBorderColor());
        gc.setFill(rc.getPanelBackground());
        gc.fillRect(taskLineXStart, taskLineYStart, taskLineAdditionalBoxWidth, taskLineHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, taskLineAdditionalBoxWidth, taskLineHeight);
    }

    default void drawAdditionalTaskLineName(GraphicsContext gc, int taskLineXStart, int taskLineYStart, double taskLineAdditionalBoxWidth, int taskLineHeight, IRenderingContext rc)
    {
        gc.setStroke(rc.getPanelForegroundColor());
        gc.strokeText(getDescription(), taskLineXStart + rc.getTextPadding(),
                taskLineYStart + (int) Math.round(taskLineHeight/2.0 + rc.getTextHeight()/2.0),
                taskLineAdditionalBoxWidth - 2 * rc.getTextPadding());
    }

    /**
     * Render the background of the task line.
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the X offset where the background coverage has to start
     * @param taskLineYStart the Y offset where the background coverage has to start
     * @param renderedLines the current number of lines rendered so far
     * @param rc the {@link IRenderingContext}
     */
    void renderLineBackground(GraphicsContext gc, int taskLineXStart, int taskLineYStart, int renderedLines, IRenderingContext rc);

    /**
     * Render the time interval of the task line
     * @param gc the {@link GraphicsContext}
     * @param taskLineYStart the Y offset where the interval has to start
     * @param taskLineHeight the height of the task line that the interval should fill
     * @param rc the {@link IRenderingContext}
     * @param foreground draw interval on foreground
     */
    void renderLineInterval(GraphicsContext gc, int taskLineYStart, int taskLineHeight, IRenderingContext rc, boolean foreground);

    /**
     * This method requests the rendering of this task line, in line with the information provided as method's arguments.
     * This method is not supposed to be called by external class users.
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the X offset where the task line has to start: this value is 0 for top-level task lines
     * @param taskLineYStart the Y offset where the task line has to start
     * @param rc the {@link IRenderingContext}
     */
    void render(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc);

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
     * This method is used to propagate {@link eu.dariolucia.jfx.timeline.Timeline}'s registered events down to ITaskLine
     * objects for possible graphical reactions. Actions to be performed at the level of {@link TaskItem} must be externally
     * handled.
     * @param e the raised event
     * @param x the x coordinate in Canvas area coordinates
     * @param y the y coordinate in Canvas area coordinates
     */
    void notifyEvent(Event e, double x, double y);

    /**
     * Return the list of the observable properties, used to monitor the inner list of task lines or task items.
     * This method is not supposed to be called by external class users.
     * @return the observable properties
     */
    Observable[] getObservableProperties();

    /**
     * Return all the {@link TaskItem} contained in this task line. This method computes recursively all such items.
     * @return all the {@link TaskItem} contained in this task line
     */
    List<TaskItem> getTaskItems();

}
