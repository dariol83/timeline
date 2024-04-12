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
import eu.dariolucia.jfx.timeline.model.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Timeline Example");
        // Create timeline
        Timeline tl = new Timeline();
        // Add data defaults for testing
        Instant currentTime = Instant.now();
        tl.setMinTime(currentTime);
        tl.setMaxTime(currentTime.plusSeconds(12800));
        tl.setViewPortStart(currentTime);
        tl.setViewPortDuration(3600 * 24 * 20);
        tl.setBackgroundColor(Color.BEIGE.brighter().brighter());
        tl.setTaskPanelWidth(250);
        // Add task lines
        {
            currentTime = currentTime.minus(2, ChronoUnit.DAYS);
            // Design
            GroupTaskLine designGroup = new GroupTaskLine("Design");
            TaskLine backendDesign = new TaskLine("Backend", "Backend Design");
            backendDesign.getItems().add(createTaskItem("General Design", currentTime.plus(3, ChronoUnit.DAYS), Duration.ofDays(2).toSeconds(), 3600 * 14));
            backendDesign.getItems().add(createTaskItem("Optimisation", currentTime.plus(6, ChronoUnit.DAYS), Duration.ofDays(1).toSeconds(), 0));
            TaskLine frontEndDesign = new TaskLine("Frontend", "Frontend Design");
            frontEndDesign.getItems().add(createTaskItem("Design", currentTime.plus(3, ChronoUnit.DAYS), Duration.ofDays(4).toSeconds(), Duration.ofDays(3).toSeconds()));
            designGroup.getItems().addAll(backendDesign, frontEndDesign);
            tl.getItems().add(designGroup);
        }
        {
            currentTime = currentTime.plus(5, ChronoUnit.DAYS);
            // Implementation
            GroupTaskLine implementationGroup = new GroupTaskLine("Implementation");

            GroupTaskLine backendGroup = new GroupTaskLine("Backend");
            TaskLine backendImpl = new TaskLine("Development");
            backendImpl.getItems().add(createTaskItem("General Implementation", currentTime.plus(1, ChronoUnit.DAYS), Duration.ofDays(8).toSeconds(), 0));
            TaskLine backendTest = new TaskLine("Testing");
            backendTest.getItems().add(createTaskItem("Unit Testing", currentTime.plus(3, ChronoUnit.DAYS), Duration.ofDays(4).toSeconds(), 0));
            backendGroup.getItems().addAll(backendImpl, backendTest);

            GroupTaskLine frontEndGroup = new GroupTaskLine("FrontEnd");
            TaskLine frontEndImpl = new TaskLine("Development");
            frontEndImpl.getItems().add(createTaskItem("Implementation", currentTime.plus(2, ChronoUnit.DAYS), Duration.ofDays(6).toSeconds(), 0));
            TaskLine frontEndTest = new TaskLine("Testing");
            frontEndTest.getItems().add(createTaskItem("Unit Testing", currentTime.plus(3, ChronoUnit.DAYS), Duration.ofDays(1).toSeconds(), 0));
            frontEndGroup.getItems().addAll(frontEndImpl, frontEndTest);

            TaskLine documentation = new TaskLine("Documentation", "Documentation");
            documentation.getItems().add(createTaskItem("Writing Docs", currentTime.plus(1, ChronoUnit.DAYS), Duration.ofDays(8).toSeconds(), Duration.ofDays(1).toSeconds()));

            implementationGroup.getItems().addAll(backendGroup, frontEndGroup, documentation);

            tl.getItems().add(implementationGroup);
        }
        {
            currentTime = currentTime.plus(10, ChronoUnit.DAYS);
            // Acceptance
            TaskLine acceptance = new TaskLine("Acceptance", "Acceptance");
            acceptance.getItems().add(createTaskItem("Acceptance Tests", currentTime.plus(0, ChronoUnit.DAYS), Duration.ofDays(1).toSeconds(), 0));
            tl.getItems().add(acceptance);
        }

        TimeInterval ti = new TimeInterval(null, currentTime.minus(9, ChronoUnit.DAYS).minusSeconds(9000));
        ti.setColor(new Color(Color.PAPAYAWHIP.getRed(), Color.PAPAYAWHIP.getGreen(), Color.PAPAYAWHIP.getBlue(), 0.3));
        ti.setForeground(true);
        tl.getTimeIntervals().add(ti);

        TimeCursor tc = new TimeCursor(currentTime.minus(9, ChronoUnit.DAYS).minusSeconds(9000));
        tc.setColor(Color.LIGHTGRAY);

        TimeCursor deadline = new TimeCursor(currentTime.plus(1, ChronoUnit.DAYS).plusSeconds(15000));
        deadline.setColor(Color.RED);
        tl.getTimeCursors().addAll(tc, deadline);

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
