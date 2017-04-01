/*
 * Copyright 2017 philj.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ideastormsoftware.presmedia.sources;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author philj
 */
public interface SyncSource {

    static final ConcurrentHashMap<Object, Set<SyncSourceListener>> listenerMap = new ConcurrentHashMap<>();

    default Set<SyncSourceListener> getListeners() {
        return listenerMap.computeIfAbsent(this, (t) -> Collections.synchronizedSet(new HashSet<SyncSourceListener>()));
    }

    default void addListener(SyncSourceListener l) {
        Set<SyncSourceListener> listeners = getListeners();
        listeners.add(l);
    }

    default void removeListener(SyncSourceListener l) {
        Set<SyncSourceListener> listeners = getListeners();
        listeners.remove(l);
    }

    default void notifyListeners() {
        Set<SyncSourceListener> listeners = getListeners();
        synchronized (listeners) {
            listeners.forEach(SyncSourceListener::frameNotify);
        }
    }
}
