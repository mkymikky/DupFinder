package de.b0n.dir.model;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueue;
import de.b0n.dir.processor.SearchProcessorModel;

/**
 * Created by huluvu424242 on 05.03.17.
 */
public class DupFinderModel<G,E> extends SearchProcessorModel<G,E> {

    public DupFinderModel(){

    }

    @Override
    public SearchProcessorModel createNewModel(){
        return new DupFinderModel();
    }

    /**
     * Liefert die Gesamtanzahl der Elemente über alle Gruppen. Falls dieser
     * SearchProcessorModel mehr als Integer.MAX_VALUE Elemente enthält, liefert er
     * Integer.MAX_VALUE.
     *
     * @return Anzahl aller Elemente im SearchProcessorModel
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
