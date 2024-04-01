package eu.dariolucia.jfx.timeline.model;

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

    public RenderingContext(double taskPanelWidth, double lineRowHeight, double textHeight, double textPadding, Instant viewPortStart, Instant viewPortEnd,
                            ToDoubleFunction<Instant> instant2xFunction) {
        this.taskPanelWidth = taskPanelWidth;
        this.lineRowHeight = lineRowHeight;
        this.textHeight = textHeight;
        this.viewPortStart = viewPortStart;
        this.viewPortEnd = viewPortEnd;
        this.textPadding = textPadding;
        this.instant2xFunction = instant2xFunction;
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
}
