package eu.dariolucia.jfx.timeline.examples;

import eu.dariolucia.jfx.timeline.Timeline;
import eu.dariolucia.jfx.timeline.model.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TestApplicationTooltip extends Application
{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Timeline with tooltip Example");
        // Create timeline
        Timeline tl = new Timeline();
        // Set displayed date format
        tl.setDateFormat(ChronoUnit.MONTHS, DateTimeFormatter.ofPattern("LLL yyyy"));
        tl.setDateFormat(ChronoUnit.DAYS, DateTimeFormatter.ofPattern("dd LLL yyyy"));
        // Add data defaults for testing
        Instant currentTime = Instant.now();
        tl.setMinTime(currentTime);
        tl.setMaxTime(currentTime.plus(10, ChronoUnit.DAYS));
        tl.setViewPortStart(currentTime);
        tl.setViewPortDuration(3600 * 24 * 10);
        tl.setTaskPanelWidth(180);

        // Add HierarchicalGroup and task line
        HierarchicalGroupTaskLine HGroup = new HierarchicalGroupTaskLine("HGroup1");
        HGroup.setCollapsible(true);
        tl.getItems().add(HGroup);
        TaskLine taskLine = new TaskLine("Line1");
        HGroup.getItems().add(taskLine);
        /// /////////////////////////////////////////////////////////

        // Add task item and time point
        TaskItem taskItem = new TaskItem("Item1", currentTime.plus(2, ChronoUnit.DAYS), 3600*24*5, 0);
        TimePoint point = new TimePoint("testpoint", currentTime.plus(3, ChronoUnit.DAYS), TimePointType.CIRCLE);
        point.setColor(Color.GOLD);
        taskItem.getTimePoints().add(point);
        taskLine.getItems().add(taskItem);
        /// /////////////////////////////////////////////////////////

        //Create Tooltip for time point and change color
        TimeTooltip tooltip = new TimeTooltip("TestTooltip\n\nIt is first tooltip");
        tooltip.setTooltipBorderColor(Color.RED);
        tooltip.setTooltipTextColor(Color.GREEN);
        point.setTooltip(tooltip);
        /// /////////////////////////////////////////////////////////

        //Create FlatGroup and task line
        FlatGroupTaskLine FGroup = new FlatGroupTaskLine("FGroup1");
        FGroup.setCollapsible(true);
        HGroup.getItems().add(FGroup);
        taskLine = new TaskLine("Line1");
        FGroup.getItems().add(taskLine);
        /// /////////////////////////////////////////////////////////

        //Create task item and time interval
        taskItem = new TaskItem("Item1", currentTime.plus(4, ChronoUnit.DAYS), 3600*24*2, 0);
        taskItem.setTrimIntervals(false);
        taskLine.getItems().add(taskItem);
        TimeInterval interval = new TimeInterval(currentTime.plus(5, ChronoUnit.DAYS), null);
        interval.setForeground(true);
        taskItem.getIntervals().add(interval);
        /// /////////////////////////////////////////////////////////

        //Create Tooltip for time interval
        tooltip = new TimeTooltip("Second Tooltip\n\nTooltip on TimeInterval in task item");
        interval.setTooltip(tooltip);
        /// /////////////////////////////////////////////////////////

        //Create Tooltip for task item
        tooltip = new TimeTooltip("Third Tooltip - Tooltip on TaskItem");
        taskItem.setTooltip(tooltip);
        /// /////////////////////////////////////////////////////////

        //Create task item where time point be overlapped time interval
        taskItem = new TaskItem("Item2", currentTime.plus(7, ChronoUnit.DAYS), 3600*24*2, 0);
        taskLine.getItems().add(taskItem);
        point = new TimePoint("Overlap Point", currentTime.plus(8, ChronoUnit.DAYS), TimePointType.ROUND_RECT);
        point.setColor(Color.BLUE);
        taskItem.getTimePoints().add(point);
        tooltip = new TimeTooltip("Fourth Tooltip - Interval Overlap Point test");
        point.setTooltip(tooltip);
        /// /////////////////////////////////////////////////////////

        //Create task line with time interval
        taskLine = new TaskLine("Line2");
        FGroup.getItems().add(taskLine);
        taskItem = new TaskItem("Item2", currentTime.plus(2, ChronoUnit.DAYS), 3600*24, 0);
        taskLine.getItems().add(taskItem);
        interval = new TimeInterval(currentTime.plus(2, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS), null);
        interval.setTooltip(new TimeTooltip("Tooltip on time interval in task line"));
        interval.setForeground(true);
        taskLine.getIntervals().add(interval);
        /// /////////////////////////////////////////////////////////

        //Create global time interval and add tooltip
        interval = new TimeInterval(currentTime.plus(8, ChronoUnit.DAYS), currentTime.plus(8, ChronoUnit.DAYS).plus(20, ChronoUnit.HOURS));
        interval.setForeground(true);
        interval.setColor(new Color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 0.3));
        tl.getTimeIntervals().add(interval);
        interval.setTooltip(new TimeTooltip("=========================\n\n\n\nIt's tooltip for global time interval\n\n\n\n========================="));
        /// /////////////////////////////////////////////////////////

        // Add to application and render
        StackPane root = new StackPane();
        root.getChildren().add(tl);
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }

    private static TaskItem createTaskItem(String name, Instant start, long duration, long actualDuration) {
        return new TaskItem(name, start, duration, actualDuration);
    }
}
