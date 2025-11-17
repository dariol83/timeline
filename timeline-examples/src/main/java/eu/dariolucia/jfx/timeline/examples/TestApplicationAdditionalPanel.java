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

public class TestApplicationAdditionalPanel extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Timeline with additional task panel (right) Example");
        // Create timeline
        Timeline tl = new Timeline();
        // Add data defaults for testing
        Instant currentTime = Instant.now();
        tl.setMinTime(currentTime.minus(10, ChronoUnit.DAYS));
        tl.setHorizontalScrollbarVisible(true);
        tl.setMaxTime(currentTime.plus(20, ChronoUnit.DAYS));
        tl.setViewPortStart(currentTime);
        tl.setViewPortDuration(3600 * 24 * 20);
        tl.setTaskPanelWidth(180);

        // Set additional panel width (default is 0)
        tl.setAdditionalPanelWidth(100);

        // Add task lines
        HierarchicalGroupTaskLine HGroup = new HierarchicalGroupTaskLine("HierarchicalGroup", "HGroupTestDesc");
        HGroup.setCollapsible(true);

        //
        TaskLine taskLine = new TaskLine("TaskLine1", "Line1TestDesc");
        HGroup.getItems().add(taskLine);
        taskLine.getItems().add(createTaskItem("Item1", currentTime.plus(12, ChronoUnit.HOURS), Duration.ofDays(2).toSeconds(), 3600 * 14));
        taskLine.getItems().add(createTaskItem("Item2", currentTime.plus(3, ChronoUnit.DAYS), Duration.ofDays(1).toSeconds(), 0));
        taskLine.getItems().add(createTaskItem("Item3", currentTime.plus(10, ChronoUnit.DAYS), Duration.ofDays(1).toSeconds(), 0));

        //
        taskLine = new TaskLine("TaskLine2", "Line2TestDesc");
        HGroup.getItems().add(taskLine);
        taskLine.getItems().add(createTaskItem("Item1", currentTime.plus(3, ChronoUnit.DAYS), Duration.ofDays(4).toSeconds(), Duration.ofDays(3).toSeconds()));

        FlatGroupTaskLine FGroup = new FlatGroupTaskLine("FlatGroup", "FlatGroupTestDesc");
        FGroup.setCollapsible(true);
        //
        taskLine = new TaskLine("TaskLine1", "Line3TestDesc");
        taskLine.getItems().add(createTaskItem("Item1", currentTime.plus(20, ChronoUnit.HOURS), Duration.ofDays(1).toSeconds(), 3600 * 14));
        taskLine.getItems().add(createTaskItem("Item2", currentTime.plus(3, ChronoUnit.DAYS), 3600*12, 3600*4));
        FGroup.getItems().add(taskLine);
        taskLine = new TaskLine("TaskLine2", "Line4TestDesc");
        TaskItem item = createTaskItem("Item1", currentTime.plus(4, ChronoUnit.DAYS), Duration.ofDays(4).toSeconds(), 3600 * 3);
        taskLine.getItems().add(item);

        //
        item = createTaskItem("Item2", currentTime.plus(9, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS), Duration.ofDays(7).toSeconds(), 3600 * 5);
        taskLine.getItems().add(item);

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
