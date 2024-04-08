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

package eu.dariolucia.jfx.timeline;

import eu.dariolucia.jfx.timeline.internal.TimelineSingleSelectionModel;
import eu.dariolucia.jfx.timeline.model.*;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.SelectionModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A timeline is a graphical JavaFX component that can be used to display tasks and groups of tasks, time cursors
 * and time intervals on a timeline. Each element is individually customizable in terms of rendering via standard JavaFX
 * properties and via subclassing for more personalised rendering.
 */
public class Timeline extends GridPane {

    /* *****************************************************************************************
     * Constants
     * *****************************************************************************************/
    private static final double TEXT_PADDING = 5;
    private static final double TASK_PANEL_WIDTH_DEFAULT = 100;

    /* *****************************************************************************************
     * JavaFX elements
     * *****************************************************************************************/
    private final Canvas imageArea;
    private final ScrollBar horizontalScroll;
    private final ScrollBar verticalScroll;
    private final Label labelCornerfiller;
    private final SelectionModel<TaskItem> selectionModel;

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/
    private final SimpleObjectProperty<Instant> minTime = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Instant> maxTime = new SimpleObjectProperty<>();
    private final SimpleLongProperty viewPortDuration = new SimpleLongProperty();
    private final SimpleObjectProperty<Instant> viewPortStart = new SimpleObjectProperty<>();
    private final SimpleDoubleProperty taskPanelWidth = new SimpleDoubleProperty(TASK_PANEL_WIDTH_DEFAULT);
    private final SimpleObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.WHITE);
    private final SimpleBooleanProperty scrollbarsVisible = new SimpleBooleanProperty();
    private final ObservableList<ITaskLine> items = FXCollections.observableArrayList(ITaskLine::getObservableProperties);
    private final ObservableList<TimeCursor> timeCursors = FXCollections.observableArrayList(timeCursor -> new Observable[] {
            timeCursor.colorProperty(), timeCursor.timeProperty() });
    private final ObservableList<TimeInterval> timeIntervals = FXCollections.observableArrayList(timeInterval -> new Observable[] {
            timeInterval.colorProperty(), timeInterval.startTimeProperty(), timeInterval.endTimeProperty(), timeInterval.foregroundProperty() });
    private final SimpleBooleanProperty enableMouseSelection = new SimpleBooleanProperty(true);

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/
    private double textHeight = -1;
    private double headerRowHeight = -1;
    private double lineRowHeight = -1;
    private ChronoUnit headerElement = ChronoUnit.SECONDS;
    private int[] currentYViewportItems;
    private List<TaskItem> flatTaskItem;

    /**
     * Class constructor.
     */
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
        // Create the vertical scrollbar
        this.verticalScroll = new ScrollBar();
        this.verticalScroll.setOrientation(Orientation.VERTICAL);
        // Create a fill label
        this.labelCornerfiller = new Label("");

        // Set horizontal scrollbar limits (fixed)
        this.horizontalScroll.setMin(0);
        this.horizontalScroll.setMax(100);
        this.verticalScroll.setMin(0);
        this.verticalScroll.setMax(1);
        // Add mechanism to change start of viewport with horizontal scrollbar (scrollbar update -> update viewport)
        this.horizontalScroll.valueProperty().addListener((e,o,n) -> updateStartTime());
        this.verticalScroll.valueProperty().addListener((e,o,n) -> internalRefresh());
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
        taskPanelWidthProperty().addListener((e,o,n) -> recomputeViewport());
        // Add listener when background color is updated
        backgroundColorProperty().addListener((e,o,n) -> internalRefresh());
        // Add listener when scrollbar visible is updated
        scrollbarsVisibleProperty().addListener((e,o,n) -> scrollbarsStatusChanged());
        // Add listener to changes to the observable list structure (add, remove)
        this.items.addListener(this::itemsUpdated);
        // Add listener to changes to the observable list of time cursors
        this.timeCursors.addListener(this::timeCursorsUpdated);
        // Add listener to changes to the observable list of time intevals
        this.timeIntervals.addListener(this::timeIntervalsUpdated);

        // Create the selection model
        this.selectionModel = new TimelineSingleSelectionModel(this);
        this.selectionModel.selectedItemProperty().addListener((e,o,n) -> internalRefresh());

        // Add listener for task item selection
        this.imageArea.addEventHandler(MouseEvent.MOUSE_CLICKED, this::mouseClickedAction);
        this.imageArea.addEventHandler(ScrollEvent.ANY, this::mouseScrolledAction);

        // Perform initial drawing
        Platform.runLater(this::recomputeArea);
    }

    /**
     * This method returns the vertical scrollbar if visible, otherwise null.
     * @return the vertical {@link ScrollBar} if visible, otherwise null.
     */
    public ScrollBar getVerticalScroll() {
        if(isScrollbarsVisible()) {
            return verticalScroll;
        } else {
            return null;
        }
    }

    /**
     * This method returns the horizontal scrollbar if visible, otherwise null.
     * @return the horizontal {@link ScrollBar} if visible, otherwise null.
     */
    public ScrollBar getHorizontalScroll() {
        if(isScrollbarsVisible()) {
            return horizontalScroll;
        } else {
            return null;
        }
    }

    /**
     * This method returns the Canvas used to draw the timeline.
     * @return the {@link Canvas} of the timeline
     */
    public Canvas getImageArea() {
        return imageArea;
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

    /**
     * This method puts the provided {@link ITaskLine} (or recursively its parent, if this is not the top level {@link ITaskLine}
     * in the viewport (at the highest possible position).
     * @param taskLine the task line to be put in the viewport
     */
    public void scrollTo(ITaskLine taskLine) {
        while(taskLine.getParent() != null) {
            taskLine = taskLine.getParent();
        }
        // Compute the value for the vertical scroll
        double value = 0;
        for(ITaskLine tl : this.items) {
            if(tl.equals(taskLine)) {
                this.verticalScroll.setValue(Math.min(value, this.verticalScroll.getMax()));
                return;
            } else {
                value += tl.getNbOfLines() * this.lineRowHeight;
            }
        }
    }

    /**
     * This method recomputes the underlying rendering area and redraw it.
     */
    public void refresh() {
        recomputeArea();
    }

    /**
     * This method maps a X value in {@link Canvas} coordinates to the related {@link Instant} time. X can be outside the
     * rendering area.
     * @param x the X coordinate to convert
     * @return the instant mapped to that X
     */
    public Instant toInstant(double x) {
        // x indicates the pixel in canvas coordinates: first of all, we need to remove the task panel size
        x -= getTaskPanelWidth();
        // Now compute the percentage
        double percentage = x / (this.imageArea.getWidth() - getTaskPanelWidth());
        // Find out the instant from the viewport start
        return getViewPortStart().plusSeconds(Math.round(percentage * getViewPortDuration()));
    }

    /**
     * This method maps an {@link Instant} to the related X in {@link Canvas} coordinates.
     * @param time the instant to convert
     * @return the X mapped to the instant
     */
    public double toX(Instant time) {
        // The X can be off map
        long timeSecs = time.getEpochSecond();
        long startTimeSecs = getViewPortStart().getEpochSecond();
        // Now check where timeSecs linearly is
        double percentage = (timeSecs - startTimeSecs)/ (double) getViewPortDuration();
        // Now translate to pixels
        return percentage * (this.imageArea.getWidth() - getTaskPanelWidth()) + getTaskPanelWidth();
    }

    /**
     * Verify if the given {@link Instant} is in the rendered viewport.
     * @param time the time to check
     * @return true if in viewport, otherwise false
     */
    public boolean inViewport(Instant time) {
        long startTimeSecs = getViewPortStart().getEpochSecond();
        long endTimeSecs = startTimeSecs + getViewPortDuration();
        return time.getEpochSecond() >= startTimeSecs && time.getEpochSecond() <= endTimeSecs;
    }

    /**
     * Verify if the given interval is in the rendered viewport.
     * @param startTime the start time
     * @param endTime the end time
     * @return true if in viewport, otherwise false
     */
    public boolean inViewport(Instant startTime, Instant endTime) {
        long startTimeSecs = getViewPortStart().getEpochSecond();
        long endTimeSecs = startTimeSecs + getViewPortDuration();
        long intervalStartTimeSecs = startTime == null ? Long.MIN_VALUE : startTime.getEpochSecond();
        long intervalEndTimeSecs = endTime == null ? Long.MAX_VALUE : endTime.getEpochSecond();

        return (intervalStartTimeSecs >= startTimeSecs && intervalStartTimeSecs <= endTimeSecs) ||
                (intervalEndTimeSecs >= startTimeSecs && intervalEndTimeSecs <= endTimeSecs) ||
                (intervalStartTimeSecs <= startTimeSecs && intervalEndTimeSecs >= endTimeSecs);
    }

    public ITaskLine getTaskLineAt(double x, double y) {
        for (ITaskLine tl : this.items) {
            if (tl.contains(x, y)) {
                return tl;
            }
        }
        return null;
    }

    public TaskItem getTaskItemAt(double x, double y) {
        for (ITaskLine tl : this.items) {
            if (tl.contains(x, y)) {
                for (TaskItem ti : tl.getTaskItems()) {
                    if (ti.contains(x, y)) {
                        return ti;
                    }
                }
            }
        }
        return null;
    }

    /* *****************************************************************************************
     * Object properties
     * *****************************************************************************************/

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

    public ObservableList<ITaskLine> getItems() {
        return items;
    }

    public ObservableList<TimeCursor> getTimeCursors() {
        return timeCursors;
    }

    public ObservableList<TimeInterval> getTimeIntervals() {
        return timeIntervals;
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

    public List<TaskItem> getTaskItemList() {
        if(this.flatTaskItem == null) {
            this.flatTaskItem = this.items.stream().flatMap(l -> l.getTaskItems().stream()).collect(Collectors.toList());
        }
        return this.flatTaskItem;
    }

    public SelectionModel<TaskItem> getSelectionModel() {
        return selectionModel;
    }

    public boolean isEnableMouseSelection() {
        return enableMouseSelection.get();
    }

    public SimpleBooleanProperty enableMouseSelectionProperty() {
        return enableMouseSelection;
    }

    public void setEnableMouseSelection(boolean enableMouseSelection) {
        this.enableMouseSelection.set(enableMouseSelection);
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public SimpleObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public boolean isScrollbarsVisible() {
        return scrollbarsVisible.get();
    }

    public SimpleBooleanProperty scrollbarsVisibleProperty() {
        return scrollbarsVisible;
    }

    public void setScrollbarsVisible(boolean scrollbarsVisible) {
        this.scrollbarsVisible.set(scrollbarsVisible);
    }

    /* *****************************************************************************************
     * Utility access method
     * *****************************************************************************************/

    public TaskItem getTaskItemAt(int i) {
        return getTaskItemList().get(i);
    }

    public long getTaskItemCount() {
        return getTaskItemList().size();
    }

    /* *****************************************************************************************
     * Internal methods
     * *****************************************************************************************/

    private void internalRefresh() {
        GraphicsContext gc = this.imageArea.getGraphicsContext2D();
        gc.setFontSmoothingType(FontSmoothingType.LCD);
        RenderingContext rc = new RenderingContext(getTaskPanelWidth(), this.headerRowHeight, this.lineRowHeight, this.textHeight, TEXT_PADDING,
                getViewPortStart(), getViewPortStart().plusSeconds(getViewPortDuration()),
                this.imageArea.getWidth(), this.imageArea.getHeight(),
                this::toX, new LinkedHashSet<>(Collections.singleton(selectionModel.getSelectedItem())));
        // Draw the background
        drawBackground(gc);
        // Draw empty side panel
        drawEmptySidePanel(gc);
        // Draw time intervals in background
        drawTimeIntervals(gc, rc, false);
        // Draw task lines
        drawTaskLines(gc, rc);
        // Draw calendar headers: need conversion functions Instant -> x on screen
        drawHeaders(gc);
        // Draw time intervals in foreground
        drawTimeIntervals(gc, rc, true);
        // Draw cursors
        drawCursors(gc, rc);
    }

    private void scrollbarsStatusChanged() {
        if(this.horizontalScroll.getParent() == null) {
            addScrollbars();
        } else {
            removeScrollbars();
        }
        // At the end, recompute area
        recomputeArea();
    }

    private void removeScrollbars() {
        getChildren().remove(this.horizontalScroll);
        getChildren().remove(this.verticalScroll);
        getChildren().remove(this.labelCornerfiller);
    }

    private void addScrollbars() {
        getChildren().add(this.horizontalScroll);
        GridPane.setRowIndex(this.horizontalScroll, 1);
        GridPane.setColumnIndex(this.horizontalScroll, 0);
        getChildren().add(this.verticalScroll);
        GridPane.setRowIndex(this.verticalScroll, 0);
        GridPane.setColumnIndex(this.verticalScroll, 1);
        getChildren().add(labelCornerfiller);
        GridPane.setRowIndex(labelCornerfiller, 1);
        GridPane.setColumnIndex(labelCornerfiller, 1);
    }

    private void mouseScrolledAction(ScrollEvent scrollEvent) {
        if(scrollEvent.isControlDown()) {
            if (scrollEvent.getDeltaY() < 0) {
                setViewPortDuration((long) (getViewPortDuration() + getViewPortDuration()/10.0));
            } else {
                setViewPortDuration(Math.max(10, (long) (getViewPortDuration() - getViewPortDuration()/10.0)));
            }
        } else {
            if (scrollEvent.getDeltaY() < 0) {
                this.verticalScroll.increment();
            } else {
                this.verticalScroll.decrement();
            }
        }
    }

    private void timeCursorsUpdated(ListChangeListener.Change<? extends TimeCursor> c) {
        boolean refreshNeeded = false;
        while(c.next()) {
            if(c.wasAdded()) {
                for(TimeCursor tc : c.getAddedSubList()) {
                    tc.setTimeline(this);
                    if(inViewport(tc.getTime())) {
                        refreshNeeded = true;
                    }
                }
            }
            if(c.wasRemoved()) {
                for(TimeCursor tc : c.getRemoved()) {
                    tc.setTimeline(null);
                    if(inViewport(tc.getTime())) {
                        refreshNeeded = true;
                    }
                }
            }
            if(c.wasUpdated()) {
                for(TimeCursor tc : c.getList()) {
                    if(inViewport(tc.getTime())) {
                        refreshNeeded = true;
                        break;
                    }
                }
            }
        }
        if(refreshNeeded) {
            // Repaint
            internalRefresh();
        }
    }

    private void timeIntervalsUpdated(ListChangeListener.Change<? extends TimeInterval> c) {
        boolean refreshNeeded = false;
        while(c.next()) {
            if(c.wasAdded()) {
                for(TimeInterval tc : c.getAddedSubList()) {
                    tc.setTimeline(this);
                    if(inViewport(tc.getStartTime(), tc.getEndTime())) {
                        refreshNeeded = true;
                    }
                }
            }
            if(c.wasRemoved()) {
                for(TimeInterval tc : c.getRemoved()) {
                    tc.setTimeline(null);
                    if(inViewport(tc.getStartTime(), tc.getEndTime())) {
                        refreshNeeded = true;
                    }
                }
            }
            if(c.wasUpdated()) {
                for(TimeInterval tc : c.getList()) {
                    if(inViewport(tc.getStartTime(), tc.getEndTime())) {
                        refreshNeeded = true;
                        break;
                    }
                }
            }
        }
        if(refreshNeeded) {
            // Repaint
            internalRefresh();
        }
    }

    private void mouseClickedAction(MouseEvent mouseEvent) {
        // Get X,Y coordinates and check which task item is affected
        TaskItem selectedTaskItem = getTaskItemAt(mouseEvent.getX(), mouseEvent.getY());
        // Update selection
        if(isEnableMouseSelection() && !Objects.equals(selectedTaskItem, this.selectionModel.getSelectedItem())) {
            this.selectionModel.select(selectedTaskItem);
        }
    }

    private void itemsUpdated(ListChangeListener.Change<? extends ITaskLine> c) {
        boolean refreshNeeded = false;
        while(c.next()) {
            if(c.wasAdded()) {
                refreshNeeded = true;
                c.getAddedSubList().forEach(tl -> tl.setTimeline(this));
            }
            if(c.wasRemoved()) {
                refreshNeeded = true;
                c.getRemoved().forEach(tl -> tl.setTimeline(null));
            }
            if(!refreshNeeded && c.wasUpdated()) {
                refreshNeeded = isChangeInViewport(c);
            }
        }
        if(refreshNeeded) {
            // Update vertical scroll
            updateVScrollbarSettings();
            // Repaint
            internalRefresh();
        }
    }

    private boolean isChangeInViewport(ListChangeListener.Change<? extends ITaskLine> c) {
        boolean structureChanged = false;
        for(ITaskLine tl : c.getList()) {
            structureChanged |= tl.computeRenderingStructure();
        }
        if(this.currentYViewportItems == null || this.currentYViewportItems[0] == -1) {
            // Strange state, assume change
            return true;
        }
        return structureChanged || (c.getFrom() >= this.currentYViewportItems[0] && c.getFrom() <= this.currentYViewportItems[1]) ||
                (c.getTo() >= this.currentYViewportItems[0] && c.getTo() <= this.currentYViewportItems[1]) ||
                (c.getFrom() <= this.currentYViewportItems[0] && c.getTo() >= this.currentYViewportItems[1]);
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
        for(ITaskLine tl : this.items) {
            nbLines += tl.getNbOfLines();
        }
        return nbLines;
    }

    private void recomputeViewport() {
        updateHScrollbarPosition();
        internalRefresh();
    }

    private void updateHScrollbarSettings() {
        // Update the scrollbar max value
        this.horizontalScroll.setMax((double) (getMaxTime().getEpochSecond() - getMinTime().getEpochSecond()));
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
        toAdd = (long) (toAdd * percentage);
        Instant newStartValue = getMinTime().plusSeconds(toAdd);
        setViewPortStart(newStartValue);
    }

    private void recomputeArea() {
        // Based on the size of the area, compute the optimal way to draw the header information
        this.headerElement = computeHeaderElement(this.imageArea.getGraphicsContext2D(), this.imageArea.getWidth(), getViewPortDuration());
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
        internalRefresh();
    }

    private ChronoUnit computeHeaderElement(GraphicsContext gc, double pixelWidth, long durationSeconds) {
        if(pixelWidth == 0) {
            // Use seconds, not initialised
            return ChronoUnit.SECONDS;
        }
        double secondsSize = RenderingContext.getTextWidth(gc, "00:00:00") + 4*TEXT_PADDING;
        double minutesSize = RenderingContext.getTextWidth(gc, "00:00") + 4*TEXT_PADDING;
        double hoursSize = RenderingContext.getTextWidth(gc, "00") + 4*TEXT_PADDING;
        double daysSize = RenderingContext.getTextWidth(gc, "0000-00-00") + 4*TEXT_PADDING;
        double monthsSize = RenderingContext.getTextWidth(gc, "0000-00") + 4*TEXT_PADDING;

        double pixelForSecond = pixelWidth / durationSeconds;
        double temp;
        if(pixelForSecond > secondsSize) {
            return ChronoUnit.SECONDS;
        } else if((temp = pixelForSecond * 60) > minutesSize) {
            return ChronoUnit.MINUTES;
        } else if((temp = temp * 60) > hoursSize) {
            return ChronoUnit.HOURS;
        } else if((temp = temp * 24) > daysSize) {
            return ChronoUnit.DAYS;
        } else if(temp * 30 > monthsSize) {
            return ChronoUnit.MONTHS;
        } else {
            return ChronoUnit.YEARS;
        }
    }

    private void drawTimeIntervals(GraphicsContext gc, RenderingContext rc, boolean foreground) {
        for(TimeInterval tc : this.timeIntervals) {
            if(tc.isForeground() == foreground && inViewport(tc.getStartTime(), tc.getEndTime())) {
                tc.render(gc, rc);
            }
        }
    }

    private void drawCursors(GraphicsContext gc, RenderingContext rc) {
        for(TimeCursor tc : this.timeCursors) {
            if(inViewport(tc.getTime())) {
                tc.render(gc, rc);
            }
        }
        gc.setLineWidth(1);
        gc.setLineDashes();
    }

    private void drawBackground(GraphicsContext gc) {
        gc.clearRect(0, 0, this.imageArea.getWidth(), this.imageArea.getHeight());
        gc.setFill(getBackgroundColor());
        gc.fillRect(0, 0, this.imageArea.getWidth(), this.imageArea.getHeight());
    }

    private void drawTaskLines(GraphicsContext gc, RenderingContext rc) {
        // You render only line blocks that are contained in the viewport, as defined by the vertical scroll value
        double yStart = this.verticalScroll.getValue();
        double yEnd = yStart + this.imageArea.getHeight() - this.headerRowHeight;
        int processedLines = 0;
        //
        int startLine = -1;
        int endLine = -1;
        int i = 0;
        for(ITaskLine line : this.items) {
            // Compute the Y span of the rendering for this task line
            double taskLineYStart = processedLines * this.lineRowHeight;
            processedLines += line.getNbOfLines();
            double taskLineYEnd = processedLines * this.lineRowHeight;
            if((taskLineYStart > yStart && taskLineYStart < yEnd) ||
                    (taskLineYEnd > yStart && taskLineYEnd < yEnd) ||
                    (taskLineYStart < yStart && taskLineYEnd > yEnd)) {
                // Render: translate the taskLineYStart in the right viewport coordinates
                taskLineYStart -= yStart;
                taskLineYStart += this.headerRowHeight;
                // Ask the rendering of the timeline
                line.render(gc, 0, taskLineYStart, rc);
                // Remember at this level what you rendered
                endLine = i;
                if(startLine == -1) {
                    startLine = i;
                }
            } else {
                // Inform that this taskline is not rendered
                line.noRender();
            }
            ++i;
        }
        // Rendering completed
        this.currentYViewportItems = new int[] { startLine, endLine };
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
            Instant endTime = null;
            if(this.headerElement == ChronoUnit.YEARS || this.headerElement == ChronoUnit.MONTHS) {
                endTime = startTime.atOffset(ZoneOffset.UTC).plus(1, this.headerElement).toInstant();
            } else {
                endTime = startTime.plus(1, this.headerElement);
            }
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
            case SECONDS: return String.format("%02d:%02d:%02d", time.getHour(), time.getMinute(), time.getSecond());
            case MINUTES: return String.format("%02d:%02d", time.getHour(), time.getMinute());
            case HOURS: return String.format("%02d", time.getHour());
            case DAYS: return String.format("%04d-%02d-%02d", time.getYear(), time.get(ChronoField.MONTH_OF_YEAR) + 1, time.getDayOfMonth());
            case MONTHS: return String.format("%04d-%02d", time.getYear(), time.get(ChronoField.MONTH_OF_YEAR) + 1);
            default: return String.format("%04d", time.getYear());
        }
    }

    private Instant getAdjustedStartTime(Instant viewPortStart, ChronoUnit headerElement) {
        switch (headerElement) {
            case YEARS: {
                // Truncate to day
                Instant toReturn = viewPortStart.truncatedTo(ChronoUnit.DAYS);
                // Count the days to the beginning of the month
                LocalDateTime ldt = LocalDateTime.ofInstant(toReturn, ZoneId.of("UTC"));
                int days = ldt.getDayOfYear();
                // Subtract these
                return ldt.minus(days, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC);
            }
            case MONTHS: {
                // Truncate to day
                Instant toReturn = viewPortStart.truncatedTo(ChronoUnit.DAYS);
                // Count the days to the beginning of the month
                LocalDateTime ldt = LocalDateTime.ofInstant(toReturn, ZoneId.of("UTC"));
                int days = ldt.getDayOfMonth();
                // Subtract these
                return ldt.minus(days, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC);
            }
            default:
                return viewPortStart.truncatedTo(headerElement);
        }
    }

    private void measureFontHeight(Font font) {
        if(textHeight == -1) {
            Text text = new Text("Ig");
            text.setBoundsType(TextBoundsType.VISUAL);
            text.setFont(font);
            textHeight = text.getBoundsInLocal().getHeight();
            headerRowHeight = textHeight + 2 * TEXT_PADDING;
            lineRowHeight = textHeight + 6 * TEXT_PADDING;
        }
    }
}
