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
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.time.Instant;

/**
 * A task item is a concrete task, with a name, start time, expected duration, actual duration and color.
 * This class can be subclassed and the render() method can be overwritten. It is nevertheless important, that the
 * last rendered bounding box is saved/reset using the related methods.
 */
public class TaskItem extends LineElement {

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/
    private final SimpleObjectProperty<Instant> startTime = new SimpleObjectProperty<>();
    private final SimpleLongProperty expectedDuration = new SimpleLongProperty();
    private final SimpleLongProperty actualDuration = new SimpleLongProperty();
    private final SimpleObjectProperty<Paint> taskBackground = new SimpleObjectProperty<>(Color.PAPAYAWHIP);
    private final SimpleObjectProperty<Paint> taskProgressBackground = new SimpleObjectProperty<>(Color.PAPAYAWHIP.darker());
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

    /**
     * Return the properties that should trigger an update notification in case of
     * change. Subclasses should override, if properties are added.
     * @return the list of properties as array of {@link Observable}
     */
    public Observable[] getObservableProperties() {
        return new Observable[] {
                startTimeProperty(), nameProperty(), expectedDurationProperty(), actualDurationProperty(),
                taskBackgroundProperty(), taskTextColorProperty(), taskProgressBackgroundProperty() };
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
            // Expected end coordinate
            int endX = (int) rc.toX(endTimeExp);
            // Is selected
            boolean isSelected = rc.getSelectedTaskItems().contains(this);
            // Render now expected
            int taskHeight = (int) Math.round(rc.getLineRowHeight() - 2 * rc.getTextPadding());
            drawTaskItem(gc, startX, startY, endX - startX, taskHeight, isSelected, rc);
            // Render now actual
            int actualEndX = -1;
            if(endTimeAct != null) {
                actualEndX = (int) rc.toX(endTimeAct);
                // Draw task progress
                drawTaskItemProgress(gc, startX, startY, actualEndX - startX, taskHeight, isSelected, rc);
            }
            // Render text
            drawTaskText(gc, startX, startY, endX - startX, taskHeight, isSelected, rc);
            // Remember rendering box in pixel coordinates
            updateLastRenderedBounds(new BoundingBox(startX, startY, Math.max(endX, actualEndX) - startX, taskHeight));
        }
    }

    protected void drawTaskText(GraphicsContext gc, int startX, int startY, int width, int height, boolean isSelected, IRenderingContext rc) {
        gc.setStroke(getTaskTextColor());
        // Render text in the middle
        int textWidth = rc.getTextWidth(gc, getName());
        gc.strokeText(getName(), (int) Math.round(startX + (width)/2.0 - textWidth/2.0), (int) Math.round(startY - rc.getTextPadding() + rc.getLineRowHeight()/2.0 + rc.getTextHeight()/2.0));
    }

    /**
     * Draw the task progress. Subclasses can override, as long as the bounding box is preserved. The boundaries defined
     * by the arguments are those falling within the task item, and the width is proportional to the length of the progress.
     * @param gc the {@link GraphicsContext}
     * @param startX the start X of the progress in Canvas coordinates
     * @param startY the start Y of the progress in Canvas coordinates
     * @param width the width - actual progress
     * @param height the height
     * @param isSelected whether the task item is selected
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskItemProgress(GraphicsContext gc, int startX, int startY, int width, int height, boolean isSelected, IRenderingContext rc) {
        int actualStartX = startX + (isSelected ? 1 : 0); // Account for selection
        int actualStartY = (int) Math.round(startY + rc.getTextPadding());
        // Take the selection into account
        width -= isSelected ? 1 : 0;
        height = (int) Math.round(height - 2 * rc.getTextPadding());
        gc.setFill(getTaskProgressBackground());
        gc.fillRect(actualStartX, actualStartY, width, height);
    }

    /**
     * Draw the task item. Subclasses can override, as long as the bounding box is preserved.
     * @param gc the {@link GraphicsContext}
     * @param startX the start X of the progress in Canvas coordinates
     * @param startY the start Y of the progress in Canvas coordinates
     * @param width the width
     * @param height the height
     * @param isSelected whether the task item is selected
     * @param rc the {@link IRenderingContext}
     */
    protected void drawTaskItem(GraphicsContext gc, int startX, int startY, int width, int height, boolean isSelected, IRenderingContext rc) {
        Paint bgColor = getTaskBackground();
        Color borderColor = isSelected ? rc.getSelectBorderColor() : rc.getTaskBorderColor();
        gc.setFill(bgColor);
        gc.setStroke(borderColor);
        if(isSelected) {
            gc.setLineWidth(rc.getSelectBorderWidth());
            gc.setEffect(rc.getSelectBorderEffect());
        }
        // Fill task bar
        gc.fillRoundRect(startX, startY, width, height, rc.getTextPadding(), rc.getTextPadding());
        // Draw task border
        gc.strokeRoundRect(startX, startY, width, height, rc.getTextPadding(), rc.getTextPadding());
        // Restore effect and line
        if(isSelected) {
            gc.setLineWidth(1);
            gc.setEffect(null);
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
