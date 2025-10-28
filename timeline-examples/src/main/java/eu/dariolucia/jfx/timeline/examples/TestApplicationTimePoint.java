package eu.dariolucia.jfx.timeline.examples;

import eu.dariolucia.jfx.timeline.Timeline;
import eu.dariolucia.jfx.timeline.model.TaskItem;
import eu.dariolucia.jfx.timeline.model.TaskLine;
import eu.dariolucia.jfx.timeline.model.TimePoint;
import eu.dariolucia.jfx.timeline.model.TimePointType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.Instant;

public class TestApplicationTimePoint extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TimePoint Timeline Test");
        // Create timeline
        Timeline tl = new Timeline();
        tl.setVerticalScrollbarVisible(true);
        tl.setHorizontalScrollbarVisible(true);
        tl.setViewPortDuration(130);
        tl.setViewPortStart(Instant.now().plusSeconds(25));
        tl.setBackgroundColor(Color.BLACK);

        // Add task line
        TaskLine theLine = new TaskLine("Task Line 1");
        TaskItem task = new TaskItem("Task 1", Instant.now().plusSeconds(30), 60, 16);

        // Add Circle TimePoint
        TimePoint point = new TimePoint("P1", task.getStartTime().plusSeconds(15), TimePointType.CIRCLE);
        point.setColor(Color.RED);
        task.getTimePoints().add(point);

        // Add Rect TimePoint
        point = new TimePoint("P2", task.getStartTime().plusSeconds(50), TimePointType.ROUND_RECT);
        point.setColor(Color.GREEN);
        task.getTimePoints().add(point);

        theLine.getItems().add(task);
        tl.getItems().add(theLine);

        theLine = new TaskLine("Task Line 2", "Second task line");
        task = new TaskItem("Task 2", Instant.now().plusSeconds(60), 90, 78);

        // Add Image TimePoint
        point = new TimePoint("P3", task.getStartTime().plusSeconds(10), TimePointType.IMG);
        point.setColor(Color.GOLD);
        point.setTextColor(Color.RED);
        point.setImage(new Image(getClass().getResourceAsStream("/star-icon.png")));
        task.getTimePoints().add(point);

        theLine.getItems().add(task);
        tl.getItems().add(theLine);

        // Add to application and render
        StackPane root = new StackPane();
        root.getChildren().add(tl);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
