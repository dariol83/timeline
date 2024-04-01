package eu.dariolucia.jfx.timeline;

import eu.dariolucia.jfx.timeline.model.ITaskLine;
import eu.dariolucia.jfx.timeline.model.RenderingContext;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class Timeline extends GridPane {

    public static final double TEXT_PADDING = 5;
    public static final double TASK_PANEL_WIDTH_DEFAULT = 100;
    private static final double MIN_WIDTH_PER_ELEMENT = 40;
    private final Canvas imageArea;
    private final ScrollBar horizontalScroll;
    private final ScrollBar verticalScroll;

    private final SimpleObjectProperty<Instant> minTime = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Instant> maxTime = new SimpleObjectProperty<>();
    private final SimpleLongProperty viewPortDuration = new SimpleLongProperty();
    private final SimpleObjectProperty<Instant> viewPortStart = new SimpleObjectProperty<>();

    private final SimpleDoubleProperty taskPanelWidth = new SimpleDoubleProperty(TASK_PANEL_WIDTH_DEFAULT);
    private final ObservableList<ITaskLine> lines = FXCollections.observableArrayList(ITaskLine::getObservableProperties);

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/
    private double textHeight = -1;
    private double headerRowHeight = -1;
    private double lineRowHeight = -1;
    private ChronoUnit headerElement = ChronoUnit.SECONDS;

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

        // Set horizontal scrollbar limits (fixed)
        this.horizontalScroll.setMin(0);
        this.horizontalScroll.setMax(100);
        this.verticalScroll.setMin(0);
        this.verticalScroll.setMax(1);
        // Add mechanism to change start of viewport with horizontal scrollbar (scrollbar update -> update viewport)
        this.horizontalScroll.valueProperty().addListener((e,o,n) -> updateStartTime());
        this.verticalScroll.valueProperty().addListener((e,o,n) -> refresh());
        // Add listener when timeline is resized (structural update)
        ap.widthProperty().addListener((e,o,n) -> recomputeArea());
        ap.heightProperty().addListener((e,o,n) -> recomputeArea());
        // Add listener when time properties are updated (structural update)
        minTimeProperty().addListener((e,v,n) -> recomputeArea());
        maxTimeProperty().addListener((e,v,n) -> recomputeArea());
        viewPortDurationProperty().addListener((e,v,n) -> recomputeArea());
        // Add listener when start time is updated (viewport update -> update scrollbar)
        viewPortStartProperty().addListener((e,o,n) -> recomputeViewport());
        // Add listener when task panel width is updated
        taskPanelWidthProperty().addListener((e,o,n) -> refresh());
        // Add listener to changes to the observable list structure (add, remove)
        this.lines.addListener(this::itemsUpdated);

        Platform.runLater(this::recomputeArea);
    }

    private void itemsUpdated(ListChangeListener.Change<? extends ITaskLine> c) {
        System.out.println("Change detected for timeline");
        while(c.next()) {
            if(c.wasAdded()) {
                System.out.println("Added: " + c.getAddedSubList());
            }
            if(c.wasRemoved()) {
                System.out.println("Removed: " + c.getRemoved());
            }
            if(c.wasUpdated()) {
                System.out.println("Updated: " + c.getList());
            }
        }
        // Update vertical scroll
        updateVScrollbarSettings();
        // TODO: depending on the location of the change, some optimisation could be performed (e.g. do not refresh if
        //  the change is not inside the viewport
        refresh();
    }

    private void updateVScrollbarSettings() {
        // Get the current value
        double currentValue = this.verticalScroll.getValue();
        // Get the total number of lines
        int totalLines = getTotalNbOfLines();
        if(totalLines > 0) {
            // Compute the max size
            double fullSize = totalLines * this.lineRowHeight;
            double totalSize = fullSize - this.imageArea.getHeight() + this.headerRowHeight;
            if (currentValue > totalSize) {
                currentValue = totalSize;
            }
            // Update the scrollbar max value
            this.verticalScroll.setMax(totalSize);
            this.verticalScroll.setValue(currentValue);
            this.verticalScroll.setUnitIncrement(this.lineRowHeight);
            // Set block increment
            this.verticalScroll.setBlockIncrement(this.imageArea.getHeight() - this.headerRowHeight);
            // Set visible amount if area is ready
            if(this.imageArea.getHeight() > 0) {
                this.verticalScroll.setVisibleAmount((totalSize / fullSize) * (this.imageArea.getHeight() - this.headerRowHeight));
            } else {
                this.verticalScroll.setVisibleAmount(1);
            }
        } else {
            this.verticalScroll.setMax(1);
            this.verticalScroll.setValue(0);
            this.verticalScroll.setUnitIncrement(1);
            this.verticalScroll.setBlockIncrement(1);
            this.verticalScroll.setVisibleAmount(1);
        }
    }

    private int getTotalNbOfLines() {
        int nbLines = 0;
        for(ITaskLine tl : this.lines) {
            nbLines += tl.getNbOfLines();
        }
        return nbLines;
    }

    private void recomputeViewport() {
        updateHScrollbarPosition();
        refresh();
    }

    /**
     * Changes the timeline so that it shows all the complete duration as configured by the min and max times in the
     * viewport. The start time of the viewport is set equal to the min time and the viewport duration is set equal to
     * the difference between the max and the min times.
     */
    public void fitToTimeline() {
        setViewPortDuration(getMaxTime().getEpochSecond() - getMinTime().getEpochSecond());
        setViewPortStart(getMinTime());
    }

    private void updateHScrollbarSettings() {
        // Update the scrollbar max value
        this.horizontalScroll.setMax(getMaxTime().getEpochSecond() - getMinTime().getEpochSecond());
        // Update the unit and block values
        this.horizontalScroll.setBlockIncrement(getViewPortDuration());
        switch (this.headerElement) {
            case MINUTES: {
                this.horizontalScroll.setUnitIncrement(30);
            }
            break;
            case HOURS: {
                this.horizontalScroll.setUnitIncrement(900);
            }
            break;
            case DAYS: {
                this.horizontalScroll.setUnitIncrement(43200);
            }
            break;
            case MONTHS: {
                this.horizontalScroll.setUnitIncrement(864000);
            }
            break;
            case YEARS: {
                this.horizontalScroll.setUnitIncrement(15552000);
            }
            break;
            default: {
                this.horizontalScroll.setUnitIncrement(1);
            }
        }
        // Compute the X axis (time) characteristics
        // a) position of the H scrollbar depending on the start time
        updateHScrollbarPosition();
        // b) size of the H scrollbar depending on the duration with respect to the whole configured time span
        long secondsSpan = getMaxTime().getEpochSecond() - getMinTime().getEpochSecond();
        double percentage = (double) getViewPortDuration() / secondsSpan;
        this.horizontalScroll.setVisibleAmount(percentage * this.horizontalScroll.getMax());
    }

    private void updateHScrollbarPosition() {
        long secondsSpan = (getMaxTime().getEpochSecond() - getViewPortDuration()) - getMinTime().getEpochSecond();
        long position = getViewPortStart().getEpochSecond() - getMinTime().getEpochSecond();
        double percentage = (double) position / secondsSpan;
        this.horizontalScroll.setValue(percentage * this.horizontalScroll.getMax());
    }

    private void updateStartTime() {
        // Compute the Instant where the scrollbar now is
        double percentage = this.horizontalScroll.getValue() / this.horizontalScroll.getMax();
        long toAdd = (getMaxTime().getEpochSecond() - getViewPortDuration()) - getMinTime().getEpochSecond();
        toAdd *= percentage;
        Instant newStartValue = getMinTime().plusSeconds(toAdd);
        setViewPortStart(newStartValue);
    }

    private void recomputeArea() {
        // Based on the size of the area, compute the optimal way to draw the header information
        this.headerElement = computeHeaderElement(this.imageArea.getWidth(), getViewPortDuration());
        // Recompute text and row height
        measureFontHeight(this.imageArea.getGraphicsContext2D().getFont());

        if(getMaxTime() == null || getMinTime() == null) {
            // Cannot update right now
            return;
        }
        if(getViewPortStart() == null) {
            setViewPortStart(getMinTime());
        }
        if(getViewPortDuration() == 0) {
            setViewPortDuration(getMaxTime().getEpochSecond() - getMinTime().getEpochSecond());
        }
        // Recompute scrollbar settings
        updateHScrollbarSettings();
        updateVScrollbarSettings();
        // Repaint after this
        refresh();
    }

    private ChronoUnit computeHeaderElement(double pixelWidth, long durationSeconds) {
        if(pixelWidth == 0) {
            // Use seconds, not initialised
            return ChronoUnit.SECONDS;
        }
        double pixelForSecond = pixelWidth / durationSeconds;
        double temp = 0;
        if(pixelForSecond > MIN_WIDTH_PER_ELEMENT) {
            return ChronoUnit.SECONDS;
        } else if((temp = pixelForSecond * 60) > MIN_WIDTH_PER_ELEMENT) {
            return ChronoUnit.MINUTES;
        } else if((temp = temp * 60) > MIN_WIDTH_PER_ELEMENT) {
            return ChronoUnit.HOURS;
        } else if((temp = temp * 24) > MIN_WIDTH_PER_ELEMENT) {
            return ChronoUnit.DAYS;
        } else if(temp * 30 > MIN_WIDTH_PER_ELEMENT) {
            return ChronoUnit.MONTHS;
        } else {
            return ChronoUnit.YEARS;
        }
    }

    public void refresh() {
        GraphicsContext gc = this.imageArea.getGraphicsContext2D();
        // Draw the background
        drawBackground(gc);
        // Draw empty side panel
        drawEmptySidePanel(gc);
        // Draw task lines
        RenderingContext rc = new RenderingContext(getTaskPanelWidth(), this.lineRowHeight, this.textHeight, TEXT_PADDING,
                getViewPortStart(), getViewPortStart().plusSeconds(getViewPortDuration()), this::toX);
        drawTaskLines(gc, rc);
        // Draw calendar headers: need conversion functions Instant -> x on screen
        drawHeaders(gc);

        // Debug data
        // gc.setStroke(Color.BLACK);
        // gc.strokeText(String.format("%s %s %d %s", getMinTime(), getMaxTime(), getViewPortDuration(), getViewPortStart()), 0 ,textHeight + 40);
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.BEIGE);
        gc.fillRect(0, 0, this.imageArea.getWidth(), this.imageArea.getHeight());
    }

    private void drawTaskLines(GraphicsContext gc, RenderingContext rc) {
        // You render only line blocks that are contained in the viewport, as defined by the vertical scroll value
        double yStart = this.verticalScroll.getValue();
        double yEnd = yStart + this.imageArea.getHeight() - this.headerRowHeight;
        int processedLines = 0;
        for(ITaskLine line : this.lines) {
            // Compute the Y span of the rendering for this task line
            double taskLineYStart = processedLines * this.lineRowHeight;
            processedLines += line.getNbOfLines();
            double taskLineYEnd = processedLines * this.lineRowHeight;
            if((taskLineYStart > yStart && taskLineYStart < yEnd) ||
                    (taskLineYEnd > yStart && taskLineYEnd < yEnd)) {
                // Render: translate the taskLineYStart in the right viewport coordinates
                taskLineYStart -= yStart;
                taskLineYStart += this.headerRowHeight;
                // Ask the rendering of the timeline
                line.render(gc, 0, taskLineYStart, rc);
            }
            // Check if end line was found
            if(taskLineYEnd > yEnd) {
                break;
            }
        }
        // Rendering completed
    }

    private void drawEmptySidePanel(GraphicsContext gc) {
        gc.setFill(Color.LIGHTGRAY);
        gc.setStroke(Color.DARKGRAY);
        gc.fillRect(0,this.headerRowHeight, 100, this.imageArea.getHeight() - this.headerRowHeight);
        gc.strokeRect(0,this.headerRowHeight, 100, this.imageArea.getHeight() - this.headerRowHeight);
    }

    private void drawHeaders(GraphicsContext gc) {
        // The header is composed by one line, containing the time information depending on the resolution
        // Get the start time and round it to the previous step, depending on the headerElement value
        Instant startTime = getAdjustedStartTime(getViewPortStart(), this.headerElement);
        Instant finalTime = getViewPortStart().plusSeconds(getViewPortDuration());
        // Now start rendering, until the end of the box falls outside the rendering area
        boolean inArea = true;
        while(inArea) {
            Instant endTime = startTime.plus(1, this.headerElement);
            renderHeader(gc, startTime, endTime, this.headerElement);
            if(endTime.isAfter(finalTime)) {
                inArea = false;
            } else {
                startTime = endTime;
            }
        }
        // Fill the TL corner block
        gc.setFill(Color.LIGHTGRAY);
        gc.setStroke(Color.DARKGRAY);
        gc.fillRect(0, 0, getTaskPanelWidth(), this.headerRowHeight);
        gc.strokeRect(0, 0, getTaskPanelWidth(), this.headerRowHeight);
    }

    private void renderHeader(GraphicsContext gc, Instant startTime, Instant endTime, ChronoUnit headerElement) {
        double xStart = toX(startTime);
        double xEnd = toX(endTime);
        double height = this.headerRowHeight;
        // Fill rectangle
        gc.setFill(Color.LIGHTGRAY);
        gc.setStroke(Color.DARKGRAY);
        gc.fillRect(xStart, 0, xEnd - xStart, height);
        gc.strokeRect(xStart, 0, xEnd - xStart, height);
        // Write text
        gc.setStroke(Color.BLACK);
        String toWrite = formatHeaderText(startTime, headerElement);
        gc.strokeText(toWrite, xStart + TEXT_PADDING, this.textHeight + TEXT_PADDING);
    }

    private String formatHeaderText(Instant startTime, ChronoUnit headerElement) {
        ZonedDateTime time = startTime.atZone(ZoneId.of("UTC"));
        switch(headerElement) {
            case SECONDS: return String.format("%02d:%02d:%02d", time.get(ChronoField.HOUR_OF_DAY), time.get(ChronoField.MINUTE_OF_HOUR), time.get(ChronoField.SECOND_OF_MINUTE));
            case MINUTES: return String.format("%02d:%02d", time.get(ChronoField.HOUR_OF_DAY), time.get(ChronoField.MINUTE_OF_HOUR));
            case HOURS: return String.format("%02d", time.get(ChronoField.HOUR_OF_DAY));
            case DAYS: return String.format("%04d-%02d-%02d", time.get(ChronoField.YEAR), time.get(ChronoField.MONTH_OF_YEAR) + 1, time.get(ChronoField.DAY_OF_MONTH));
            case MONTHS: return String.format("%04d-%02d", time.get(ChronoField.YEAR), time.get(ChronoField.MONTH_OF_YEAR) + 1);
            default: return String.format("%04d", time.get(ChronoField.YEAR));
        }
    }

    private Instant getAdjustedStartTime(Instant viewPortStart, ChronoUnit headerElement) {
        return viewPortStart.truncatedTo(headerElement);
    }

    private void measureFontHeight(Font font) {
        if(textHeight == -1) {
            Text text = new Text("Ig");
            text.setBoundsType(TextBoundsType.VISUAL);
            text.setFont(font);
            textHeight = text.getBoundsInLocal().getHeight();
            headerRowHeight = textHeight + 2 * TEXT_PADDING;
            lineRowHeight = textHeight + 4 * TEXT_PADDING;
        }
    }

    public Instant toInstant(double x) {
        // x indicates the pixel in canvas coordinates: first of all, we need to remove the task panel size
        x -= getTaskPanelWidth();
        // Now compute the percentage
        double percentage = x / (this.imageArea.getWidth() - getTaskPanelWidth());
        // Find out the instant from the viewport start
        return getViewPortStart().plusSeconds(Math.round(percentage * getViewPortDuration()));
    }

    public double toX(Instant time) {
        // The X can be off map
        long timeSecs = time.getEpochSecond();
        long startTimeSecs = getViewPortStart().getEpochSecond();
        // Now check where timeSecs linearly is
        double percentage = (timeSecs - startTimeSecs)/ (double) getViewPortDuration();
        // Now translate to pixels
        return percentage * (this.imageArea.getWidth() - getTaskPanelWidth()) + getTaskPanelWidth();
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

    public ObservableList<ITaskLine> getLines() {
        return lines;
    }

    public double getTaskPanelWidth() {
        return taskPanelWidth.get();
    }

    public SimpleDoubleProperty taskPanelWidthProperty() {
        return taskPanelWidth;
    }

    public void setTaskPanelWidth(double taskPanelWidth) {
        this.taskPanelWidth.set(taskPanelWidth);
    }
}
