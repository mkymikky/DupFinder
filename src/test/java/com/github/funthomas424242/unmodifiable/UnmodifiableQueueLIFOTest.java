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
        final UnmodifiableQueue<String> newQueue = queue.addElement("Hallo");
        assertNotNull(newQueue);
        assertEquals(1, newQueue.size());
        assertFalse(newQueue.isEmpty());
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void fuegeNullEintragZuLeererListeHinzu() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueLIFO<>();
        final UnmodifiableQueue<String> newQueue = queue.addElement(null);
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
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueLIFO<>();
        final UnmodifiableQueue<String> newQueue = queue.addElement(null).addElement("Heinz").addElement("Karl");
        assertNotNull(newQueue);
        assertEquals(3, newQueue.size());
        // alte Kette unverändert
        assertNotNull(queue);
        assertEquals(0, queue.size());
    }

    @Test
    public void peekLiefertDasLetzteEingefuegteElement() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueLIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl");
        final String lastElement = queue.peek();
        assertEquals("Karl", lastElement);
    }

    @Test
    public void peekLiefertNullBeiLeererQueue() {
        final UnmodifiableQueueLIFO<String> queue = new UnmodifiableQueueLIFO<String>();
        final String lastElement = queue.peek();
        assertNull(lastElement);
    }

    @Test
    public void iteratorLiefertElementeLIFO() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueLIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl");
        final Iterator<String> iterator = queue.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("Karl", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Heinz", iterator.next());
        assertTrue(iterator.hasNext());
        assertNull(iterator.next());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void removeLiefertEineUmsEinsKleinereKette() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueLIFO<String>()
                .addElement("1").addElement("2").addElement("3");
        assertEquals(3, queue.size());
        final UnmodifiableQueue<String> newQueue = queue.removeElement();
        assertNotNull(newQueue);
        assertEquals(2,newQueue.size());
        assertEquals("2",newQueue.peek());
        // alte Kette unverändert
        assertEquals(3, queue.size());
    }

    @Test
    public void removeAufLeereKetteLiefertNull() {
        final UnmodifiableQueueLIFO<String> queue = new UnmodifiableQueueLIFO<String>();
        assertEquals(0, queue.size());
        final UnmodifiableQueue<String> newQueue = queue.removeElement();
        assertNull(newQueue);
        // alte Kette unverändert
        assertEquals(0, queue.size());
    }

    @Test
    public void toArrayGibtEinGefuelltesObjectArrayZurueck() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueLIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl").addElement("Mark");
        final int size = queue.size();
        assertEquals(4,size);
        final Object[] elements=queue.toArray();
        assertNotNull(elements);
        assertEquals(size,elements.length);
    }

    @Test
    public void toArrayGibtEinGefuelltesElementArrayZurueck() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueLIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl").addElement("Mark");
        final int size = queue.size();
        assertEquals(4,size);
        final String[] arrayToFill = new String[size];
        queue.to(arrayToFill);
        assertNotNull(arrayToFill);
        assertEquals(size,arrayToFill.length);
    }

}