package org.lastmilehealth.collect.android.manager;

import org.lastmilehealth.collect.android.listeners.OnEventListener;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Anton Donchev on 10.05.2017.
 */

public class EventHandlerImpl implements EventHandler {
    private final Collection<OnEventListener> onManagerEventListenerCollecttion = new HashSet<>();

    public void registerEventListener(OnEventListener listener) {
        if (listener != null) {
            onManagerEventListenerCollecttion.add(listener);
        }
    }

    public void unregisterEventListener(OnEventListener listener) {
        if (listener != null) {
            onManagerEventListenerCollecttion.remove(listener);
        }
    }

    public void onEvent(int event) {
        for (OnEventListener listener : onManagerEventListenerCollecttion) {
            if (listener != null) {
                listener.onEvent(event);
            }
        }
    }
}
