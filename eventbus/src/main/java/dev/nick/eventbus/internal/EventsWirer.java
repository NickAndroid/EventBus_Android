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

package dev.nick.eventbus.internal;

import android.support.annotation.NonNull;

import com.nick.scalpel.config.Configuration;
import com.nick.scalpel.core.AbsClassWirer;
import com.nick.scalpel.core.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventReceiver;
import dev.nick.eventbus.annotation.CallInMainThread;
import dev.nick.eventbus.annotation.Events;
import dev.nick.eventbus.annotation.ReceiverMethod;

/**
 * Created by nick on 16-4-1.
 * Email: nick.guo.dev@icloud.com
 */
public class EventsWirer extends AbsClassWirer {

    private Subscriber mSubscriber;

    public EventsWirer(Configuration configuration, Subscriber subscriber) {
        super(configuration);
        this.mSubscriber = subscriber;
    }

    @Override
    public void wire(final Object o) {
        Class clz = o.getClass();

        int[] events = null;

        if (clz.isAnnotationPresent(Events.class)) {
            Events annotation = (Events) clz.getAnnotation(Events.class);
            events = annotation.value();
        }

        Method[] methods = clz.getDeclaredMethods();

        for (final Method m : methods) {
            ReflectionUtils.makeAccessible(m);
            int modifier = m.getModifiers();
            boolean isPublic = Modifier.isPublic(modifier);
            if (!isPublic) continue;
            String methodName = m.getName();
            boolean isHandle = methodName.startsWith("handle")
                    || m.isAnnotationPresent(ReceiverMethod.class);
            if (!isHandle) continue;

            boolean noParam;

            Class[] params = m.getParameterTypes();
            noParam = params.length == 0;

            final boolean eventParam = params.length == 1 && params[0] == Event.class;

            if (!noParam && !eventParam) continue;

            int[] usingEvents = events;
            if (m.isAnnotationPresent(Events.class)) {
                Events methodAnno = m.getAnnotation(Events.class);
                usingEvents = methodAnno.value();
                logD("Using method annotation events for:" + methodName);
            }

            if (usingEvents == null) continue;

            final boolean callInMain = m.isAnnotationPresent(CallInMainThread.class);

            EventReceiver receiver = new SimpleEventsReceiver(usingEvents, methodName) {
                @Override
                public void onReceive(@NonNull Event event) {
                    logV("Invoking for event:" + event);
                    if (eventParam)
                        ReflectionUtils.invokeMethod(m, o, event);
                    else ReflectionUtils.invokeMethod(m, o);
                }

                @Override
                public boolean callInMainThread() {
                    return callInMain;
                }
            };

            logV("Creating receiver:" + receiver);

            mSubscriber.subscribe(receiver);
        }
    }

    @Override
    public Class<? extends Annotation> annotationClass() {
        return Events.class;
    }

    abstract class SimpleEventsReceiver implements EventReceiver {

        int[] events;
        // For debug.
        String methodName;

        public SimpleEventsReceiver(int[] events, String methodName) {
            this.events = events;
            this.methodName = methodName;
        }

        @Override
        public int[] events() {
            return events;
        }

        @Override
        public String toString() {
            return "SimpleEventsReceiver{" +
                    "events=" + events +
                    ", methodName='" + methodName + '\'' +
                    '}';
        }
    }
}
