package com.github.funthomas424242.unmodifiable;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public class UnmodifiableQueueFIFOTest {


    @Test
    public void erstelleLeeresListenObjekt() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<>();
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void fuegeEintragZuLeererListeHinzu() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<>();
        assertNotNull(queue);
        assertEquals(0, queue.size());
        final UnmodifiableQueueFIFO<String> newQueue = queue.addElement("Hallo");
        assertNotNull(newQueue);
        assertEquals(1, newQueue.size());
        assertFalse(newQueue.isEmpty());
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void fuegeNullEintragZuLeererListeHinzu() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<>();
        final UnmodifiableQueueFIFO<String> newQueue = queue.addElement(null);
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
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<>();
        final UnmodifiableQueueFIFO<String> newQueue = queue.addElement(null).addElement("Heinz").addElement("Karl");
        assertNotNull(newQueue);
        assertEquals(3, newQueue.size());
        // alte Kette unverändert
        assertNotNull(queue);
        assertEquals(0, queue.size());
    }

    @Test
    public void peekLiefertDasErsteEingefuegteElementNull() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl");
        final String firstElement = queue.peek();
        assertNull(firstElement);
    }

    @Test
    public void peekLiefertDasErsteEingefuegteElement() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<String>()
                .addElement("Bernd").addElement("Heinz").addElement("Karl");
        final String firstElement = queue.peek();
        assertEquals("Bernd", firstElement);
    }

    @Test
    public void peekLiefertNullBeiLeererQueue() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<String>();
        final String lastElement = queue.peek();
        assertNull(lastElement);
    }

    @Test
    public void iteratorLiefertElementeFIFO() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueFIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl");
        final Iterator<String> iterator = queue.iterator();
        assertTrue(iterator.hasNext());
        assertNull(iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Heinz", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Karl", iterator.next());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void removeLiefertEineUmsEinsKleinereKette() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueFIFO<String>()
                .addElement("1").addElement("2").addElement("3");
        assertEquals(3, queue.size());
        final UnmodifiableQueue<String> newQueue = queue.removeElement();
        assertNotNull(newQueue);
        assertEquals(2, newQueue.size());
        assertEquals("2", newQueue.peek());
        // alte Kette unverändert
        assertEquals(3, queue.size());
    }

    @Test
    public void removeAufEinserKetterLiefertLeereKette() {
        final UnmodifiableQueue<String> queue = new UnmodifiableQueueFIFO<String>().addElement("3");
        assertEquals(1, queue.size());
        assertEquals("3",queue.peek());
        final UnmodifiableQueue<String> newQueue = queue.removeElement();
        assertNotNull(newQueue);
        assertEquals(0, newQueue.size());
        assertNull(newQueue.peek());
        // alte Kette unverändert
        assertEquals(1, queue.size());
        assertEquals("3",queue.peek());
    }

    @Test
    public void removeAufLeereKetteLiefertNull() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<String>();
        assertEquals(0, queue.size());
        final UnmodifiableQueue<String> newQueue = queue.removeElement();
        assertNull(newQueue);
        // alte Kette unverändert
        assertEquals(0, queue.size());
    }

    @Test
    public void toArrayGibtEinGefuelltesObjectArrayZurueck() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl").addElement("Mark");
        final int size = queue.size();
        assertEquals(4, size);
        final Object[] elements = queue.toArray();
        assertNotNull(elements);
        assertEquals(size, elements.length);
    }

    @Test
    public void toArrayGibtEinGefuelltesElementArrayZurueck() {
        final UnmodifiableQueueFIFO<String> queue = new UnmodifiableQueueFIFO<String>()
                .addElement(null).addElement("Heinz").addElement("Karl").addElement("Mark");
        final int size = queue.size();
        assertEquals(4, size);
        final String[] arrayToFill = new String[size];
        queue.to(arrayToFill);
        assertNotNull(arrayToFill);
        assertEquals(size, arrayToFill.length);
    }

}