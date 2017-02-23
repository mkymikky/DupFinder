package de.b0n.dir.processor;

import de.b0n.dir.DupFinderCallback;

import java.io.File;
import java.util.Queue;

/**
 * Created by huluvu424242 on 23.02.17.
 */
public class DummyCallback implements DupFinderCallback{
    @Override
    public void failedFiles(int size) {

    }

    @Override
    public void duplicateGroup(Queue<File> duplicateGroup) {

    }

    @Override
    public void uniqueFiles(int uniqueFileCount) {

    }

    @Override
    public void enteredNewFolder(File folder) {

    }

    @Override
    public void unreadableFolder(File folder) {

    }
}
