package eu.dariolucia.jfx.timeline.model;

import javafx.scene.canvas.GraphicsContext;

public interface ITaskLine {

    String getName();

    String getDescription();

    boolean isGroup();

    int getNbOfLines();

    void render(GraphicsContext gc, double taskLineXStart, double taskLineYStart, RenderingContext rc);
}
