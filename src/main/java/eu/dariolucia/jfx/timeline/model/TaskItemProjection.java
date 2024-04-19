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

/**
 * This enum is a hint to request the visualisation of the task item projection (aggregated start/end time) for a
 * {@link CompositeTaskLine}.
 */
public enum TaskItemProjection {
    /**
     * Never show the task item projection for the composite
     */
    NONE,
    /**
     * Show the task item projection for the composite on collapse
     */
    COLLAPSE,
    /**
     * Always show the task item projection for the composite
     */
    ALWAYS
}
