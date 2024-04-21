package eu.dariolucia.jfx.timeline.model;

import javafx.scene.paint.Color;

/**
 * Utility class with static methods for {@link Color} handling.
 */
public class ColorUtil {

    private ColorUtil() {
        throw new IllegalAccessError("No instance possible");
    }

    public static Color computeOddColor(Color reference) {
        if(reference.equals(Color.WHITE)) {
            return new Color(0.97, 0.97, 0.97, 1.0);
        } else if(reference.equals(Color.BLACK)) {
            return new Color(0.15, 0.15, 0.15, 1.0);
        } else {
            return reference.desaturate();
        }
    }

    public static Color percentageUpdate(Color reference, double factor) {
        return new Color(reference.getRed() + reference.getRed() * factor,
                reference.getGreen() + reference.getGreen() * factor,
                reference.getBlue() + reference.getBlue() * factor,
                reference.getOpacity());
    }
}
