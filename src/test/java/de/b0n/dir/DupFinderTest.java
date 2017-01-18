package de.b0n.dir;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Created by huluvu424242 on 15.01.17.
 */
public class DupFinderTest {

    private static final String OS_NAME = System.getProperty("os.name");

    String unreadableFolder;

    @Before
    public void setUp() {

        // each OS must be add to supportedOS() too
        if ("Linux".equals(OS_NAME)) {
            unreadableFolder = "/root";
        } else {
            unreadableFolder = null;
        }
    }

    /**
     * OS currently supported by specific tests
     *
     * @return
     */
    private boolean supportedOS() {
        final String[] supportedOS = new String[]{"Linux"};
        return Arrays.asList(supportedOS).contains(OS_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore  //TODO Nullpointer gewollt?
    public void nullArguments() throws InterruptedException {
        DupFinder.main(null);
    }

    @Test
    public void noArguments() throws IOException, InterruptedException {
        try {
            DupFinder.main(new String[]{"abc"});
            fail();

        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().startsWith("FEHLER: Parameter <Verzeichnis> existiert nicht:"));
        }catch (Throwable th){
            throw th;
        }
    }

    @Test
    public void noFolderArgument() throws IOException, InterruptedException {
        try {
            DupFinder.main(new String[]{"pom.xml"});
            fail();

        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().startsWith("FEHLER: Parameter <Verzeichnis> ist kein Verzeichnis:"));
        }catch (Throwable th){
            throw th;
        }
    }

    @Test
    public void noReadableFolderArgument() throws IOException, InterruptedException {
        // conditional test
        assumeTrue(supportedOS());

        try {
            DupFinder.main(new String[]{unreadableFolder});
            fail();

        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().startsWith("FEHLER: Parameter <Verzeichnis> ist nicht lesbar:"));
        }catch (Throwable th){
           throw th;
        }

    }
}
