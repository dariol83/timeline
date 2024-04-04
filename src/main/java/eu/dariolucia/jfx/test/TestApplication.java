package eu.dariolucia.jfx.test;

import eu.dariolucia.jfx.timeline.Timeline;
import eu.dariolucia.jfx.timeline.model.GroupTaskLine;
import eu.dariolucia.jfx.timeline.model.TaskItem;
import eu.dariolucia.jfx.timeline.model.TaskLine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
        TaskLine theLine = new TaskLine("Task Line 1", "First task line");
        theLine.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(30), 98, 0));
        theLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(130), 28, 0));
        theLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(190), 5, 0));
        tl.getLines().add(theLine);

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
        {
            GroupTaskLine group = new GroupTaskLine("Group 1");

            TaskLine taskLine = new TaskLine("Task Line 3 group 1", "Yet another task line");
            taskLine.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(1600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(123), 377, 0));
            taskLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(920), 32, 0));

            TaskLine taskLine2 = new TaskLine("Task Line 3 group 2", "Yet another task line");
            taskLine2.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(600), 140, 0));
            taskLine2.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(1), 77, 0));
            taskLine2.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(120), 465, 0));

            TaskLine taskLine3 = new TaskLine("Task Line 3 group 3", "Yet another task line");
            taskLine3.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(600), 140, 0));
            taskLine3.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(120), 465, 0));

            group.getItems().addAll(taskLine, taskLine2, taskLine3);

            tl.getLines().add(group);
        }
        for(int i = 4; i < 50; ++i) {
            TaskLine taskLine = new TaskLine("Task Line " + i, i + "th task line");
            taskLine.getItems().add(new TaskItem("Task 1 (" + i + ")", currentTime.plusSeconds(600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2 (" + i + ")", currentTime.plusSeconds(0), 77, 0));
            taskLine.getItems().add(new TaskItem("Task 3 (" + i + ")", currentTime.plusSeconds(920), 7000, 0));
            tl.getLines().add(taskLine);
        }
        tl.setTaskPanelWidth(200);

        theLine.setName("Name is changed!sads ad as");

        // Launch a thread that does some changes
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(4000);
                TaskItem it = theLine.getItems().get(0);
                for(int i = 0; i < 5; ++i) {
                    Thread.sleep(4000);
                    Platform.runLater(() -> it.setStartTime(it.getStartTime().plusSeconds(120)));
                }
                Platform.runLater(() -> tl.getSelectionModel().select(it));
                for(int i = 0; i < 5; ++i) {
                    Thread.sleep(4000);
                    Platform.runLater(() -> it.setExpectedDuration(it.getExpectedDuration() + 60));
                    Platform.runLater(() -> it.setActualDuration(it.getActualDuration() + 20));
                }
                Platform.runLater(() -> it.setTaskBackgroundColor(Color.RED));
                Platform.runLater(() -> it.setName("New Task!"));
            } catch (InterruptedException e) {
                // ignore
            }
        });
        t.setDaemon(true);
        t.start();
        // Add to application and render
        StackPane root = new StackPane();
        root.getChildren().add(tl);
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }
}
