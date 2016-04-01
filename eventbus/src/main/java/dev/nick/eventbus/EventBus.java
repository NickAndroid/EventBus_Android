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

import android.app.Application;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.WorkerThreadHandler;
import com.nick.scalpel.annotation.opt.RetrieveBean;
import com.nick.scalpel.config.Configuration;
import com.nick.scalpel.core.utils.Preconditions;

import java.util.Arrays;

import dev.nick.eventbus.internal.EventsWirer;
import dev.nick.eventbus.internal.PublisherService;

/**
 * Created by nick on 16-4-1.
 * Email: nick.guo.dev@icloud.com
 */
public class EventBus {

    private static final String LOG_TAG = "EventBus";

    private boolean mDebugable;

    @RetrieveBean
    private PublisherService mService;

    @WorkerThreadHandler
    private Handler mHandler;

    private EventsWirer mWirer;

    private static EventBus sBus;

    private EventBus(Application application) {
        Scalpel scalpel = Scalpel.getInstance();
        if (scalpel == null) scalpel = Scalpel.create(application).config(Configuration.builder()
                .debug(true)
                .logTag(LOG_TAG)
                .build());
        scalpel.wire(application.getApplicationContext(), this);
        mWirer = new EventsWirer(scalpel.getConfiguration(), mService);
        scalpel.addClassWirer(mWirer);
        log("Event bus created!");
    }

    public synchronized static void create(@NonNull Application application) {
        create(application, false);
    }

    public synchronized static void create(@NonNull Application application, boolean debug) {
        if (sBus == null) sBus = new EventBus(Preconditions.checkNotNull(application));
        sBus.mDebugable = debug;
    }

    public static EventBus getInstance() {
        return sBus;
    }

    public void publish(@NonNull final Event event) {
        log("publish:" + event);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mService.publish(Preconditions.checkNotNull(event));
            }
        });
    }

    public void publishEmpty(final int... events) {
        log("publishEmpty:" + Arrays.toString(events));
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int e : events) {
                    publish(new Event(e));
                }
            }
        });
    }

    public void subscribe(@NonNull final EventReceiver receiver) {
        log("subscribe:" + receiver);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mService.subscribe(Preconditions.checkNotNull(receiver));
            }
        });
    }

    public void subscribe(final Object object) {
        log("subscribe:" + object);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mWirer.wire(Preconditions.checkNotNull(object));
            }
        });
    }

    private void log(Object message) {
        if (mDebugable) Log.d(LOG_TAG, String.valueOf(message));
    }
}
