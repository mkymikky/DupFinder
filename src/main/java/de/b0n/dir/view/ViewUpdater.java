package de.b0n.dir.view;

import java.io.File;
import java.util.Queue;

/**
 * Created by huluvu424242 on 16.01.17.
 */
public abstract class ViewUpdater implements Runnable {


    protected final Queue<Queue<File>> duplicates;

    public Queue<Queue<File>> getDuplicates(){
        return duplicates;
    }

    public ViewUpdater(final Queue<Queue<File>> duplicates) {
        this.duplicates = duplicates;
    }

    @Override
    public void run() {
       doUpdate();
    }

    abstract void doUpdate();

}
