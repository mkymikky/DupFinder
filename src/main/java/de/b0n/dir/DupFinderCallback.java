package de.b0n.dir;

import de.b0n.dir.processor.DuplicateContentFinderCallback;
import de.b0n.dir.processor.DuplicateLengthFinderCallback;

/**
 * Created by huluvu424242 on 22.02.17.
 */
public interface DupFinderCallback extends DuplicateLengthFinderCallback, DuplicateContentFinderCallback {

    public static final DupFinderCallback DUMMY_CALLBACK = new DupFinderCallback(){};

}
