package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.AbstractMonPaneTestBase;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.assertj.swing.edt.GuiActionRunner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.openlcb.can.AliasMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MonitorPaneTest extends AbstractMonPaneTestBase {

    private TrafficControllerScaffold tcs = null;
    private CanSystemConnectionMemo memo = null;

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    @Override
    public void testConcreteCtor() {
        Throwable thrown = catchThrowable(() -> GuiActionRunner.execute(() -> ((MonitorPane)pane).initComponents(memo)));
        assertThat(thrown).isNull();
    }

    @Test
    @Override
    public void testGetHelpTarget(){
        assertThat(panel.getHelpTarget()).isEqualTo("package.jmri.jmrix.openlcb.swing.monitor.MonitorPane");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        tcs = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        memo.store(new AliasMap(), org.openlcb.can.AliasMap.class);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class, memo);
        panel = pane = new MonitorPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = Bundle.getMessage("MonitorTitle");
    }

    @AfterEach
    @Override
    public void tearDown() {
        pane.dispose();
        pane = null;
        memo.dispose();
        memo = null;
        tcs.terminateThreads();
        tcs = null;
        jmri.util.JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(MonitorPaneTest.class);
}
