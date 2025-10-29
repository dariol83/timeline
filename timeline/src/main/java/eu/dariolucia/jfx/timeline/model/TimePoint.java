package eu.dariolucia.jfx.timeline.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.time.Instant;

public class TimePoint extends LineElement {
    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/
    /**
     * Image for displaying the label
     */
    private final SimpleObjectProperty<Image> img = new SimpleObjectProperty<>();
    /**
     * Type of the point.
     */
    private final SimpleObjectProperty<TimePointType> type = new SimpleObjectProperty<>();
    /**
     * Time of the point.
     */
    private final SimpleObjectProperty<Instant> time = new SimpleObjectProperty<>();
    /**
     * Color of the point.
     */
    private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(Color.PEACHPUFF);
    /**
     * Text color of the point.
     */
    private final SimpleObjectProperty<Color> textColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * It is used to store an image with a changed color, saves rendering time.
     */
    private WritableImage cachedWritableImage = null;

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/

    /**
     * Class constructor
     * @param name the name of the point
     * @param time the time of the point
     * @param Type the type of the point; see {@link TimePointType}
     */
    public TimePoint(String name, Instant time, TimePointType Type) {
        super(name);
        setTime(time);
        setType(Type);

        img.addListener((observable, oldValue, newValue) -> {
            cachedWritableImage = null;
        });
    }

    /* *****************************************************************************************
     * Property Accessors
     * *****************************************************************************************/

    public Image getImage() {
        return img.get();
    }

    public SimpleObjectProperty<Image> imageProperty() {
        return img;
    }

    public void setImage(Image img)
    {
        this.img.set(img);
    }

    public TimePointType getType() {
        return type.get();
    }

    public SimpleObjectProperty<TimePointType> typeProperty() {
        return type;
    }

    public void setType(TimePointType type)
    {
        this.type.set(type);
    }

    public Instant getTime() {
        return time.get();
    }

    public SimpleObjectProperty<Instant> timeProperty() {
        return time;
    }

    public void setTime(Instant time) {
        this.time.set(time);
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

    public Color getTextColor() {
        return textColor.get();
    }

    public SimpleObjectProperty<Color> textColorProperty() {
        return textColor;
    }

    public void setTextColor(Color color) {
        this.textColor.set(color);
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    /**
     * Render the time point. Subclasses can override.
     * @param gc the {@link GraphicsContext}
     * @param rc the {@link IRenderingContext}
     * @param StartX the start X of the parent task item in Canvas coordinates
     * @param StartY the start Y of the parent task item in Canvas coordinates
     * @param taskItemWidth the width of the parent task item
     * @param taskItemHeight the height of the parent task item
     */
    protected void render(GraphicsContext gc, IRenderingContext rc, int StartX, int StartY, int taskItemWidth, int taskItemHeight) {
        double X = rc.toX(getTime());
        if(X > StartX+taskItemWidth || X < StartX) return;

        double MaxSize = taskItemHeight-(2*rc.getTextPadding());

        double size = (StartX+taskItemWidth)-X;
        if(size > MaxSize) size = MaxSize;

        StartY += taskItemHeight/2 - (int)size/2;

        gc.setStroke(getColor());
        gc.setFill(getColor());
        gc.setFont(gc.getFont());

        gc.setLineWidth(1);

        switch (getType())
        {
            case CIRCLE:
            {
                gc.strokeOval(X, StartY, size, size);
                gc.fillOval(X, StartY, size, size);
                break;
            }
            case ROUND_RECT:
            {
                gc.strokeRoundRect(X, StartY, size, size, rc.getTextPadding(), rc.getTextPadding());
                gc.fillRoundRect(X, StartY, size, size, rc.getTextPadding(), rc.getTextPadding());
                break;
            }
            case RECT:
            {
                gc.strokeRect(X, StartY, size, size);
                gc.fillRect(X, StartY, size, size);
                break;
            }
            case IMG:
            {
                if(cachedWritableImage == null)
                {
                    cachedWritableImage = new WritableImage((int) getImage().getWidth(), (int) getImage().getHeight());

                    for (int y = 0; y < cachedWritableImage.getHeight(); y++)
                    {
                        for (int x = 0; x < cachedWritableImage.getWidth(); x++)
                        {
                            Color SourceColor = getImage().getPixelReader().getColor(x, y);
                            if(SourceColor.isOpaque()) cachedWritableImage.getPixelWriter().setColor(x, y, getColor());
                        }
                    }
                }

                gc.drawImage(cachedWritableImage, X, StartY, size, size);
            }
        }

        if(!getName().isBlank() && rc.getTextWidth(gc, getName()) <= size)
        {
            gc.setStroke(getTextColor());
            gc.setTextAlign(TextAlignment.CENTER);

            gc.strokeText(getName(), X + size/2, StartY + size/2 + rc.getTextHeight()/2.0);

            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    /**
     * Return the properties that should trigger an update notification in case of
     * change. Subclasses should override, if properties are added.
     * @return the list of properties as array of {@link Observable}
     */
    public Observable[] getObservableProperties() {
        return new Observable[] {
                imageProperty(), nameProperty(), typeProperty(),
                timeProperty(), colorProperty(), textColorProperty() };
    }
}
