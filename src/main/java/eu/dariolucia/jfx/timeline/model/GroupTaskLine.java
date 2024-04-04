package eu.dariolucia.jfx.timeline.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A task group represents a group of task lines in a timeline.
 */
public class GroupTaskLine implements ITaskLine {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final ObservableList<ITaskLine> items = FXCollections.observableArrayList(ITaskLine::getObservableProperties);
    private ITaskLine parent;

    public GroupTaskLine(String name) {
        this(name, null);
    }

    public GroupTaskLine(String name, String description) {
        this.name.set(name);
        this.description.set(description);
        this.items.addListener(this::listUpdated);
    }

    private void listUpdated(ListChangeListener.Change<? extends ITaskLine> change) {
        while(change.next()) {
            if(change.wasAdded()) {
                change.getAddedSubList().forEach(ti -> ti.setParent(this));
            }
            if(change.wasRemoved()) {
                change.getRemoved().forEach(ti -> ti.setParent(null));
            }
        }
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
        double groupBoxWidth = rc.getLineRowHeight();
        // Render the sublines
        int i = 0;
        for(ITaskLine line : this.items) {
            line.render(gc, taskLineXStart + groupBoxWidth, taskLineYStart + i * rc.getLineRowHeight(), rc);
            ++i;
        }
        // Draw the group box
        gc.setFill(Color.LIGHTGRAY);
        gc.setStroke(Color.DARKGRAY);
        gc.fillRect(taskLineXStart, taskLineYStart, groupBoxWidth, getNbOfLines() * rc.getLineRowHeight());
        gc.strokeRect(taskLineXStart, taskLineYStart, groupBoxWidth, getNbOfLines() * rc.getLineRowHeight());
        gc.setStroke(Color.BLACK);

        gc.save();
        gc.translate(taskLineXStart, taskLineYStart);
        gc.rotate(-90);
        // Render in the middle
        double textWidth = rc.getTextWidth(gc, getName());
        double offset = nbLines * rc.getLineRowHeight()/2;
        gc.strokeText(getName(), - offset - textWidth/2, groupBoxWidth/2 + rc.getTextHeight()/2);
        gc.restore();
    }

    @Override
    public Observable[] getObservableProperties() {
        return new Observable[] { nameProperty(), descriptionProperty(), getItems() };
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

    @Override
    public ITaskLine getParent() {
        return parent;
    }

    @Override
    public List<TaskItem> getTaskItems() {
        return this.items.stream().flatMap(i -> i.getTaskItems().stream()).collect(Collectors.toList());
    }

    @Override
    public void setParent(ITaskLine parent) {
        this.parent = parent;
    }
}
