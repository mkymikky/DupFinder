package com.github.funthomas424242.unmodifiable;

import java.util.Iterator;

/**
 * Created by huluvu424242 on 25.02.17.
 */
// TODO Implementierung falsch
public class UnmodifiableQueueFIFO<E> implements UnmodifiableQueue<E> {

    protected final Link<E> root;
    protected final Link<E> firstChainLink;
    protected final int size;

    /**
     * Konstruiert eine neue Queue durch Anhängen eines Elements an eine bestehende Queue.
     *
     * @param predecessorQueue
     * @param element
     */
    protected UnmodifiableQueueFIFO(UnmodifiableQueueFIFO<E> predecessorQueue, E element) {
        this.root = new Link<E>(predecessorQueue.root, element);
        this.size = predecessorQueue.size + 1;
        if (predecessorQueue.firstChainLink == null) {
            this.firstChainLink = root;
        } else {
            this.firstChainLink = predecessorQueue.firstChainLink;
        }
    }

    /**
     * Konstruiert eine neue Queue ab einem bestimmten breits existierenden Kettenglied.
     *
     * @param rootLink
     * @param firstChainLink
     * @param size
     */
    protected UnmodifiableQueueFIFO(final Link<E> rootLink, final Link<E> firstChainLink, final int size) {
        this.root = rootLink;
        this.firstChainLink = firstChainLink;
        this.size = size;
    }

    /**
     * Konstruiert eine leer Queue.
     */
    public UnmodifiableQueueFIFO() {
        this.root = null;
        this.firstChainLink = null;
        this.size = 0;
    }

    @Override
    public UnmodifiableQueueFIFO<E> addElement(final E element) {

        return new UnmodifiableQueueFIFO<E>(this, element);
    }

    @Override
    public UnmodifiableQueue<E>  removeElement() {
        if (isEmpty()) {
            return null;
        } else {
            if(size()==1) {
                return new UnmodifiableQueueFIFO<E>(root.getPredecessor(), null, size - 1);
            }else {
                return new UnmodifiableQueueFIFO<E>(root.getPredecessor(), firstChainLink, size - 1);
            }
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public E peek() {
        if (isEmpty()) {
            return null;
        } else {
            return root.getElement();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            // TODO Implementierung falsch
            Link<E> curLink = root;

            @Override
            public boolean hasNext() {
                return curLink != null;
            }

            @Override
            public E next() {
                final E nextElement = curLink.getElement();
                curLink = curLink.getPredecessor();
                return nextElement;
            }
        };
    }

    @Override
    public Object[] toArray() {
        final Object[] elements = new Object[size()];
        final Iterator<E> iterator = this.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            elements[index] = iterator.next();
            index++;
        }
        return elements;
    }


    @Override
    public void to(final E[] arrayToFill) {

        final Iterator<E> iterator = this.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            arrayToFill[index] = iterator.next();
            index++;
        }

    }


//
//    @Override
//    public boolean contains(Object o) {
//        return false;
//    }
//
//
//
//    @Override
//    public boolean add(E e) {
//        return false;
//    }
//
//    @Override
//    public boolean remove(Object o) {
//        return false;
//    }
//
//    @Override
//    public boolean containsAll(Collection<?> c) {
//        return false;
//    }
//
//    @Override
//    public boolean addAll(Collection<? extends E> c) {
//        return false;
//    }
//
//    @Override
//    public boolean removeAll(Collection<?> c) {
//        return false;
//    }
//
//    @Override
//    public boolean retainAll(Collection<?> c) {
//        return false;
//    }
//
//    @Override
//    public void clear() {
//
//    }
//
//    @Override
//    public boolean offer(E e) {
//        return false;
//    }
//
//    @Override
//    public E remove() {
//        return null;
//    }
//
//    @Override
//    public E poll() {
//        return null;
//    }
//
//    @Override
//    public E element() {
//        return null;
//    }


    /**
     * Kettenglieder
     *
     * @param <ET> Type der Elemente in der Kette.
     */
    protected class Link<ET> {

        protected Link<ET> predecessor;
        //        protected Link startLink;
        protected ET element;

        protected Link(final Link<ET> predecessor, final ET element) {
            this.predecessor = predecessor;
//            this.startLink = startLink;
            this.element = element;
        }

        protected Link<ET> getPredecessor() {
            return this.predecessor;
        }

        protected ET getElement() {
            return this.element;
        }

//        protected  Link getStartLink() {return this.startLink;}

    }

}
