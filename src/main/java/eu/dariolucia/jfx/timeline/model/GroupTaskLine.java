package eu.dariolucia.jfx.timeline.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;

import java.util.LinkedList;

/**
 * A task group represents a group of task lines in a timeline.
 */
public class GroupTaskLine implements ITaskLine {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final ObservableList<ITaskLine> items = FXCollections.observableList(new LinkedList<>());

    public GroupTaskLine(String name) {
        this(name, null);
    }

    public GroupTaskLine(String name, String description) {
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
        return true;
    }

    @Override
    public int getNbOfLines() {
        int nbLines = 0;
        for(ITaskLine tl : this.items) {
            nbLines += tl.getNbOfLines();
        }
        return nbLines;
    }

    @Override
    public void render(GraphicsContext gc, double taskLineYStart, RenderingContext rc) {
        // TODO:
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public ObservableList<ITaskLine> getItems() {
        return items;
    }
}
