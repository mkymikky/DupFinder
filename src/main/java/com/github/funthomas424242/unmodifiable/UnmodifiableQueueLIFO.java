package com.github.funthomas424242.unmodifiable;

import java.util.Iterator;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public class UnmodifiableQueueLIFO<E> implements UnmodifiableQueue<UnmodifiableQueueLIFO, E> {

    protected final Link<E> root;
    protected final int size;

    /**
     * Konstruiert eine neue Queue durch Anhängen eines Elements an eine bestehende Queue.
     *
     * @param predecessorQueue
     * @param element
     */
    protected UnmodifiableQueueLIFO(UnmodifiableQueueLIFO predecessorQueue, E element) {
        this.root = new Link<>(predecessorQueue.root, element);
        this.size = predecessorQueue.size + 1;
    }

    /**
     * Konstruiert eine neue Queue ab einem bestimmten breits existierenden Kettenglied.
     *
     * @param rootLink
     * @param size
     */
    protected UnmodifiableQueueLIFO(final Link<E> rootLink, final int size) {
        this.root = rootLink;
        this.size = size;
    }

    /**
     * Konstruiert eine leer Queue.
     */
    public UnmodifiableQueueLIFO() {
        this.root = null;
        this.size = 0;
    }

    @Override
    public UnmodifiableQueueLIFO<E> addElement(final E element) {
        return new UnmodifiableQueueLIFO<>(this, element);
    }

    @Override
    public UnmodifiableQueueLIFO<E> removeElement(){
        return new UnmodifiableQueueLIFO<E>(root.getPredecessor(),size -1);
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
        if(isEmpty()){
            return null;
        }else {
            return root.getElement();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

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


//
//    @Override
//    public boolean contains(Object o) {
//        return false;
//    }
//
//
//    @Override
//    public Object[] toArray() {
//        return new Object[0];
//    }
//
//    @Override
//    public <T> T[] toArray(T[] a) {
//        return null;
//    }
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

        protected Link predecessor;
        protected ET element;

        protected Link(final Link predecessor, final ET element) {
            this.predecessor = predecessor;
            this.element = element;
        }

        protected Link getPredecessor() {
            return this.predecessor;
        }

        protected ET getElement() {
            return this.element;
        }

    }

}
