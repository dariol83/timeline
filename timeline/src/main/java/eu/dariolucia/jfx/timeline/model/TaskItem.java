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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.time.Instant;

/**
 * A task item is a concrete task, with a name, description, start time, expected duration, actual duration and color.
 * This class can be subclassed and the render() method can be overwritten. It is nevertheless important, that the
 * last rendered bounding box is saved/reset using the related methods.
 */
public class TaskItem extends LineElement {

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/

    /**
     * Start time of the task item.
     */
    private final SimpleObjectProperty<Instant> startTime = new SimpleObjectProperty<>();
    /**
     * Expected duration of the task item. It is used to compute the length of the task item rendered box.
     */
    private final SimpleLongProperty expectedDuration = new SimpleLongProperty();
    /**
     * Actual duration of the task item. If set and positive (>0) an internal line will be drawn inside (and also outside,
     * if it exceeds the expected duration) of the task item rendered box.
     */
    private final SimpleLongProperty actualDuration = new SimpleLongProperty();
    /**
     * Background of the task item (color, gradient, pattern).
     */
    private final SimpleObjectProperty<Paint> taskBackground = new SimpleObjectProperty<>(Color.PAPAYAWHIP);
    /**
     * Background of the task actual duration line (color, gradient, pattern).
     */
    private final SimpleObjectProperty<Paint> taskProgressBackground = new SimpleObjectProperty<>(Color.PAPAYAWHIP.darker());
    /**
     * Text color of the task item name.
     */
    private final SimpleObjectProperty<Color> taskTextColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * If true, all the intervals in this task item will be trimmed to the size of the task item.
     */
    private final SimpleBooleanProperty trimIntervals = new SimpleBooleanProperty(true);
    /**
     * List of {@link TimePoint} on a task item
     */
    private final ObservableList<TimePoint> timePoints = FXCollections.observableArrayList(TimePoint::getObservableProperties);
    /**
     * List of {@link TimeInterval} on a task item
     */
    private final ObservableList<TimeInterval> intervals = FXCollections.observableArrayList(TimeInterval::getObservableProperties);
    /**
     * Tooltip for task item
     */
    private final SimpleObjectProperty<TimeTooltip> tooltip = new SimpleObjectProperty<>(null);

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/
    private Object userData;

    /**
     * Constructor of a task item.
     * @param name the name of the task item
     * @param startTime the start time of the task item
     * @param expectedDuration the expected duration of the task item
     */
    public TaskItem(String name, Instant startTime, long expectedDuration) {
        this(name, startTime, expectedDuration, 0);
    }

    /**
     Constructor of a task item.
     * @param name the name of the task item
     * @param startTime the start time of the task item
     * @param expectedDuration the expected duration of the task item
     * @param actualDuration the actual duration of the task item
     */
    public TaskItem(String name, Instant startTime, long expectedDuration, long actualDuration) {
        super(name, null);
        this.startTime.set(startTime);
        this.expectedDuration.set(expectedDuration);
        this.actualDuration.set(actualDuration);

        this.timePoints.addListener(this::listUpdated);
        this.intervals.addListener(this::listUpdated);
    }

    /* *****************************************************************************************
     * Property Accessors
     * *****************************************************************************************/

    public Instant getStartTime() {
        return startTime.get();
    }

