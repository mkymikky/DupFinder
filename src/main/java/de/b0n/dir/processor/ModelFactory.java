package de.b0n.dir.processor;

/**
 * Created by huluvu424242 on 05.03.17.
 */
public interface ModelFactory<G,E> {

    AbstractModel<G,E> createModel();
}
