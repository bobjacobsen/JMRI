package jmri.jmrix.loconet.accy7thgen;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.LinkedList;

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
    
    private LinkedList<Accy7thGenDevice> devices;

    AccyDeviceDataModel(int row, int column) {
        super();
        // number of columns, rows
        devices = new LinkedList<Accy7thGenDevice>();
        this.columns = column;        
    }

    public void removeDevices() {
        devices.clear();
        this.fireTableDataChanged();
    }
    
    /**
     * Return the number of rows to be displayed. 
     * <p>
     * This should probably use a local cache instead of counting/searching each
     * time.
     *
     * @return the number of rows
     */
    @Override
    public int getRowCount() {
        return devices.size();
    }

    @Override
    public int getColumnCount() {
        return columns;
    }

    @Override
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
    
    public Accy7thGenDevice add(Accy7thGenDevice dev) {
        Accy7thGenDevice ds;
        int d;
        if ((d = haveDevice(dev)) > -1) {
            ds = devices.get(d);
            // existing Device & Ser. Num.
            ds.firstOnes = dev.firstOnes;
            ds.baseAddr = dev.baseAddr;
            this.fireTableRowsUpdated(d, d);
        }  else {
            ds = dev;
            devices.add(ds);
            this.fireTableRowsInserted( devices.size()-1,  devices.size()-1);
        }
        return ds;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case DEVICECOLUMN:
            case TURNOUTSCOLUMN:
            case SENSORSCOLUMN:
            case REPORTERSCOLUMN:
            case ASPECTSCOLUMN:
            case POWERSCOLUMN:
            case SERNUMCOLUMN:
            case BASEADDRCOLUMN:
                return String.class;
            default:
                return null;
        }
    }

    @Override
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
    
    @Override
    public Object getValueAt(int row, int col) {
        Accy7thGenDevice ds = devices.get(row);
        switch (col) {
            case DEVICECOLUMN:
                return LnConstants.IPL_NAME(ds.device);
            case SERNUMCOLUMN:   
                return ds.serNum;
            case BASEADDRCOLUMN:
                return ds.baseAddr;
            case TURNOUTSCOLUMN:
                String res = ds.getTurnouts();
                if (ds.getTurnoutsBroadcast()) {
                    res += " *";
                }
                return res;
            case SENSORSCOLUMN:
                return ds.getSensors();
            case REPORTERSCOLUMN:
                return ds.getReporters();
            case ASPECTSCOLUMN:
                return ds.getApects();
            case POWERSCOLUMN:
                return ds.getPowers();
            default:
                return null;
        }
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
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

        } else {
            this.fireTableCellUpdated(row - 1, col);
        }
    }     
    
    private static class Notify implements Runnable {

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
