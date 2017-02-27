package com.github.funthomas424242.unmodifiable;

import java.util.Iterator;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public interface UnmodifiableQueue<E> {

    UnmodifiableQueue<E>  addElement(final E element);

    UnmodifiableQueue<E> removeElement();

    Iterator<E> iterator();

    int size();

    boolean isEmpty();

    E peek();

    Object[] toArray();

    void to(final E[] arrayToFill);


}
