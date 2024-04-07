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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A task line represents a line in a timeline, and contains {@link TaskItem} instances.
 * In its current implementation, a task line is always rendered on a single line: overalapping task items are rendered
 * one on top of the other, therefore the end result might not be the best.
 * This class can be subclassed and the render() method can be overwritten. It is nevertheless important, that the
 * last rendered bounding box is saved/reset using the related methods.
 */
public class TaskLine implements ITaskLine {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final ObservableList<TaskItem> items = FXCollections.observableArrayList(param -> new Observable[] {
            param.startTimeProperty(), param.nameProperty(), param.expectedDurationProperty(), param.actualDurationProperty(),
            param.taskBackgroundColorProperty(), param.taskTextColorProperty() });
    private ITaskLine parent;
    private Timeline timeline;
    private BoundingBox lastRenderedBounds;

    private List<RenderingLine> renderingLines = new ArrayList<>();

    public TaskLine(String name) {
        this(name, null);
    }

    public TaskLine(String name, String description) {
        this.name.set(name);
        this.description.set(description);
        this.items.addListener(this::listUpdated);
    }

    private void listUpdated(ListChangeListener.Change<? extends TaskItem> change) {
        while(change.next()) {
            if(change.wasAdded()) {
                change.getAddedSubList().forEach(ti -> {
                    ti.setParent(this);
                    ti.setTimeline(this.timeline);
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

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    @Override
    public int getNbOfLines() {
        return Math.max(1, this.renderingLines.size());
    }

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

    @Override
    public void render(GraphicsContext gc, double taskLineXStart, double taskLineYStart, RenderingContext rc) {
        // Render the tasks in each rendered line
        double newTaskLineYStart = taskLineYStart;
        for(RenderingLine rl : this.renderingLines) {
            rl.render(gc, newTaskLineYStart, rc);
            newTaskLineYStart += rc.getLineRowHeight();
        }
        // Render the line in the task panel
        double taskLineHeight = rc.getLineRowHeight() * getNbOfLines();
        gc.setStroke(Color.DARKGRAY);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, taskLineHeight);
        gc.strokeRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, taskLineHeight);
        // Render text
        gc.setStroke(Color.BLACK);
        gc.strokeText(getName(), taskLineXStart + rc.getTextPadding(), taskLineYStart + taskLineHeight/2 + rc.getTextHeight()/2, rc.getTaskPanelWidth() - 2 * rc.getTextPadding() - taskLineXStart);
        // Remember boundaries
        updateLastRenderedBounds(new BoundingBox(taskLineXStart, taskLineYStart,
                rc.getImageAreaWidth() - taskLineXStart, taskLineHeight));
    }

    protected void updateLastRenderedBounds(BoundingBox boundingBox) {
        this.lastRenderedBounds = boundingBox;
    }

    protected BoundingBox getLastRenderedBounds() {
        return lastRenderedBounds;
    }

    @Override
    public void noRender() {
        this.items.forEach(TaskItem::noRender);
        updateLastRenderedBounds(null);
    }

    @Override
    public boolean contains(double x, double y) {
        return this.lastRenderedBounds != null && this.lastRenderedBounds.contains(x, y);
    }

    @Override
    public boolean isRendered() {
        return this.lastRenderedBounds != null;
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public ObservableList<TaskItem> getItems() {
        return items;
    }

    @Override
    public Observable[] getObservableProperties() {
        return new Observable[] { nameProperty(), descriptionProperty(), getItems() };
    }

    @Override
    public ITaskLine getParent() {
        return parent;
    }

    @Override
    public List<TaskItem> getTaskItems() {
        return Collections.unmodifiableList(this.items);
    }

    @Override
    public void setParent(ITaskLine parent) {
        this.parent = parent;
    }

    @Override
    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
        this.items.forEach(i -> i.setTimeline(timeline));
    }

    @Override
    public String toString() {
        return "TaskLine{" +
                "name=" + getName() +
                ", description=" + getDescription() +
                '}';
    }

    private static class RenderingLine {

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

        public void render(GraphicsContext gc, double taskLineYStart, RenderingContext rc) {
            for(TaskItem ti : this.line) {
                ti.render(gc, taskLineYStart, rc);
            }
            // Render bottom line
            gc.setStroke(Color.LIGHTGRAY);
            gc.strokeLine(rc.getTaskPanelWidth(), taskLineYStart + rc.getLineRowHeight(), rc.getImageAreaWidth(), taskLineYStart + rc.getLineRowHeight());
        }
    }
}
