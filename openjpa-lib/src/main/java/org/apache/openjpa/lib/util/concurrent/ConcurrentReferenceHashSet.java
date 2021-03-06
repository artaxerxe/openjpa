/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.apache.openjpa.lib.util.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.set.MapBackedSet;

/**
 * A concurrent set whose values may be stored as weak or soft references.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ConcurrentReferenceHashSet implements Set, Serializable {

    /**
     * Hard reference marker.
     */
    public static final int HARD = 0;

    /**
     * Soft reference marker.
     */
    public static final int SOFT = 1;

    /**
     * Weak reference marker.
     */
    public static final int WEAK = 2;

    private static final Object DUMMY_VAL = new Object();

    private final Set _set;

    /**
     * Construct a set with the given reference type.
     */
    public ConcurrentReferenceHashSet(int refType) {
        if (refType == HARD)
            _set = MapBackedSet.decorate(new ConcurrentHashMap(), DUMMY_VAL);
        else {
            int mapRefType = (refType == WEAK) ? ConcurrentReferenceHashMap.WEAK
                : ConcurrentReferenceHashMap.SOFT;
            _set = MapBackedSet.decorate(new ConcurrentReferenceHashMap
                (mapRefType, ConcurrentReferenceHashMap.HARD), DUMMY_VAL);
        }
    }

    public boolean add(Object obj) {
        return _set.add(obj);
    }

    public boolean addAll(Collection coll) {
        return _set.addAll(coll);
    }

    public void clear() {
        _set.clear();
    }

    public boolean contains(Object obj) {
        return _set.contains(obj);
    }

    public boolean containsAll(Collection coll) {
        return _set.containsAll(coll);
    }

    public boolean isEmpty() {
        return _set.isEmpty();
    }

    public Iterator iterator() {
        return _set.iterator();
    }

    public boolean remove(Object obj) {
        return _set.remove(obj);
    }

    public boolean removeAll(Collection coll) {
        return _set.removeAll(coll);
    }

    public boolean retainAll(Collection coll) {
        return _set.retainAll(coll);
    }

    public int size() {
        return _set.size();
    }

    public Object[] toArray() {
        return _set.toArray();
    }

    public Object[] toArray(Object[] arr) {
        return _set.toArray(arr);
    }

    public int hashCode() {
        return _set.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof ConcurrentReferenceHashSet)
            obj = ((ConcurrentReferenceHashSet) obj)._set;
        return _set.equals(obj);
    }
}
