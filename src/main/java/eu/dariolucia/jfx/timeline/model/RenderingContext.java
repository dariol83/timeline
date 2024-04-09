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
import javafx.scene.text.Text;

import java.time.Instant;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public final class RenderingContext {

    private final double taskPanelWidth;
    private final double lineRowHeight;
    private final double textHeight;
    private final ToDoubleFunction<Instant> instant2xFunction;
    private final double textPadding;
    private final Instant viewPortStart;
    private final Instant viewPortEnd;
    private final Set<TaskItem> selectedTaskItems;
    private final double imageAreaWidth;
    private final double imageAreaHeight;
    private final double headerRowHeight;
    private final Color panelBackgroundColor;
    private final Color panelForegroundColor;
    private final Color panelBorderColor;
    private final Color selectBorderColor;



    public RenderingContext(double taskPanelWidth, double headerRowHeight, double lineRowHeight, double textHeight, double textPadding,
                            Instant viewPortStart, Instant viewPortEnd,
                            double imageAreaWidth, double imageAreaHeight,
                            ToDoubleFunction<Instant> instant2xFunction, Set<TaskItem> selectedItems,
                            Color panelBackgroundColor, Color panelForegroundColor,
                            Color panelBorderColor, Color selectBorderColor) {
        this.taskPanelWidth = taskPanelWidth;
        this.headerRowHeight = headerRowHeight;
        this.lineRowHeight = lineRowHeight;
        this.textHeight = textHeight;
        this.viewPortStart = viewPortStart;
        this.viewPortEnd = viewPortEnd;
        this.imageAreaWidth = imageAreaWidth;
        this.imageAreaHeight = imageAreaHeight;
        this.textPadding = textPadding;
        this.instant2xFunction = instant2xFunction;
        this.selectedTaskItems = selectedItems;
        this.panelBackgroundColor = panelBackgroundColor;
        this.panelForegroundColor = panelForegroundColor;
        this.panelBorderColor = panelBorderColor;
        this.selectBorderColor = selectBorderColor;
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

    public Set<TaskItem> getSelectedTaskItems() {
        return selectedTaskItems;
    }

    public double getImageAreaHeight() {
        return imageAreaHeight;
    }

    public double getImageAreaWidth() {
        return imageAreaWidth;
    }

    public double getHeaderRowHeight() {
        return headerRowHeight;
    }

    public Color getPanelBackgroundColor() {
        return panelBackgroundColor;
    }

    public Color getPanelForegroundColor() {
        return panelForegroundColor;
    }

    public Color getSelectBorderColor() {
        return selectBorderColor;
    }

    public Color getPanelBorderColor() {
        return panelBorderColor;
    }

    public boolean isInViewPort(Instant start, Instant end) {
        return (start.isAfter(this.viewPortStart) && start.isBefore(this.viewPortEnd)) || (
                end.isAfter(this.viewPortStart) && end.isBefore(this.viewPortEnd)) ||
                (start.isBefore(this.viewPortStart) && end.isAfter(this.viewPortEnd));
    }

    public static double getTextWidth(GraphicsContext gc, String text) {
        Text theText = new Text(text);
        theText.setFont(gc.getFont());
        return theText.getBoundsInLocal().getWidth();
    }
}
