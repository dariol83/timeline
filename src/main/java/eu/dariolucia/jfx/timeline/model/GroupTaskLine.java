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

import java.util.List;
import java.util.stream.Collectors;

/**
 * A task group represents a group of task lines or subgroups in a timeline.
 * This class can be subclassed and the render() method can be overwritten. It is nevertheless important, that the
 * last rendered bounding box is saved/reset using the related methods.
 */
public class GroupTaskLine implements ITaskLine {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final ObservableList<ITaskLine> items = FXCollections.observableArrayList(ITaskLine::getObservableProperties);
    private ITaskLine parent;
    private Timeline timeline;
    private BoundingBox lastRenderedBounds;

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
        this.name.set(name);
        this.description.set(description);
        this.items.addListener(this::listUpdated);
    }

    private void listUpdated(ListChangeListener.Change<? extends ITaskLine> change) {
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
        int nbLines = 0;
        for(ITaskLine tl : this.items) {
            nbLines += tl.getNbOfLines();
        }
        return nbLines;
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
    public void render(GraphicsContext gc, double taskLineXStart, double taskLineYStart, IRenderingContext rc) {
        int nbLines = getNbOfLines();
        double groupBoxWidth = rc.getLineRowHeight();
        // Render the sub lines
        int i = 0;
        for(ITaskLine line : this.items) {
            line.render(gc, taskLineXStart + groupBoxWidth, taskLineYStart + i * rc.getLineRowHeight(), rc);
            i += line.getNbOfLines();
        }
        double groupBoxHeight = getNbOfLines() * rc.getLineRowHeight();
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
        double textWidth = rc.getTextWidth(gc, getName());
        double offset = nbLines * rc.getLineRowHeight()/2;
        gc.strokeText(getName(), - offset - textWidth/2, groupBoxWidth/2 + rc.getTextHeight()/2);
        gc.restore();
        // Remember box
        double groupBoxTotalWidth = rc.toX(rc.getViewPortEnd()) - taskLineXStart;
        this.lastRenderedBounds = new BoundingBox(taskLineXStart, taskLineYStart, groupBoxTotalWidth, groupBoxHeight);
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

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public ObservableList<ITaskLine> getItems() {
        return items;
    }

    @Override
    public ITaskLine getParent() {
        return parent;
    }

    @Override
    public List<TaskItem> getTaskItems() {
        return this.items.stream().flatMap(i -> i.getTaskItems().stream()).collect(Collectors.toList());
    }

    @Override
    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
        this.items.forEach(i -> i.setTimeline(timeline));
        if(this.timeline != null) {
            // If added to a new timeline, the rendering structure must be recomputed
            computeRenderingStructure();
        }
    }

    @Override
    public void setParent(ITaskLine parent) {
        this.parent = parent;
    }
}
