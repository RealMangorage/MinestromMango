package org.mangorage.server.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public final class NonNullList<E> extends AbstractList<E> {
    private final List<E> list;
    @Nullable
    private final E defaultValue;

    public static <E> NonNullList<E> withSize(int size, E defaultValue) {
        Objects.requireNonNull(defaultValue);
        E[] objects = (E[]) new Object[size];
        Arrays.fill(objects, defaultValue);
        return new NonNullList<E>(Arrays.asList(objects), defaultValue);
    }

    @SafeVarargs
    public static <E> NonNullList<E> of(E defaultValue, E... elements) {
        return new NonNullList<E>(Arrays.asList(elements), defaultValue);
    }

    public static <E> NonNullList<E> of(E defaultValue, int size, E... elements) {
        if (elements.length > size)
            throw new IllegalStateException("Elements array cant be larger than size " + size);
        NonNullList<E> list = withSize(size, defaultValue);
        for (int i = 0; i < elements.length; i++) {
            list.set(i, elements[i]);
        }
        return list;
    }

    public static <E> NonNullList<E> of(E defaultValue, int size, List<E> list) {
        return of(defaultValue, size, (E[]) list.toArray());
    }

    private NonNullList(List<E> list, @Nullable E defaultValue) {
        this.list = list;
        this.defaultValue = defaultValue;
    }

    @NotNull
    public E get(int i) {
        return (E)this.list.get(i);
    }

    public E set(int i, E object) {
        Objects.requireNonNull(object);
        return (E)this.list.set(i, object);
    }

    public void add(int i, E object) {
        Objects.requireNonNull(object);
        this.list.add(i, object);
    }

    public E remove(int i) {
        return (E)this.list.remove(i);
    }

    public int size() {
        return this.list.size();
    }

    public void clear() {
        if (this.defaultValue == null) {
            super.clear();
        } else {
            for(int i = 0; i < this.size(); ++i) {
                this.set(i, this.defaultValue);
            }
        }

    }
}

