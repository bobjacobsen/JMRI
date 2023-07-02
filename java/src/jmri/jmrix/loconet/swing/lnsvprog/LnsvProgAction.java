package jmri.jmrix.loconet.swing.lnsvprog;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Egbert Broerse Copyright 2020
 * @author Bob Jacobsen  Copyright (C) 2023
 */
public class LnsvProgAction extends LnNamedPaneAction {

    public LnsvProgAction() {
        super(Bundle.getMessage("MenuItemLnsvProg"),
                new JmriJFrameInterface(),
                LnsvProgPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}
