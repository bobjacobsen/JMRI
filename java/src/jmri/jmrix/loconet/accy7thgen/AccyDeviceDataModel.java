package jmri.jmrix.loconet.accy7thgen;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.util.ArrayList;

import jmri.jmrix.loconet.*;

/**
 *
 * @author aptly
 */
public class AccyDeviceDataModel extends javax.swing.table.AbstractTableModel {
    static public final int DEVICECOLUMN = 0;
    static public final int SERNUMCOLUMN = 1;
    static public final int BASEADDRCOLUMN = 2;
    static public final int TURNOUTSCOLUMN = 3;
    static public final int SENSORSCOLUMN = 4;
    static public final int REPORTERSCOLUMN = 5;
    static public final int ASPECTSCOLUMN = 6;
    static public final int POWERSCOLUMN = 7;
    
    private int columns;
    
    private ArrayList<Accy7thGenDevice> devices;

    AccyDeviceDataModel(int row, int column) {
        // number of columns, rows
        devices = new ArrayList<Accy7thGenDevice>();
        for (int i = 0; i < row; ++i) {
            devices.add(i,new Accy7thGenDevice((-1) - i, -1,-1, 0));
        }
        this.columns = column;
    }

    public void removeDevices() {
        devices.clear();
    }
    
    /**
     * Return the number of rows to be displayed. 
     * <p>
     * This should probably use a local cache instead of counting/searching each
     * time.
     *
     * @return the number of rows
     */
    public int getRowCount() {
        return devices.size();
    }

    public int getColumnCount() {
        return columns;
    }

    public String getColumnName(int col) {
        switch (col) {
            case DEVICECOLUMN:
                return Bundle.getMessage("DeviceHeader");
            case SERNUMCOLUMN:
                return Bundle.getMessage("SerNumHeader");     // no heading, as button is clear
            case BASEADDRCOLUMN:
                return Bundle.getMessage("BaseAddrHeader");
            case TURNOUTSCOLUMN:
                return Bundle.getMessage("TurnoutHeader");
            case SENSORSCOLUMN:
                return Bundle.getMessage("SensorsHeader");
            case REPORTERSCOLUMN:
                return Bundle.getMessage("ReportersHeader");
            case ASPECTSCOLUMN:
                return Bundle.getMessage("AspectsHeader");
            case POWERSCOLUMN:
                return Bundle.getMessage("PowersHeader");
            default:
                return "unknown"; // NOI18N
        }
    }
    
    public  int haveDevice(Accy7thGenDevice dev) {
        int i = -1;
        for (Accy7thGenDevice ds : devices) {
            i++;
            if ((ds.device == dev.device) && (ds.serNum == dev.serNum)) {
                return i;
            }
        }
        return -1;
    }
    
    public void add(Accy7thGenDevice dev) {
        int d;
        if ((d = haveDevice(dev)) > -1) {
            Accy7thGenDevice ds = devices.get(d);
            // existing Device & Ser. Num.
            ds.firstOnes = dev.firstOnes;
            ds.baseAddr = dev.baseAddr;
            log.warn("New vales for an existing device/serialNum.");
            this.fireTableRowsUpdated(d, d);
        }  else {
            devices.add(dev);

            log.warn("devices count {}, Device {}, Ser Num {}, baseAddr {}, firstOnes {}", 
                    devices.size(),
                    devices.get(0).device,
                    devices.get(0).serNum,
                    devices.get(0).baseAddr,
                    devices.get(0).firstOnes);
            this.fireTableRowsInserted( devices.size()-1,  devices.size()-1);
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
            case DEVICECOLUMN:
            case SERNUMCOLUMN:
            case TURNOUTSCOLUMN:
            case SENSORSCOLUMN:
            case REPORTERSCOLUMN:
            case ASPECTSCOLUMN:
            case POWERSCOLUMN:
                return Integer.class;
            case BASEADDRCOLUMN:
                return Integer.class;
            default:
                return null;
        }
    }
    
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case DEVICECOLUMN:
            case SERNUMCOLUMN:   
            case BASEADDRCOLUMN:
                return false;
            case TURNOUTSCOLUMN:
            case SENSORSCOLUMN:
            case REPORTERSCOLUMN:
            case ASPECTSCOLUMN:
            case POWERSCOLUMN:
            default:
                return true;
        }
    }
    
    public int getPreferredWidth(int col) {
        switch (col) {
            case DEVICECOLUMN:
                return new JTextField(10).getPreferredSize().width;
            case SERNUMCOLUMN:
                return new JTextField(8).getPreferredSize().width;
            case BASEADDRCOLUMN:
                return new JTextField(8).getPreferredSize().width;
            case TURNOUTSCOLUMN:
            case SENSORSCOLUMN:
            case REPORTERSCOLUMN:
            case ASPECTSCOLUMN:
            case POWERSCOLUMN:
                return new JTextField(12).getPreferredSize().width;
            default:
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }                
    
    public Object getValueAt(int row, int col) {
        log.warn("getValueAt row {} col {}", row, col);
        for (Accy7thGenDevice j : devices) {
            log.warn("Row {} Device {} SerNum {} Base Addr {} routes {}",
                    row, j.device, j.serNum, j.baseAddr, j.firstOnes);
        }
        Accy7thGenDevice ds = devices.get(row);
        switch (col) {
            case DEVICECOLUMN:
                return ds.device;
            case SERNUMCOLUMN:   
                return ds.serNum;
            case BASEADDRCOLUMN:
                return ds.baseAddr;
            case TURNOUTSCOLUMN:
                return ds.getTurnouts();
            case SENSORSCOLUMN:
                return ds.getSensors();
            case REPORTERSCOLUMN:
                return ds.getPowers();
            case ASPECTSCOLUMN:
                return ds.getApects();
            case POWERSCOLUMN:
                return ds.getPowers();
            default:
                return null;
        }
    }
    
    public void setValueAt(Object value, int row, int col) {
        log.warn("Setting row {} col {} to {}", row, col, value);
        Accy7thGenDevice ds;
        boolean newDev = false;
        if (row <  devices.size()) {
            ds = devices.get(row);
        }
        else {
            ds = new Accy7thGenDevice(0,0,0,0);
            newDev = true;
            }            
        switch (col) {
            case DEVICECOLUMN:
                ds.device = (Integer) value;
                break;
            case SERNUMCOLUMN:
                ds.serNum = (Integer) value;
                break;
            case BASEADDRCOLUMN:
                ds.baseAddr = (Integer) value;
                break;
            default:
                return;
        }
        if (newDev) {
            devices.add(ds);
            this.fireTableRowsInserted(row-1, row-1);
            log.warn("added a row");

        } else {
            this.fireTableCellUpdated(row - 1, col);
            log.warn("updated a row");
        }
    }     
    
    /**
     * Configure a table to have our standard rows and columns. This is
     * optional, in that other table formats can use this table model. But we
     * put it here to help keep it consistent.
     *
     * @param devicesTable the table to configure
     */
    public void configureTable(JTable devicesTable) {
    }

    static private class Notify implements Runnable {

        private final int _row;
        javax.swing.table.AbstractTableModel _model;

        public Notify(int row, javax.swing.table.AbstractTableModel model) {
            _row = row;
            _model = model;
        }

        @Override
        public void run() {
            if (-1 == _row) {  // notify about entire table
                _model.fireTableDataChanged();
            } else {
                // notify that _row has changed
                _model.fireTableRowsUpdated(_row - 1, _row - 1);  // just that row
            }
        }
    }
     
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AccyDeviceDataModel.class);

}
