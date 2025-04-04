package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.cars.gui.CarSetFrame;

/**
 * Swing action to create and register a CarsSetFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * 
 */
public class EnableDestinationAction extends AbstractAction {

    CarSetFrame _csFrame;

    public EnableDestinationAction(CarSetFrame frame) {
        super(Bundle.getMessage("MenuEnableDestination"));
        _csFrame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _csFrame.setDestinationEnabled(true);
    }
}


