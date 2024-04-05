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
import javafx.scene.text.Text;

import java.time.Instant;
import java.util.function.ToDoubleFunction;

public final class RenderingContext {

    private final double taskPanelWidth;
    private final double lineRowHeight;
    private final double textHeight;
    private final ToDoubleFunction<Instant> instant2xFunction;
    private final double textPadding;
    private final Instant viewPortStart;
    private final Instant viewPortEnd;
    private final TaskItem selectedTaskItem;

    public RenderingContext(double taskPanelWidth, double lineRowHeight, double textHeight, double textPadding, Instant viewPortStart, Instant viewPortEnd,
                            ToDoubleFunction<Instant> instant2xFunction, TaskItem selectedTaskItem) {
        this.taskPanelWidth = taskPanelWidth;
        this.lineRowHeight = lineRowHeight;
        this.textHeight = textHeight;
        this.viewPortStart = viewPortStart;
        this.viewPortEnd = viewPortEnd;
        this.textPadding = textPadding;
        this.instant2xFunction = instant2xFunction;
        this.selectedTaskItem = selectedTaskItem;
    }

    public double getTaskPanelWidth() {
        return taskPanelWidth;
    }

    public double getTextHeight() {
        return textHeight;
    }

    public double getLineRowHeight() {
        return lineRowHeight;
    }

    public double toX(Instant time) {
        return instant2xFunction.applyAsDouble(time);
    }

    public double getTextPadding() {
        return textPadding;
    }

    public Instant getViewPortStart() {
        return this.viewPortStart;
    }

    public Instant getViewPortEnd() {
        return this.viewPortEnd;
    }

    public TaskItem getSelectedTaskItem() {
        return selectedTaskItem;
    }

    public double getTextWidth(GraphicsContext gc, String text) {
        Text theText = new Text(text);
        theText.setFont(gc.getFont());
        return theText.getBoundsInLocal().getWidth();
    }
}
