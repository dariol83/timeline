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

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;

import java.time.Instant;

/**
 * A class used to place time intervals (also open-ended) on the timeline.
 * This class can be subclassed and the render() method can be overwritten.
 */
public class TimeInterval extends Interval {

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/

    /**
     * Class constructor.
     * @param startTime the start time, can be null
     * @param endTime the end time, can be null
     */
    public TimeInterval(Instant startTime, Instant endTime) {
        super(startTime, endTime);
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    /**
     * Render the time interval. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param rc the {@link IRenderingContext}
     */
    @Override
    public void render(GraphicsContext gc, IRenderingContext rc) {
        render(gc, rc, rc.getHeaderRowHeight(), rc.getImageAreaHeight());
    }

    public void render(GraphicsContext gc, IRenderingContext rc, int startY, int Height) {
        // Render only if in viewport
        if(rc.isInViewPort(getStartTime(), getEndTime()))
        {
            double startX;
            double endX;

            //We limit the interval to the size of the Task Item if the trim property is true.
            if(getParent() != null && getParent() instanceof TaskItem && ((TaskItem) getParent()).isTrimIntervals())
            {
                TaskItem item = (TaskItem) getParent();
                Instant EndTime = item.getStartTime().plusSeconds(item.getExpectedDuration());

                startX = getStartTime().isBefore(item.getStartTime()) ? rc.toX(item.getStartTime()) : rc.toX(getStartTime());
                endX = getEndTime().isAfter(EndTime) ? rc.toX(EndTime) : rc.toX(getEndTime());
            }
            //Otherwise, we limit the interval within the viewport.
            else
            {
                startX = getStartTime().isBefore(rc.getViewPortStart()) ? rc.getViewPortStartX() : rc.toX(getStartTime());
                endX = getEndTime().isAfter(rc.getViewPortEnd()) ? rc.getViewPortEndX() : rc.toX(getEndTime());
            }

            gc.setFill(getColor());
            gc.fillRect(startX, startY, endX - startX, Height);

            // Remember rendering box in pixel coordinates
            updateLastRenderedBounds(new BoundingBox(startX, startY, endX - startX, Height));
        }
        else noRender();
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    @Override
    public String toString() {
        return "TimeInterval{" +
                "startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", foreground=" + isForeground() +
                ", color=" + getColor() +
                '}';
    }
}
