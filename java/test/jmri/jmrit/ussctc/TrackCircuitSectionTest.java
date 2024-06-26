package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import jmri.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for TrackCircuitSection class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
@SuppressWarnings("unchecked")
public class TrackCircuitSectionTest {

    @Test
    public void testLayoutMonitoring() throws JmriException {
        Assertions.assertNotNull(sensor);
        sensor.setKnownState(Sensor.INACTIVE);

        TrackCircuitSection t = new TrackCircuitSection("Sec1 track input", "Sec 1 track output", station);
        Assertions.assertNotNull(t);

        sensor.setKnownState(Sensor.ACTIVE);

        // initialization sets indicators to follow actual turnout state
        Assert.assertTrue(requestIndicationStart);
    }

    @Test
    public void testIndicationStart() throws JmriException {

        // test getting indication from layout
        TrackCircuitSection t = new TrackCircuitSection("Sec1 track input", "Sec 1 track output", station);

        // check multiple patterns for state -> return value
        Assertions.assertNotNull(sensor);
        sensor.setState(Sensor.INACTIVE);
        Assert.assertEquals(CodeGroupOneBit.Single0, t.indicationStart());

        sensor.setState(Sensor.ACTIVE);
        Assert.assertEquals(CodeGroupOneBit.Single1, t.indicationStart());

        sensor.setState(Sensor.INACTIVE);
        Assert.assertEquals(CodeGroupOneBit.Single0, t.indicationStart());

        sensor.setState(Sensor.UNKNOWN);
        Assert.assertEquals(CodeGroupOneBit.Single1, t.indicationStart());

        sensor.setState(Sensor.INACTIVE);
        Assert.assertEquals(CodeGroupOneBit.Single0, t.indicationStart());

        sensor.setState(Sensor.INCONSISTENT);
        Assert.assertEquals(CodeGroupOneBit.Single1, t.indicationStart());
    }

    @Test
    public void testIndicationComplete0() throws JmriException  {

        TrackCircuitSection t = new TrackCircuitSection("Sec1 track input", "Sec 1 track output", station);

        Assertions.assertNotNull(indicator);
        indicator.setCommandedState(Turnout.INCONSISTENT);

        t.indicationComplete(CodeGroupOneBit.Single0);

        Assert.assertEquals(Turnout.CLOSED, indicator.getKnownState());
    }

    @Test
    public void testIndicationComplete1() throws JmriException  {

        TrackCircuitSection t = new TrackCircuitSection("Sec1 track input", "Sec 1 track output", station);

        Assertions.assertNotNull(indicator);
        indicator.setCommandedState(Turnout.INCONSISTENT);

        t.indicationComplete(CodeGroupOneBit.Single1);

        Assert.assertEquals(Turnout.THROWN, indicator.getKnownState());
    }

    CodeLine codeline;
    Station station;
    boolean requestIndicationStart;

    private Turnout indicator = null;
    private Sensor sensor = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();

        indicator = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1"); indicator.setUserName("Sec 1 track output");

        sensor = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2"); sensor.setUserName("Sec1 track input");

        codeline = new CodeLine("Code Indication Start", "Code Send Start", "IT101", "IT102", "IT103", "IT104");

        requestIndicationStart = false;
        station = new Station("test", codeline, new CodeButton("IS221", "IS222")) {
            @Override
            public void requestIndicationStart() {
                requestIndicationStart = true;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
