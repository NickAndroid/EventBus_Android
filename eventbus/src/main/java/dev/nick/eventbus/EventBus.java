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
import android.os.IBinder;
import android.os.RemoteException;
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

    private Config mConfig;

    @RetrieveBean
    private PublisherService mService;

    @WorkerThreadHandler
    private Handler mHandler;

    private EventsWirer mWirer;

    private static EventBus sBus;

    private EventBus(Application application) {
        mConfig = new Config();
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

    public synchronized static Config create(@NonNull Application application) {
        if (sBus == null) sBus = new EventBus(Preconditions.checkNotNull(application));
        return sBus.mConfig;
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

    public void publishEmptyEvent(final int... events) {
        log("publishEmptyEvent:" + Arrays.toString(events));
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
        subscribeBinder(receiver);
    }

    private void subscribeBinder(@NonNull final IEventReceiver receiver) {
        log("subscribe:" + receiver);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mService.subscribe(Preconditions.checkNotNull(receiver));
            }
        });
    }

    public void subscribe(@NonNull final Object object) {
        log("subscribe:" + object);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mWirer.wire(Preconditions.checkNotNull(object));
            }
        });
    }

    public void unSubscribe(@NonNull final EventReceiver receiver) {
        log("unSubscribe:" + receiver);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mService.unSubscribe(Preconditions.checkNotNull(receiver));
            }
        });
    }

    public void unSubscribe(@NonNull final Object object) {
        log("unSubscribe:" + object);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mWirer.unWire(object);
            }
        });
    }

    private void log(Object message) {
        if (mConfig.debuggable) Log.d(LOG_TAG, String.valueOf(message));
    }

    public IBinder generateStub() {
        return new IEventBus.Stub() {

            @Override
            public void publish(Event event) throws RemoteException {
                EventBus.this.publish(event);
            }

            @Override
            public void publishEmptyEvent(int event) throws RemoteException {
                EventBus.this.publishEmptyEvent(event);
            }

            @Override
            public void subscribe(IEventReceiver receiver) throws RemoteException {
                EventBus.this.subscribeBinder(receiver);
            }

            @Override
            public void unSubscribe(IEventReceiver receiver) throws RemoteException {
                EventBus.this.unSubscribe(receiver);
            }
        };
    }

    public static class Config {
        private boolean debuggable;

        public void setDebuggable(boolean debuggable) {
            this.debuggable = debuggable;
        }
    }
}
