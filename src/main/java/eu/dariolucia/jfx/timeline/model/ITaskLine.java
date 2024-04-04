package eu.dariolucia.jfx.timeline.model;

import javafx.beans.Observable;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;
import java.util.stream.Stream;

public interface ITaskLine {

    String getName();

    String getDescription();

    boolean isGroup();

    int getNbOfLines();

    void render(GraphicsContext gc, double taskLineXStart, double taskLineYStart, RenderingContext rc);

    Observable[] getObservableProperties();

    void setParent(ITaskLine parent);

    ITaskLine getParent();

    List<TaskItem> getTaskItems();
}
