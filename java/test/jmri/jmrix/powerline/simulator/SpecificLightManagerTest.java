package jmri.jmrix.powerline.simulator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SpecificLightManager class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificLightManagerTest {

   private SpecificSystemConnectionMemo memo = null;

   @Test
   public void testConstructor(){
      Assert.assertNotNull("SpecificLightManager constructor",new SpecificLightManager(new SpecificTrafficController(memo)));
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new SpecificSystemConnectionMemo();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
        memo.dispose();
        memo = null;
   }

}
