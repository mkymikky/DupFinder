package de.b0n.dir.view;


import java.io.File;
import java.util.Queue;

/**
 * Created by huluvu424242 on 16.01.17.
 */
public interface AbstractView {

    ViewUpdater createViewUpdater(final Queue<Queue<File>> duplicates);

}
