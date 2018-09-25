package apps.startup;

import apps.PerformCTCModel;
import java.awt.Component;
import javax.swing.JFileChooser;
import jmri.jmrit.XmlFile;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StartupModelFactory.class)
public class PerformCTCFactory extends AbstractFileModelFactory {
    @Override
    public Class<? extends StartupModel> getModelClass() {
        return PerformCTCModel.class;
    }
    @Override
    public PerformCTCModel newModel() {
        return new PerformCTCModel();
    }
    @Override
    protected JFileChooser setFileChooser() {
        return XmlFile.userFileChooser("XML files", "xml");
    }
}
