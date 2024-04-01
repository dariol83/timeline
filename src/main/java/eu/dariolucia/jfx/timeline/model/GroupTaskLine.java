package eu.dariolucia.jfx.timeline.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
    public void render(GraphicsContext gc, double taskLineXStart, double taskLineYStart, RenderingContext rc) {
        int nbLines = getNbOfLines();
        // Render the sublines
        int i = 0;
        for(ITaskLine line : this.items) {
            line.render(gc, taskLineXStart + 2*rc.getTextPadding() + rc.getTextHeight(), taskLineYStart + i * rc.getLineRowHeight(), rc);
            ++i;
        }
        // Draw the group box
        gc.setFill(Color.LIGHTGRAY);
        gc.setStroke(Color.DARKGRAY);
        gc.fillRect(taskLineXStart, taskLineYStart, 2*rc.getTextPadding() + rc.getTextHeight(), getNbOfLines() * rc.getLineRowHeight());
        gc.strokeRect(taskLineXStart, taskLineYStart, 2*rc.getTextPadding() + rc.getTextHeight(), getNbOfLines() * rc.getLineRowHeight());
        gc.setStroke(Color.BLACK);

        gc.save();
        gc.translate(taskLineXStart, taskLineYStart);
        gc.rotate(-90);
        // Render in the middle TODO: find a way to cache the string length given the name and the font!
        double textWidth = rc.getTextWidth(gc, getName());
        double offset = nbLines * rc.getLineRowHeight()/2;
        gc.strokeText(getName(), - offset - textWidth/2, (2*rc.getTextPadding() + rc.getTextHeight())/2 + rc.getTextHeight()/2);// rc.getTextPadding() + rc.getTextHeight());
        gc.restore();
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
