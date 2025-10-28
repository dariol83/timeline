package eu.dariolucia.jfx.timeline.model;

/**
 * This enum is the type of label displayed in the task item, used for a
 * {@link TimePoint}.
 */
public enum TimePointType {
    /**
     * A square with rounded corners, similar in shape to the task item box
     */
    ROUND_RECT,
    /**
     * A square with sharp corners
     */
    RECT,
    /**
     * Round label
     */
    CIRCLE,
    /**
     * Uses the image to determine the shape of the label
     */
    IMG
}
