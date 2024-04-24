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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A task line represents a line in a timeline, and it is the only {@link ITaskLine} object that contains {@link TaskItem}
 * instances.
 * Depending on the overlaps among contained task items, a task line can be rendered on multiple lines.
 * This class can be subclassed and the render() method can be overwritten. It is nevertheless important, that the
 * last rendered bounding box is saved/reset using the related methods.
 */
public class TaskLine extends LineElement implements ITaskLine {

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/

    /**
     * List of {@link TaskItem} belonging to the task line.
     */
    private final ObservableList<TaskItem> items = FXCollections.observableArrayList(TaskItem::getObservableProperties);

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/

    private BoundingBox lastRenderedBounds;
    protected final List<RenderingLine> renderingLines = new ArrayList<>();

    /**
     * Constructor of a task line.
     * @param name the name of the task line, as it appears in the task panel
     */
    public TaskLine(String name) {
        this(name, null);
    }

    /**
     * Constructor of a task line.
     * @param name the name of the task line, as it appears in the task panel
     * @param description the description of the task line
     */
    public TaskLine(String name, String description) {
        super(name, description);
        this.items.addListener(this::listUpdated);
    }

    /* *****************************************************************************************
     * Property Accessors
     * *****************************************************************************************/

    public ObservableList<TaskItem> getItems() {
        return items;
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    @Override
    public void renderLineBackground(GraphicsContext gc, int taskLineXStart, int taskLineYStart, int renderedLines, IRenderingContext rc) {
        // Render the background of the lines
        int newTaskLineYStart = taskLineYStart;
        for(int i = 0; i < this.renderingLines.size(); ++i) {
            drawTaskLineSingleLineBackground(gc, taskLineXStart, newTaskLineYStart, (renderedLines + i) % 2 == 0, rc);
            newTaskLineYStart += rc.getLineRowHeight();
        }
    }

    /**
     * Draw the background of a rendering line. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the start X in Canvas coordinates of the task line
     * @param taskLineYStart the start Y in Canvas coordinates of the task line
     * @param isEvenLine whether the line is even or odd
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskLineSingleLineBackground(GraphicsContext gc, int taskLineXStart, int taskLineYStart, boolean isEvenLine, IRenderingContext rc) {
        gc.setFill(isEvenLine ? rc.getBackgroundColor() : ColorUtil.computeOddColor(rc.getBackgroundColor()));
        gc.fillRect(taskLineXStart, taskLineYStart, rc.getImageAreaWidth() - taskLineXStart, rc.getLineRowHeight());
    }

    @Override
    public void render(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
        int taskLineHeight = rc.getLineRowHeight() * getNbOfLines();
        // Render the tasks in each rendered line
        int newTaskLineYStart = taskLineYStart;
        int i = 0;
        for(RenderingLine rl : this.renderingLines) {
            drawTaskLineSingleLine(gc, rl.getLine(), newTaskLineYStart, i == this.renderingLines.size() - 1, rc);
            newTaskLineYStart += rc.getLineRowHeight();
            ++i;
        }
        // Render the task line box in the task panel
        drawTaskLinePanelBox(gc, taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, taskLineHeight, rc);
        // Render text
        drawTaskLineName(gc, taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, taskLineHeight, rc);
        // Remember boundaries
        updateLastRenderedBounds(new BoundingBox(taskLineXStart, taskLineYStart,
                rc.getImageAreaWidth() - taskLineXStart, taskLineHeight));
    }

    /**
     * Draw the contents of a single rendering line. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param taskItemsInLine the {@link TaskItem} to be rendered
     * @param taskLineYStart the start Y in Canvas coordinates of the task line
     * @param lastLine whether this is the last rendering line of the task line
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskLineSingleLine(GraphicsContext gc, List<TaskItem> taskItemsInLine, int taskLineYStart, boolean lastLine, IRenderingContext rc) {
        // Render task items
        for(TaskItem ti : taskItemsInLine) {
            ti.render(gc, taskLineYStart, rc);
        }
        // Render bottom line
        if(lastLine) {
            gc.setStroke(rc.getPanelBorderColor());
            gc.strokeLine(rc.getTaskPanelWidth(), taskLineYStart + rc.getLineRowHeight(), rc.getImageAreaWidth(), taskLineYStart + rc.getLineRowHeight());
        }
    }

    /**
     * Draw the name of the task line in the task panel box. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the start X in Canvas coordinates of the task line
     * @param taskLineYStart the start Y in Canvas coordinates of the task line
     * @param taskLinePanelBoxWidth the width of the task line box in the task panel
     * @param taskLineHeight the full height of the task line, i.e. including all rendering lines heights
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskLineName(GraphicsContext gc, int taskLineXStart, int taskLineYStart, double taskLinePanelBoxWidth, int taskLineHeight, IRenderingContext rc) {
        gc.setStroke(rc.getPanelForegroundColor());
        gc.strokeText(getName(), taskLineXStart + rc.getTextPadding(), taskLineYStart + (int) Math.round(taskLineHeight/2.0 + rc.getTextHeight()/2.0), taskLinePanelBoxWidth - 2 * rc.getTextPadding());
    }

    /**
     * Draw the box of the task line in the task panel. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param taskLineXStart the start X in Canvas coordinates of the task line
     * @param taskLineYStart the start Y in Canvas coordinates of the task line
     * @param taskLinePanelBoxWidth the width of the task line box in the task panel
     * @param taskLineHeight the full height of the task line, i.e. including all rendering lines heights
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskLinePanelBox(GraphicsContext gc, int taskLineXStart, int taskLineYStart, double taskLinePanelBoxWidth, int taskLineHeight, IRenderingContext rc) {
        gc.setStroke(rc.getPanelBorderColor());
        gc.setFill(rc.getPanelBackground());
        gc.fillRect(taskLineXStart, taskLineYStart, taskLinePanelBoxWidth, taskLineHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, taskLinePanelBoxWidth, taskLineHeight);
    }

    @Override
    public void noRender() {
        this.items.forEach(TaskItem::noRender);
        updateLastRenderedBounds(null);
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    private void listUpdated(ListChangeListener.Change<? extends TaskItem> change) {
        while(change.next()) {
            if(change.wasAdded()) {
                change.getAddedSubList().forEach(ti -> {
                    ti.setParent(this);
                    ti.setTimeline(getTimeline());
                });
            }
            if(change.wasRemoved()) {
                change.getRemoved().forEach(ti -> {
                    ti.setParent(null);
                    ti.setTimeline(null);
                });
            }
        }
    }

    @Override
    public int getNbOfLines() {
        return Math.max(1, this.renderingLines.size());
    }

    /**
     * This method is called by the {@link Timeline} class - directly or indirectly - when an update is detected, and
     * the timeline must know if a change in the rendering structure occurred. Subclasses can override, e.g. to implement
     * different rendering approaches.
     * @return true if a change in the rendering structure of the task line occurred (i.e. one rendering line added or removed),
     * otherwise false
     */
    @Override
    public boolean computeRenderingStructure() {
        int oldSize = this.renderingLines.size();
        // Create one rendering line by default
        this.renderingLines.clear();
        this.renderingLines.add(new RenderingLine());
        // For each task...
        for (TaskItem ti : this.items) {
            boolean added = false;
            // ...check if there is a rendering line that can accept this task
            for (RenderingLine rl : this.renderingLines) {
                // If no overlap, add task
                if (!rl.overlap(ti)) {
                    rl.getLine().add(ti);
                    // Task allocated
                    added = true;
                    break;
                }
                // Check next line
            }
            // If not added...
            if (!added) {
                // ...create a line and add it to the new line
                RenderingLine rl = new RenderingLine();
                rl.getLine().add(ti);
                this.renderingLines.add(rl);
            }
        }
        return this.renderingLines.size() != oldSize;
    }


    protected void updateLastRenderedBounds(BoundingBox boundingBox) {
        this.lastRenderedBounds = boundingBox;
    }

    protected BoundingBox getLastRenderedBounds() {
        return lastRenderedBounds;
    }

    @Override
    public boolean contains(double x, double y) {
        return this.lastRenderedBounds != null && this.lastRenderedBounds.contains(x, y);
    }

    @Override
    public boolean isRendered() {
        return this.lastRenderedBounds != null;
    }

    @Override
    public Observable[] getObservableProperties() {
        return new Observable[] { nameProperty(), descriptionProperty(), getItems() };
    }

    @Override
    public List<TaskItem> getTaskItems() {
        return Collections.unmodifiableList(this.items);
    }

    @Override
    public void setTimeline(Timeline timeline) {
        super.setTimeline(timeline);
        this.items.forEach(i -> i.setTimeline(timeline));
        if(getTimeline() != null) {
            // If added to a new timeline, the rendering structure must be recomputed
            computeRenderingStructure();
        }
    }

    @Override
    public String toString() {
        return "TaskLine{" +
                "name=" + getName() +
                ", description=" + getDescription() +
                '}';
    }

    @Override
    public void notifyEvent(Event e, double x, double y) {
        // Nothing
    }

    protected static class RenderingLine {

        private final List<TaskItem> line = new ArrayList<>();

        public List<TaskItem> getLine() {
            return this.line;
        }

        public boolean overlap(TaskItem item) {
            for(TaskItem ti : this.line) {
                if(ti.overlapWith(item)) {
                    return true;
                }
            }
            return false;
        }
    }
}
