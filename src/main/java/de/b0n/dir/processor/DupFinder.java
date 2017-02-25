package de.b0n.dir.processor;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueue;
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

    public Queue<UnmodifiableQueue<File>> searchDuplicatesIn(final File folder, final DupFinderCallback callback){
        final DuplicateLengthFinder lengthFinder = new DuplicateLengthFinder(this.model);
        lengthFinder.readFilesRecursiveOf(folder,callback);
        final DuplicateContentFinder contentFinder = new DuplicateContentFinder(this.model,callback);
        return contentFinder.determineDuplicates();
    }

}
