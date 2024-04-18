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

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.time.Instant;

/**
 * A task item is a concrete task, with a name, start time, expected duration, actual duration and color.
 * This class can be subclassed and the render() method can be overwritten. It is nevertheless important, that the
 * last rendered bounding box is saved/reset using the related methods.
 */
public class TaskItem extends TimelineElement {

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/
    private final SimpleObjectProperty<Instant> startTime = new SimpleObjectProperty<>();
    private final SimpleLongProperty expectedDuration = new SimpleLongProperty();
    private final SimpleLongProperty actualDuration = new SimpleLongProperty();
    private final SimpleObjectProperty<Color> taskBackgroundColor = new SimpleObjectProperty<>(Color.PAPAYAWHIP);
    private final SimpleObjectProperty<Color> taskTextColor = new SimpleObjectProperty<>(Color.BLACK);

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/
    private BoundingBox lastRenderedBounds;
    private Object userData;

    public TaskItem(String name, Instant startTime, long expectedDuration) {
        this(name, startTime, expectedDuration, 0);
    }

    public TaskItem(String name, Instant startTime, long expectedDuration, long actualDuration) {
        super(name, null);
        this.startTime.set(startTime);
        this.expectedDuration.set(expectedDuration);
        this.actualDuration.set(actualDuration);
    }

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

    public Color getTaskBackgroundColor() {
        return taskBackgroundColor.get();
    }

    public SimpleObjectProperty<Color> taskBackgroundColorProperty() {
        return taskBackgroundColor;
    }

    public void setTaskBackgroundColor(Color taskBackgroundColor) {
        this.taskBackgroundColor.set(taskBackgroundColor);
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

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    public void render(GraphicsContext gc, int taskLineYStart, IRenderingContext rc) {
        Instant endTimeExp = getStartTime().plusSeconds(getExpectedDuration());
        Instant endTimeAct = getActualDuration() >= 0 ? getStartTime().plusSeconds(getActualDuration()) : null;
        Instant endTime = endTimeAct != null && endTimeAct.isAfter(endTimeExp) ? endTimeAct : endTimeExp;
        // Render only if in viewport
        if(rc.isInViewPort(getStartTime(), endTime)) {
            // Convert to X coordinates
            int startX = (int) rc.toX(getStartTime());
            int startY = taskLineYStart + (int) rc.getTextPadding();
            // Expected
            int endX = (int) rc.toX(endTimeExp);
            // Render now expected
            boolean isSelected = rc.getSelectedTaskItems().contains(this);
            Color bgColor = getTaskBackgroundColor();
            Color borderColor = isSelected ? rc.getSelectBorderColor() : bgColor.darker();
            gc.setFill(bgColor);
            gc.setStroke(borderColor);
            if(isSelected) {
                gc.setLineWidth(rc.getSelectBorderWidth());
                gc.setEffect(rc.getSelectBorderEffect());
            }
            double taskHeight = rc.getLineRowHeight() - 2 * rc.getTextPadding();
            gc.fillRect(startX, startY, endX - startX, taskHeight);
            // Draw the selection
            gc.strokeRect(startX, startY, endX - startX, taskHeight);
            // Restore effect and line
            gc.setLineWidth(1);
            gc.setEffect(null);
            // Render now actual
            int actualEndX = -1;
            if(endTimeAct != null) {
                actualEndX = (int) rc.toX(endTimeAct);
                int actualStartX = startX + (isSelected ? 1 : 0); // Account for selection
                gc.setFill(bgColor.darker());
                gc.fillRect(actualStartX, startY + rc.getTextPadding(), actualEndX - actualStartX, rc.getLineRowHeight() - 4*rc.getTextPadding());
            }
            gc.setStroke(getTaskTextColor());
            // Render in the middle
            int textWidth = rc.getTextWidth(gc, getName());
            gc.strokeText(getName(), (int) Math.round(startX + (endX - startX)/2.0 - textWidth/2.0), (int) Math.round(startY - rc.getTextPadding() + rc.getLineRowHeight()/2.0 + rc.getTextHeight()/2.0));
            // Remember rendering box in pixel coordinates
            updateLastRenderedBounds(new BoundingBox(startX, startY, Math.max(endX, actualEndX) - startX, taskHeight));
        }
    }

    protected void updateLastRenderedBounds(BoundingBox boundingBox) {
        this.lastRenderedBounds = boundingBox;
    }

    protected BoundingBox getLastRenderedBounds() {
        return lastRenderedBounds;
    }

    public boolean contains(double x, double y) {
        return this.lastRenderedBounds != null && this.lastRenderedBounds.contains(x, y);
    }

    public boolean isRendered() {
        return this.lastRenderedBounds != null;
    }

    public boolean contains(Instant time) {
        long timeSeconds = time.getEpochSecond();
        long startTimeSecond = getStartTime().getEpochSecond();
        long endTimeSecond = getStartTime().plusSeconds(Math.max(getActualDuration(), getExpectedDuration())).getEpochSecond();
        return timeSeconds >= startTimeSecond && timeSeconds <= endTimeSecond;
    }

    public void noRender() {
        updateLastRenderedBounds(null);
    }

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
