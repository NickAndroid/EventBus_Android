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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nick.scalpel.ScalpelAutoActivity;
import com.nick.scalpel.annotation.binding.BindService;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventBus;
import dev.nick.eventbus.IEventBus;
import dev.nick.eventbus.annotation.CallInMainThread;
import dev.nick.eventbus.annotation.Events;

public class MainActivity extends ScalpelAutoActivity implements BindService.Callback {

    @BindService(action = "bind.eventbus", pkg = "dev.nick.eventbussample", autoUnbind = true, callback = "this")
    IEventBus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getInstance().publishEmptyEvent(Constants.EVENT_FAB_CLICKED);
            }
        });

        Intent intent = new Intent(this, MyService.class);
        startService(intent);

        EventBus.getInstance().subscribe(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().publishEmptyEvent(Constants.EVENT_ACTIVITY_FINISHED);
    }

    @CallInMainThread
    @Events(Constants.EVENT_LOG)
    public void handleLog(Event event) {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(event.getData().getString("data"));
    }

    @Override
    public void onServiceBound(ComponentName name, ServiceConnection connection, Intent intent) {
        Log.d("EventBus", "onServiceBound");
        new Binding(mBus).bind();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
