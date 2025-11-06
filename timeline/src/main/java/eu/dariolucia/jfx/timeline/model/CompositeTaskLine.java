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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A composite task line represents a collection of task lines in a timeline.
 * This class must be subclassed and the render methods be provided.
 */
public abstract class CompositeTaskLine extends LineElement implements ITaskLine {

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/

    /**
     * If true, the task line can be collapsed and the collapsed/expanded marker is rendered.
     */
    private final SimpleBooleanProperty collapsible = new SimpleBooleanProperty(false);
    /**
     * If true, the task line is rendered collapsed, if the group is collapsible.
     */
    private final SimpleBooleanProperty collapsed = new SimpleBooleanProperty(false);
    /**
     * If true, group collapsing can be toggled with the mouse click.
     */
    private final SimpleBooleanProperty mouseCollapsingEnabled = new SimpleBooleanProperty(true);
    /**
     * List of {@link ITaskLine} contained lines.
     */
    private final ObservableList<ITaskLine> items = FXCollections.observableArrayList(ITaskLine::getObservableProperties);

    private final ObservableList<TimeInterval> intervals = FXCollections.observableArrayList(TimeInterval::getObservableProperties);

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/
    private boolean collapsedState = false;
    private BoundingBox collapseButtonBoundingBox = null;
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
        this.intervals.addListener(this::listUpdated);
    }

    /* *****************************************************************************************
     * Property Accessors
     * *****************************************************************************************/

    public boolean isCollapsible() {
        return collapsible.get();
    }

    public SimpleBooleanProperty collapsibleProperty() {
        return collapsible;
    }

    public void setCollapsible(boolean collapsable) {
        this.collapsible.set(collapsable);
    }

    public boolean isCollapsed() {
        return collapsed.get();
    }

    public SimpleBooleanProperty collapsedProperty() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed.set(collapsed);
    }

    public boolean isMouseCollapsingEnabled() {
        return mouseCollapsingEnabled.get();
    }

    public SimpleBooleanProperty mouseCollapsingEnabledProperty() {
        return mouseCollapsingEnabled;
    }

    public void setMouseCollapsingEnabled(boolean mouseCollapsingEnabled) {
        this.mouseCollapsingEnabled.set(mouseCollapsingEnabled);
    }

    /**
     * Return the contained {@link ITaskLine} items.
     * @return the contained {@link ITaskLine} items
     */
    public ObservableList<ITaskLine> getItems() {
        return items;
    }

    public ObservableList<TimeInterval> getIntervals() {
        return intervals;
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    @Override
    public void render(GraphicsContext gc, int taskLineXStart, int taskLineYStart, IRenderingContext rc) {
        // Render
        int renderedTotalHeight = getHeight(rc);
        renderLineInterval(gc, taskLineYStart, renderedTotalHeight, rc, false);
        doRender(gc, taskLineXStart, taskLineYStart, rc);
        renderLineInterval(gc, taskLineYStart, renderedTotalHeight, rc, true);
        // Remember box
        double groupBoxTotalWidth = rc.toX(rc.getViewPortEnd()) - taskLineXStart;
        this.lastRenderedBounds = new BoundingBox(taskLineXStart, taskLineYStart, groupBoxTotalWidth, renderedTotalHeight);
    }

    @Override
    public void renderLineInterval(GraphicsContext gc, int taskLineYStart, int taskLineHeight, IRenderingContext rc, boolean foreground) {
        for(TimeInterval i : intervals)
        {
            if(i.isForeground() == foreground) i.render(gc, rc, taskLineYStart, taskLineHeight);
        }
    }

    /**
     * Render the group task line. Sub-classes must implement.
     * @param gc the {@link GraphicsContext}
     * @param groupXStart the X start of the group in canvas coordinates
     * @param groupYStart the Y start of the group in canvas coordinates
     * @param rc the {@link IRenderingContext}
     * @return the total height of the rendered task line, in pixels
     */
    protected abstract void doRender(GraphicsContext gc, int groupXStart, int groupYStart, IRenderingContext rc);

    @Override
    public void noRender() {
        this.items.forEach(ITaskLine::noRender);
        this.lastRenderedBounds = null;
    }

    /**
     * Subclasses may override/modify, but consider deriving the corresponding implementation in the direct subclasses
     * of this class.
     * @param gc the {@link GraphicsContext}
     * @param taskLineYStart the Y start of the line in Canvas coordinates
     * @param rc the {@link IRenderingContext}
     */
    protected void drawProjectedTasks(GraphicsContext gc, int taskLineYStart, IRenderingContext rc) {
        // Consider only the tasks in the viewport visibility
        List<TaskItem> tasksToMerge = getTaskItems().stream().filter(task -> rc.isInViewPort(task.getStartTime(), task.getStartTime().plusSeconds(Math.max(task.getExpectedDuration(), task.getActualDuration())))).collect(Collectors.toList());
        drawVisibleProjectedTasks(gc, tasksToMerge, taskLineYStart, rc);
    }

    /**
     * Draw the projections of the specified {@link TaskItem}s on the task line header line.
     * @param gc the {@link GraphicsContext}
     * @param tasksToMerge the {@link TaskItem} to be merged on the task line header line
     * @param taskLineYStart the Y start of the line in Canvas coordinates
     * @param rc the {@link IRenderingContext}
     */
    protected void drawVisibleProjectedTasks(GraphicsContext gc, List<TaskItem> tasksToMerge, int taskLineYStart, IRenderingContext rc) {
        gc.setFill(rc.getTaskProjectionBackgroundColor());
        int startY = taskLineYStart + (int) rc.getTextPadding();
        for(TaskItem ti : tasksToMerge) {
            int startX = Math.max((int) rc.toX(ti.getStartTime()), (int) rc.getTaskPanelWidth());
            int endX = (int) rc.toX(ti.getStartTime().plusSeconds(Math.max(ti.getExpectedDuration(), ti.getActualDuration())));
            gc.fillRect(startX, startY, endX - startX, (int) (rc.getLineRowHeight() - 2 * rc.getTextPadding()));
        }
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    private void listUpdated(ListChangeListener.Change<? extends ILineElement> change) {
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

    /**
     * To be called by child classes with the indication of the bounding box of the collapse button, if any.
     * @param box the {@link BoundingBox} of the collapse button in Canvas coordinates, or null
     */
    protected void setCollapseButtonBoundingBox(BoundingBox box) {
        this.collapseButtonBoundingBox = box;
    }

    /**
     * Return true if the group is collapsed, otherwise false.
     * @return true if the group is collapsed, otherwise false
     */
    public boolean isCollapsedState() {
        return collapsedState;
    }

    @Override
    public boolean computeRenderingStructure() {
        boolean changed = false;
        for(ITaskLine tl : this.items) {
            changed |= tl.computeRenderingStructure();
        }
        boolean oldCollapsedState = collapsedState;
        this.collapsedState = isCollapsible() && isCollapsed();
        return (this.collapsedState != oldCollapsedState) || changed;
    }

    /**
     * Return the bounding box if the line was rendered in the latest rendering cycle, otherwise null
     * @return the bounding box in canvas coordinates if rendered, otherwise null
     */
    protected BoundingBox getLastRenderedBounds() {
        return lastRenderedBounds;
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
        return new Observable[] { nameProperty(), descriptionProperty(), getItems(), collapsibleProperty(),
        collapsedProperty() };
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

    @Override
    public void notifyEvent(Event e, double x, double y) {
        if(this.collapseButtonBoundingBox != null && this.collapseButtonBoundingBox.contains(x, y)) {
            if(e.getEventType() == MouseEvent.MOUSE_CLICKED && isMouseCollapsingEnabled()) {
                // Flip the collapsed property
                setCollapsed(!isCollapsed());
            }
        } else {
            // Default implementation, do nothing and propagate down
            this.items.forEach(i -> i.notifyEvent(e, x, y));
        }
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    @Override
    public int getNbOfLines()
    {
        if(!isCollapsedState())
        {
            int nbLines = 0;
            for (ITaskLine tl : getItems()) nbLines += tl.getNbOfLines();
            return nbLines;
        }
        else return 1;// Collapsed: 1 line
    }

}
