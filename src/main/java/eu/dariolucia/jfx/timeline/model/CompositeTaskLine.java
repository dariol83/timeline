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
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A composite task line represents a collection of task lines in a timeline.
 * This class must be subclassed and the render methods be provided.
 */
public abstract class CompositeTaskLine extends TimelineElement implements ITaskLine {

    private final ObservableList<ITaskLine> items = FXCollections.observableArrayList(ITaskLine::getObservableProperties);
    private BoundingBox lastRenderedBounds;

    /**
     * Class constructor with no description.
     * @param name the name of the composite
     */
    protected CompositeTaskLine(String name) {
        this(name, null);
    }

    /**
     * Class constructor with name and description.
     * @param name the name of the composite
     * @param description the description of the composite
     */
    protected CompositeTaskLine(String name, String description) {
        super(name, description);
        this.items.addListener(this::listUpdated);
    }

    private void listUpdated(ListChangeListener.Change<? extends ITaskLine> change) {
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
    public boolean computeRenderingStructure() {
        boolean changed = false;
        for(ITaskLine tl : this.items) {
            changed |= tl.computeRenderingStructure();
        }
        return changed;
    }

    @Override
    public void render(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
        // Render
        int renderedTotalHeight = doRender(gc, taskLineXStart, taskLineYStart, rc);
        // Remember box
        double groupBoxTotalWidth = rc.toX(rc.getViewPortEnd()) - taskLineXStart;
        this.lastRenderedBounds = new BoundingBox(taskLineXStart, taskLineYStart, groupBoxTotalWidth, renderedTotalHeight);
    }

    protected abstract int doRender(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc);

    protected BoundingBox getLastRenderedBounds() {
        return lastRenderedBounds;
    }

    @Override
    public void noRender() {
        this.items.forEach(ITaskLine::noRender);
        this.lastRenderedBounds = null;
    }

    @Override
    public boolean isRendered() {
        return this.lastRenderedBounds != null;
    }

    @Override
    public boolean contains(double x, double y) {
        return this.lastRenderedBounds != null && this.lastRenderedBounds.contains(x, y);
    }

    @Override
    public Observable[] getObservableProperties() {
        return new Observable[] { nameProperty(), descriptionProperty(), getItems() };
    }

    public ObservableList<ITaskLine> getItems() {
        return items;
    }

    @Override
    public List<TaskItem> getTaskItems() {
        return this.items.stream().flatMap(i -> i.getTaskItems().stream()).collect(Collectors.toList());
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
}