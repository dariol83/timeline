package eu.dariolucia.jfx.timeline.examples;

import eu.dariolucia.jfx.timeline.Timeline;
import eu.dariolucia.jfx.timeline.model.TaskItem;
import eu.dariolucia.jfx.timeline.model.TaskLine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * Not an example, just testing a bug
 * 
 * @author YaoLin
 */
public class TestApplicationMonthHeaderElement extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Timeline Test Month Header Element Bug");
        Timeline tl = new Timeline();
        // Setup test local time
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2025, 6, 29), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(ldt.toLocalDate(), LocalTime.MAX);
        long duration = ChronoUnit.SECONDS.between(ldt, end);
        // Setup month header element
        tl.setViewPortStart(ldt.minusMonths(1).toInstant(ZoneOffset.UTC));
        tl.setViewPortDuration(ChronoUnit.SECONDS.between(ldt.minusMonths(1), ldt.plusMonths(1)));
        // Add task line
        TaskLine theLine = new TaskLine("Task Line 1", "First task line");
        theLine.getItems().add(new TaskItem("Task 1", ldt.toInstant(ZoneOffset.UTC), duration, 16));
        tl.getItems().add(theLine);

        // Add to application and render
        StackPane root = new StackPane();
        root.getChildren().add(tl);
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }
}
