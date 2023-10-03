package jmri.jmrix.loconet.accy7thgen;

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
    private Integer SensorAddrStart;
    private Integer SensorAddrEnd;
    private Integer ReportersAddrStart;
    private Integer ReportersAddrEnd;
    private Integer AspectAddrStart;
    private Integer AspectAddrEnd;
    private Integer PowerAddrStart;
    private Integer PowerAddrEnd;
    
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
        PowerAddrEnd = getPowerEnd();
    }
    
    private Integer getTurnoutAddressStart() {
        switch (device) {
            case 124:
                // DS78V
                return baseAddr;
            default:
                return -1;
        }
    }

    private Integer getTurnoutAddressEnd() {
        switch (device) {
            case 124:
                // DS78V
                if ((firstOnes & 0xf) == 6) {
                    return baseAddr+15;
                } else {
                    return baseAddr+7;
                }
            default:
                return -1;
        }
    }

    private Integer getSensorAddressStart() {
        switch (device) {
            case 124:
                // DS78V
                return baseAddr;
            default:
                return -1;
        }
    }

    private Integer getSensorAddressEnd() {
        switch (device) {
            case 124:
                // DS78V
                return baseAddr+15;
            default:
                return -1;
        }
    }

    
    private Integer getReportersStart() {
        switch (device) {
            case 124:
                // DS78V
            default:
                return -1;
        }
    }

    private Integer getReportersEnd() {
        switch (device) {
            case 124:
                // DS78V
            default:
                return -1;
        }
    }

    private Integer getAspectStart() {
        switch (device) {
            case 124:
                // DS78V
            default:
                return -1;
        }
    }

    private Integer getAspectEnd() {
        switch (device) {
            case 124:
                // DS78V
            default:
                return -1;
        }
    }

    private Integer getPowerStart() {
        switch (device) {
            case 124:
                // DS78V
            default:
                return -1;
        }
    }

    private Integer getPowerEnd() {
        switch (device) {
            case 124:
                // DS78V
            default:
                return -1;
        }
    }

    public String getTurnouts() {
        if (turnoutAddrStart > 0) {
            return Integer.toString(turnoutAddrStart)+"-"+Integer.toString(turnoutAddrEnd);
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
        if (ReportersAddrStart > 0) {
            return Integer.toString(ReportersAddrStart)+"-"+Integer.toString(ReportersAddrEnd);
        } else {
            return "";
        }
    }
    
    public String getApects() {
        if (AspectAddrStart > 0) {
        return Integer.toString(AspectAddrStart)+"-"+Integer.toString(AspectAddrEnd);
        } else {
            return "";
        }
    }
    
    public String getPowers() {
        if (PowerAddrStart > 0) {
          return Integer.toString(PowerAddrStart)+"-"+Integer.toString(PowerAddrEnd);
        } else {
            return "";
        }
    }
    
    
    
}
