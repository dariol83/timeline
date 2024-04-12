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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.time.Instant;

/**
 * A class used to place time intervals (also open-ended) on the timeline.
 * This class can be subclassed and the render() method can be overwritten.
 */
public class TimeInterval {

    private Timeline timeline;
    private final SimpleObjectProperty<Instant> startTime = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Instant> endTime = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty foreground = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(new Color(Color.LIMEGREEN.getRed(), Color.LIMEGREEN.getGreen(), Color.LIMEGREEN.getBlue(), 0.5));

    public TimeInterval(Instant startTime, Instant endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
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

    public Instant getEndTime() {
        return endTime.get();
    }

    public SimpleObjectProperty<Instant> endTimeProperty() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime.set(endTime);
    }

    public boolean isForeground() {
        return foreground.get();
    }

    public SimpleBooleanProperty foregroundProperty() {
        return foreground;
    }

    public void setForeground(boolean foreground) {
        this.foreground.set(foreground);
    }

    public Color getColor() {
        return color.get();
    }

    public SimpleObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    @Override
    public String toString() {
        return "TimeInterval{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", foreground=" + foreground +
                ", color=" + color +
                '}';
    }

    public void render(GraphicsContext gc, IRenderingContext rc) {
        double startX = getStartTime() == null || getStartTime().isBefore(rc.getViewPortStart()) ? rc.getTaskPanelWidth() : rc.toX(getStartTime());
        double endX = getEndTime() == null || getEndTime().isAfter(rc.getViewPortEnd()) ? rc.getImageAreaWidth() : rc.toX(getEndTime());
        gc.setFill(getColor());
        gc.fillRect(startX, rc.getHeaderRowHeight(), endX - startX, rc.getImageAreaHeight());
    }
}
