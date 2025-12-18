package jmri.jmrix.loconet;

import java.time.*;
import java.time.temporal.*;
import java.util.Date;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for LnClockControlTest class.
 *
 * @author Bob Jacobsen (c) 2025
 **/
public class LnClockControlTest {

    private LnClockControl ldt;
    private Date time;
    
    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;
    private SlotManager slotmanager;
 
    @Test
    public void testCheckStartValues(){
        assertEquals(1.0, ldt.getRate(), "initial rate");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();

        // prepare an interface
        lnis = new LocoNetInterfaceScaffold(memo);
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
      
        ldt = new LnClockControl(memo);
        time = ldt.getTime();
    }

    @AfterEach
    public void tearDown(){
        memo.dispose();
        lnis = null;

        JUnitUtil.tearDown();
    }

}