    public SimpleObjectProperty<Instant> startTimeProperty() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime.set(startTime);
    }

    public long getExpectedDuration() {
        return expectedDuration.get();
    }

    public SimpleLongProperty expectedDurationProperty() {
        return expectedDuration;
    }

    public void setExpectedDuration(long expectedDuration) {
        this.expectedDuration.set(expectedDuration);
    }

    public long getActualDuration() {
        return actualDuration.get();
    }

    public SimpleLongProperty actualDurationProperty() {
        return actualDuration;
    }

    public void setActualDuration(long actualDuration) {
        this.actualDuration.set(actualDuration);
    }

    public Paint getTaskBackground() {
        return taskBackground.get();
    }

    public SimpleObjectProperty<Paint> taskBackgroundProperty() {
        return taskBackground;
    }

    public void setTaskBackground(Paint taskBackground) {
        this.taskBackground.set(taskBackground);
    }

    public Color getTaskTextColor() {
        return taskTextColor.get();
    }

    public SimpleObjectProperty<Color> taskTextColorProperty() {
        return taskTextColor;
    }

    public void setTaskTextColor(Color taskTextColor) {
        this.taskTextColor.set(taskTextColor);
    }

    public Paint getTaskProgressBackground() {
        return taskProgressBackground.get();
    }

    public SimpleObjectProperty<Paint> taskProgressBackgroundProperty() {
        return taskProgressBackground;
    }

    public void setTaskProgressBackground(Paint taskProgressBackground) {
        this.taskProgressBackground.set(taskProgressBackground);
    }

    public void setTrimIntervals(boolean trimIntervals)
    {
        this.trimIntervals.set(trimIntervals);
    }

    public boolean isTrimIntervals() {
        return trimIntervals.get();
    }

    public SimpleBooleanProperty trimIntervalsProperty() {
        return trimIntervals;
    }

    public ObservableList<TimePoint> getTimePoints() {
        return timePoints;
    }

    public ObservableList<TimeInterval> getIntervals() {
        return intervals;
    }

    public TimeTooltip getTooltip() {
        return tooltip.get();
    }

    public void setTooltip(TimeTooltip tooltip) {
        this.tooltip.set(tooltip);
    }

    public SimpleObjectProperty<TimeTooltip> tooltipProperty() {
        return tooltip;
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    /**
     * Task rendering method. Subclasses could override this method, but it is recommended to override the specific
     * draw methods, which handle the rendering of the specific parts of the task item in isolation.
     * @param gc the {@link GraphicsContext}
     * @param taskLineYStart the start Y of the parent {@link TaskLine}
     * @param rc the {@link IRenderingContext}
     */
    public void render(GraphicsContext gc, int taskLineYStart, IRenderingContext rc) {
        Instant endTimeExp = getStartTime().plusSeconds(getExpectedDuration());
        Instant endTimeAct = getActualDuration() >= 0 ? getStartTime().plusSeconds(getActualDuration()) : null;
        Instant endTime = endTimeAct != null && endTimeAct.isAfter(endTimeExp) ? endTimeAct : endTimeExp;

        int startY = taskLineYStart + (int) rc.getTextPadding();
        int taskHeight = (int) Math.round(rc.getLineRowHeight() - 2 * rc.getTextPadding());

        // Render only if in viewport
        if(rc.isInViewPort(getStartTime(), endTime)) {
            // Convert to X coordinates
            int startX = (int) rc.toX(getStartTime());
            // Expected end coordinate
            int endX = (int) rc.toX(endTimeExp);
            // Is selected
            boolean isSelected = rc.getSelectedTaskItems().contains(this);
            // Render now expected
            drawTaskItemBox(gc, startX, startY, endX - startX, taskHeight, isSelected, rc);
            // Render now actual
            int actualEndX = -1;
            if(endTimeAct != null) {
                actualEndX = (int) rc.toX(endTimeAct);
                // Draw task progress
                drawTaskItemProgress(gc, startX, startY, actualEndX - startX, taskHeight, isSelected, rc);
            }
            // Render text
            drawTaskItemName(gc, startX, startY, endX - startX, taskHeight, isSelected, rc);
            //Render time points
            drawTaskItemTimePoints(gc, rc, startX, startY, endX - startX, taskHeight);
            // Remember rendering box in pixel coordinates
            updateLastRenderedBounds(new BoundingBox(startX, startY, Math.max(endX, actualEndX) - startX, taskHeight));
        }
    }

    @Override
    public void noRender() {
        getTimePoints().forEach(LineElement::noRender);
        getIntervals().forEach(LineElement::noRender);
        super.noRender();
    }

    /**
     * Draw time points on a task item. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param rc the {@link IRenderingContext}
     * @param taskItemStartX the start X of the task item in Canvas coordinates
     * @param taskItemStartY the start Y of the task item in Canvas coordinates
     * @param taskItemWidth the width of the task item
     * @param taskItemHeight the height of the task item
     */
    protected void drawTaskItemTimePoints(GraphicsContext gc, IRenderingContext rc, int taskItemStartX, int taskItemStartY, int taskItemWidth, int taskItemHeight)
    {
        for(TimePoint p : timePoints) p.render(gc, rc, taskItemStartX, taskItemStartY, taskItemWidth, taskItemHeight);
    }
    /**
     * Draw the time interval on a task item. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param taskLineStartY the Y offset where the task line begins in which the task item is located
     * @param rc the {@link IRenderingContext}
     * @param foreground draw interval on foreground
     */
    public void drawTaskItemInterval(GraphicsContext gc, int taskLineStartY, IRenderingContext rc, boolean foreground)
    {
        int YStart = taskLineStartY + (int)rc.getTextPadding();
        int Height = (int) Math.round(rc.getLineRowHeight() - 2 * rc.getTextPadding());

        for(TimeInterval i : intervals)
        {
            if(i.isForeground() == foreground) i.render(gc, rc, YStart, Height);
        }
    }

    /**
     * Draw the name of the task item. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param taskItemStartX the start X of the task item in Canvas coordinates
     * @param taskItemStartY the start Y of the task item in Canvas coordinates
     * @param taskItemWidth the width of the task item
     * @param taskItemHeight the height of the task item
     * @param isSelected whether the task item is selected
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskItemName(GraphicsContext gc, int taskItemStartX, int taskItemStartY, int taskItemWidth, int taskItemHeight, boolean isSelected, IRenderingContext rc) {
        // Set the text color
        gc.setFill(getTaskTextColor());
        Font original = gc.getFont();
        // If selected, use bold text
        if(isSelected) {
            gc.setFont(Font.font(original.getFamily(), original.getSize() + 2));
        }
        // Render text in the middle
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(getName(), (int) Math.round(taskItemStartX + (taskItemWidth)/2.0), (int) Math.round(taskItemStartY + taskItemHeight/2.0 + rc.getTextHeight()/2.0), taskItemWidth);
        gc.setTextAlign(TextAlignment.LEFT);
        // If selected, restore font
        if(isSelected) {
            gc.setFont(original);
        }
    }

    /**
     * Draw the task progress. Subclasses can override. The boundaries defined
     * by the arguments are those falling within the task item, and the width is proportional to the length of the progress.
     * @param gc the {@link GraphicsContext}
     * @param taskItemStartX the start X of the task item in Canvas coordinates
     * @param taskItemStartY the start Y of the task item in Canvas coordinates
     * @param actualProgressWidth the width of the actual progress, it can go beyond the width of the task item
     * @param taskItemHeight the height of the task item
     * @param isSelected whether the task item is selected
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskItemProgress(GraphicsContext gc, int taskItemStartX, int taskItemStartY, int actualProgressWidth, int taskItemHeight, boolean isSelected, IRenderingContext rc) {
        int actualStartX = taskItemStartX + (isSelected ? 1 : 0); // Account for selection
        int actualStartY = (int) Math.round(taskItemStartY + rc.getTextPadding());
        // Take the selection into account
        actualProgressWidth -= isSelected ? 1 : 0;
        taskItemHeight = (int) Math.round(taskItemHeight - 2 * rc.getTextPadding());
        gc.setFill(getTaskProgressBackground());
        gc.fillRect(actualStartX, actualStartY, actualProgressWidth, taskItemHeight);
    }

    /**
     * Draw the task item. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param taskItemStartX the start X of the task item in Canvas coordinates
     * @param taskItemStartY the start Y of the task item in Canvas coordinates
     * @param taskItemWidth the width of the task item
     * @param taskItemHeight the height of the task item
     * @param isSelected whether the task item is selected
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskItemBox(GraphicsContext gc, int taskItemStartX, int taskItemStartY, int taskItemWidth, int taskItemHeight, boolean isSelected, IRenderingContext rc) {
        Paint bgColor = getTaskBackground();
        Color borderColor = isSelected ? rc.getSelectBorderColor() : rc.getTaskBorderColor();
        gc.setFill(bgColor);
        gc.setStroke(borderColor);
        if(isSelected) {
            gc.setLineWidth(rc.getSelectBorderWidth());
            gc.setEffect(rc.getSelectBorderEffect());
        }
        // Fill task bar
        gc.fillRoundRect(taskItemStartX, taskItemStartY, taskItemWidth, taskItemHeight, rc.getTextPadding(), rc.getTextPadding());
        // Draw task border
        gc.strokeRoundRect(taskItemStartX, taskItemStartY, taskItemWidth, taskItemHeight, rc.getTextPadding(), rc.getTextPadding());
        // Restore effect and line
        if(isSelected) {
            gc.setLineWidth(1);
            gc.setEffect(null);
        }
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    /**
     * Return the user data object attached to this task item. Task item can contain a user data object, i.e. an opaque
     * object that users of this class can attach to a task item.
     * @return the user data
     */
    public Object getUserData() {
        return userData;
    }

    /**
     * Set the user data object attached to this task item. Task item can contain a user data object, i.e. an opaque
     * object that users of this class can attach to a task item.
     * @param userData the user data object
     */
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    /**
     * Return the properties that should trigger an update notification in case of
     * change. Subclasses should override, if properties are added.
     * @return the list of properties as array of {@link Observable}
     */
    public Observable[] getObservableProperties() {
        return new Observable[] {
                startTimeProperty(), nameProperty(), expectedDurationProperty(), actualDurationProperty(), getTimePoints(), getIntervals(),
                taskBackgroundProperty(), taskTextColorProperty(), taskProgressBackgroundProperty(), trimIntervalsProperty(), tooltipProperty() };
    }

    /**
     * Return true if the task item includes the specified time, otherwise false.
     * @param time the time to check
     * @return true if the task item includes the specified time, otherwise false
     */
    public final boolean contains(Instant time) {
        long timeSeconds = time.getEpochSecond();
        long startTimeSecond = getStartTime().getEpochSecond();
        long endTimeSecond = getStartTime().plusSeconds(Math.max(getActualDuration(), getExpectedDuration())).getEpochSecond();
        return timeSeconds >= startTimeSecond && timeSeconds <= endTimeSecond;
    }

    /**
     * Check if the provided {@link TaskItem} overlaps with this task item.
     * @param item the item to check
     * @return true if there is an overlap, otherwise false
     */
    public boolean overlapWith(TaskItem item) {
        long thisStartTime = getStartTime().getEpochSecond();
        long thisEndTime = Math.max(getStartTime().getEpochSecond() + getActualDuration(), getStartTime().getEpochSecond() + getExpectedDuration());
        long itemStartTime = item.getStartTime().getEpochSecond();
        long itemEndTime = Math.max(item.getStartTime().getEpochSecond() + item.getActualDuration(), item.getStartTime().getEpochSecond() + item.getExpectedDuration());
        return (thisStartTime >= itemStartTime && thisStartTime < itemEndTime) ||
                (thisEndTime > itemStartTime && thisEndTime <= itemEndTime) ||
                (thisStartTime <= itemStartTime && thisEndTime >= itemEndTime) ||
                (itemStartTime <= thisStartTime && itemEndTime >= thisEndTime);
    }

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

    @Override
    public String toString() {
        return "TaskItem{" +
                "name=" + getName() +
                ", startTime=" + getStartTime() +
                ", expectedDuration=" + getExpectedDuration() +
                ", actualDuration=" + getActualDuration() +
                '}';
    }
}
