package eu.dariolucia.jfx.test;

import eu.dariolucia.jfx.timeline.Timeline;
import eu.dariolucia.jfx.timeline.model.TaskItem;
import eu.dariolucia.jfx.timeline.model.TaskLine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TestApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Timeline Test");
        // Create timeline
        Timeline tl = new Timeline();
        // Add data defaults for testing
        Instant currentTime = new GregorianCalendar(2024, Calendar.FEBRUARY, 10, 10, 0, 0).toInstant();
        tl.setMinTime(currentTime);
        tl.setMaxTime(currentTime.plusSeconds(12800));
        tl.setViewPortStart(currentTime);
        tl.setViewPortDuration(1200);
        // Add task lines
        {
            TaskLine taskLine = new TaskLine("Task Line 1", "First task line");
            taskLine.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(30), 98, 0));
            taskLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(130), 28, 0));
            taskLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(190), 5, 0));
            tl.getLines().add(taskLine);
        }
        {
            TaskLine taskLine = new TaskLine("Task Line 2", "Second task line");
            taskLine.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(0), 77, 0));
            taskLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(920), 7000, 0));
            tl.getLines().add(taskLine);
        }
        {
            TaskLine taskLine = new TaskLine("Task Line 3 mega long task line name", "Third task line");
            taskLine.getItems().add(new TaskItem("Task 1 with a super long name that cannot fit in the label", currentTime.plusSeconds(600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(0), 77, 0));
            taskLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(920), 7000, 0));
            tl.getLines().add(taskLine);
        }
        for(int i = 4; i < 50; ++i) {
            TaskLine taskLine = new TaskLine("Task Line " + i, i + "th task line");
            taskLine.getItems().add(new TaskItem("Task 1 (" + i + ")", currentTime.plusSeconds(600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2 (" + i + ")", currentTime.plusSeconds(0), 77, 0));
            taskLine.getItems().add(new TaskItem("Task 3 (" + i + ")", currentTime.plusSeconds(920), 7000, 0));
            tl.getLines().add(taskLine);
        }
        // Add to application and render
        StackPane root = new StackPane();
        root.getChildren().add(tl);
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }
}
