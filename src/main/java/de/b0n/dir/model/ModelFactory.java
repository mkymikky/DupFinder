package de.b0n.dir.model;

import de.b0n.dir.processor.AbstractProcessorModel;

/**
 * Created by huluvu424242 on 05.03.17.
 */
public interface ModelFactory<G,E> {

    AbstractProcessorModel<G,E> createModel();
}
