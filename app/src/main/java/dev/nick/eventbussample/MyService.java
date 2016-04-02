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

package dev.nick.eventbussample;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventBus;
import dev.nick.eventbus.EventReceiver;
import dev.nick.eventbus.annotation.Events;
import dev.nick.eventbus.annotation.ReceiverMethod;

/**
 * Created by nick on 16-4-1.
 * Email: nick.guo.dev@icloud.com
 */
@Events(Constants.EVENT_ACTIVITY_FINISHED)
public class MyService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("Service started.");
        EventBus.getInstance().subscribe(this);
        customReceiver();
    }

    @Events(Constants.EVENT_FAB_CLICKED)
    public void handleFabClick() {
        log("handleFabClick");
    }

    @Events(Constants.EVENT_FAB_CLICKED)
    public void handleFabClickWithParam(Event event) {
        log("handleFabClickWithParam:" + event);
    }

    @ReceiverMethod
    @Events({Constants.EVENT_FAB_CLICKED, Constants.EVENT_ACTIVITY_FINISHED})
    public void customName() {
        log("customName");
    }

    @ReceiverMethod
    public void customNameWithParam(Event event) {
        log("customNameWithParam:" + event);
    }

    void log(Object msg) {
        Thread calling = Thread.currentThread();
        Log.d("EventBusSample", String.valueOf(msg) + ", calling in thread:" + calling.getName());

        Bundle data = new Bundle();
        data.putString("data", String.valueOf(msg));
        Event event = new Event(Constants.EVENT_LOG, data);

        EventBus.getInstance().publish(event);
    }

    void customReceiver() {
        EventBus.getInstance().subscribe(new EventReceiver() {
            @Override
            public void onReceive(@NonNull Event event) {
                log("onReceive:" + event);
            }

            @Override
            public int[] events() {
                return new int[]{Constants.EVENT_FAB_CLICKED};
            }

            @Override
            public boolean callInMainThread() {
                return false;
            }
        });
    }
}
