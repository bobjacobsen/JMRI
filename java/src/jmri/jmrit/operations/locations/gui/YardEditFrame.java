package jmri.jmrit.operations.locations.gui;

import javax.swing.BorderFactory;

import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.tools.ChangeTrackTypeAction;

/**
 * Frame for user edit of a yard
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
public class YardEditFrame extends TrackEditFrame {

    public YardEditFrame() {
        super(Bundle.getMessage("AddYard"));
    }
    
    @Override
    public void initComponents(Track track) {
        setTitle(Bundle.getMessage("EditYard", track.getLocation().getName()));
        initComponents(track.getLocation(), track);
    }

    @Override
    public void initComponents(Location location, Track track) {
        _type = Track.YARD;
        super.initComponents(location, track);

        _toolMenu.insert(new ChangeTrackTypeAction(this), TOOL_MENU_OFFSET);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Yards", true); // NOI18N

        // override text strings for tracks
        panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainYard")));
        paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesYard")));
        deleteTrackButton.setText(Bundle.getMessage("DeleteYard"));
        addTrackButton.setText(Bundle.getMessage("AddYard"));
        saveTrackButton.setText(Bundle.getMessage("SaveYard"));
        // finish
        dropPanel.setVisible(false); // don't show drop and pick up panel
        pickupPanel.setVisible(false);
        pack();
        setVisible(true);
    }

//    private final static Logger log = LoggerFactory.getLogger(YardEditFrame.class);
}
