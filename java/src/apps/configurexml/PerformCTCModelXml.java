package apps.configurexml;

import apps.StartupActionsManager;
import apps.PerformCTCModel;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformCTCModelXml extends AbstractXmlAdapter {
    private final static Logger log = LoggerFactory.getLogger(PerformCTCModelXml.class);
    public PerformCTCModelXml() {
    }
    @Override
    public Element store(Object o) {
        Element element = new Element("perform");
        element.setAttribute("name", FileUtil.getPortableFilename("CTC.xml"));  // Right now hard coded, later something like: ((StartupCTCModel)o).getFilename()
        element.setAttribute("type", "XmlFile");
        element.setAttribute("class", this.getClass().getName());
        return element;
    }
    @Override
    public boolean loadDeferred() {
        return true;
    }
    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        PerformCTCModel model = new PerformCTCModel();
        String fileName = FileUtil.getAbsoluteFilename(shared.getAttribute("name").getValue());
//          Later:
//          For safety, check existance of file, and don't "model.setFilename(filename)" if invalid.
//          model.setFileName(fileName);
        InstanceManager.getDefault(StartupActionsManager.class).addAction(model);
        return result;
    }
    @Override
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
}
