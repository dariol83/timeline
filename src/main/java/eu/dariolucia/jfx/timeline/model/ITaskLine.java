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
import javafx.beans.Observable;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * This interface is used to abstract the access to the lines of a timeline (group or single).
 */
public interface ITaskLine {

    String getName();

    String getDescription();

    int getNbOfLines();

    void render(GraphicsContext gc, double taskLineXStart, double taskLineYStart, RenderingContext rc);

    void noRender();

    boolean isRendered();

    boolean contains(double x, double y);

    Observable[] getObservableProperties();

    void setParent(ITaskLine parent);

    ITaskLine getParent();

    List<TaskItem> getTaskItems();

    void setTimeline(Timeline timeline);

    Timeline getTimeline();
}
