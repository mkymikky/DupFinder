package de.b0n.dir;

import de.b0n.dir.processor.DuplicateLengthFinder;
import org.junit.Ignore;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.*;
import java.util.InputMismatchException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/**
 * Created by huluvu424242 on 15.01.17.
 */
public class DupFinderTest {


    private long copy(final InputStream inStream, final Writer writer) throws IOException {
        final InputStreamReader input = new InputStreamReader(inStream);
        char[] buffer = new char[1024];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            writer.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    @Test(expected = IllegalArgumentException.class)
    @Ignore  //TODO Nullpointer gewollt?
    public void nullArguments() {
        DupFinder.main(null);
    }

    @Test
    public void noArguments() throws IOException {
        final PipedInputStream inStream = new PipedInputStream();
        final PipedOutputStream outStream = new PipedOutputStream(inStream);
        final BufferedOutputStream bufOutStream = new BufferedOutputStream(outStream);
        final PrintStream printStream = new PrintStream(bufOutStream);
        System.setErr(printStream);
        try {


            DupFinder.main(new String[]{});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        final StringWriter writer = new StringWriter();

        assertEquals(" ", writer.toString());
        inStream.close();

    }

//    @Test(expected=IllegalArgumentException.class)
//    public void noThreadPool() {
//        final File folder = new File(PATH_SAME_SIZE_IN_FLAT_FOLDER);
//        DuplicateLengthFinder.getResult(null, folder);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void noFolder() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        DuplicateLengthFinder.getResult(threadPool, null);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void scanInvalidFolder() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        final File folder = new File(PATH_INVALID_FOLDER);
//        DuplicateLengthFinder.getResult(threadPool, folder);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void scanFile() {
//        final ExecutorService threadPool = Executors.newWorkStealingPool();
//        final File folder = new File(PATH_FILE);
//        DuplicateLengthFinder.getResult(threadPool, folder);
//    }
//


}
