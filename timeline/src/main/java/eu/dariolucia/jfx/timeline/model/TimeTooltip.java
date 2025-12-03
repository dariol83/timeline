package eu.dariolucia.jfx.timeline.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class TimeTooltip extends LineElement
{
    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/
    /**
     * Tooltip background (color, pattern, gradient).
     */
    private final SimpleObjectProperty<Paint> tooltipBackground = new SimpleObjectProperty<>(Color.LIGHTGRAY);
    /**
     * Tooltip border color.
     */
    private final SimpleObjectProperty<Color> tooltipBorderColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * Tooltip text color.
     */
    private final SimpleObjectProperty<Color> tooltipTextColor = new SimpleObjectProperty<>(Color.BLACK);

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/

    /**
     * Class constructor
     * @param msg Message to display in tooltip
     */
    public TimeTooltip(String msg)
    {
        super(msg);
    }

    /* *****************************************************************************************
     * Property Accessors
     * *****************************************************************************************/

    public Paint getTooltipBackground() {
        return tooltipBackground.get();
    }

    public void setTooltipBackground(Paint tooltipBackground) {
        this.tooltipBackground.set(tooltipBackground);
    }

    public SimpleObjectProperty<Paint> tooltipBackgroundProperty() {
        return tooltipBackground;
    }

    public Color getTooltipBorderColor() {
        return tooltipBorderColor.get();
    }

    public void setTooltipBorderColor(Color tooltipBorderColor) {
        this.tooltipBorderColor.set(tooltipBorderColor);
    }

    public SimpleObjectProperty<Color> tooltipBorderColorProperty() {
        return tooltipBorderColor;
    }

    public Color getTooltipTextColor() {
        return tooltipTextColor.get();
    }

    public void setTooltipTextColor(Color tooltipTextColor) {
        this.tooltipTextColor.set(tooltipTextColor);
    }

    public SimpleObjectProperty<Color> tooltipTextColorProperty() {
        return tooltipTextColor;
    }

    /* *****************************************************************************************
     * Rendering Methods
     * *****************************************************************************************/

    public void render(GraphicsContext gc, IRenderingContext rc, double startX, double startY) {
        String[] Lines = getName().split("\n");

        double Width = rc.getTextWidth(gc, getName()) + rc.getTextPadding()*2;
        double Height = rc.getTextHeight()*Lines.length + rc.getTextPadding()*2;

        gc.setFill(getTooltipBackground());
        gc.setStroke(getTooltipBorderColor());

        if(startX + rc.getTextPadding()*2 + Width <= rc.getViewPortEndX()) startX += rc.getTextPadding()*2; //Indentation so that the tooltip is not under the cursor
        else startX -= Width; //If the tooltip extends beyond the right border of the viewport, we will display the tooltip to the left of the cursor.

        gc.fillRect(startX, startY, Width, Height);
        gc.strokeRect(startX, startY, Width, Height);

        gc.setStroke(getTooltipTextColor());
        for(int i = 0; i < Lines.length; i++)
        {
            gc.strokeText(Lines[i], startX + rc.getTextPadding(), startY + rc.getTextHeight()*(i+1) + rc.getTextPadding());
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
                nameProperty(), tooltipBackgroundProperty(),
                tooltipBorderColorProperty(), tooltipTextColorProperty()};
    }
}
