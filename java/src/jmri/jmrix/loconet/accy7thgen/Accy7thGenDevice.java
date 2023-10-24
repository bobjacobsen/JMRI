package jmri.jmrix.loconet.accy7thgen;

import jmri.jmrix.loconet.LnConstants;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accessory 7th Generation Device.
 * 
 * @author Bob Milhaupt
 */
public class Accy7thGenDevice extends java.beans.Beans {
    private String device;
    private int serNum;
    private int baseAddr;
    private int  firstOpSws;
    private Integer turnoutAddrStart;
    private Integer turnoutAddrEnd;
    private Integer SensorAddrStart;
    private Integer SensorAddrEnd;
    private Integer ReportersAddrStart;
    private Integer ReportersAddrEnd;
    private Integer AspectAddrStart;
    private Integer AspectAddrEnd;
    private Integer PowerAddrStart;
    public JButton action;
    
    /**
     * Constructor
     * @param device - integer device type (0-7f)
     * @param serNum - Integer serial number (0-0xFFFF or 0-0x3FFF)
     * @param baseAddr - Integer base address (0-2045)
     * @param firstOps - Integer first Ops (0-255)
     */
    public Accy7thGenDevice(int device, int serNum, int baseAddr, int firstOps) {
        setDevice(device);
        this.serNum = serNum;
        this.baseAddr = baseAddr;
        this.firstOpSws = firstOps;
        turnoutAddrStart = gitTurnoutAddressStart();
        turnoutAddrEnd = gitTurnoutAddressEnd();
        SensorAddrStart = gitSensorAddressStart();
        SensorAddrEnd = gitSensorAddressEnd();
        ReportersAddrStart = gitReportersStart();
        ReportersAddrEnd = gitReportersEnd();
        AspectAddrStart = gitAspectStart();
        AspectAddrEnd = gitAspectEnd();
        PowerAddrStart = gitPowerStart();
        action = new JButton("Change Base Addr");
        action.setActionCommand("changeAddr");

    } 
               
    public JButton getAction() {
        return action;
    }

    public void setAction(JButton action) {
        this.action = action;
    }

    public String getDevice() {
        return device;
    }

    public final void setDevice(int device) {
        String s = LnConstants.IPL_NAME(device);
        switch (s) {
            case "SE74":
            case "PM74":
            case "DS78V":
            case "DS74":
                break;
            default:
                s = "Unknown - "+Integer.toString(device);
                            }
        this.device = s;
    }

    public void setDevice(String s) {
        String dev = s;
        switch (dev) {
            case "SE74":
            case "PM74":
            case "DS78V":
            case "DS74":
                break;
            default:
                dev = "Unknown - "+s;
        }
        this.device = dev;
    }

    public int getSerNum() {
        return serNum;
    }

    public void setSerNum(int serNum) {
        this.serNum = serNum;
    }

    public int getBaseAddr() {
        return baseAddr;
    }

    public void setBaseAddr(int baseAddr) {
        this.baseAddr = baseAddr;
    }

    public int getFirstOpSws() {
        return firstOpSws;
    }

    public void setFirstOpSws(int firstOps) {
        this.firstOpSws = firstOps;
    }
    
    private Integer gitTurnoutAddressStart() {
        switch (device) {
            case "DS74":
            case "DS78V":
            case "SE74":
                return baseAddr;
            default:
                return -1;
        }
    }

    private Integer gitTurnoutAddressEnd() {
        switch (device) {
            case "DS74":
                return baseAddr + 3;
            case "DS78V":
                if ((firstOpSws & 0x1e) == 0x0C) {
                    // four position mode
                    return baseAddr + 15;
                }
                return baseAddr+7;
            case "SE74":
                int i = baseAddr+35;
                if ((firstOpSws & 0x20) == 0x20) {
                    i = baseAddr+3;
                }
                return i;
            default:
                return -1;
        }
    }
    
    private Integer gitSensorAddressStart() {
        switch (device) {
            case "DS74":
            case "DS78V":
                return baseAddr;
            case "SE74":
                return (2 * baseAddr) - 1;
            case "PM74":
                return (2 * (baseAddr - 1)) + 1;
            default:
                return -1;
        }
    }

