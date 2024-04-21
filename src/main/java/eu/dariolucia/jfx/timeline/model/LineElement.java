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

/** Common abstract class for elements handled in a {@link Timeline}. */
public abstract class LineElement implements ILineElement {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private ITaskLine parent;
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
    public ITaskLine getParent() {
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
    public void setParent(ITaskLine parent) {
        this.parent = parent;
    }

}
