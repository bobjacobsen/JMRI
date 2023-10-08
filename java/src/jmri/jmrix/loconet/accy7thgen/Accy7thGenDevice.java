package jmri.jmrix.loconet.accy7thgen;

import jmri.jmrix.loconet.LnConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accessory 7th Generation Device.
 * 
 * @author Bob Milhaupt
 */
public class Accy7thGenDevice {
    public int device;
    public int serNum;
    public int baseAddr;
    public int  firstOnes;
    private Integer turnoutAddrStart;
    private Integer turnoutAddrEnd;
    private boolean turnoutFootnote;
    private Integer SensorAddrStart;
    private Integer SensorAddrEnd;
    private Integer ReportersAddrStart;
    private Integer ReportersAddrEnd;
    private Integer AspectAddrStart;
    private Integer AspectAddrEnd;
    private Integer PowerAddrStart;
    
    public Accy7thGenDevice(int device, int serNum, int baseAddr, int firstOnes) {
        this.device = device;
        this.serNum = serNum;
        this.baseAddr = baseAddr;
        this.firstOnes = firstOnes;
        turnoutAddrStart = getTurnoutAddressStart();
        turnoutAddrEnd = getTurnoutAddressEnd();
        SensorAddrStart = getSensorAddressStart();
        SensorAddrEnd = getSensorAddressEnd();
        ReportersAddrStart = getReportersStart();
        ReportersAddrEnd = getReportersEnd();
        AspectAddrStart = getAspectStart();
        AspectAddrEnd = getAspectEnd();
        PowerAddrStart = getPowerStart();
    }
    
    private Integer getTurnoutAddressStart() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS74:
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS78V:
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                return baseAddr;
            default:
                return -1;
        }
    }

    private Integer getTurnoutAddressEnd() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS74:
                return baseAddr + 3;
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS78V:
                return baseAddr+7;
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                int i = baseAddr+35;
                if ((firstOnes & 0x20) == 0x20) {
                    i = baseAddr+3;
                }
                return i;
            default:
                return -1;
        }
    }
    
    private Integer getSensorAddressStart() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS74:
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS78V:
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                return baseAddr;
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
                return (2 * (baseAddr - 1)) + 1;
            default:
                return -1;
        }
    }

    private Integer getSensorAddressEnd() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS74:
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                return baseAddr+7;
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS78V:
                return baseAddr+15;
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
                // Four sensors: ( 2 * (Base Addr -1)) + 1), +2, +4, +6
                // This is sorta onsistent with DT602 "version 0.1 
                // subversion 8" firmware...  I think...
                return ((2 * (baseAddr -1)) + 1) + 7;
            default:
                return -1;
        }
    }

    private Integer getReportersStart() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
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

    private Integer getReportersEnd() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
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

    private Integer getAspectStart() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                if ((firstOnes & 0x20) == 0x20) {
                    return baseAddr;
                } else {
                    return 0;
                }
            default:
                return -1;
        }
    }

    private Integer getAspectEnd() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                int i = 0;
                if ((firstOnes & 0x20) == 0x20) {
                    i = baseAddr+15;
                }
                if (i > 2047) {
                    i = 4047;
                }
                return i;
            default:
                return -1;
        }
    }

    private Integer getPowerStart() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
                int i = ((baseAddr - 1) & 0xFF) + 1;
                return i;
            default:
                return -1;
        }
    }

    public boolean getTurnoutsBroadcast() {
        switch (device) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
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
    
    public String getApects() {
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
    
    public boolean isConflicting(Integer row, Integer col) {
        switch (col) {
            case 2:
                log.warn("row {} col {} is true!", row, col);
                return true;
            case 0:
            case 1:
            default:
                return false;
        }
    } 
    
    private static final Logger log = LoggerFactory.getLogger(Accy7thGenDevice.class);
    
}