    private Integer gitSensorAddressEnd() {
        switch (device) {
            case "DS74":
            case "SE74":
                return baseAddr+7;
            case "DS78V":
                if ((firstOpSws & 0x1e) == 0x0C) {
                    return (2 * baseAddr) + 30;
                }
                return (2 * baseAddr)+14;
            case "PM74":
                return ((2 * (baseAddr -1)) + 1) + 7;
            default:
                return -1;
        }
    }

    private Integer gitReportersStart() {
        switch (device) {
            case "PM74":
                int i = baseAddr - 1;
                int j = baseAddr;
                int k = baseAddr + 1;
                int l = baseAddr + 2;
                int st = i;
                if ((st & 128) == 0) {
                    return (st % 128) + 1;
                }
                st = j;
                if ((st & 128) == 0) {
                    return (st % 128) + 1;
                }
                st = k;
                if ((st & 128) == 0) {
                    return (st % 128) + 1;
                }
                st = l;
                if ((st & 128) == 0) {
                    return (st % 128) + 1;
                }
                return -1;
            default:
                return -1;
        }
    }

    private Integer gitReportersEnd() {
        switch (device) {
            case "PM74":
                int i = baseAddr - 1;
                int j = baseAddr;
                int k = baseAddr + 1;
                int l = baseAddr + 2;
                int st = l;
                if ((st & 128) == 0) {
                    return (st % 128) + 1;
                }
                st = k;
                if ((st & 128) == 0) {
                    return (st % 128) + 1;
                }
                st = j;
                if ((st & 128) == 0) {
                    return (st % 128) + 1;
                }
                st = i;
                if ((st & 128) == 0) {
                    return (st % 128) + 1;
                }
                return -1;
            default:
                return -1;
        }
    }

    private Integer gitAspectStart() {
        switch (device) {
            case "SE74":
                if ((firstOpSws & 0x20) == 0x20) {
                    return baseAddr;
                } else {
                    return 0;
                }
            default:
                return -1;
        }
    }

    private Integer gitAspectEnd() {
        switch (device) {
            case "SE74":
                int i = 0;
                if ((firstOpSws & 0x20) == 0x20) {
                    i = baseAddr+15;
                }
                if (i > 2048) {
                    i = 2048;
                }
                return i;
            default:
                return -1;
        }
    }

    private Integer gitPowerStart() {
        switch (device) {
            case "PM74":
                int i = ((baseAddr - 1) & 0xFF) + 1;
                return i;
            default:
                return -1;
        }
    }

    public boolean gitTurnoutsBroadcast() {
        switch (device) {
            case "SE74":
                return true;
            default:
                return false;
        }
    }

    public String getTurnouts() {
        if (turnoutAddrStart > 0) {
            return Integer.toString(turnoutAddrStart)+"-"+
                    Integer.toString(turnoutAddrEnd);
            
        } else {
            return "";
        }
    }
    
    public String getSensors() {
        if (SensorAddrStart > 0) {
            return Integer.toString(SensorAddrStart)+"-"+Integer.toString(SensorAddrEnd);
        } else {
            return "";
        }
    }
    
    public String getReporters() {
        String res ;
        if ((ReportersAddrStart > 0) && (ReportersAddrEnd > 0)) {
            res = Integer.toString(ReportersAddrStart)+"-"+Integer.toString(ReportersAddrEnd);
        } else if ((ReportersAddrStart > 0) && (ReportersAddrEnd <= 0)) {
            res = Integer.toString(ReportersAddrStart);
        } else if ((ReportersAddrStart <= 0) && (ReportersAddrEnd > 0)) {
            res = Integer.toString(ReportersAddrEnd);
        } else {
            res = "";
        }
        return res;
    }
    
    public boolean gitAspectsBroadcast() {
        switch (device) {
            case "SE74":
                return (AspectAddrStart > 0) ? true: false;
            default:
                return false;
        }
    }

    public String getAspects() {
        if (AspectAddrStart > 0) {
            return Integer.toString(AspectAddrStart)+"-"+Integer.toString(AspectAddrEnd);
        } else {
            return "";
        }
    }
    
    public String getPowers() {
        if (PowerAddrStart >= 0) {
            // only one power address (for 4 sub-addresses) for PM74
            return Integer.toString(PowerAddrStart);
        } else {
            return "";
        }
    }   
}
