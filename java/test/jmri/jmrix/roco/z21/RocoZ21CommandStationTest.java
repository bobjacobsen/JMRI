package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for RocoZ21CommandStation class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class RocoZ21CommandStationTest {

   @Test
   public void testConstructor(){
      Assert.assertNotNull("RocoZ21CommandStation constructor",new RocoZ21CommandStation());
   }

   @Test
   public void testSerialNumber(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertEquals("initial serial number",0,rcs.getSerialNumber());
      rcs.setSerialNumber(3456);
      Assert.assertEquals("serial number after set",3456,rcs.getSerialNumber());
   }

   @Test
   public void testSoftwareVersion(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertEquals("initial software version",0,rcs.getSoftwareVersion(),0.0);
      rcs.setSoftwareVersion(3456);
      Assert.assertEquals("software version after set",3456,rcs.getSoftwareVersion(),0.0);
   }

   @Test
   public void testHardwareVersion(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertEquals("initial hardware version",0,rcs.getHardwareVersion());
      rcs.setHardwareVersion(3456);
      Assert.assertEquals("software version after set",3456,rcs.getHardwareVersion());
   }

   @Test
   public void testBroadcastFlags(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertEquals("initial Broadcast Flags",0,rcs.getZ21BroadcastFlags());
      rcs.setZ21BroadcastFlags(3456);
      Assert.assertEquals("Broadcast Flags after set",3456,rcs.getZ21BroadcastFlags());
   }

   @Test
   public void testXPressNetFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial XpressNet Flag",rcs.getXPressNetMessagesFlag());
      rcs.setXPressNetMessagesFlag(true);
      Assert.assertTrue("XpressNet Flag after set",rcs.getXPressNetMessagesFlag());
      rcs.setXPressNetMessagesFlag(false);
      Assert.assertFalse("XpressNet Flag after reset",rcs.getXPressNetMessagesFlag());
   }

   @Test
   public void testRMBusFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial RMBus Flag",rcs.getRMBusMessagesFlag());
      rcs.setRMBusMessagesFlag(true);
      Assert.assertTrue("RMBus Flag after set",rcs.getRMBusMessagesFlag());
      rcs.setRMBusMessagesFlag(false);
      Assert.assertFalse("RMBus Flag after reset",rcs.getRMBusMessagesFlag());
   }

   @Test
   public void testRailComFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial RailCom Flag",rcs.getRailComMessagesFlag());
      rcs.setRailComMessagesFlag(true);
      Assert.assertTrue("RailCom Flag after set",rcs.getRailComMessagesFlag());
      rcs.setRailComMessagesFlag(false);
      Assert.assertFalse("RailCom Flag after reset",rcs.getRailComMessagesFlag());
   }

   @Test
   public void testSystemStatusFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial System Status Flag",rcs.getSystemStatusMessagesFlag());
      rcs.setSystemStatusMessagesFlag(true);
      Assert.assertTrue("SystemStatus Flag after set",rcs.getSystemStatusMessagesFlag());
      rcs.setXPressNetMessagesFlag(false);
      Assert.assertFalse("SystemStatus Flag after reset",rcs.getXPressNetMessagesFlag());
   }

   @Test
   public void testXPressNetLocoMotiveFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial XpressNet Locomotive Flag",rcs.getXPressNetLocomotiveMessagesFlag());
      rcs.setXPressNetLocomotiveMessagesFlag(true);
      Assert.assertTrue("XpressNet Locomotive Flag after set",rcs.getXPressNetLocomotiveMessagesFlag());
      rcs.setXPressNetLocomotiveMessagesFlag(false);
      Assert.assertFalse("XpressNet Locomotive Flag after reset",rcs.getXPressNetLocomotiveMessagesFlag());
   }

   @Test
   public void testLocoNetFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial LocoNet Flag",rcs.getLocoNetMessagesFlag());
      rcs.setLocoNetMessagesFlag(true);
      Assert.assertTrue("LocoNet Flag after set",rcs.getLocoNetMessagesFlag());
      rcs.setLocoNetMessagesFlag(false);
      Assert.assertFalse("LocoNet Flag after reset",rcs.getLocoNetMessagesFlag());
   }

   @Test
   public void testLocoNetLocoMotiveFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial LocoNet Locomotive Flag",rcs.getLocoNetLocomotiveMessagesFlag());
      rcs.setLocoNetLocomotiveMessagesFlag(true);
      Assert.assertTrue("LocoNet Locomotive Flag after set",rcs.getLocoNetLocomotiveMessagesFlag());
      rcs.setLocoNetLocomotiveMessagesFlag(false);
      Assert.assertFalse("LocoNet Locomotive Flag after reset",rcs.getLocoNetLocomotiveMessagesFlag());
   }

   @Test
   public void testLocoNetTurnoutFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial LocoNet Turnout Flag",rcs.getLocoNetTurnoutMessagesFlag());
      rcs.setLocoNetTurnoutMessagesFlag(true);
      Assert.assertTrue("LocoNet Turnout Flag after set",rcs.getLocoNetTurnoutMessagesFlag());
      rcs.setLocoNetTurnoutMessagesFlag(false);
      Assert.assertFalse("LocoNet Turnout Flag after reset",rcs.getLocoNetTurnoutMessagesFlag());
   }

   @Test
   public void testLocoNetOccupancyFlag(){
      RocoZ21CommandStation rcs = new RocoZ21CommandStation();
      Assert.assertFalse("initial LocoNet Occupancy Flag",rcs.getLocoNetOccupancyMessagesFlag());
      rcs.setLocoNetOccupancyMessagesFlag(true);
      Assert.assertTrue("LocoNet Occupancy Flag after set",rcs.getLocoNetOccupancyMessagesFlag());
      rcs.setLocoNetOccupancyMessagesFlag(false);
      Assert.assertFalse("LocoNet Occupancy Flag after reset",rcs.getLocoNetOccupancyMessagesFlag());
   }

    @Test
    public void testCanBoosterFlag(){
        RocoZ21CommandStation rcs = new RocoZ21CommandStation();
        Assert.assertFalse("initial CAN Booster Flag",rcs.getCanBoosterFlag());
        rcs.setCanBoosterFlag(true);
        Assert.assertTrue("CAN Booster Flag after set",rcs.getCanBoosterFlag());
        rcs.setCanBoosterFlag(false);
        Assert.assertFalse("CAN Booster Flag after reset",rcs.getCanBoosterFlag());
    }

    @Test
    public void testFastClockFlag(){
        RocoZ21CommandStation rcs = new RocoZ21CommandStation();
        Assert.assertFalse("initial Fast Clock Flag",rcs.getFastClockFlag());
        rcs.setFastClockFlag(true);
        Assert.assertTrue("Fast Clock Flag after set",rcs.getFastClockFlag());
        rcs.setFastClockFlag(false);
        Assert.assertFalse("Fast Clock Flag after reset",rcs.getFastClockFlag());
    }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
