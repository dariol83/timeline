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
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.time.Instant;

/**
 * A class used to place cursors on the timeline.
 */
public class TimeCursor {

    private Timeline timeline;
    private final SimpleObjectProperty<Instant> time = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(Color.BLACK);

    public TimeCursor(Instant time) {
        setTime(time);
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    public Instant getTime() {
        return time.get();
    }

    public SimpleObjectProperty<Instant> timeProperty() {
        return time;
    }

    public void setTime(Instant time) {
        this.time.set(time);
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
        return "TimeCursor{" +
                "time=" + getTime() +
                ", color=" + getColor() +
                '}';
    }
}
