package eu.dariolucia.jfx.timeline.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A task line represents a line in a timeline.
 */
public class TaskLine implements ITaskLine {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final ObservableList<TaskItem> items = FXCollections.observableArrayList(param -> new Observable[] { param.startTimeProperty(), param.nameProperty(), param.expectedDurationProperty(), param.actualDurationProperty() });

    public TaskLine(String name) {
        this(name, null);
    }

    public TaskLine(String name, String description) {
        this.name.set(name);
        this.description.set(description);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public int getNbOfLines() {
        return 1;
    }

    @Override
    public void render(GraphicsContext gc, double taskLineXStart, double taskLineYStart, RenderingContext rc) {
        // Render the tasks in the line
        for(TaskItem ti : this.items) {
            ti.render(gc, taskLineYStart, rc);
        }
        // Render the line in the task panel
        gc.setStroke(Color.DARKGRAY);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, rc.getLineRowHeight());
        gc.strokeRect(taskLineXStart, taskLineYStart, rc.getTaskPanelWidth() - taskLineXStart, rc.getLineRowHeight());
        // Render text
        gc.setStroke(Color.BLACK);
        gc.strokeText(getName(), taskLineXStart + rc.getTextPadding(), taskLineYStart + rc.getLineRowHeight() - rc.getTextHeight(), rc.getTaskPanelWidth() - 2 * rc.getTextPadding() - taskLineXStart);
        // Render bottom line
        gc.setStroke(Color.LIGHTGRAY);
        gc.strokeLine(rc.getTaskPanelWidth(), taskLineYStart + rc.getLineRowHeight(), rc.toX(rc.getViewPortEnd()), taskLineYStart + rc.getLineRowHeight());
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public ObservableList<TaskItem> getItems() {
        return items;
    }

    @Override
    public Observable[] getObservableProperties() {
        return new Observable[] { nameProperty(), descriptionProperty(), getItems() };
    }

    @Override
    public String toString() {
        return "TaskLine{" +
                "name=" + getName() +
                ", description=" + getDescription() +
                '}';
    }
}
