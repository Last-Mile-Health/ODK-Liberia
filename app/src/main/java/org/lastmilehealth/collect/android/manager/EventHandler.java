package org.lastmilehealth.collect.android.manager;

import org.lastmilehealth.collect.android.listeners.OnEventListener;

/**
 * Created by Anton Donchev on 09.05.2017.
 */

public interface EventHandler {

    void registerEventListener(OnEventListener listener);

    void unregisterEventListener(OnEventListener listener);

    void onEvent(int event);
}
