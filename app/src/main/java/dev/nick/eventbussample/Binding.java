package dev.nick.eventbussample;

import android.os.RemoteException;
import android.util.Log;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.IEventBus;
import dev.nick.eventbus.IEventReceiver;

/**
 * Created by nick on 16-4-2.
 * Email: nick.guo.dev@icloud.com
 */
public class Binding {
    IEventBus mBus;

    public Binding(IEventBus bus) {
        this.mBus = bus;
    }

    void bind() {
        try {
            mBus.subscribe(new IEventReceiver.Stub() {
                @Override
                public void onReceive(Event event) throws RemoteException {
                    Log.d("EventBus.Binding", "onReceive:" + event);
                }

                @Override
                public int[] events() throws RemoteException {
                    return new int[]{Constants.EVENT_FAB_CLICKED};
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
