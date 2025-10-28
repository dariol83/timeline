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

/**
 * Common interface for all line-related objects.
 */
public interface ILineElement {

    /**
     * Return the name of the line element, which is displayed in the task panel.
     * @return the name of the line element
     */
    String getName();

    /**
     * Return the description of the line element.
     * @return the name of the line element
     */
    String getDescription();

    /**
     * Set the parent task line of this line element. This is null if the line element is a top level task line.
     * This method is not supposed to be called by external class users.
     * @param parent the parent task line
     */
    void setParent(ILineElement parent);

    /**
     * Get the parent task line of this line element. This is null if the line element is a top level task line.
     * This method is not supposed to be called by external class users.
     * @return the parent task line, or null if this is a top level line element
     */
    ILineElement getParent();

    /**
     * Set the owning timeline of this line element. This is null if the line element is not in a {@link Timeline}.
     * This method is not supposed to be called by external class users.
     * @param timeline the owning {@link Timeline}, or null if this is not part of a {@link Timeline}
     */
    void setTimeline(Timeline timeline);

    /**
     * Get the owning {@link Timeline} of this line element. This is null if the line element is not in a {@link Timeline}.
     * This method is not supposed to be called by external class users.
     * @return the owning {@link Timeline} of this line element, or null if this is not part of a {@link Timeline}
     */
    Timeline getTimeline();
}
