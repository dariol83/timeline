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

import eu.dariolucia.jfx.timeline.model.*;
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
        theLine.getItems().add(new TaskItem("Task 4", currentTime.plusSeconds(60 * 11), 80, 140));
        tl.getItems().add(theLine);

        {
            TaskLine taskLine = new TaskLine("Task Line 2", "Second task line");
            taskLine.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(0), 77, 0));
            taskLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(920), 7000, 0));
            tl.getItems().add(taskLine);
        }
        {
            TaskLine taskLine = new TaskLine("Task Line 3 mega long task line name", "Third task line");
            taskLine.getItems().add(new TaskItem("Task 1 with a super long name that cannot fit in the label", currentTime.plusSeconds(600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(0), 77, 0));
            taskLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(920), 7000, 0));
            tl.getItems().add(taskLine);
        }
        GroupTaskLine group = new GroupTaskLine("Group 1");
        GroupTaskLine group2 = new GroupTaskLine("Group 2");
        {
            TaskLine taskLine = new TaskLine("Task Line 3 group 1", "Yet another task line");
            taskLine.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(1600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(123), 377, 0));
            taskLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(920), 32, 0));

            TaskLine taskLine2 = new TaskLine("Task Line 3 group 2", "Yet another task line");
            taskLine2.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(600), 140, 0));
            taskLine2.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(1), 77, 0));
            taskLine2.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(120), 465, 0));

            TaskLine taskLine3 = new TaskLine("Task Line 3 group 2", "Yet another task line");
            taskLine3.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(600), 140, 0));
            taskLine3.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(120), 465, 0));

            TaskLine taskLine4 = new TaskLine("Task Line 4 group 1", "Yet another task line");
            taskLine4.getItems().add(new TaskItem("Task 5", currentTime.plusSeconds(600), 140, 0));
            taskLine4.getItems().add(new TaskItem("Task 6", currentTime.plusSeconds(120), 465, 0));

            group2.getItems().addAll(taskLine2, taskLine3);
            group.getItems().addAll(taskLine, group2, taskLine4);

            tl.getItems().add(group);
        }
        GroupTaskLine group3 = new GroupTaskLine("Group 3");
        {
            TaskLine taskLine = new TaskLine("Task Line 1 group 3", "Yet another task line");
            taskLine.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(1600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(123), 377, 0));
            taskLine.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(920), 32, 0));

            TaskLine taskLine2 = new TaskLine("Task Line 2 group 3", "Yet another task line");
            taskLine2.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(600), 140, 0));
            taskLine2.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(1), 77, 0));
            taskLine2.getItems().add(new TaskItem("Task 3", currentTime.plusSeconds(120), 465, 0));

            TaskLine taskLine3 = new TaskLine("Task Line 3 group 3", "Yet another task line");
            taskLine3.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(600), 140, 0));
            taskLine3.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(120), 465, 0));

            group3.getItems().addAll(taskLine, taskLine2, taskLine3);
            tl.getItems().add(group3);
        }

        for(int i = 4; i < 50; ++i) {
            TaskLine taskLine = new TaskLine("Task Line " + i, i + "th task line");
            taskLine.getItems().add(new TaskItem("Task 1 (" + i + ")", currentTime.plusSeconds(600), 140, 0));
            taskLine.getItems().add(new TaskItem("Task 2 (" + i + ")", currentTime.plusSeconds(0), 77, 0));
            taskLine.getItems().add(new TaskItem("Task 3 (" + i + ")", currentTime.plusSeconds(920), 7000, 0));
            tl.getItems().add(taskLine);
        }
        tl.setTaskPanelWidth(200);

        theLine.setName("Name is changed!sads ad as");
        Platform.runLater(() -> tl.getTimeCursors().add(new TimeCursor(theLine.getItems().get(0).getStartTime().plusSeconds(50))));
        Platform.runLater(() -> tl.getTimeIntervals().add(new TimeInterval(theLine.getItems().get(0).getStartTime().plusSeconds(120),
                theLine.getItems().get(0).getStartTime().plusSeconds(220))));
        TimeInterval timeInterval = new TimeInterval(theLine.getItems().get(0).getStartTime().plusSeconds(520),
                theLine.getItems().get(0).getStartTime().plusSeconds(620));
        timeInterval.setForeground(true);
        Platform.runLater(() -> tl.getTimeIntervals().add(timeInterval));

        // Launch a thread that does some changes
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> tl.setScrollbarsVisible(true));
                Thread.sleep(2000);
                Platform.runLater(() -> tl.setScrollbarsVisible(false));
                Thread.sleep(2000);
                Platform.runLater(() -> tl.setScrollbarsVisible(true));

                TaskItem it = theLine.getItems().get(0);
                for(int i = 0; i < 3; ++i) {
                    Thread.sleep(3000);
                    Platform.runLater(() -> it.setStartTime(it.getStartTime().plusSeconds(220)));
                }
                Thread.sleep(3000);
                Platform.runLater(() -> tl.getSelectionModel().select(it));
                for(int i = 0; i < 3; ++i) {
                    Thread.sleep(3000);
                    Platform.runLater(() -> it.setExpectedDuration(it.getExpectedDuration() + 60));
                    Platform.runLater(() -> it.setActualDuration(it.getActualDuration() + 20));
                }
                Platform.runLater(() -> it.setTaskBackgroundColor(Color.RED));
                Platform.runLater(() -> it.setName("New Task!"));
                Thread.sleep(3000);
                Platform.runLater(() -> it.setStartTime(it.getStartTime().minusSeconds(520)));
                Thread.sleep(3000);
                Platform.runLater(() -> theLine.getItems().remove(it));
                Thread.sleep(3000);
                Platform.runLater(() -> ((TaskLine) (group2.getItems().get(0))).getItems().add(new TaskItem("Task 4", currentTime.plusSeconds(123), 377, 0)));
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
