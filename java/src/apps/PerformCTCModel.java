package apps;

import apps.startup.AbstractStartupModel;
import apps.startup.StartupPauseModel;
import java.io.File;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
//import jmri.jmrit.ctc.ctcMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformCTCModel extends AbstractStartupModel {
    private final static Logger log = LoggerFactory.getLogger(PerformCTCModel.class);
    public String getFileName() {
        return this.getName();
    }
    public void setFileName(String n) {
        this.setName(n);
    }
    @Override
    public void performAction() throws JmriException {
        log.info("Loading file {}", this.getFileName());
//        ctcMain blah = new ctcMain();
        // load the file
//      File file = new File(this.getFileName());
//      ConfigureManager cm = InstanceManager.getNullableDefault(ConfigureManager.class);
//      if (cm != null) {
//          cm.load(file);
//      }
    }
}
