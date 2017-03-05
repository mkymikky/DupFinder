package de.b0n.dir.model;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueue;
import com.github.funthomas424242.unmodifiable.UnmodifiableQueueFIFO;
import de.b0n.dir.processor.AbstractProcessorModel;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by huluvu424242 on 05.03.17.
 */
public class DupFinderModel<G,E> extends AbstractProcessorModel<G,E> {

    public DupFinderModel(){

    }

    @Override
    public ModelFactory getModelFactory() {

        return new ModelFactory(){

            @Override
            public AbstractProcessorModel createModel() {
                return new DupFinderModel();
            }
        };
    }

    /**
     * Liefert die Gesamtanzahl der Elemente über alle Gruppen. Falls dieser
     * AbstractProcessorModel mehr als Integer.MAX_VALUE Elemente enthält, liefert er
     * Integer.MAX_VALUE.
     *
     * @return Anzahl aller Elemente im AbstractProcessorModel
     */
    public int size() {
        int size = 0;
        for (UnmodifiableQueue<E> group : values()) {
            size += group.size();
            if (size < 0) {
                size = Integer.MAX_VALUE;
            }
        }
        return size;
    }





}
