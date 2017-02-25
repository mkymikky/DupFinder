package com.github.funthomas424242.unmodifiable;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public class UnmodifiableQueueTest {


    @Test
    public void erstelleLeeresListenObjekt(){
        final UnmodifiableQueue<String> queue= new UnmodifiableQueue<>();
        assertNotNull(queue);
        assertEquals(0,queue.size());
    }

    @Test
    public void fuegeEintragZuLeererListeHinzu(){
        final UnmodifiableQueue<String> queue= new UnmodifiableQueue<>();

        final UnmodifiableQueue<String> newQueue=queue.addElement("Hallo");
        assertNotNull(newQueue);
        assertEquals(1,newQueue.size());
    }

    @Test
    public void fuegeNullEintragZuLeererListeHinzu(){
        final UnmodifiableQueue<String> queue= new UnmodifiableQueue<>();
        final UnmodifiableQueue<String> newQueue=queue.addElement(null);
        assertNotNull(newQueue);
        assertEquals(1,newQueue.size());
        // alte Kette unverändert
        assertNotNull(queue);
        assertEquals(0,queue.size());
    }

    @Test
    public void fuegeDreiEintraegeZurLeerenListeHinzu(){
        final UnmodifiableQueue<String> queue= new UnmodifiableQueue<>();
        final UnmodifiableQueue<String> newQueue=queue.addElement(null).addElement("Heinz").addElement("Karl");
        assertNotNull(newQueue);
        assertEquals(3,newQueue.size());
        // alte Kette unverändert
        assertNotNull(queue);
        assertEquals(0,queue.size());
    }

}