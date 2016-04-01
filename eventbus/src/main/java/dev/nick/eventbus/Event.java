/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.eventbus;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.nick.scalpel.core.utils.Preconditions;

/**
 * Created by nick on 16-4-1.
 * Email: nick.guo.dev@icloud.com
 */
public class Event implements Cloneable {

    int eventType;
    Bundle data;

    public Event(int eventType, Bundle data) {
        this.eventType = eventType;
        this.data = data;
    }

    public Event(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }

    public Bundle getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventType=" + eventType +
                ", data=" + data +
                '}';
    }

    public static Event fromClone(@NonNull Event event) {
        try {
            return (Event) Preconditions.checkNotNull(event, "Event cannot be null.").clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Internal error:" + e);
        }
    }
}
