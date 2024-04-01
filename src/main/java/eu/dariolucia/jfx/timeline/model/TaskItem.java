package eu.dariolucia.jfx.timeline.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.time.Instant;

/**
 * A task item is a concrete task, with a name, start time, expected duration, actual duration, status.
 */
public class TaskItem {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleObjectProperty<Instant> startTime = new SimpleObjectProperty<>();
    private final SimpleLongProperty expectedDuration = new SimpleLongProperty();
    private final SimpleLongProperty actualDuration = new SimpleLongProperty();

    public TaskItem(String name, Instant startTime, long expectedDuration) {
        this(name, startTime, expectedDuration, 0);
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

    public void render(GraphicsContext gc, double taskLineYStart, RenderingContext rc) {
        Instant endTime = getStartTime().plusSeconds(getExpectedDuration());
        // Render only if in viewport
        if(isInViewPort(getStartTime(), endTime, rc.getViewPortStart(), rc.getViewPortEnd())) {
            // Convert to X coordinates
            double startX = rc.toX(getStartTime());
            double endX = rc.toX(endTime);
            double startY = taskLineYStart + rc.getTextPadding();
            // Render now
            gc.setFill(Color.LIGHTCYAN);
            gc.setStroke(Color.DARKCYAN);
            gc.fillRect(startX, startY, endX - startX, rc.getLineRowHeight() - 2*rc.getTextPadding());
            gc.strokeRect(startX, startY, endX - startX, rc.getLineRowHeight() - 2*rc.getTextPadding());
            gc.setStroke(Color.BLACK);
            // Render in the middle TODO: find a way to cache the string length given the name and the font!
            double textWidth = rc.getTextWidth(gc, getName());
            gc.strokeText(getName(), startX + (endX - startX)/2 - textWidth/2, startY + rc.getLineRowHeight() - 2*rc.getTextPadding() - rc.getTextHeight()/2);
        }
    }

    private boolean isInViewPort(Instant start, Instant end, Instant viewPortStart, Instant viewPortEnd) {
        return (start.isAfter(viewPortStart) && start.isBefore(viewPortEnd)) || (end.isAfter(viewPortStart) && end.isBefore(viewPortEnd)) ||
                (start.isBefore(viewPortStart) && end.isAfter(viewPortEnd));
    }
}
