package eu.dariolucia.jfx.timeline.examples;

import eu.dariolucia.jfx.timeline.Timeline;
import eu.dariolucia.jfx.timeline.model.*;
        import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestApplicationExtendedTimeInterval extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Timeline with Time Intervals Example");
        // Create timeline
        Timeline tl = new Timeline();
        // Add data defaults for testing
        Instant currentTime = Instant.now();
        tl.setMinTime(currentTime.minus(1, ChronoUnit.DAYS));
        tl.setMaxTime(currentTime.plusSeconds(12800));
        tl.setViewPortStart(currentTime);
        tl.setViewPortDuration(3600 * 24 * 20);
        tl.setTaskPanelWidth(180);

        // Add task lines
        HierarchicalGroupTaskLine HGroup = new HierarchicalGroupTaskLine("HierarchicalGroup");
        HGroup.setCollapsible(true);
        // Add time interval on hierarchical task line in the foreground
        TimeInterval interval = new TimeInterval(currentTime.plus(3, ChronoUnit.DAYS), currentTime.plus(6, ChronoUnit.DAYS));
        interval.setColor(new Color(Color.PURPLE.getRed(), Color.PURPLE.getGreen(), Color.PURPLE.getBlue(), 0.4));
        interval.setForeground(true);
        HGroup.getIntervals().add(interval);
        //
        TaskLine taskLine = new TaskLine("TaskLine1");
        HGroup.getItems().add(taskLine);
        taskLine.getItems().add(createTaskItem("Item1", currentTime.plus(12, ChronoUnit.HOURS), Duration.ofDays(2).toSeconds(), 3600 * 14));
        taskLine.getItems().add(createTaskItem("Item2", currentTime.plus(3, ChronoUnit.DAYS), Duration.ofDays(1).toSeconds(), 0));
        taskLine.getItems().add(createTaskItem("Item3", currentTime.plus(10, ChronoUnit.DAYS), Duration.ofDays(1).toSeconds(), 0));
        // Add time interval on task line in the foreground
        interval = new TimeInterval(currentTime.plus(1, ChronoUnit.DAYS), currentTime.plus(3, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS));
        interval.setColor(new Color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 0.4));
        interval.setForeground(true);
        taskLine.getIntervals().add(interval);
        //
        taskLine = new TaskLine("TaskLine2");
        HGroup.getItems().add(taskLine);
        taskLine.getItems().add(createTaskItem("Item1", currentTime.plus(3, ChronoUnit.DAYS), Duration.ofDays(4).toSeconds(), Duration.ofDays(3).toSeconds()));

        FlatGroupTaskLine FGroup = new FlatGroupTaskLine("FlatGroup");
        FGroup.setCollapsible(true);
        // Add time interval on flat task line in the background
        interval = new TimeInterval(currentTime.plus(1, ChronoUnit.DAYS), currentTime.plus(2, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS));
        interval.setColor(new Color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), 0.4));
        interval.setForeground(false);
        FGroup.getIntervals().add(interval);
        //
        taskLine = new TaskLine("TaskLine1");
        taskLine.getItems().add(createTaskItem("Item1", currentTime.plus(20, ChronoUnit.HOURS), Duration.ofDays(1).toSeconds(), 3600 * 14));
        taskLine.getItems().add(createTaskItem("Item2", currentTime.plus(3, ChronoUnit.DAYS), 3600*12, 3600*4));
        FGroup.getItems().add(taskLine);
        taskLine = new TaskLine("TaskLine2");
        TaskItem item = createTaskItem("Item1", currentTime.plus(4, ChronoUnit.DAYS), Duration.ofDays(4).toSeconds(), 3600 * 3);
        taskLine.getItems().add(item);
        // Add time interval on task item in the foreground (This interval will be NOT truncated to the size of the TaskItem)
        item.setTrimIntervals(false);
        interval = new TimeInterval(currentTime.plus(5, ChronoUnit.DAYS), currentTime.plus(9, ChronoUnit.DAYS));
        interval.setColor(new Color(Color.FUCHSIA.getRed(), Color.FUCHSIA.getGreen(), Color.FUCHSIA.getBlue(), 0.3));
        interval.setForeground(true);
        item.getIntervals().add(interval);
        //
        item = createTaskItem("Item2", currentTime.plus(9, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS), Duration.ofDays(7).toSeconds(), 3600 * 5);
        taskLine.getItems().add(item);
        // Add time interval on task item in the foreground (This interval will be truncated to the size of the TaskItem)
        interval = new TimeInterval(currentTime, currentTime.plus(13, ChronoUnit.DAYS));
        interval.setColor(new Color(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), 0.2));
        interval.setForeground(true);
        item.getIntervals().add(interval);
        //

        FGroup.getItems().add(taskLine);
        HGroup.getItems().add(FGroup);

        tl.getItems().add(HGroup);

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

