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

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.time.Instant;
import java.util.Set;

/**
 * This interface exposes a set of information to the rendering functions of the different model objects.
 */
public interface IRenderingContext {

    /**
     * Return the effect to be applied to selected {@link TaskItem} when rendered.
     * @return the effect for selected {@link TaskItem}
     */
    Effect getSelectBorderEffect();

    /**
     * Return the width to be used for the border of selected {@link TaskItem}.
     * @return the border width for selected {@link TaskItem}
     */
    double getSelectBorderWidth();

    /**
     * Return the color to be used for the border of selected {@link TaskItem}.
     * @return the border color for selected {@link TaskItem}
     */
    Color getSelectBorderColor();

    /**
     * Return the font to be used to render text strings.
     * @return the font
     */
    Font getTextFont();

    /**
     * Return true if group lines must be highlighted (when rendered)
     * @return true if group lines must be highlighted, otherwise false
     */
    boolean isHighlightLine();

    /**
     * Return the hint for the rendering of task item projections on group lines.
     * @return the projection hint for group lines
     */
    TaskItemProjection getTaskProjectionHint();

    /**
     * Return the color for the rendering of task item projections on group lines.
     * @return the color for task item projections
     */
    Color getTaskProjectionBackgroundColor();

    /**
     * Return the color for the {@link TaskItem} when not selected
     * @return the {@link TaskItem} border color
     */
    Color getTaskBorderColor();

    /**
     * Calculate the rendered width of the provided text string with the provided {@link GraphicsContext}
     * @param gc the {@link GraphicsContext}
     * @param text the string to render
     * @return the size in pixel of the rendered text
     */
    int getTextWidth(GraphicsContext gc, String text);

    /**
     * Return the size in pixel of the task panel (left).
     * @return the size in pixel of the task panel
     */
    double getTaskPanelWidth();

    /**
     * Return the size in pixel of the additional panel (right).
     * @return the size in pixel of the additional panel
     */
    double getAdditionalPanelWidth();

    /**
     * Return the height in pixel of a text line with the configured font (hardcoded string "Ig" used for the measure)
     * @return the height in pixel of a text line
     */
    int getTextHeight();

    /**
     * Return the height in pixel of a task line.
     * @return the height in pixel of a task line
     */
    int getLineRowHeight();

    /**
     * Convert the provided time into an X value in Canvas coordinate system. The value can be offscreen.
     * @param time the time to convert
     * @return the X coordinate in Canvas coordinate system
     */
    double toX(Instant time);

    /**
     * Return the amount of padding in pixel between rendered text and the container element.
     * @return the text padding in pixel
     */
    double getTextPadding();

    /**
     * Return the start time configured for the viewport.
     * @return the viewport start time
     */
    Instant getViewPortStart();

    /**
     * Return the end time configured for the viewport (start plus duration).
     * @return the viewport end time
     */
    Instant getViewPortEnd();

    /**
     * Return the set of selected {@link TaskItem} (only 1 in the current implementation)
     * @return the set of selected {@link TaskItem}
     */
    Set<TaskItem> getSelectedTaskItems();

    /**
     * Return the height of the canvas in pixel.
     * @return the height of the canvas in pixel
     */
    int getImageAreaHeight();

    /**
     * Return the width of the canvas in pixel.
     * @return the width of the canvas in pixel
     */
    int getImageAreaWidth();

    /**
     * Return the width of the viewport in pixel.
     * @return the width of the viewport in pixel.
     */
    default double getViewPortWidth()
    {
        return Math.abs(getViewPortEndX() - getViewPortStartX());
    }

    /**
     * Return the start X coordinate of the viewport.
     * @return the start X coordinate of the viewport.
     */
    double getViewPortStartX();

    /**
     * Return the end X coordinate of the viewport.
     * @return the end X coordinate of the viewport.
     */
    double getViewPortEndX();

    /**
     * Return the height of the header row in pixel.
     * @return the height of the header row in pixel
     */
    int getHeaderRowHeight();

    /**
     * Return the background color of the canvas.
     * @return the background color of the canvas
     */
    Color getBackgroundColor();

    /**
     * Return the background color of the task panel.
     * @return the background color of the task panel
     */
    Paint getPanelBackground();

    /**
     * Return the foreground color of the task panel.
     * @return the foreground color of the task panel
     */
    Color getPanelForegroundColor();

    /**
     * Return the border color of the task panel.
     * @return the border color of the task panel
     */
    Color getPanelBorderColor();

    /**
     * Return true if the interval is visible from the viewport, otherwise false.
     * @param start the start time of the interval
     * @param end the end time of the interval
     * @return true if the interval is visible from the viewport, otherwise false
     */
    boolean isInViewPort(Instant start, Instant end);
}
