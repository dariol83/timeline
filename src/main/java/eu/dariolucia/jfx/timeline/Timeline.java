package eu.dariolucia.jfx.timeline;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.time.Instant;

public class Timeline extends GridPane {

    public static final double TASK_PANEL_WIDTH = 100;
    public static final double HSCROLL_MAX_VAL = 1000;
    private final Canvas imageArea;
    private final ScrollBar horizontalScroll;
    private final ScrollBar verticalScroll;

    private final SimpleObjectProperty<Instant> minTime = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Instant> maxTime = new SimpleObjectProperty<>();
    private final SimpleLongProperty viewPortDuration = new SimpleLongProperty();
    private final SimpleObjectProperty<Instant> viewPortStart = new SimpleObjectProperty<>();

    private double textHeight = -1;

    public Timeline() {
        // Set grid layout constraints
        ColumnConstraints firstColConstr = new ColumnConstraints();
        firstColConstr.setHgrow(Priority.ALWAYS);
        ColumnConstraints secondColConstr = new ColumnConstraints();
        secondColConstr.setHgrow(Priority.NEVER);
        RowConstraints firstRowConstr = new RowConstraints();
        firstRowConstr.setVgrow(Priority.ALWAYS);
        RowConstraints secondRowConstr = new RowConstraints();
        secondRowConstr.setVgrow(Priority.NEVER);
        getColumnConstraints().addAll(firstColConstr, secondColConstr);
        getRowConstraints().addAll(firstRowConstr, secondRowConstr);
        // Create the image area and add
        this.imageArea = new Canvas();
        Pane ap = new Pane();
        ap.getChildren().add(this.imageArea);
        getChildren().add(ap);
        this.imageArea.heightProperty().bind(ap.heightProperty());
        this.imageArea.widthProperty().bind(ap.widthProperty());
        GridPane.setRowIndex(ap, 0);
        GridPane.setColumnIndex(ap, 0);
        // Create the horizontal scrollbar
        this.horizontalScroll = new ScrollBar();
        this.horizontalScroll.setOrientation(Orientation.HORIZONTAL);
        getChildren().add(this.horizontalScroll);
        GridPane.setRowIndex(this.horizontalScroll, 1);
        GridPane.setColumnIndex(this.horizontalScroll, 0);
        // Create the horizontal scrollbar
        this.verticalScroll = new ScrollBar();
        this.verticalScroll.setOrientation(Orientation.VERTICAL);
        getChildren().add(this.verticalScroll);
        GridPane.setRowIndex(this.verticalScroll, 0);
        GridPane.setColumnIndex(this.verticalScroll, 1);
        // Create a fill label
        Label filler = new Label("");
        getChildren().add(filler);
        GridPane.setRowIndex(filler, 1);
        GridPane.setColumnIndex(filler, 1);

        // Add data defaults
        Instant currentTime = Instant.now();
        setMinTime(currentTime);
        setMaxTime(currentTime.plusSeconds(280));
        setViewPortStart(currentTime);
        setViewPortDuration(60);

        // Set horizontal scrollbar limits (fixed)
        this.horizontalScroll.setMin(0);
        this.horizontalScroll.setMax(HSCROLL_MAX_VAL);
        // Add mechanism to change start of viewport with horizontal scrollbar
        this.horizontalScroll.valueProperty().addListener((e,o,n) -> updateStartTime());
        viewPortStartProperty().addListener((e,o,n) -> recomputeArea());
        // Add listener when timeline is resized
        widthProperty().addListener((e,o,n) -> recomputeArea());
        heightProperty().addListener((e,o,n) -> recomputeArea());

        Platform.runLater(this::recomputeArea);
    }

    private void updateStartTime() {
        // Compute the Instant where the scrollbar now is
        double percentage = this.horizontalScroll.getValue() / HSCROLL_MAX_VAL;
        long toAdd = (getMaxTime().getEpochSecond() - getViewPortDuration()) - getMinTime().getEpochSecond();
        toAdd *= percentage;
        Instant newStartValue = getMinTime().plusSeconds(toAdd);
        setViewPortStart(newStartValue);
    }

