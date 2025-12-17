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

import eu.dariolucia.jfx.timeline.model.TimelineSingleSelectionModel;
import eu.dariolucia.jfx.timeline.model.*;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionModel;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A timeline is a graphical JavaFX component that can be used to display tasks and groups of tasks, time cursors
 * and time intervals on a timeline. Each element is individually customizable in terms of rendering via standard JavaFX
 * properties and via subclassing for more personalised rendering.
 */
public class Timeline extends GridPane implements IRenderingContext {

    /* *****************************************************************************************
     * Constants
     * *****************************************************************************************/
    private static final double TEXT_PADDING_DEFAULT = 5;
    private static final double TASK_PANEL_WIDTH_DEFAULT = 100;
    private static final double ADDITIONAL_PANEL_WIDTH_DEFAULT = 0;

    /* *****************************************************************************************
     * JavaFX elements
     * *****************************************************************************************/
    private final Canvas imageArea;
    private final ScrollBar horizontalScroll;
    private final ScrollBar verticalScroll;
    private final Label labelCornerFiller;
    private final SelectionModel<TaskItem> selectionModel;

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/

    /**
     * Minimum time of the timeline, mapped to the horizontal scrollbar value min value (0).
     */
    private final SimpleObjectProperty<Instant> minTime = new SimpleObjectProperty<>(Instant.now());
    /**
     * Maximum time of the timeline, used to compute the horizontal scrollbar max value (max value - min value, in seconds).
     */
    private final SimpleObjectProperty<Instant> maxTime = new SimpleObjectProperty<>(Instant.now().plusSeconds(3600));
    /**
     * Duration of the viewport, in seconds.
     */
    private final SimpleLongProperty viewPortDuration = new SimpleLongProperty(600);
    /**
     * Start time of the viewport.
     */
    private final SimpleObjectProperty<Instant> viewPortStart = new SimpleObjectProperty<>(minTime.get());
    /**
     * Read-only property. End time of the viewport, computed as viewPortStart + viewPortDuration.
     */
    private final SimpleObjectProperty<Instant> viewPortEnd = new SimpleObjectProperty<>();
    /**
     * Width of the task panel (left), in pixels.
     */
    private final SimpleDoubleProperty taskPanelWidth = new SimpleDoubleProperty(TASK_PANEL_WIDTH_DEFAULT);
    /**
     * Width of the additional panel (right), in pixels.
     */
    private final SimpleDoubleProperty additionalPanelWidth = new SimpleDoubleProperty(ADDITIONAL_PANEL_WIDTH_DEFAULT);
    /**
     * Distance of the text from the border of the enclosing graphical item, in pixels.
     */
    private final SimpleDoubleProperty textPadding = new SimpleDoubleProperty(TEXT_PADDING_DEFAULT);
    /**
     * Timeline background color.
     */
    private final SimpleObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.WHITE);
    /**
     * Timeline header (top) background (color, pattern, gradient).
     */
    private final SimpleObjectProperty<Paint> headerBackground = new SimpleObjectProperty<>(Color.LIGHTGRAY);
    /**
     * Timeline header (top) foreground color.
     */
    private final SimpleObjectProperty<Color> headerForegroundColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * Timeline header (top) border color.
     */
    private final SimpleObjectProperty<Color> headerBorderColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * Timeline task panel (left) background (color, pattern, gradient).
     */
    private final SimpleObjectProperty<Paint> panelBackground = new SimpleObjectProperty<>(Color.LIGHTGRAY);
    /**
     * Timeline task panel (left) foreground color.
     */
    private final SimpleObjectProperty<Color> panelForegroundColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * Timeline task panel (left) border color.
     */
    private final SimpleObjectProperty<Color> panelBorderColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * Border color of the selected task item.
     */
    private final SimpleObjectProperty<Color> selectBorderColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * Effect to be applied to the selected task item.
     */
    private final SimpleObjectProperty<Effect> selectBorderEffect = new SimpleObjectProperty<>(null);
    /**
     * Width of the border of the selected task item, in pixels.
     */
    private final SimpleDoubleProperty selectBorderWidth = new SimpleDoubleProperty(3.0);
    /**
     * Border color of the task items.
     */
    private final SimpleObjectProperty<Color> taskBorderColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * Font to be used for string rendering in the timeline widget.
     */
    private final SimpleObjectProperty<Font> textFont = new SimpleObjectProperty<>(null);
    /**
     * Visibility of the horizontal scrollbar.
     */
    private final SimpleBooleanProperty horizontalScrollbarVisible = new SimpleBooleanProperty();
    /**
     * Visibility of the vertical scrollbar.
     */
    private final SimpleBooleanProperty verticalScrollbarVisible = new SimpleBooleanProperty();
    /**
     * List of {@link ITaskLine} in the timeline.
     */
    private final ObservableList<ITaskLine> items = FXCollections.observableArrayList(ITaskLine::getObservableProperties);
    /**
     * List of {@link TimeCursor} in the timeline.
     */
    private final ObservableList<TimeCursor> timeCursors = FXCollections.observableArrayList(timeCursor -> new Observable[] {
            timeCursor.colorProperty(), timeCursor.timeProperty() });
    /**
     * List of {@link TimeInterval} in the timeline.
     */
    private final ObservableList<TimeInterval> timeIntervals = FXCollections.observableArrayList(TimeInterval::getObservableProperties);
    /**
     * If true, mouse is allowed to selected/deselect a {@link TaskItem} by clicking on it.
     */
    private final SimpleBooleanProperty enableMouseSelection = new SimpleBooleanProperty(true);
    /**
     * If true, mouse is allowed to scroll lines vertically using the mouse wheel.
     */
    private final SimpleBooleanProperty enableMouseScroll = new SimpleBooleanProperty(true);
    /**
     * If true, mouse is allowed to zoom the timeline using Ctrl + the mouse wheel.
     */
    private final SimpleBooleanProperty enableZoomMouseScroll = new SimpleBooleanProperty(true);
    /**
     * If true, vertical lines based on the header time indicators are drawn.
     */
    private final SimpleBooleanProperty enableVerticalLines = new SimpleBooleanProperty(true);
    /**
     * If true, task lines are rendered with alternate background.
     */
    private final SimpleBooleanProperty enableAlternateColorLines = new SimpleBooleanProperty(true);
    /**
     * If true, task line headers for {@link HierarchicalGroupTaskLine} and collapsed {@link FlatGroupTaskLine} are rendered
     * with a highlighted color (derived from the background color).
     */
    private final SimpleBooleanProperty highlightLine = new SimpleBooleanProperty(true);
    /**
     * If true, the horizontal scrollbar fit in viewport.
     */
    private final SimpleBooleanProperty horizontalScrollbarFitInViewPort = new SimpleBooleanProperty();
    /**
     * Color of the task item projection on the task line headers.
     */
    private final SimpleObjectProperty<Color> taskProjectionBackgroundColor = new SimpleObjectProperty<>(Color.BURLYWOOD);
    /**
     * Hint to select when task item projection on task line headers shall be rendered.
     */
    private final SimpleObjectProperty<TaskItemProjection> taskProjectionHint = new SimpleObjectProperty<>(TaskItemProjection.COLLAPSE);
    /**
     * Tooltip for displaying
     */
    private TimeTooltip showedTooltip = null;
    /**
     * The X coordinate of the cursor
     */
    private double cursorX = 0.0;
    /**
     * The Y coordinate of the cursor
     */
    private double cursorY = 0.0;
    /**
     * Contains a set of date formats for each data type displayed in the header
     */
    private final HashMap<ChronoUnit, DateTimeFormatter> DateFormat = new HashMap<>();
    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/
    private int textHeight = -1;
    private int headerRowHeight = -1;
    private int lineRowHeight = -1;
    private ChronoUnit headerElement = ChronoUnit.YEARS;
    private int[] currentYViewportItems;
    private List<TaskItem> flatTaskItem;

    /**
     * Class constructor.
     */
    public Timeline() {
        // Set grid layout constraints
        ColumnConstraints firstColConstraint = new ColumnConstraints();
        firstColConstraint.setHgrow(Priority.ALWAYS);
        ColumnConstraints secondColConstraint = new ColumnConstraints();
        secondColConstraint.setHgrow(Priority.NEVER);
        RowConstraints firstRowConstraint = new RowConstraints();
        firstRowConstraint.setVgrow(Priority.ALWAYS);
        RowConstraints secondRowConstraint = new RowConstraints();
        secondRowConstraint.setVgrow(Priority.NEVER);
        getColumnConstraints().addAll(firstColConstraint, secondColConstraint);
        getRowConstraints().addAll(firstRowConstraint, secondRowConstraint);
        // Set default date format
        this.DateFormat.put(ChronoUnit.SECONDS, DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.DateFormat.put(ChronoUnit.MINUTES, DateTimeFormatter.ofPattern("HH:mm:00"));
        this.DateFormat.put(ChronoUnit.HOURS, DateTimeFormatter.ofPattern("HH:00:00"));
        this.DateFormat.put(ChronoUnit.DAYS, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.DateFormat.put(ChronoUnit.MONTHS, DateTimeFormatter.ofPattern("yyyy-MM"));
        this.DateFormat.put(ChronoUnit.YEARS, DateTimeFormatter.ofPattern("yyyy"));
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
        this.labelCornerFiller = new Label("");
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
        taskPanelWidthProperty().addListener(panelChangeWidth);
        // Add listener when additional panel width is updated
        additionalPanelWidthProperty().addListener(panelChangeWidth);
        // Add listener when colors are updated
        backgroundColorProperty().addListener((e,o,n) -> internalRefresh());
        headerBackgroundProperty().addListener((e, o, n) -> internalRefresh());
        headerForegroundColorProperty().addListener((e, o, n) -> internalRefresh());
        panelBackgroundProperty().addListener((e, o, n) -> internalRefresh());
        panelForegroundColorProperty().addListener((e, o, n) -> internalRefresh());
        panelBorderColorProperty().addListener((e, o, n) -> internalRefresh());
        taskBorderColorProperty().addListener((e, o, n) -> internalRefresh());
        headerBorderColorProperty().addListener((e, o, n) -> internalRefresh());
        enableVerticalLinesProperty().addListener((e, o, n) -> internalRefresh());
        selectBorderColorProperty().addListener((e, o, n) -> internalRefresh());
        selectBorderWidthProperty().addListener((e,v,n) -> internalRefresh());
        selectBorderEffectProperty().addListener((e,v,n) -> internalRefresh());
        enableAlternateColorLinesProperty().addListener((e,v,n) -> internalRefresh());
        highlightLineProperty().addListener((e,v,n) -> internalRefresh());
        taskProjectionBackgroundColorProperty().addListener((e, v, n) -> internalRefresh());
        taskProjectionHintProperty().addListener((e,v,n) -> internalRefresh());
        textFontProperty().addListener((e,v,n) -> textSettingsUpdated());
        textPaddingProperty().addListener((e,v,n) -> textSettingsUpdated());
        // Add listener when scrollbar visible is updated
        horizontalScrollbarVisibleProperty().addListener((e,o,n) -> horizontalScrollbarStatusChanged());
        verticalScrollbarVisibleProperty().addListener((e,o,n) -> verticalScrollbarStatusChanged());
        horizontalScrollbarFitInViewPortProperty().addListener((e, o, n) -> updateHorizontalScrollbarFitInViewPort());

        // Add listener to changes to the observable list structure (add, remove)
        this.items.addListener(this::itemsUpdated);
        // Add listener to changes to the observable list of time cursors
        this.timeCursors.addListener(this::timeCursorsUpdated);
        // Add listener to changes to the observable list of time intervals
        this.timeIntervals.addListener(this::timeIntervalsUpdated);

        // Create the selection model
        this.selectionModel = new TimelineSingleSelectionModel(this);
        this.selectionModel.selectedItemProperty().addListener((e,o,n) -> internalRefresh());

        // Add listener for task item selection
        this.imageArea.addEventHandler(MouseEvent.MOUSE_CLICKED, this::mouseClickedAction);
        // Add listener for tooltip cursor position update
        this.imageArea.addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoveAction);
        this.imageArea.addEventHandler(ScrollEvent.ANY, this::mouseScrolledAction);

        // Perform initial drawing
        Platform.runLater(this::recomputeArea);
    }

    /**
     * This method returns the vertical scrollbar if visible, otherwise null.
     * @return the vertical {@link ScrollBar} if visible, otherwise null.
     */
    public ScrollBar getVerticalScroll() {
        if(isVerticalScrollbarVisible()) {
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
        if(isHorizontalScrollbarVisible()) {
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
     * This method puts the provided {@link ITaskLine} (or recursively its parent, if this is not the top level {@link ITaskLine})
     * in the viewport (at the highest possible position).
     * @param taskLine the task line to be put in the viewport
     */
    public void scrollTo(ITaskLine taskLine) {
        while(taskLine.getParent() != null) {
            taskLine = (ITaskLine) taskLine.getParent();
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
     * This method maps an X value in {@link Canvas} coordinates to the related {@link Instant} time. X can be outside the
     * rendering area.
     * @param x the X coordinate to convert
     * @return the instant mapped to that X
     */
    public Instant toInstant(double x) {
        // x indicates the pixel in canvas coordinates: first of all, we need to remove the task panel size
        x -= getTaskPanelWidth();
        // Now compute the percentage
        double percentage = x / getViewPortWidth();
        // Find out the instant from the viewport start
        return getViewPortStart().plusSeconds(Math.round(percentage * getViewPortDuration()));
    }

    /**
     * This method maps an {@link Instant} to the related X in {@link Canvas} coordinates.
     * @param time the instant to convert
     * @return the X mapped to the instant
     */
    @Override
    public double toX(Instant time) {
        // The X can be off map
        long timeSecs = time.getEpochSecond();
        long startTimeSecs = getViewPortStart().getEpochSecond();
        // Now check where timeSecs linearly is
        double percentage = (timeSecs - startTimeSecs)/ (double) getViewPortDuration();
        // Now translate to pixels
        return (percentage * getViewPortWidth()) + getTaskPanelWidth();
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

    /**
     * Return the {@link ITaskLine} under the coordinates x,y in Canvas coordinate space.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the {@link ITaskLine} at the given coordinates, or null
     */
    public ITaskLine getTaskLineAt(double x, double y) {
        for (ITaskLine tl : this.items) {
            if (tl.contains(x, y)) {
                return tl;
            }
        }
        return null;
    }

    /**
     * Return the {@link TaskItem} under the coordinates x,y in Canvas coordinate space.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the {@link TaskItem} at the given coordinates, or null
     */
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

    /**
     * <h1>This function allows you to set the type of date formatting for each type of time unit.</h1>
     * <ul>Default values:
     *     <li>{@link ChronoUnit}.SECONDS = "HH:mm:ss"</li>
     *     <li>{@link ChronoUnit}.MINUTES = "HH:mm:00"</li>
     *     <li>{@link ChronoUnit}.HOURS = "HH:00:00"</li>
     *     <li>{@link ChronoUnit}.DAYS = "yyyy-MM-dd"</li>
     *     <li>{@link ChronoUnit}.MONTHS = "yyyy-MM"</li>
     *     <li>{@link ChronoUnit}.YEARS = "yyyy"</li>
     * </ul>
     * <p><h1>P.S</h1>When the displayed unit of time is: {@link ChronoUnit}.HOURS, {@link ChronoUnit}.MINUTES or {@link ChronoUnit}.SECONDS.
     * The date will be displayed in two lines, where the {@link ChronoUnit}.DAYS format will be used for the first line,
     * and one of the above format will be used for the second line.</p>
     * @param unit The {@link ChronoUnit} for which the format is set
     * @param format The format for displaying the date
     */
    public void setDateFormat(ChronoUnit unit, DateTimeFormatter format)
    {
        if(DateFormat.containsKey(unit)) DateFormat.replace(unit, format);
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

    @Override
    public Instant getViewPortStart() {
        return viewPortStart.get();
    }

    @Override
    public Instant getViewPortEnd() {
        return viewPortEnd.get();
    }

    @Override
    public Set<TaskItem> getSelectedTaskItems() {
        return Collections.singleton(this.selectionModel.getSelectedItem());
    }

    @Override
    public int getImageAreaHeight() {
        return (int) this.imageArea.getHeight();
    }

    @Override
    public int getImageAreaWidth() {
        return (int) this.imageArea.getWidth();
    }

    @Override
    public double getViewPortStartX() {
        return getTaskPanelWidth();
    }

    @Override
    public double getViewPortEndX() {
        return getImageAreaWidth() - getAdditionalPanelWidth();
    }

    @Override
    public double getViewPortEndY() {
        return getImageAreaHeight();
    }

    @Override
    public int getHeaderRowHeight() {
        return this.headerRowHeight;
    }

    public ReadOnlyObjectProperty<Instant> viewPortEndProperty() {
        return viewPortEnd;
    }

    public SimpleObjectProperty<Instant> viewPortStartProperty() {
        return viewPortStart;
    }

    public void setViewPortStart(Instant viewPortStart) {
        this.viewPortStart.set(viewPortStart);
    }

    /**
     * Return the top level {@link ITaskLine} in the timeline.
     * @return the top level {@link ITaskLine} in the timeline
     */
    public ObservableList<ITaskLine> getItems() {
        return items;
    }

    /**
     * Return the {@link TimeCursor} objects in the timeline.
     * @return the {@link TimeCursor} objects in the timeline
     */
    public ObservableList<TimeCursor> getTimeCursors() {
        return timeCursors;
    }

    /**
     * Return the {@link TimeInterval} objects in the timeline.
     * @return the {@link TimeInterval} objects in the timeline
     */
    public ObservableList<TimeInterval> getTimeIntervals() {
        return timeIntervals;
    }

    @Override
    public double getTaskPanelWidth() {
        return taskPanelWidth.get();
    }

    @Override
    public double getAdditionalPanelWidth() {
        return additionalPanelWidth.get();
    }

    @Override
    public int getTextHeight() {
        return this.textHeight;
    }

    @Override
    public int getLineRowHeight() {
        return this.lineRowHeight;
    }

    public SimpleDoubleProperty taskPanelWidthProperty() {
        return taskPanelWidth;
    }

    public void setTaskPanelWidth(double taskPanelWidth) {
        this.taskPanelWidth.set(taskPanelWidth);
    }

    public SimpleDoubleProperty additionalPanelWidthProperty() {
        return additionalPanelWidth;
    }

    public void setAdditionalPanelWidth(double additionalPanelWidth) {
        this.additionalPanelWidth.set(additionalPanelWidth);
    }

    /**
     * Navigate the registered task lines and return the flat list of {@link TaskItem} objects.
     * @return the {@link TaskItem} objects of this timeline
     */
    public List<TaskItem> getTaskItemList() {
        if(this.flatTaskItem == null) {
            this.flatTaskItem = this.items.stream().flatMap(l -> l.getTaskItems().stream()).collect(Collectors.toList());
        }
        return this.flatTaskItem;
    }

    /**
     * Return the selection model.
     * @return the selection model
     */
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

    @Override
    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public SimpleObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public boolean isHorizontalScrollbarVisible() {
        return horizontalScrollbarVisible.get();
    }

    public SimpleBooleanProperty horizontalScrollbarVisibleProperty() {
        return horizontalScrollbarVisible;
    }

    public void setHorizontalScrollbarVisible(boolean scrollbarsVisible) {
        this.horizontalScrollbarVisible.set(scrollbarsVisible);
    }

    public boolean isVerticalScrollbarVisible() {
        return verticalScrollbarVisible.get();
    }

    public SimpleBooleanProperty verticalScrollbarVisibleProperty() {
        return verticalScrollbarVisible;
    }

    public void setVerticalScrollbarVisible(boolean verticalScrollbarVisible) {
        this.verticalScrollbarVisible.set(verticalScrollbarVisible);
    }

    public Paint getHeaderBackground() {
        return headerBackground.get();
    }

    public SimpleObjectProperty<Paint> headerBackgroundProperty() {
        return headerBackground;
    }

    public void setHeaderBackground(Paint headerBackground) {
        this.headerBackground.set(headerBackground);
    }

    public Color getHeaderForegroundColor() {
        return headerForegroundColor.get();
    }

    public SimpleObjectProperty<Color> headerForegroundColorProperty() {
        return headerForegroundColor;
    }

    public void setHeaderForegroundColor(Color headerForegroundColor) {
        this.headerForegroundColor.set(headerForegroundColor);
    }

    @Override
    public Paint getPanelBackground() {
        return panelBackground.get();
    }

    public SimpleObjectProperty<Paint> panelBackgroundProperty() {
        return panelBackground;
    }

    public void setPanelBackground(Paint panelBackground) {
        this.panelBackground.set(panelBackground);
    }

    @Override
    public Color getPanelForegroundColor() {
        return panelForegroundColor.get();
    }

    public SimpleObjectProperty<Color> panelForegroundColorProperty() {
        return panelForegroundColor;
    }

    public void setPanelForegroundColor(Color panelForegroundColor) {
        this.panelForegroundColor.set(panelForegroundColor);
    }

    @Override
    public Color getPanelBorderColor() {
        return panelBorderColor.get();
    }

    @Override
    public boolean isInViewPort(Instant start, Instant end) {
        return (start.isAfter(getViewPortStart()) && start.isBefore(getViewPortEnd())) || (
                end.isAfter(getViewPortStart()) && end.isBefore(getViewPortEnd())) ||
                (start.isBefore(getViewPortStart()) && end.isAfter(getViewPortEnd()));
    }

    public SimpleObjectProperty<Color> panelBorderColorProperty() {
        return panelBorderColor;
    }

    public void setPanelBorderColor(Color panelBorderColor) {
        this.panelBorderColor.set(panelBorderColor);
    }

    public Color getHeaderBorderColor() {
        return headerBorderColor.get();
    }

    public SimpleObjectProperty<Color> headerBorderColorProperty() {
        return headerBorderColor;
    }

    public void setHeaderBorderColor(Color headerBorderColor) {
        this.headerBorderColor.set(headerBorderColor);
    }

    @Override
    public Color getSelectBorderColor() {
        return selectBorderColor.get();
    }

    public SimpleObjectProperty<Color> selectBorderColorProperty() {
        return selectBorderColor;
    }

    public void setSelectBorderColor(Color selectBorderColor) {
        this.selectBorderColor.set(selectBorderColor);
    }

    @Override
    public Effect getSelectBorderEffect() {
        return selectBorderEffect.get();
    }

    public SimpleObjectProperty<Effect> selectBorderEffectProperty() {
        return selectBorderEffect;
    }

    public void setSelectBorderEffect(Effect selectBorderEffect) {
        this.selectBorderEffect.set(selectBorderEffect);
    }

    @Override
    public double getSelectBorderWidth() {
        return selectBorderWidth.get();
    }

    public SimpleDoubleProperty selectBorderWidthProperty() {
        return selectBorderWidth;
    }

    public void setSelectBorderWidth(double selectBorderWidth) {
        this.selectBorderWidth.set(selectBorderWidth);
    }

    @Override
    public Font getTextFont() {
        return textFont.get();
    }

    public SimpleObjectProperty<Font> textFontProperty() {
        return textFont;
    }

    public void setTextFont(Font textFont) {
        this.textFont.set(textFont);
    }

    public boolean isEnableMouseScroll() {
        return enableMouseScroll.get();
    }

    public SimpleBooleanProperty enableMouseScrollProperty() {
        return enableMouseScroll;
    }

    public void setEnableMouseScroll(boolean enableMouseScroll) {
        this.enableMouseScroll.set(enableMouseScroll);
    }

    @Override
    public double getTextPadding() {
        return textPadding.get();
    }

    public SimpleDoubleProperty textPaddingProperty() {
        return textPadding;
    }

    public void setTextPadding(double textPadding) {
        this.textPadding.set(textPadding);
    }

    public boolean isEnableZoomMouseScroll() {
        return enableZoomMouseScroll.get();
    }

    public SimpleBooleanProperty enableZoomMouseScrollProperty() {
        return enableZoomMouseScroll;
    }

    public void setEnableZoomMouseScroll(boolean enableZoomMouseScroll) {
        this.enableZoomMouseScroll.set(enableZoomMouseScroll);
    }

    public boolean isEnableVerticalLines() {
        return enableVerticalLines.get();
    }

    public SimpleBooleanProperty enableVerticalLinesProperty() {
        return enableVerticalLines;
    }

    public void setEnableVerticalLines(boolean enableVerticalLines) {
        this.enableVerticalLines.set(enableVerticalLines);
    }

    public boolean isEnableAlternateColorLines() {
        return enableAlternateColorLines.get();
    }

    public SimpleBooleanProperty enableAlternateColorLinesProperty() {
        return enableAlternateColorLines;
    }

    public void setEnableAlternateColorLines(boolean enableAlternateColorLines) {
        this.enableAlternateColorLines.set(enableAlternateColorLines);
    }

    @Override
    public boolean isHighlightLine() {
        return highlightLine.get();
    }

    public SimpleBooleanProperty highlightLineProperty() {
        return highlightLine;
    }

    public void setHighlightLine(boolean highlightLine) {
        this.highlightLine.set(highlightLine);
    }

    public boolean isHorizontalScrollbarFitInViewPort() {
        return horizontalScrollbarFitInViewPort.get();
    }

    public SimpleBooleanProperty horizontalScrollbarFitInViewPortProperty() {
        return horizontalScrollbarFitInViewPort;
    }

    public void setHorizontalScrollbarFitInViewPort(boolean horizontalScrollbarFitInViewPort) {
        this.horizontalScrollbarFitInViewPort.set(horizontalScrollbarFitInViewPort);
    }

    @Override
    public TaskItemProjection getTaskProjectionHint() {
        return taskProjectionHint.get();
    }

    public SimpleObjectProperty<TaskItemProjection> taskProjectionHintProperty() {
        return taskProjectionHint;
    }

    public void setTaskProjectionHint(TaskItemProjection taskProjectionHint) {
        this.taskProjectionHint.set(taskProjectionHint);
    }

    @Override
    public Color getTaskProjectionBackgroundColor() {
        return taskProjectionBackgroundColor.get();
    }

    public SimpleObjectProperty<Color> taskProjectionBackgroundColorProperty() {
        return taskProjectionBackgroundColor;
    }

    public void setTaskProjectionBackgroundColor(Color taskProjectionBackgroundColor) {
        this.taskProjectionBackgroundColor.set(taskProjectionBackgroundColor);
    }

    @Override
    public Color getTaskBorderColor() {
        return taskBorderColor.get();
    }

    public SimpleObjectProperty<Color> taskBorderColorProperty() {
        return taskBorderColor;
    }

    public void setTaskBorderColor(Color taskBorderColor) {
        this.taskBorderColor.set(taskBorderColor);
    }

    /* *****************************************************************************************
     * Utility access method
     * *****************************************************************************************/

    /**
     * Return the {@link TaskItem} at the specified position.
     * @param i the i-th {@link TaskItem} to retrieve
     * @return the {@link TaskItem} at i-th position
     */
    public TaskItem getTaskItemAt(int i) {
        return getTaskItemList().get(i);
    }

    /**
     * Return the number of {@link TaskItem} in this timeline.
     * @return the number of {@link TaskItem} in this timeline
     */
    public long getTaskItemCount() {
        return getTaskItemList().size();
    }

    /**
     * Make a snapshot image of the current {@link Timeline} rendering status in the Canvas area.
     * @return the image of the {@link Timeline}
     */
    public Image print() {
        SnapshotParameters parameters = new SnapshotParameters();
        final WritableImage snapshot = new WritableImage(getImageAreaWidth(), getImageAreaHeight());
        this.imageArea.snapshot(parameters, snapshot);
        return snapshot;
    }

    /* *****************************************************************************************
     * Internal methods
     * *****************************************************************************************/

    private void textSettingsUpdated() {
        textHeight = -1;
        recomputeArea();
    }

    private void horizontalScrollbarStatusChanged() {
        if(this.horizontalScroll.getParent() == null && isHorizontalScrollbarVisible()) {
            addHScrollbar();
        } else if(this.horizontalScroll.getParent() != null && !isHorizontalScrollbarVisible()) {
            removeHScrollbar();
        }
        // At the end, recompute area
        recomputeArea();
    }

    private void addHScrollbar() {
        getChildren().add(this.horizontalScroll);
        GridPane.setRowIndex(this.horizontalScroll, 1);
        GridPane.setColumnIndex(this.horizontalScroll, 0);
        if(isVerticalScrollbarVisible()) {
            getChildren().add(labelCornerFiller);
            GridPane.setRowIndex(labelCornerFiller, 1);
            GridPane.setColumnIndex(labelCornerFiller, 1);
        }
    }

    private void removeHScrollbar() {
        getChildren().remove(this.horizontalScroll);
        getChildren().remove(this.labelCornerFiller);
    }

    private void updateHorizontalScrollbarFitInViewPort() {
        if (isHorizontalScrollbarFitInViewPort()) {
            GridPane.setMargin(this.horizontalScroll, new Insets(0, getAdditionalPanelWidth(), 0, getTaskPanelWidth()));
        } else {
            GridPane.setMargin(this.horizontalScroll, new Insets(0, 0, 0, 0));
        }
    }

    private void verticalScrollbarStatusChanged() {
        if(this.verticalScroll.getParent() == null && isVerticalScrollbarVisible()) {
            addVScrollbar();
        } else if(this.verticalScroll.getParent() != null && !isVerticalScrollbarVisible()) {
            removeVScrollbar();
        }
        // At the end, recompute area
        recomputeArea();
    }

    private void addVScrollbar() {
        getChildren().add(this.verticalScroll);
        GridPane.setRowIndex(this.verticalScroll, 0);
        GridPane.setColumnIndex(this.verticalScroll, 1);
        if(isHorizontalScrollbarVisible()) {
            getChildren().add(labelCornerFiller);
            GridPane.setRowIndex(labelCornerFiller, 1);
            GridPane.setColumnIndex(labelCornerFiller, 1);
        }
    }

    private void removeVScrollbar() {
        getChildren().remove(this.verticalScroll);
        getChildren().remove(this.labelCornerFiller);
    }

    private void mouseScrolledAction(ScrollEvent scrollEvent) {
        if (scrollEvent.isControlDown()) {
            if(isEnableZoomMouseScroll()) {
                if (scrollEvent.getDeltaY() < 0) {
                    setViewPortDuration((long) (getViewPortDuration() + getViewPortDuration() / 10.0));
                } else {
                    setViewPortDuration(Math.max(10, (long) (getViewPortDuration() - getViewPortDuration() / 10.0)));
                }
            }
        } else if(isEnableMouseScroll()) {
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

    //[TaskLines]->[TaskItems]->[TimePoints]->Tooltip
    private TimeTooltip getTooltipInTimePoints()
    {
        TimePoint point = getItems().stream()
                .filter(taskLine -> taskLine.contains(cursorX, cursorY) && !taskLine.getTaskItems().isEmpty())
                .flatMap(taskLine -> taskLine.getTaskItems().stream())
                .filter(taskItem -> taskItem.contains(cursorX, cursorY) && !taskItem.getTimePoints().isEmpty())
                .flatMap(taskItem -> taskItem.getTimePoints().stream())
                .filter(timePoint -> timePoint.contains(this.cursorX, this.cursorY) && timePoint.getTooltip() != null)
                .findFirst().orElse(null);

        if(point != null) return point.getTooltip();

        return null;
    }

    //[Timeline]->[TimeInterval]->Tooltip
    //[TaskLines]->[TimeInterval]->Tooltip
    //[TaskLines]->[TaskItems]->[TimeInterval]->Tooltip
    private TimeTooltip getTooltipInTimeInterval()
    {
        //Global intervals on timeline
        TimeInterval interval = getTimeIntervals().stream()
                .filter(timeInterval -> timeInterval.contains(cursorX, cursorY) && timeInterval.getTooltip() != null)
                .findFirst().orElse(null);

        //intervals on task lines and task items
        if(interval == null)
        {
            interval = getItems().stream()
                    .filter(taskLine -> taskLine.contains(cursorX, cursorY) && !taskLine.getAllLineInterval().isEmpty())
                    .flatMap(taskLine -> taskLine.getAllLineInterval().stream())
                    .filter(timeInterval -> timeInterval.contains(cursorX, cursorY) && timeInterval.getTooltip() != null)
                    .findFirst().orElse(null);
        }

        if(interval != null) return interval.getTooltip();

        return null;
    }

    //[TaskLines]->[TaskItems]->Tooltip
    private TimeTooltip getTooltipInTaskItem()
    {
        TaskItem item = getItems().stream()
                .filter(taskLine -> taskLine.contains(cursorX, cursorY) && !taskLine.getTaskItems().isEmpty())
                .flatMap(taskLine -> taskLine.getTaskItems().stream())
                .filter(taskItem -> taskItem.contains(cursorX, cursorY) && taskItem.getTooltip() != null)
                .findFirst().orElse(null);

        if(item != null) return item.getTooltip();

        return null;
    }

    private void mouseMoveAction(MouseEvent mouseEvent)
    {
        cursorX = mouseEvent.getX();
        cursorY = mouseEvent.getY();

        //refreshNeeded used for remove of last tooltip
        boolean refreshNeeded = showedTooltip != null;
        showedTooltip = null;

        //[First priority level]
        //Looking for priority tooltips for time point
        //We display them even if they are under the time interval with another tooltip
        showedTooltip = getTooltipInTimePoints();

        //[Second priority level]
        //If couldn't find the tooltip in task items, go ahead and search for the tooltip in all time intervals.
        if(showedTooltip == null) showedTooltip = getTooltipInTimeInterval();

        //[Third priority level]
        //If the tooltip is still not found, proceed to search for the tooltip inside the task items.
        if(showedTooltip == null) showedTooltip = getTooltipInTaskItem();

        if(showedTooltip != null || refreshNeeded) internalRefresh();
    }

    private void mouseClickedAction(MouseEvent mouseEvent) {
        // Get X,Y coordinates and check which task item is affected
        TaskItem selectedTaskItem = getTaskItemAt(mouseEvent.getX(), mouseEvent.getY());
        // Update selection
        if(isEnableMouseSelection() && !Objects.equals(selectedTaskItem, this.selectionModel.getSelectedItem())) {
            this.selectionModel.select(selectedTaskItem);
        }
        // Check if anything must be done at the level of task lines
        for(ITaskLine tl : getItems()) {
            if(tl.isRendered() && tl.contains(mouseEvent.getX(), mouseEvent.getY())) {
                tl.notifyEvent(mouseEvent, mouseEvent.getX(), mouseEvent.getY());
                // It can only go in one task line
                break;
            }
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
            int fullSize = totalLines * this.lineRowHeight;
            double totalSize = fullSize - getImageAreaHeight() + this.headerRowHeight;
            if(totalSize < 0) {
                // There is no need of a scrollbar in this case
                this.verticalScroll.setMax(0);
                this.verticalScroll.setValue(0);
                this.verticalScroll.setUnitIncrement(0);
                this.verticalScroll.setBlockIncrement(0);
                this.verticalScroll.setVisibleAmount(0);
            } else {
                if (currentValue > totalSize) {
                    currentValue = totalSize;
                }
                // Update the scrollbar max value
                this.verticalScroll.setMax(totalSize);
                this.verticalScroll.setValue(currentValue);
                this.verticalScroll.setUnitIncrement(this.lineRowHeight);
                // Set block increment
                this.verticalScroll.setBlockIncrement(getImageAreaHeight() - this.headerRowHeight);
                // Set visible amount if area is ready
                if (getImageAreaHeight() > 0) {
                    this.verticalScroll.setVisibleAmount((totalSize / fullSize) * (getImageAreaHeight() - this.headerRowHeight));
                } else {
                    this.verticalScroll.setVisibleAmount(1);
                }
            }
        } else {
            this.verticalScroll.setMax(0);
            this.verticalScroll.setValue(0);
            this.verticalScroll.setUnitIncrement(0);
            this.verticalScroll.setBlockIncrement(0);
            this.verticalScroll.setVisibleAmount(0);
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
        // Compute viewport end
        this.viewPortEnd.set(this.viewPortStart.get().plusSeconds(this.viewPortDuration.get()));
        updateHScrollbarPosition();
        internalRefresh();
    }

    private void updateHScrollbarSettings() {
        // Update the scrollbar max value
        this.horizontalScroll.setMax((getMaxTime().getEpochSecond() - getMinTime().getEpochSecond()));
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
        this.headerElement = computeHeaderElement(this.imageArea.getGraphicsContext2D(), getImageAreaWidth(), getViewPortDuration());
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
        // Compute viewport end
        this.viewPortEnd.set(this.viewPortStart.get().plusSeconds(this.viewPortDuration.get()));
        // Recompute scrollbar settings
        updateHScrollbarSettings();
        updateVScrollbarSettings();
        // Repaint after this
        internalRefresh();
    }

    private ChronoUnit computeHeaderElement(GraphicsContext gc, double pixelWidth, long durationSeconds) {
        if(pixelWidth == 0) {
            // Use years, remove performance issue, not initialised
            return ChronoUnit.YEARS;
        }
        int daysSize = (int) (getTextWidth(gc, "0000-00-00") + 4 * getTextPadding());
        int monthsSize = (int) (getTextWidth(gc, "0000-00") + 4 * getTextPadding());

        double pixelForSecond = pixelWidth / durationSeconds;
        double temp;
        if(pixelForSecond > daysSize) {
            return ChronoUnit.SECONDS;
        } else if((temp = pixelForSecond * 60) > daysSize) {
            return ChronoUnit.MINUTES;
        } else if((temp = temp * 60) > daysSize) {
            return ChronoUnit.HOURS;
        } else if((temp = temp * 24) > daysSize) {
            return ChronoUnit.DAYS;
        } else if(temp * 30 > monthsSize) {
            return ChronoUnit.MONTHS;
        } else {
            return ChronoUnit.YEARS;
        }
    }

    @Override
    public int getTextWidth(GraphicsContext gc, String text) {
        Text theText = new Text(text);
        theText.setFont(gc.getFont());
        return (int) theText.getBoundsInLocal().getWidth();
    }

    private String formatHeaderText(Instant startTime, ChronoUnit headerElement) {
        ZonedDateTime time = startTime.atZone(ZoneId.of("UTC"));

        return time.format(DateFormat.get(headerElement));
    }

    private Instant getAdjustedStartTime(Instant viewPortStart, ChronoUnit headerElement) {
        ZonedDateTime zdt = viewPortStart.atZone(ZoneId.of("UTC"));
        switch (headerElement) {
            case YEARS:
                return zdt.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS).toInstant();
            case MONTHS:
                return zdt.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant();
            default:
                return viewPortStart.truncatedTo(headerElement);
        }
    }

    private void measureFontHeight(Font font) {
        if(textHeight == -1) {
            Text text = new Text("Ig");
            text.setBoundsType(TextBoundsType.VISUAL);
            text.setFont(font);
            textHeight = (int) text.getBoundsInLocal().getHeight();
            headerRowHeight = 2 * textHeight + (int) (3 * getTextPadding());
            lineRowHeight = textHeight + (int) (6 * getTextPadding());
        }
    }

    /* *****************************************************************************************
     * Rendering methods
     * *****************************************************************************************/

    private void internalRefresh() {
        GraphicsContext gc = this.imageArea.getGraphicsContext2D();
        internalRefresh(gc);
    }

    private void internalRefresh(GraphicsContext gc) {
        if(getTextFont() != null) {
            gc.setFont(getTextFont());
        }
        gc.setFontSmoothingType(FontSmoothingType.GRAY);
        gc.setImageSmoothing(false);
        // Draw the background
        drawBackground(gc);
        // Draw calendar headers, no vertical lines
        drawHeaders(gc);
        // Draw empty side panel (left)
        drawEmptySidePanel(gc);
        // Draw empty additional panel (right)
        drawEmptyAdditionalPanel(gc);
        // Prepare the clipping
        gc.save();
        gc.beginPath();
        gc.rect(0, getHeaderRowHeight(), getImageAreaWidth(), getImageAreaHeight() - getHeaderRowHeight());
        gc.closePath();
        gc.clip();
        // Draw timeline background
        drawTimeLineBackground(gc, this);
        // Draw header vertical lines
        drawHeaderVerticalLines(gc);
        // Draw time intervals in background
        drawTimeIntervals(gc, this, false);
        // Draw task lines
        drawTaskLines(gc, this);
        // Draw time intervals in foreground
        drawTimeIntervals(gc, this, true);
        // Draw cursors
        drawCursors(gc, this);
        // Draw Tooltip
        if(this.showedTooltip != null) this.showedTooltip.render(gc, this, this.cursorX, this.cursorY);
        // Restore
        gc.restore();
    }

    private void drawTimeLineBackground(GraphicsContext gc, IRenderingContext rc) {
        if(isEnableAlternateColorLines()) {
            // You render only line blocks that are contained in the viewport, as defined by the vertical scroll value
            int yStart = (int) this.verticalScroll.getValue();
            int yEnd = yStart + getImageAreaHeight() - getHeaderRowHeight();
            int processedLines = 0;
            int renderedLines = 0;
            //
            int startLine = -1;
            int i = 0;
            for (ITaskLine line : this.items) {
                // Compute the Y span of the rendering for this task line
                int taskLineYStart = processedLines * this.lineRowHeight;
                processedLines += line.getNbOfLines();
                int taskLineYEnd = processedLines * this.lineRowHeight;
                if ((taskLineYStart >= yStart && taskLineYStart <= yEnd) ||
                        (taskLineYEnd >= yStart && taskLineYEnd <= yEnd) ||
                        (taskLineYStart <= yStart && taskLineYEnd >= yEnd)) {
                    // Render: translate the taskLineYStart in the right viewport coordinates
                    taskLineYStart -= yStart;
                    taskLineYStart += this.headerRowHeight;
                    // Ask the rendering of the timeline
                    line.renderLineBackground(gc, (int) getTaskPanelWidth(), taskLineYStart, renderedLines, rc);
                    // Remember at this level what you rendered
                    if (startLine == -1) {
                        startLine = i;
                    }
                    renderedLines = processedLines;
                }
                ++i;
            }
        }
    }

    private void drawTimeIntervals(GraphicsContext gc, IRenderingContext rc, boolean foreground) {
        for(TimeInterval tc : this.timeIntervals) {
            if(tc.isForeground() == foreground && inViewport(tc.getStartTime(), tc.getEndTime())) {
                tc.render(gc, rc);
            }
        }
    }

    private void drawCursors(GraphicsContext gc, IRenderingContext rc) {
        for(TimeCursor tc : this.timeCursors) {
            if(inViewport(tc.getTime())) {
                tc.render(gc, rc);
            }
        }
    }

    private void drawBackground(GraphicsContext gc) {
        gc.clearRect(0, 0, getImageAreaWidth(), getImageAreaHeight());
        gc.setFill(getBackgroundColor());
        gc.fillRect(0, 0, getImageAreaWidth(), getImageAreaHeight());
    }

    private void drawTaskLines(GraphicsContext gc, IRenderingContext rc) {
        // You render only line blocks that are contained in the viewport, as defined by the vertical scroll value
        int yStart = (int) this.verticalScroll.getValue();
        int yEnd = yStart + getImageAreaHeight() - getHeaderRowHeight();
        int processedLines = 0;
        //
        int startLine = -1;
        int endLine = -1;
        int i = 0;
        for(ITaskLine line : this.items) {
            // Compute the Y span of the rendering for this task line
            int taskLineYStart = processedLines * this.lineRowHeight;
            processedLines += line.getNbOfLines();
            int taskLineYEnd = processedLines * this.lineRowHeight;
            if((taskLineYStart >= yStart && taskLineYStart <= yEnd) ||
                    (taskLineYEnd >= yStart && taskLineYEnd <= yEnd) ||
                    (taskLineYStart <= yStart && taskLineYEnd >= yEnd)) {
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
                // Inform that this task line is not rendered
                line.noRender();
            }
            ++i;
        }
        // Rendering completed
        this.currentYViewportItems = new int[] { startLine, endLine };
    }

    /**
     * Draw the background of the task panel (left)
     * @param gc the {@link GraphicsContext}
     */
    protected void drawEmptySidePanel(GraphicsContext gc) {
        gc.setFill(getPanelBackground());
        gc.setStroke(getPanelBorderColor());
        gc.fillRect(0, this.headerRowHeight, getTaskPanelWidth(), getImageAreaHeight() - this.headerRowHeight);
        gc.strokeRect(0, this.headerRowHeight, getTaskPanelWidth(), getImageAreaHeight() - this.headerRowHeight);
    }

    protected void drawEmptyAdditionalPanel(GraphicsContext gc) {
        gc.setFill(getPanelBackground());
        gc.setStroke(getPanelBorderColor());
        gc.fillRect(getViewPortEndX(), this.headerRowHeight, getAdditionalPanelWidth(), getImageAreaHeight() - this.headerRowHeight);
        gc.strokeRect(getViewPortEndX(), this.headerRowHeight, getAdditionalPanelWidth(), getImageAreaHeight() - this.headerRowHeight);
    }

    private void drawHeaders(GraphicsContext gc) {
        // The header is composed by one line, containing the time information depending on the resolution
        // Get the start time and round it to the previous step, depending on the headerElement value
        Instant startTime = getAdjustedStartTime(getViewPortStart(), this.headerElement);
        Instant finalTime = getViewPortStart().plusSeconds(getViewPortDuration());
        // Now start rendering, until the end of the box falls outside the rendering area
        boolean inArea = true;
        while(inArea) {
            Instant endTime;
            if(this.headerElement == ChronoUnit.YEARS || this.headerElement == ChronoUnit.MONTHS) {
                endTime = startTime.atOffset(ZoneOffset.UTC).plus(1, this.headerElement).toInstant();
            } else {
                endTime = startTime.plus(1, this.headerElement);
            }
            drawHeaderBox(gc, startTime, endTime, this.headerElement);
            if(endTime.isAfter(finalTime)) {
                inArea = false;
            } else {
                startTime = endTime;
            }
        }
        // Fill the TL corner block
        gc.setFill(getHeaderBackground());
        gc.setStroke(getHeaderBorderColor());
        gc.fillRect(0, 0, getTaskPanelWidth(), this.headerRowHeight);
        gc.strokeRect(0, 0, getTaskPanelWidth(), this.headerRowHeight);
        gc.fillRect(getViewPortEndX(), 0, getAdditionalPanelWidth(), this.headerRowHeight);
        gc.strokeRect(getViewPortEndX(), 0, getAdditionalPanelWidth(), this.headerRowHeight);
    }

    private void drawHeaderVerticalLines(GraphicsContext gc) {
        if(isEnableVerticalLines()) {
            Instant startTime = getAdjustedStartTime(getViewPortStart(), this.headerElement);
            Instant finalTime = getViewPortStart().plusSeconds(getViewPortDuration());
            // Now start rendering, until the end of the box falls outside the rendering area
            boolean inArea = true;
            while (inArea) {
                Instant endTime;
                if (this.headerElement == ChronoUnit.YEARS || this.headerElement == ChronoUnit.MONTHS) {
                    endTime = startTime.atOffset(ZoneOffset.UTC).plus(1, this.headerElement).toInstant();
                } else {
                    endTime = startTime.plus(1, this.headerElement);
                }
                drawHeaderLine(gc, startTime);
                if (endTime.isAfter(finalTime)) {
                    inArea = false;
                } else {
                    startTime = endTime;
                }
            }
        }
    }

    /**
     * Draw the vertical header line corresponding to the provided start time.
     * @param gc the {@link GraphicsContext}
     * @param startTime the time of the header line
     */
    protected void drawHeaderLine(GraphicsContext gc, Instant startTime) {
        int xStart = (int) toX(startTime);
        if(xStart < getTaskPanelWidth() || xStart > getViewPortEndX()) {
            return;
        }
        int height = this.headerRowHeight;
        // Vertical line (start)
        gc.setLineDashes(1, 3);
        gc.strokeLine(xStart, height, xStart, getImageAreaHeight());
        gc.setLineDashes();
    }

    /**
     * Draw a single header element (column) in the header top line.
     * @param gc the {@link GraphicsContext}
     * @param startTime the start time of the header element
     * @param endTime the end time of the header element
     * @param headerElement the resolution of the header element
     */
    protected void drawHeaderBox(GraphicsContext gc, Instant startTime, Instant endTime, ChronoUnit headerElement) {
        int xStart = (int) toX(startTime);
        int xEnd = (int) toX(endTime);
        int height = this.headerRowHeight;
        // Fill rectangle
        gc.setFill(getHeaderBackground());
        gc.setStroke(getHeaderBorderColor());
        gc.fillRect(xStart, 0, xEnd - xStart, height);
        gc.strokeRect(xStart, 0, xEnd - xStart, height);
        // Write text
        gc.setFill(getHeaderForegroundColor());
        if(headerElement == ChronoUnit.SECONDS || headerElement == ChronoUnit.MINUTES || headerElement == ChronoUnit.HOURS) {
            // Two lines
            String toWriteDays = formatHeaderText(startTime, ChronoUnit.DAYS);
            gc.fillText(toWriteDays, xStart + getTextPadding(), getTextHeight() + getTextPadding());
            String toWrite = formatHeaderText(startTime, headerElement);
            gc.fillText(toWrite, xStart + getTextPadding(), 2 * getTextHeight() + 2 * getTextPadding());
        } else {
            // One line
            String toWrite = formatHeaderText(startTime, headerElement);
            gc.fillText(toWrite, xStart + getTextPadding(), getTextHeight()/2.0 + height/2.0);
        }
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    private final ChangeListener<? super Number> panelChangeWidth = (e, o, n) -> {
        updateHorizontalScrollbarFitInViewPort();
        recomputeViewport();
    };
}
