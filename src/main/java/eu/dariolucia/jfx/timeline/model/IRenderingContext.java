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
import javafx.scene.paint.Color;

import java.time.Instant;
import java.util.Set;

public interface IRenderingContext {

    double getTextWidth(GraphicsContext gc, String text);

    double getTaskPanelWidth();

    double getTextHeight();

    double getLineRowHeight();

    double toX(Instant time);

    double getTextPadding();

    Instant getViewPortStart();

    Instant getViewPortEnd();

    Set<TaskItem> getSelectedTaskItems();

    double getImageAreaHeight();

    double getImageAreaWidth();

    double getHeaderRowHeight();

    Color getPanelBackgroundColor();

    Color getPanelForegroundColor();

    Color getSelectBorderColor();

    Color getPanelBorderColor();

    boolean isInViewPort(Instant start, Instant end);
}
