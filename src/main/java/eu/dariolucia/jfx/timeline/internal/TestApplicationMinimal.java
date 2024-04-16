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

package eu.dariolucia.jfx.timeline.internal;

import eu.dariolucia.jfx.timeline.Timeline;
import eu.dariolucia.jfx.timeline.model.TaskItem;
import eu.dariolucia.jfx.timeline.model.TaskLine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.Instant;

public class TestApplicationMinimal extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Minimal Timeline Test");
        // Create timeline
        Timeline tl = new Timeline();
        tl.setVerticalScrollbarVisible(true);
        tl.setHorizontalScrollbarVisible(true);
        // Add task line
        TaskLine theLine = new TaskLine("Task Line 1", "First task line");
        theLine.getItems().add(new TaskItem("Task 1", Instant.now().plusSeconds(30), 60, 16));
        tl.getItems().add(theLine);
        theLine = new TaskLine("Task Line 2", "Second task line");
        theLine.getItems().add(new TaskItem("Task 2", Instant.now().plusSeconds(60), 90, 78));
        tl.getItems().add(theLine);
        tl.setBackgroundColor(Color.BLACK);
        // Add to application and render
        StackPane root = new StackPane();
        root.getChildren().add(tl);
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }
}
