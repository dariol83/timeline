/*
 * Copyright (c) 2024 Dario Lucia (https://www.dariolucia.eu)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package eu.dariolucia.jfx.timeline.model;

import eu.dariolucia.jfx.timeline.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.BoundingBox;

/** Common abstract class for elements handled in a {@link Timeline}. */
public abstract class LineElement implements ILineElement {

    /* *****************************************************************************************
     * Properties
     * *****************************************************************************************/

    /**
     * Name of the element.
     */
    private final SimpleStringProperty name = new SimpleStringProperty();
    /**
     * Description of the element.
     */
    private final SimpleStringProperty description = new SimpleStringProperty();
    /**
     * Last rendered bound of the element.
     */
    private BoundingBox lastRenderedBounds;

    /* *****************************************************************************************
     * Internal variables
     * *****************************************************************************************/

    private ILineElement parent;
    private Timeline timeline;

    /**
     * Class constructor with no description.
     * @param name the name of the element
     */
    protected LineElement(String name) {
        this(name, null);
    }

    /**
     * Class constructor with name and description.
     * @param name the name of the element
     * @param description the description of the element
     */
    protected LineElement(String name, String description) {
        this.name.set(name);
        this.description.set(description);
    }

    @Override
    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    @Override
    public ILineElement getParent() {
        return parent;
    }

    @Override
    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public void setParent(ILineElement parent) {
        this.parent = parent;
    }

    /* *****************************************************************************************
     * Class-specific Methods
     * *****************************************************************************************/

    /**
     * To be called by subclasses.
     * @param boundingBox the bounding box or null
     */
    protected final void updateLastRenderedBounds(BoundingBox boundingBox) {
        this.lastRenderedBounds = boundingBox;
    }

    /**
     * Return the latest rendered bounding box of the element, or null if not rendered. To be called by subclasses.
     * @return the latest rendered bounding box in canvas coordinates, or null if not rendered
     */
    protected final BoundingBox getLastRenderedBounds() {
        return lastRenderedBounds;
    }

    /**
     * Return true if the x,y values in canvas coordinates are contained in the bounds of the element.
     * @param x the x in canvas coordinates
     * @param y the y in canvas coordinates
     * @return true if the x,y values are contained in the bounds of the element, otherwise false
     */
    public final boolean contains(double x, double y) {
        return this.lastRenderedBounds != null && this.lastRenderedBounds.contains(x, y);
    }

    /**
     * Return true if the element was rendered in the last rendering iteration, otherwise false.
     * @return true if the element was rendered in the last rendering iteration, otherwise false
     */
    public final boolean isRendered() {
        return this.lastRenderedBounds != null;
    }

    /**
     * Subclasses can override, as long as the update of the boundaries in set to null.
     */
    public void noRender() {
        updateLastRenderedBounds(null);
    }
}
