package jmri.jmrit.operations.setup.gui;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditManifestHeaderTextFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestHeaderTextFrame t = new EditManifestHeaderTextFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testEditManifestHeaderTextFrameReset() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestHeaderTextFrame f = new EditManifestHeaderTextFrame();
        f.initComponents();
        
        EditManifestHeaderTextPanel p = (EditManifestHeaderTextPanel) f.getContentPane();
        
        Assert.assertFalse("dirty1", p.isDirty());
        p.road_TextField.setText("Test text reset");
        Assert.assertTrue("dirty2", p.isDirty());

        JemmyUtil.enterClickAndLeave(p.resetButton);
        Assert.assertFalse("dirty3", p.isDirty());
        
        Assert.assertEquals("confirm", Bundle.getMessage("Road"), p.road_TextField.getText());

        // done
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testEditManifestHeaderTextFrameSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestHeaderTextFrame f = new EditManifestHeaderTextFrame();
        f.initComponents();
        
        EditManifestHeaderTextPanel p = (EditManifestHeaderTextPanel) f.getContentPane();
        
        Assert.assertFalse("dirty1", p.isDirty());
        p.road_TextField.setText("Test text");
        Assert.assertTrue("dirty2", p.isDirty());

        JemmyUtil.enterClickAndLeave(p.saveButton);
        Assert.assertFalse("dirty", p.isDirty());
        
        Assert.assertTrue("default", p.printHeaderLine1.isSelected());
        JemmyUtil.enterClickAndLeave(p.printHeaderLine1);
        Assert.assertTrue("dirty", p.isDirty());
        JemmyUtil.enterClickAndLeave(p.saveButton);
        Assert.assertFalse("dirty", p.isDirty());

        // confirm change by reloading panel
        EditManifestHeaderTextFrame f2 = new EditManifestHeaderTextFrame();
        f2.initComponents();
        
        EditManifestHeaderTextPanel p2 = (EditManifestHeaderTextPanel) f2.getContentPane();
        Assert.assertFalse("dirty3", p2.isDirty());
        Assert.assertEquals("confirm", "Test text", p2.road_TextField.getText());
        Assert.assertFalse("confirm", p2.printHeaderLine1.isSelected());
        
        JemmyUtil.enterClickAndLeave(p2.resetButton);
        Assert.assertTrue("dirty4", p2.isDirty());
        
        JemmyUtil.enterClickAndLeave(p2.saveButton);
        Assert.assertFalse("dirty5", p2.isDirty());

        // done
        JUnitUtil.dispose(f);
        JUnitUtil.dispose(f2);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestHeaderTextFrame f = new EditManifestHeaderTextFrame();
        f.initComponents();
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(EditManifestHeaderTextFrameTest.class);

}
