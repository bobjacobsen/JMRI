package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.gui.YardEditFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IgnoreUsedTrackFrameTest extends OperationsTestCase {
    
    @Test
    public void testButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        Track track = loc.getTrackByName("NI Yard", null);
        
        YardEditFrame yef = new YardEditFrame();
        yef.initComponents(track);

        IgnoreUsedTrackFrame iutf = new IgnoreUsedTrackFrame(yef);
        Assert.assertNotNull("exists",iutf);
        
        // confirm default
        Assert.assertEquals(0, track.getIgnoreUsedLengthPercentage());
        
        JemmyUtil.enterClickAndLeave(iutf.seventyfivePercent);
        JemmyUtil.enterClickAndLeave(iutf.saveButton);
        
        Assert.assertEquals(75, track.getIgnoreUsedLengthPercentage());
        
        JUnitUtil.dispose(iutf);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);
        YardEditFrame yef = new YardEditFrame();
        yef.initComponents(track);
        IgnoreUsedTrackFrame f = new IgnoreUsedTrackFrame(yef);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(IgnoreUsedTrackFrameTest.class);

}
