# Timeline Chart

## Introduction
This module contains an implementation of a timeline/Gantt chart for JavaFX. The widget supports the following features:
- Grouping of tasks in lines and group of lines;
- Handling of time overlaps, dynamically rendered on separate lines;
- Time cursors and time intervals (in foreground and background);
- Scrolling and zooming via mouse scroll;
- Single selection model for task items; 
- Task's expected duration and actual duration;
- Dynamic column sizing;
- Appearance fully customisable per task item.

![Screenshot](img/timeline.png "Timeline Chart Widget")

## Usage

    // Create timeline
    Timeline tl = new Timeline();
    // Add data defaults for testing
    Instant currentTime = Instant.now();
    tl.setMinTime(currentTime.minusSeconds(3600));
    tl.setMaxTime(currentTime.plusSeconds(12800));
    tl.setViewPortStart(currentTime);
    tl.setViewPortDuration(1200);
    // Add task lines
    TaskLine theLine = new TaskLine("Task Line 1", "First task line");
    // Add items to the task line
    theLine.getItems().add(new TaskItem("Task 1", currentTime.plusSeconds(30), 98, 0));
    theLine.getItems().add(new TaskItem("Task 2", currentTime.plusSeconds(130), 28, 0));
    // Add the task line to the timeline
    tl.getItems().add(theLine);
    // Add to application and render

## Limitations
The time resolution below the second is not supported.
