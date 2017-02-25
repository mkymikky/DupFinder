package com.github.funthomas424242.unmodifiable;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public class UnmodifiableQueue<E> {

    protected final Link<E> root;
    protected final int size;

    protected UnmodifiableQueue(UnmodifiableQueue predecessorQueue, E element) {
        this.root = new Link<>(predecessorQueue.root, element);
        this.size = predecessorQueue.size+1;
    }

    public UnmodifiableQueue() {
        this.root=null;
        this.size=0;
    }

    public UnmodifiableQueue<E> addElement(final E element) {
        return new UnmodifiableQueue<>(this,element);
    }

    public int size(){
        return this.size;
    }

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
