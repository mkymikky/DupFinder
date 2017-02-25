package de.b0n.dir.processor;

import de.b0n.dir.DupFinderCallback;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public abstract class AbstractSearchProcessor {

    protected static final DupFinderCallback DUMMY_CALLBACK = new DupFinderCallback(){};

    protected final ProcessorID ID=new ProcessorID(this.getClass().getName());

}
