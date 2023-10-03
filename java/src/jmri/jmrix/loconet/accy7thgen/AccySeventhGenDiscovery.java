package jmri.jmrix.loconet.accy7thgen;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.swing.LnPanel;
import jmri.jmrix.loconet.accy7thgen.Bundle;
import jmri.jmrix.loconet.messageinterp.LocoNetMessageInterpret;

import jmri.util.TimerUtil;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class AccySeventhGenDiscovery extends LnPanel implements LocoNetListener {
    JButton findButton;
    boolean gotTheFindMessage;
    boolean gotA7thGenReply;
    protected LnTrafficController tc;
    
    private AccyDeviceDataModel devicesModel;
    private JTable devicesTable;
    private JScrollPane devicesScroll;
    private transient TableRowSorter<AccyDeviceDataModel> sorter;

    String prefixTurnout;
    String prefixSensor;
    String prefixReporter;
    
    public AccySeventhGenDiscovery() {
        super();
    }
    
    public void initContext(Object context) {
        if (context instanceof LocoNetSystemConnectionMemo) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
    }
    
    @Override
    public void initComponents(final LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getLnTrafficController();
        prefixTurnout = memo.getSystemPrefix()+"T";
        prefixSensor  = memo.getSystemPrefix()+"S";
        prefixReporter = memo.getSystemPrefix()+"R";
        memo.getLnTrafficController().addLocoNetListener(~0, this);

    }

    @Override
    public void initComponents() {
        JPanel panel2;
        findButton = new JButton(Bundle.getMessage("FINDLABEL"));
        add(findButton);

        devicesModel = new AccyDeviceDataModel(1, 8) ;
        devicesTable = new JTable(devicesModel);
        devicesTable.setName(this.getTitle());

        sorter = new TableRowSorter<>(devicesModel);
        devicesTable.setRowSorter(sorter);
        devicesScroll = new JScrollPane(devicesTable);
        add(devicesScroll);

        // install "read" button handler
        findButton.addActionListener((ActionEvent a) -> {
            findButton.setEnabled(false);
            if (devicesModel.getRowCount() > 0) {
                devicesModel.removeDevices();
            }
            tc.sendLocoNetMessage(msgRoutesQuery());
            startRoutesResponseTimer();
        });
        gotTheFindMessage = false;
        gotA7thGenReply = false;
    }

    public void message(LocoNetMessage m) {
        if (findButton.isEnabled() == true)  {
            // Ignore messages when not lookimg fir the routes info 
            return;
        }
        
        if ((m.getOpCode() == LnConstants.OPC_IMM_PACKET_2) &&
                (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 1) &&
                (m.getElement(3) == 0) &&
                (m.getElement(4) == 0) &&
                (m.getElement(5) == 0) &&
                (m.getElement(6) == 0) &&
                (m.getElement(7) == 0) &&
                (m.getElement(8) == 0) &&
                (m.getElement(9) == 0) &&
                (m.getElement(10) == 0) &&
                (m.getElement(11) == 0) &&
                (m.getElement(12) == 0) &&
                (m.getElement(13) == 0) &&
                (m.getElement(14) == 0) 
                ) {
            log.warn("received message {}", LocoNetMessageInterpret.interpretMessage(m, 
                    prefixTurnout, prefixSensor, prefixReporter));
            // 0xEE 0x10 0x01 
            gotTheFindMessage = true;
        } else if ((m.getOpCode() == LnConstants.OPC_ALM_READ) &&
                (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 1) // (command station route report?)
                ) {
            // Ignore reply from command station "routes"!
            log.warn("received message {}", LocoNetMessageInterpret.interpretMessage(m, 
                    prefixTurnout, prefixSensor, prefixReporter));
            return;
        } else if ((m.getOpCode() == LnConstants.OPC_ALM_READ) &&
                (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 2) // (2nd gen route report?)
                ) {
            /* Some example "Routes capabilities" messages:
            * [E6 10 02 00 20 00 00 02 08 7C 2C 28 02 31 00 6A]  Device DS78V (s/n 0x128) in Servo (3-position) mode (routes currently enabled), using turnout addresses 50 thru 65, with 16 routes of 8 entries per route, may be configured using ALM messaging.
            * [E6 10 02 0E 10 00 00 02 08 74 2A 4E 27 00 01 29]  Device DS74, s/n 0x13ce, using addresses 129 thru 136 has been selected for Routes configuration.
            * [E6 10 02 00 20 00 00 02 08 7C 2C 28 00 31 00 68]  Device DS78V (s/n 0x28) in Servo (3-position) mode (routes currently enabled), using turnout addresses 50 thru 65, with 16 routes of 8 entries per route, may be configured using ALM messaging.
            * [E6 10 02 00 10 00 00 02 08 46 40 03 07 7C 01 6E]  Device SE74 (s/n 0x383) in undefined mode (routes currently disabled), using turnout addresses 253 thru 289, with 64 routes of 16 entries per route, may be configured using ALM messaging.
            */
            // 0xEE 0x10 0x02 - Routes Reply, 2nd-generation(?)
            log.warn("received message {}", LocoNetMessageInterpret.interpretMessage(m, 
                    prefixTurnout, prefixSensor, prefixReporter));
            gotA7thGenReply = true;
            int device = m.getElement(9) + (((m.getElement(8) & 0x1 ) == 1) ? 0x80 : 0);
            int serNum = m.getElement(11) + (m.getElement(12) << 7); // correct ??? pull in a couple of buts from 3 or 8?
            int baseAddr = 1 + m.getElement(13) + (m.getElement(14) << 7);
            
            int firstOnes = m.getElement(10);
            devicesModel.add(new Accy7thGenDevice(device, serNum, baseAddr, firstOnes));
            cancelRoutesResponseTimer();
            startRoutesResponseTimer();
            log.warn("initial number rows = {}", devicesModel.getRowCount() );
        }
    }
    
    private void noResponseFound() {
        // no response found now.
        findButton.setEnabled(true);
        gotTheFindMessage = false;
        log.warn("RoutesResponseTimer timer Expired.");
    }
    
    private void cancelRoutesResponseTimer() {
        rrt.cancel();
    }
    java.util.TimerTask rrt;
    private void startRoutesResponseTimer() {
        rrt = new java.util.TimerTask() {
            @Override
            public void run() {
                noResponseFound();
            }
        };
        jmri.util.TimerUtil.scheduleOnLayoutThread(rrt, 20250);
    }
    private LocoNetMessage msgRoutesQuery() {
        return new LocoNetMessage (new int[]{0xEE, 0x10, 0x01, 0x00, 0x00, 
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
    }
    
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.accy7thgen.7thgen"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItem7thGenAccy"));
    }
    @Override
    public void dispose() {
        devicesScroll = null;
        super.dispose();
    }


    private final static Logger log = LoggerFactory.getLogger(AccySeventhGenDiscovery.class);

}
