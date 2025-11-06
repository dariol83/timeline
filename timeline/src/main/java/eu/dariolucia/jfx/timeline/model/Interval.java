package eu.dariolucia.jfx.timeline.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.time.Instant;

public abstract class Interval extends LineElement {

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/

    /**
     * Start time of the time interval. If null, it is open-end.
     */
    private final SimpleObjectProperty<Instant> startTime = new SimpleObjectProperty<>();
    /**
     * End time of the time interval. If null, it is open-end.
     */
    private final SimpleObjectProperty<Instant> endTime = new SimpleObjectProperty<>();
    /**
     * If true, the time interval is drawn above {@link TaskItem}s.
     */
    private final SimpleBooleanProperty foreground = new SimpleBooleanProperty(false);
    /**
     * The color of the time interval.
     */
    private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(new Color(Color.LIMEGREEN.getRed(), Color.LIMEGREEN.getGreen(), Color.LIMEGREEN.getBlue(), 0.5));

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/

    /**
     * Class constructor.
     * @param startTime the start time, can be null
     * @param endTime the end time, can be null
     */
    public Interval(Instant startTime, Instant endTime) {
        super(null);
        setStartTime(startTime);
        setEndTime(endTime);
    }

    /* *****************************************************************************************
     * Property Accessors
     * *****************************************************************************************/

    public Instant getStartTime() {
        return startTime.get();
    }

    public SimpleObjectProperty<Instant> startTimeProperty() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime.set(startTime);
    }

    public Instant getEndTime() {
        return endTime.get();
    }

    public SimpleObjectProperty<Instant> endTimeProperty() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime.set(endTime);
    }

    public boolean isForeground() {
        return foreground.get();
    }

    public SimpleBooleanProperty foregroundProperty() {
        return foreground;
    }

    public void setForeground(boolean foreground) {
        this.foreground.set(foreground);
    }

    public Color getColor() {
        return color.get();
    }

    public SimpleObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    /**
     * Render the interval.
     * @param gc the {@link GraphicsContext}
     * @param rc the {@link IRenderingContext}
     */
    protected abstract void render(GraphicsContext gc, IRenderingContext rc);

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    /**
     * Return the properties that should trigger an update notification in case of
     * change. Subclasses should override, if properties are added.
     * @return the list of properties as array of {@link Observable}
     */
    public Observable[] getObservableProperties()
    {
        return new Observable[] { colorProperty(), startTimeProperty(), endTimeProperty(), foregroundProperty() };
    }
}