    private void recomputeArea() {
        // TODO: left panel size is variable or not? For now, make it fixed size
        // 1) Compute the X axis (time) characterstics
        // a) position of the H scrollbar depending on the start time
        long secondsSpan = (getMaxTime().getEpochSecond() - getViewPortDuration()) - getMinTime().getEpochSecond();
        long position = getViewPortStart().getEpochSecond() - getMinTime().getEpochSecond();
        double percentage = (double) position / secondsSpan;
        this.horizontalScroll.setValue(percentage * HSCROLL_MAX_VAL);
        // b) size of the H scrollbar depending on the duration with respect to the whole configured time span
        secondsSpan = getMaxTime().getEpochSecond() - getMinTime().getEpochSecond();
        percentage = (double) getViewPortDuration() / secondsSpan;
        this.horizontalScroll.setVisibleAmount(percentage * HSCROLL_MAX_VAL);

        // 2) Compute the Y axis (lines) characterstics
        // TODO:

        // Repaint after this
        refresh();
    }

    public void refresh() {
        // Measure the text height of the standard font, required for precise rendering
        GraphicsContext gc = this.imageArea.getGraphicsContext2D();
        if(textHeight == -1) {
            textHeight = measureTextHeight(gc.getFont());
        }
        gc.setFill(Color.BEIGE);
        gc.fillRect(0, 0, this.imageArea.getWidth(), this.imageArea.getHeight());

        // Draw empty side panel
        gc.setFill(Color.GRAY);
        gc.fillRect(0,0, 100, this.imageArea.getHeight());
        // Draw calendar headers: need conversion functions Instant -> x on screen
        // TODO

        // Debug data
        gc.setStroke(Color.BLACK);
        gc.strokeText(String.format("%s %s %d %s", getMinTime(), getMaxTime(), getViewPortDuration(), getViewPortStart()), 0 ,textHeight);
    }

    public Instant toInstant(double x) {
        // x indicates the pixel in canvas coordinates: first of all, we need to remove the task panel size
        x -= TASK_PANEL_WIDTH;
        // Now compute the percentage
        double percentage = x / (this.imageArea.getWidth() - TASK_PANEL_WIDTH);
        // Find out the instant from the viewport start
        return getViewPortStart().plusSeconds(Math.round(percentage * getViewPortDuration()));
    }

    public double toX(Instant time) {
        // The X can be off map
        // TODO:
        return -1;
    }

    public Instant getMinTime() {
        return minTime.get();
    }

    public SimpleObjectProperty<Instant> minTimeProperty() {
        return minTime;
    }

    public void setMinTime(Instant minTime) {
        this.minTime.set(minTime);
    }

    public Instant getMaxTime() {
        return maxTime.get();
    }

    public SimpleObjectProperty<Instant> maxTimeProperty() {
        return maxTime;
    }

    public void setMaxTime(Instant maxTime) {
        this.maxTime.set(maxTime);
    }

    public long getViewPortDuration() {
        return viewPortDuration.get();
    }

    public SimpleLongProperty viewPortDurationProperty() {
        return viewPortDuration;
    }

    public void setViewPortDuration(long viewPortDuration) {
        this.viewPortDuration.set(viewPortDuration);
    }

    public Instant getViewPortStart() {
        return viewPortStart.get();
    }

    public SimpleObjectProperty<Instant> viewPortStartProperty() {
        return viewPortStart;
    }

    public void setViewPortStart(Instant viewPortStart) {
        this.viewPortStart.set(viewPortStart);
    }

    private static double measureTextHeight(Font font) {
        Text text = new Text("Ig");
        text.setBoundsType(TextBoundsType.VISUAL);
        text.setFont(font);
        return text.getBoundsInLocal().getHeight();
    }
}
