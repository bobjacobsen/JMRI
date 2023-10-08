package jmri.jmrix.loconet.accy7thgen;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.TimerTask;

import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.table.TableRowSorter;


import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.swing.LnPanel;
import jmri.jmrix.loconet.messageinterp.LocoNetMessageInterpret;

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
        
        find7thGenDevices();
    }

    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        findButton = new JButton(Bundle.getMessage("FINDLABEL"));
        add(findButton);
        devicesModel = new AccyDeviceDataModel(1, 8) ;
        devicesTable = new JTable(devicesModel) {
            private final Color cellsOrigForeColor = new JTable().getForeground();
            private final Color conflictingColor = Color.decode("#c00000");         // Dark Red
            private final Color cellsOrigBackColor = new JTable().getBackground();
            Color cellBackColor = cellsOrigBackColor;  // Original Cells Backgound color.
            Color cellForeColor = cellsOrigForeColor;  // Original Cells Foregound (text) color.
            private final Color selection = Color.decode("#7FB3D5");         // A lighter Blue 

            private static final long serialVersionUID = 1L;
            // Here is where all the cell formatting is done
            // based on stack overflow : java - JTable cell render based on content
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, 
                    int columnIndex) {
                /* Acquire the current cell component being Rendered. ALL cells get Rendered. */
                
                JComponent component = (JComponent) super.prepareRenderer(renderer, rowIndex, columnIndex);

                cellBackColor = cellsOrigBackColor;
                boolean overlap = checkOverlappingResources(devicesTable, rowIndex, columnIndex);
                cellBackColor = (overlap)? conflictingColor: cellsOrigBackColor;
                
                component.setBackground(cellBackColor);
                component.setForeground(properTextColor(cellBackColor));

                // If a row is selected, maintain the conditional formatting...
                if (isRowSelected(rowIndex)) {
                    component.setBackground(selection);
                } else {
                    component.setBackground(cellBackColor);
                }
                return component;
            }
        };
                
        devicesTable.setName(this.getTitle());
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER );
        devicesTable.setDefaultRenderer(String.class, centerRenderer);

        sorter = new TableRowSorter<>(devicesModel);
        devicesTable.setRowSorter(sorter);
        devicesScroll = new JScrollPane(devicesTable);
        add(devicesScroll);

        // install "read" button handler
        findButton.addActionListener((ActionEvent a) -> {
            find7thGenDevices();
        });
        gotTheFindMessage = false;
        gotA7thGenReply = false;
        add(new JSeparator());
        add(new JLabel(Bundle.getMessage("FOOTNOTESE74")));
    }
    
    public void find7thGenDevices() {
        findButton.setEnabled(false);
        if (devicesModel.getRowCount() > 0) {
            devicesModel.removeDevices();
        }
        tc.sendLocoNetMessage(msgRoutesQuery());
        startRoutesResponseTimer();
    }

    public boolean checkOverlappingResources(JTable table, Integer rowIndex, Integer columnIndex) {
                boolean overlap = false;
                if ((columnIndex >= 3)  && (columnIndex <= 7) ) {
                    log.debug("checking row {} col {}", rowIndex, columnIndex);
                    // get Sensors/Reorters/Aspects/Powers used

                    String value = table.getValueAt(rowIndex, columnIndex).toString();
                    log.debug("value {}", value);
                    String value2 = value.replace(" *", "");
                    log.debug("modified value {}", value);
                    int val1;
                    int val2;
                    if (value2.contains("-")) {
                        int where = value2.indexOf('-');
                        val1 = Integer.parseInt(value2.substring(0, where));
                        val2 = Integer.parseInt(value2.substring(where + 1));
                        log.debug("sensors {} to {} inclusive.",val1, val2);
                    } else if (!value2.isEmpty()) {
                        val1 = Integer.parseInt(value2);
                        val2 = val1;
                    } else {
                        val1 = -2;
                        val2 = -2;
                    }
                    log.debug("val range {} to {}", val1, val2);
                    for (int i = 0; (i < table.getRowCount()) && (!overlap); i++) {
                        if (i == rowIndex) {
                            log.debug("Skipping this row; same as the target row!");
                        } else {
                            // get row's count and see if overlap
                            String ref = table.getValueAt(i, columnIndex).toString();
                            String ref2 = ref.replace(" *", "");
                            log.debug("modified value {}", value);
                            
                            int refval1;
                            int refval2;
                            if (ref2.contains("-")) {
                                int where = ref2.indexOf('-');
                                refval1 = Integer.parseInt(ref2.substring(0, where));
                                refval2 = Integer.parseInt(ref2.substring(where + 1));
                                log.debug("refval, low {}, hi {}", refval1, refval2);
                            } else if (!ref2.isEmpty()) {
                                refval1 = Integer.parseInt(ref2);
                                refval2 = val1;
                            } else {
                                // ??? getOut of here???
                                refval1 = -1;
                                refval2 = -1;
                            }
                            log.debug("refval range {} to {}", refval1, refval2);
                            // 1. check if val2 < refval1.
                            if ((val2 >= refval1) && (val1 <= refval2) ) {
                                overlap = true;
                                log.debug("OVERLAP!");
                            }
                            
                            if (columnIndex == 3) {
                                log.debug("Checking if broadcast (a)");
                                if ((overlap == false) && (value.contains(" *"))) {
                                    // check against "broadcast" addresses
                                    log.debug("Checking broadcast addresses (a)!");
                                    refval1 = 1021;
                                    refval2 = 1028;
                                    if ((val2 >= refval1) && (val1 <= refval2) ){
                                        overlap = true;
                                        log.debug("OVERLAP (on broadcast a)!");
                                    }
                                }
                                log.debug("Checking if broadcast (b)");
                                if ((overlap == false) && (value.contains(" *"))) {
                                    // check against "broadcast" addresses
                                    log.debug("Checking broadcast addresses (b)!");
                                    refval1 = 2041;
                                    refval2 = 2044;
                                    if ((val2 >= refval1) && (val1 <= refval2) ){
                                        overlap = true;
                                        log.debug("OVERLAP (on broadcast (b))!");
                                    }
                                }
                                log.debug("Checking if broadcast (c)");
                                if ((overlap == false) && (value.contains(" *"))) {
                                    // check against "broadcast" addresses
                                    log.debug("Checking broadcast addresses (c)!");
                                    refval1 = 2048;
                                    refval2 = 2048;
                                    if ((val2 >= refval1) && (val1 <= refval2) ){
                                        overlap = true;
                                        log.debug("OVERLAP (on broadcast (c))!");
                                    }
                                }
                            }
                        }
                    }
                }
        return overlap;
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
//            log.warn("received message {}", LocoNetMessageInterpret.interpretMessage(m, 
//                    prefixTurnout, prefixSensor, prefixReporter));
            // 0xEE 0x10 0x01 
            gotTheFindMessage = true;
        } else if ((m.getOpCode() == LnConstants.OPC_ALM_READ) &&
                (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 1) // (command station route report?)
                ) {
            // Ignore reply from command station "routes"!
//            log.warn("received message {}", LocoNetMessageInterpret.interpretMessage(m, 
//                    prefixTurnout, prefixSensor, prefixReporter));
            return;
        } else if ((m.getOpCode() == LnConstants.OPC_ALM_READ) &&
                (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 2) // (2nd gen route report?)
                ) {
            /* Some example "Routes capabilities" messages:
            * [E6 10 02 00 20 00 00 02 08 7C 2C 28 02 31 00 6A]  Device DS78V (s/n 0x128)
            *       in Servo (3-position) mode (routes currently enabled), using turnout
            *       addresses 50 thru 65, with 16 routes of 8 entries per route, may be 
            *       configured using ALM messaging.
            * [E6 10 02 0E 10 00 00 02 08 74 2A 4E 27 00 01 29]  Device DS74, s/n 0x13ce, 
            *       using addresses 129 thru 136 has been selected for Routes configuration.
            * [E6 10 02 00 20 00 00 02 08 7C 2C 28 00 31 00 68]  Device DS78V (s/n 0x28) in 
            *       Servo (3-position) mode (routes currently enabled), using turnout 
            *       addresses 50 thru 65, with 16 routes of 8 entries per route, may be 
            *       configured using ALM messaging.
            * [E6 10 02 00 10 00 00 02 08 46 40 03 07 7C 01 6E]  Device SE74 (s/n 0x383) 
            *       in undefined mode (routes currently disabled), using turnout addresses 
            *       253 thru 289, with 64 routes of 16 entries per route, may be 
            *       configured using ALM messaging.
            */
            // 0xEE 0x10 0x02 - Routes Reply, 2nd-generation(?)
//            log.warn("received message {}", LocoNetMessageInterpret.interpretMessage(m, 
//                    prefixTurnout, prefixSensor, prefixReporter));
            gotA7thGenReply = true;
            int device = m.getElement(9) + (((m.getElement(8) & 0x1 ) == 1) ? 0x80 : 0);
            int serNum = m.getElement(11) + (m.getElement(12) << 7); // correct ??? pull in 
                    // a couple of bits from 3 or 8?
            int baseAddr = 1 + m.getElement(13) + (m.getElement(14) << 7);
            
            int firstOnes = m.getElement(10);
            Accy7thGenDevice d = new Accy7thGenDevice(device, serNum, baseAddr, firstOnes);
            
            devicesModel.add(d);
            
            cancelRoutesResponseTimer();
            startRoutesResponseTimer();
//            log.warn("initial number rows = {}", devicesModel.getRowCount() );
        }
    }
    
    private void noResponseFound() {
        // no response found now.
        findButton.setEnabled(true);
        gotTheFindMessage = false;
//        log.warn("RoutesResponseTimer timer Expired.");
    }
    
    private void cancelRoutesResponseTimer() {
        rrt.cancel();
        rrt = null;
    }
    
    TimerTask rrt;
    private void startRoutesResponseTimer() {
        rrt = new java.util.TimerTask() {
            @Override
            public void run() {
                noResponseFound();
            }
        };
        jmri.util.TimerUtil.scheduleOnLayoutThread(rrt, 750);
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

    
    
    
    
    
    
    // Age Group colors

    // Row Selection Color

    // Table Default Colors

    /**
     * Returns either the Color WHITE or the Color BLACK dependent upon the
     * brightness of what the supplied background color might be. If the
     * background color is too dark then WHITE is returned. If the background
     * color is too bright then BLACK is returned.<br><br>
     *
     * @param currentBackgroundColor (Color Object) The background color text
     *                               will reside on.<br>
     *
     * @return (Color Object) The color WHITE or the Color BLACK.
     */
    public static Color properTextColor(Color currentBackgroundColor) {
        double L; // Holds the brightness value for the supplied color
        Color determinedColor;  // Default

        // Calculate color brightness from supplied color.
        int r = currentBackgroundColor.getRed();
        int g = currentBackgroundColor.getGreen();
        int b = currentBackgroundColor.getBlue();
        L = (int) Math.sqrt((r * r * .241) + (g * g * .691) + (b * b * .068));

        // Return the required text color to suit the 
        // supplied background color.
        if (L > 129) {
            determinedColor = Color.decode("#000000");  // White
        }
        else {
            determinedColor = Color.decode("#FFFFFF");  // Black
        }
        return determinedColor;
    }
    
    

    private final static Logger log = LoggerFactory.getLogger(AccySeventhGenDiscovery.class);

}
