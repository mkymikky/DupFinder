package com.github.funthomas424242.unmodifiable;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public class UnmodifiableQueueLIFOTest {


    @Test
    public void erstelleLeeresListenObjekt() {
        final UnmodifiableQueueLIFO<String> queue = new UnmodifiableQueueLIFO<>();
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void fuegeEintragZuLeererListeHinzu() {
        final UnmodifiableQueueLIFO<String> queue = new UnmodifiableQueueLIFO<>();
        assertNotNull(queue);
        assertEquals(0, queue.size());
        final UnmodifiableQueueLIFO<String> newQueue = queue.addElement("Hallo");
        assertNotNull(newQueue);
        assertEquals(1, newQueue.size());
        assertFalse(newQueue.isEmpty());
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void fuegeNullEintragZuLeererListeHinzu() {
        final UnmodifiableQueueLIFO<String> queue = new UnmodifiableQueueLIFO<>();
        final UnmodifiableQueueLIFO<String> newQueue = queue.addElement(null);
        assertNotNull(newQueue);
        assertEquals(1, newQueue.size());
        assertFalse(newQueue.isEmpty());
        // alte Kette unverändert
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void fuegeDreiEintraegeZurLeerenListeHinzu() {
        final UnmodifiableQueueLIFO<String> queue = new UnmodifiableQueueLIFO<>();
        final UnmodifiableQueueLIFO<String> newQueue = queue.addElement(null).addElement("Heinz").addElement("Karl");
        assertNotNull(newQueue);
        assertEquals(3, newQueue.size());
        // alte Kette unverändert
        assertNotNull(queue);
        assertEquals(0, queue.size());
    }

    @Test
    public void peekLiefertDasLetzteEingefuegteElement() {
        final UnmodifiableQueueLIFO<String> queue = new UnmodifiableQueueLIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl");
        final String lastElement=queue.peek();
        assertEquals("Karl",lastElement);
    }

    @Test
    public void iteratorLiefertElementeLIFO() {
        final UnmodifiableQueueLIFO<String> queue = new UnmodifiableQueueLIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl");
       final Iterator<String> iterator=queue.iterator();
       while(iterator.hasNext()){
           assertEquals("Karl",iterator.next());
           assertEquals("Heinz",iterator.next());
           assertNull(iterator.next());
           assertFalse(iterator.hasNext());
       }
        assertFalse(iterator.hasNext());
    }

}