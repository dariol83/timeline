package eu.dariolucia.jfx.timeline.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
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
     * if null then stroke and fill color is Color.PEACHPUFF, but images will use their source color
     */
    private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(null);
    /**
     * Text color of the point.
     */
    private final SimpleObjectProperty<Color> textColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * It is used to store an image with a changed color, saves rendering time.
     */
    private Image cachedImage = null;
    /**
     * Tooltip for time point
     */
    private final SimpleObjectProperty<TimeTooltip> tooltip = new SimpleObjectProperty<>(null);

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

        // Clear cached image when set new image
        this.img.addListener((observable, oldValue, newValue) -> {
            cachedImage = null;
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

    public TimeTooltip getTooltip() {
        return tooltip.get();
    }

    public void setTooltip(TimeTooltip tooltip) {
        this.tooltip.set(tooltip);
    }

    public SimpleObjectProperty<TimeTooltip> tooltipProperty() {
        return tooltip;
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
        if(X > StartX+taskItemWidth || X < StartX)
        {
            noRender();
            return;
        }

        double MaxSize = taskItemHeight-(2*rc.getTextPadding());

        double size = (StartX+taskItemWidth)-X;
        if(size > MaxSize) size = MaxSize;

        StartY += taskItemHeight/2 - (int)size/2;

        if(getColor() == null)
        {
            gc.setStroke(Color.PEACHPUFF);
            gc.setFill(Color.PEACHPUFF);
        }
        else
        {
            gc.setStroke(getColor());
            gc.setFill(getColor());
        }
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
                //cachedImage used for optimised image recolor
                if(cachedImage == null)
                {
                    //if color has been changed, then replace image color
                    if(getColor() != null)
                    {
                        WritableImage writableImage = new WritableImage((int) getImage().getWidth(), (int) getImage().getHeight());

                        for (int y = 0; y < writableImage.getHeight(); y++)
                        {
                            for (int x = 0; x < writableImage.getWidth(); x++)
                            {
                                Color SourceColor = getImage().getPixelReader().getColor(x, y);
                                writableImage.getPixelWriter().setColor(x, y, (SourceColor.isOpaque()) ? getColor() : SourceColor);
                            }
                        }

                        cachedImage = writableImage;
                    }
                    //else use source image color
                    else cachedImage = getImage();
                }

                gc.drawImage(cachedImage, X, StartY, size, size);
            }
        }

        if(!getName().isBlank() && rc.getTextWidth(gc, getName()) <= size)
        {
            gc.setStroke(getTextColor());
            gc.setTextAlign(TextAlignment.CENTER);

            gc.strokeText(getName(), X + size/2, StartY + size/2 + rc.getTextHeight()/2.0);

            gc.setTextAlign(TextAlignment.LEFT);
        }

        // Remember rendering box in pixel coordinates
        updateLastRenderedBounds(new BoundingBox(X, StartY, size, size));
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
                imageProperty(), nameProperty(), typeProperty(), tooltipProperty(),
                timeProperty(), colorProperty(), textColorProperty() };
    }
}
