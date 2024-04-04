package eu.dariolucia.jfx.timeline.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;

import java.time.Instant;

/**
 * A task item is a concrete task, with a name, start time, expected duration, actual duration, status.
 */
public class TaskItem {
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleObjectProperty<Instant> startTime = new SimpleObjectProperty<>();
    private final SimpleLongProperty expectedDuration = new SimpleLongProperty();
    private final SimpleLongProperty actualDuration = new SimpleLongProperty();
    private final SimpleObjectProperty<Color> taskBackgroundColor = new SimpleObjectProperty<>(Color.CYAN);
    private final SimpleObjectProperty<Color> taskTextColor = new SimpleObjectProperty<>(Color.BLACK);

    private TaskLine parent;

    public TaskItem(String name, Instant startTime, long expectedDuration) {
        this(name, startTime, expectedDuration, 0);
    }

    TaskLine getParent() {
        return parent;
    }

    void setParent(TaskLine parent) {
        this.parent = parent;
    }

    public TaskItem(String name, Instant startTime, long expectedDuration, long actualDuration) {
        this.name.set(name);
        this.startTime.set(startTime);
        this.expectedDuration.set(expectedDuration);
        this.actualDuration.set(actualDuration);
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

    public void render(GraphicsContext gc, double taskLineYStart, RenderingContext rc) {
        // TODO: compute as part of update and not as part of the rendering?
        Instant endTimeExp = getStartTime().plusSeconds(getExpectedDuration());
        Instant endTimeAct = getActualDuration() >= 0 ? getStartTime().plusSeconds(getActualDuration()) : null;
        Instant endTime = endTimeAct != null && endTimeAct.isAfter(endTimeExp) ? endTimeAct : endTimeExp;
        // Render only if in viewport
        if(isInViewPort(getStartTime(), endTime, rc.getViewPortStart(), rc.getViewPortEnd())) {
            // Convert to X coordinates
            double startX = rc.toX(getStartTime());
            double startY = taskLineYStart + rc.getTextPadding();
            // Expected
            double endX = rc.toX(endTimeExp);
            // Render now expected
            boolean isSelected = rc.getSelectedTaskItem() == this;
            Color toRender = getTaskBackgroundColor();
            gc.setFill(toRender);
            gc.setStroke(toRender.darker());
            if(isSelected) {
                gc.setEffect(new DropShadow());
            }
            gc.fillRect(startX, startY, endX - startX, rc.getLineRowHeight() - 2*rc.getTextPadding());
            gc.setEffect(null);
            // Selected tasks have larger stroke
            gc.strokeRect(startX, startY, endX - startX, rc.getLineRowHeight() - 2*rc.getTextPadding());
            // Restore stroke width
            gc.setEffect(null);
            // Render now actual
            if(endTimeAct != null) {
                double actualEndX = rc.toX(endTimeAct);
                gc.setFill(toRender.darker());
                gc.fillRect(startX, startY + rc.getTextPadding(), actualEndX - startX, rc.getLineRowHeight() - 4*rc.getTextPadding());
            }
            gc.setStroke(getTaskTextColor());
            // Render in the middle
            double textWidth = rc.getTextWidth(gc, getName());
            gc.strokeText(getName(), startX + (endX - startX)/2 - textWidth/2, startY - rc.getTextPadding() + rc.getLineRowHeight()/2 + rc.getTextHeight()/2);
        }
    }

    private boolean isInViewPort(Instant start, Instant end, Instant viewPortStart, Instant viewPortEnd) {
        return (start.isAfter(viewPortStart) && start.isBefore(viewPortEnd)) || (end.isAfter(viewPortStart) && end.isBefore(viewPortEnd)) ||
                (start.isBefore(viewPortStart) && end.isAfter(viewPortEnd));
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
