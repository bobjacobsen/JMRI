package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import jmri.util.PropertyChangeListenerScaffold;
import jmri.implementation.AbstractTurnoutTestBase;
import jmri.Turnout;
import jmri.jmrix.can.CanMessage;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests inherited from the abstract turnout test base, specialized for the OlcbTurnout. This is
 * testing a subset of the OlcbTurnout functionality, but tests common JMRI functionality that is
 * not specifically duplicated in the OlcbTurnoutTest.
 * Created by Balazs Racz on 1/11/18.
 */

public class OlcbTurnoutInheritedTest extends AbstractTurnoutTestBase {
    OlcbTestInterface tif;
    int baselineListeners;
    protected PropertyChangeListenerScaffold l;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
       // this test is run separately because it leaves a lot of threads behind
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        tif = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        tif.waitForStartup();
        baselineListeners = tif.iface.numMessageListeners();
        OlcbTurnout ot = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", tif.systemConnectionMemo);
        ot.finishLoad();
        t = ot;
        l = new PropertyChangeListenerScaffold();
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning") == false) {
            tif.dispose();
            tif = null;
        }
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Override
    public int numListeners() {
        return tif.iface.numMessageListeners() - baselineListeners;
    }

    @Override
    public void checkThrownMsgSent() {
        tif.flush();
        tif.assertSentMessage(":X195B4C4CN0102030405060708;");
    }

    @Override
    public void checkClosedMsgSent() {
        tif.flush();
        tif.assertSentMessage(":X195B4C4CN0102030405060709;");
    }

    @Test
    @Override
    public void testDirectFeedback() throws jmri.JmriException {
        t.setFeedbackMode(Turnout.DIRECT);
        //t.finishLoad();

        t.addPropertyChangeListener(l);
        l.resetPropertyChanged();

        t.setState(Turnout.THROWN);
        tif.flush();
        JUnitUtil.waitFor( () -> l.getPropertyChanged()," > thrown");

        Assert.assertEquals(Turnout.THROWN, t.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, t.getKnownState());

        l.resetPropertyChanged();
        t.setState(Turnout.CLOSED);
        tif.flush();
        JUnitUtil.waitFor( () -> l.getPropertyChanged(),"thrown > closed");

        Assert.assertEquals(Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t.getKnownState());

        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x195B4000
        );
        mActive.setExtended(true);

        CanMessage mInactive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 9},
                0x195B4000
        );
        mInactive.setExtended(true);

        l.resetPropertyChanged();

        //  Feedback is ignored. Neither known nor commanded state changes.
        tif.sendMessage(mActive);
        Assert.assertEquals(Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t.getKnownState());
        Assert.assertEquals("not called",0,l.getCallCount());

        tif.sendMessage(mInactive);
        Assert.assertEquals(Turnout.CLOSED, t.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, t.getKnownState());
        Assert.assertEquals("not called",0,l.getCallCount());
    }

}
