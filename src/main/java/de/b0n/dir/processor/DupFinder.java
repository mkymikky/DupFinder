package de.b0n.dir.processor;

import de.b0n.dir.DupFinderCallback;

import java.io.File;
import java.util.Queue;

/**
 * Created by huluvu424242 on 22.02.17.
 */
public class DupFinder {

    final protected Cluster<Long, File> model;

    public DupFinder(final Cluster<Long, File> model) {
        this.model = model;
    }

    public Queue<Queue<File>> searchDuplicatesIn(final File folder, final DupFinderCallback callback){
        final DuplicateLengthFinder lengthFinder = new DuplicateLengthFinder(this.model,callback);
        lengthFinder.readFilesRecursiveOf(folder);
        final DuplicateContentFinder contentFinder = new DuplicateContentFinder(this.model,callback);
        return contentFinder.determineDuplicates();
    }

}
