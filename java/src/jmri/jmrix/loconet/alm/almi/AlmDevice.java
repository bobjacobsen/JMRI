package jmri.jmrix.loconet.alm.almi;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.alm.Alm;
import jmri.jmrix.loconet.LocoNetMessage;
import java.util.List;
import java.util.ArrayList;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author Bob Milhaupt  Copyright (C) 2022
 */
public abstract class AlmDevice {

    protected int baseAddress ; // Module address in reply, value of -1 is ignored, LNCV default address : 1
    protected int deviceType; // used as LNCV ProductID, must be int to pass as part of CV "art.cv", usually 4 digits
    protected String deviceName;
    protected int serNum;
    protected String opSw1thru6;
    protected int valOpSw1thru6;
    protected int routesCaps;
    protected RosterEntry rosterEntry;

    public final int getBaseAddr() {return baseAddress;}
    public final int getDeviceType() {return deviceType;}
    public final String getDeviceName() {return deviceName;}
    public final int getSerNum() {return serNum;}
    public final String getHexSerNum() {return Integer.toHexString(serNum);}
    public final String getOpSw1thru6() {return opSw1thru6;}
    public final int getRoutesCaps() {return routesCaps;}
    
    public String interpretRoutesCaps() {
        String routesEnable = Bundle.getMessage("LN_MSG_ROUTES_ENABLED");
        if ((valOpSw1thru6 & 0x40) == 0x40) {
            routesEnable = Bundle.getMessage("LN_MSG_ROUTES_DISABLED");
        }
        switch (routesCaps) {
            case 0x8002002:
                return Bundle.getMessage("LN_MSG_ROUTES_ENABLE_AND_CAPS", routesEnable, 16, 8);
            case 0x8001002:
                return Bundle.getMessage("LN_MSG_ROUTES_ENABLE_AND_CAPS", routesEnable, 8, 8);
            case 0x4000102:
                return Bundle.getMessage("LN_MSG_ROUTES_UNSUPPORTED");
            default:
                return Bundle.getMessage("LN_MSG_ROUTES_UNSUPPORTED", 
                    routesEnable, Integer.toHexString(routesCaps));
        }
    }
    public final void setDeviceName(String s) {deviceName = s;}

    /**
     * Set the table view of the device's destination address.
     * This routine does _not_ program the device's destination address.
     *
     * @param destAddr device destination address
     */
    public final void setDestAddr(int destAddr) {this.baseAddress = destAddr;}
    protected final void setSerNum(int serNum) {this.serNum = serNum;}
    

    public final static int getCapsDeviceType(LocoNetMessage l) {
        return l.getElement(9);
    }

    public final static int getCapsOpsw1thru6(LocoNetMessage l) {
        return l.getElement(10)>>1;
    }

    public final static int getCapsSerNum(LocoNetMessage l) {
        return l.getElement(11) + (l.getElement(12) << 7);
    }

    public final static String getCapsHexSerNum(LocoNetMessage l) {
        return Integer.toHexString(l.getElement(11) + (l.getElement(12) << 7));
    }

    public final static int getCapsBaseAddr(LocoNetMessage l) {
        return l.getElement(13) + (l.getElement(14) << 7) + 1;
    }

    public static String getDeviceName(int type) {
        switch (type) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
                return "PM74"; // NOI18N
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS74:
                return "DS74";  // NOI18N
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS78V:
                return "DS78V"; // NOI18N
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                return "SE74"; // NOI18N
            default:
                return "Unknown: 0x"+ Integer.toHexString(type);
        }
    }
        
    public final static int getRoutesCaps(LocoNetMessage l) {
        return l.getElement(2)+(l.getElement(4)<<8) + (l.getElement(5) << 16) +
                (l.getElement(8) << 24);
    }
    
    public final String getObjectsList(String conPrefix, String objType, int base, 
            int count, int stride) {
        StringBuilder result = new StringBuilder();
        String prefix = conPrefix+objType;
        if (count > 8) {
            // use "from/to format"
            result.append(prefix);
            result.append(Integer.toString(base));
            result.append(" thru ");
            result.append(prefix);
            result.append(Integer.toString(base + ((count - 1) * stride)));
        } else {
            // enumerate each object
            for (int i = base; i < base + (count * stride); i = i + stride) {
                result.append(prefix);
                result.append(Integer.toString(i));
                if (i + stride < (base + (count * stride))) {
                    result.append(", ");
                }
            } 
        }
        return result.toString();
    }
    public abstract String getTurnoutsAddresses(String connectionPrefix);
    
    public abstract String getSensorsAddresses();
        
    public List<Integer> getTranspondingZonesList() {
        return new ArrayList<>();
    }
    
    public String getTranspondingZones() {
        return "None";
    }
    
    
    public List<Integer> getPowerMgmtAddressesList() {
        return new ArrayList<>();
    }
    
    public String getPowerMgmtAddresses() {
        return "None";
    }
    
    public List<Integer> getAspectAddressesList() {
        return new ArrayList<>();
    }
    
    public String getAspectAddresses() {
        return "None";
    }
    
    public RosterEntry findCorrespondingRosterEntry() {
        return null;
    }

    public RosterEntry getRosterEntry() {
        if (rosterEntry == null) {
            findCorrespondingRosterEntry();
            return null;
        } else {
            return rosterEntry;
        }
    }
//    private final static Logger log = LoggerFactory.getLogger(AlmDevice.class);


}
