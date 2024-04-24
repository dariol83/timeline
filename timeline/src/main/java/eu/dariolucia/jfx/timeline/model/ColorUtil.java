package eu.dariolucia.jfx.timeline.model;

import javafx.scene.paint.Color;

/**
 * Utility class with static methods for {@link Color} handling.
 */
public class ColorUtil {

    private ColorUtil() {
        throw new IllegalAccessError("No instance possible");
    }

    /**
     * Derive the odd color from a reference color via desaturation. If the color is white or black, a hardcoded
     * color is returned.
     * @param reference the color to process
     * @return the color to be used for the background of odd lines
     */
    public static Color computeOddColor(Color reference) {
        if(reference.equals(Color.WHITE)) {
            return new Color(0.97, 0.97, 0.97, 1.0);
        } else if(reference.equals(Color.BLACK)) {
            return new Color(0.15, 0.15, 0.15, 1.0);
        } else {
            return reference.desaturate();
        }
    }

    /**
     * Derive a color by decreasing or increasing the RGB value by a given factor (from 0.0 to 1.0)
     * @param reference the color to process
     * @param factor the derivation factor from 0.0 to 1.0
     * @return the new color
     */
    public static Color percentageUpdate(Color reference, double factor) {
        return new Color(reference.getRed() + reference.getRed() * factor,
                reference.getGreen() + reference.getGreen() * factor,
                reference.getBlue() + reference.getBlue() * factor,
                reference.getOpacity());
    }
}
