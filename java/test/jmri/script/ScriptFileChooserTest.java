package jmri.script;

import jmri.util.JUnitUtil;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ScriptFileChooserTest {

    @Test
    public void testCTor() {
        ScriptFileChooser t = new ScriptFileChooser();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void checkForPy3Filter() {
        var file = new java.io.File("jython/test/Python3Test.py3");
        ScriptFileChooser t = new ScriptFileChooser();
        for (FileFilter filter : t.getChoosableFileFilters()) {
            if (filter.equals(t.getAcceptAllFileFilter())) continue; // accepts everything
            if (filter.accept(file)) return; // passes
        }
        Assert.fail("No chooser found");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ScriptFileChooserTest.class);

}
