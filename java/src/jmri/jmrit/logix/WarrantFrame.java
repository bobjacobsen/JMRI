package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.*;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import javax.swing.*;
import javax.swing.table.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.SpeedStepMode;
import jmri.jmrit.picker.PickListModel;
import jmri.util.ThreadingUtil;
import jmri.jmrit.logix.ThrottleSetting.Command;
import jmri.jmrit.logix.ThrottleSetting.CommandValue;
import jmri.jmrit.logix.ThrottleSetting.ValueType;
import jmri.util.swing.JmriJOptionPane;

/**
 * WarrantFame creates and edits Warrants <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010
 */
public class WarrantFrame extends WarrantRoute {

    private int _rowHeight;
    private Warrant _warrant; // unregistered warrant - may be a copy of a
                              // registered warrant
    private Warrant _saveWarrant;
    private ThrottleTableModel _commandModel;
    private JTable _commandTable;
    private JScrollPane _throttlePane;
    private Dimension _viewPortDim;

    private ArrayList<ThrottleSetting> _throttleCommands = new ArrayList<>();
    private long _startTime;
    private float _speedFactor;
    private float _speed;
    private long _TTP = 0;
    private boolean _forward = true;
    private LearnThrottleFrame _learnThrottle = null;
    private static Color myGreen = new Color(0, 100, 0);

    private JTextField _sysNameBox;
    private JTextField _userNameBox;

    private JTabbedPane _tabbedPane;
    private JPanel _routePanel;
    private JPanel _commandPanel;
    private JPanel _parameterPanel;
    private final JRadioButton _isSCWarrant = new JRadioButton(Bundle.getMessage("SmallLayoutTrainAutomater"), false);
    private final JRadioButton _isWarrant = new JRadioButton(Bundle.getMessage("NormalWarrant"), true);
    private DisplayButton _speedUnits;
    private JLabel _unitsLabel;
    private float _speedConversion;
    private final JCheckBox _runForward = new JCheckBox(Bundle.getMessage("Forward"));
    private final JFormattedTextField _speedFactorTextField = new JFormattedTextField();
    private final JFormattedTextField _TTPtextField = new JFormattedTextField();
    private final JCheckBox _noRampBox = new JCheckBox();
    private final JCheckBox _shareRouteBox = new JCheckBox();
    private final JCheckBox _addTracker = new JCheckBox();
    private final JCheckBox _haltStartBox = new JCheckBox();
    private final JCheckBox _runETOnlyBox = new JCheckBox();
    private final JRadioButton _invisible = new JRadioButton();
    private final JTextField _statusBox = new JTextField(90);
    private final JRadioButton _showRoute = new JRadioButton(Bundle.getMessage("showRoute"), false);
    private final JRadioButton _showScript = new JRadioButton(Bundle.getMessage("showScript"), false);

    private final JTextField _searchStatus = new JTextField();
    private boolean _dirty = false;

    /**
     * Constructor for opening an existing warrant for editing.
     * @param w the Warrant to edit.
     */
    protected WarrantFrame(@Nonnull Warrant w) {
        super();
        // w is registered
        _saveWarrant = w;
        // temp unregistered version until editing is saved.
        _warrant = new Warrant(w.getSystemName(), w.getUserName());
        setup(_saveWarrant, false);
        init();
    }

    /**
     * Constructor for creating a new warrant or copy or concatenation of
     * warrants.
     * Called by WarrantTableAction.
     * @param startW the Warrant to Copy or Concatenate.
     * @param endW the other Warrant to Concatenate with.
     */
    protected WarrantFrame(@CheckForNull Warrant startW, @CheckForNull Warrant endW) {
        super();
        WarrantManager mgr = InstanceManager.getDefault(WarrantManager.class);
        String sName = mgr.getAutoSystemName();
        while (mgr.getBySystemName(sName) != null) {
            mgr.updateAutoNumber(sName);
            sName = mgr.getAutoSystemName();
        }
        _warrant = new Warrant(sName, null);
        if (startW != null) {
            if (endW != null) { // concatenate warrants
                WarrantTableFrame tf = WarrantTableFrame.getDefault();
                tf.setVisible(true);
                boolean includeAllCmds = tf.askStopQuestion(startW.getLastOrder().getBlock().getDisplayName());
                /*
                if (JmriJOptionPane.showConfirmDialog(f, Bundle.getMessage("stopAtBlock",
                        startW.getLastOrder().getBlock().getDisplayName()),
                        Bundle.getMessage("QuestionTitle"), JmriJOptionPane.YES_NO_OPTION,
                        JmriJOptionPane.QUESTION_MESSAGE) == JmriJOptionPane.YES_OPTION) {
                    includeAllCmds = true;
                }*/
                float entranceSpeed = setup(startW, !includeAllCmds);
                List<BlockOrder> orders = endW.getBlockOrders();
                BlockOrder bo = orders.get(0);    // block order of common midblock
                bo.setExitName(endW.getfirstOrder().getExitName());
                for (int i = 1; i < orders.size(); i++) {
                    _orders.add(new BlockOrder(orders.get(i)));
                }
                _destination.setOrder(endW.getLastOrder());
                if (_via.getOrder() == null) {
                    _via.setOrder(endW.getViaOrder());
                }
                if (_avoid.getOrder() == null) {
                    _avoid.setOrder(endW.getAvoidOrder());
                }
                float exitSpeed = 0;
                NamedBean bean = bo.getBlock(); // common block
                for (ThrottleSetting ts : endW.getThrottleCommands()) {
                    if (includeAllCmds) {
                        _throttleCommands.add(new ThrottleSetting(ts));
                    } else {
                        Command cmd = ts.getCommand();
                        if (cmd.equals(Command.SPEED)) {
                            exitSpeed = ts.getValue().getFloat();
                        } else if (cmd.equals(Command.NOOP) && !ts.getBean().equals(bean)) {
                            includeAllCmds = true;
                            long et = _speedUtil.getTimeForDistance(entranceSpeed, bo.getPathLength()) / 2;
                            RampData ramp = _speedUtil.getRampForSpeedChange(entranceSpeed, exitSpeed);
                            String blockName = bean.getDisplayName();
                            if (ramp.isUpRamp()) {
                                ListIterator<Float> iter = ramp.speedIterator(true);
                                while (iter.hasNext()) {
                                    float speedSetting = iter.next();
                                    _throttleCommands.add(new ThrottleSetting(et, Command.SPEED, -1, ValueType.VAL_FLOAT,
                                            SpeedStepMode.UNKNOWN, speedSetting, "", blockName, _speedUtil.getTrackSpeed(speedSetting)));
                                    et = ramp.getRampTimeIncrement();
                                }
                            } else {
                                ListIterator<Float> iter = ramp.speedIterator(false);
                                while (iter.hasPrevious()) {
                                    float speedSetting = iter.previous();
                                    _throttleCommands.add(new ThrottleSetting(et, Command.SPEED, -1, ValueType.VAL_FLOAT,
                                            SpeedStepMode.UNKNOWN, speedSetting, "", blockName, _speedUtil.getTrackSpeed(speedSetting)));
                                    et = ramp.getRampTimeIncrement();
                                }
                            }
                            _throttleCommands.add(new ThrottleSetting(ts));
                        }
                    }
                }
            } else {    // else just copy startW
                setup(startW, false);
            }
        } // else create new warrant
        init();
    }

    /**
     * Set up parameters from an existing warrant. note that _warrant is
     * unregistered.
     */
    private float setup(@Nonnull Warrant warrant, boolean omitLastBlockCmds) {
        _origin.setOrder(warrant.getfirstOrder());
        _destination.setOrder(warrant.getLastOrder());
        _via.setOrder(warrant.getViaOrder());
        _avoid.setOrder(warrant.getAvoidOrder());
        List<BlockOrder> list = warrant.getBlockOrders();
        _orders = new ArrayList<>(list.size());
        for (BlockOrder bo : list) {
            _orders.add(new BlockOrder(bo));
        }

        if (warrant instanceof SCWarrant) {
            _speedFactor = ((SCWarrant) warrant).getSpeedFactor();
            _TTP = ((SCWarrant) warrant).getTimeToPlatform();
            _forward = ((SCWarrant) warrant).getForward();
        }

        float entranceSpeed = 0;
        for (ThrottleSetting ts : warrant.getThrottleCommands()) {
            if (omitLastBlockCmds && !list.isEmpty()) {
                NamedBean bean = list.get(list.size()-1).getBlock();
                Command cmd = ts.getCommand();
                if (cmd.equals(Command.SPEED)) {
                    entranceSpeed = ts.getValue().getFloat();
                }
                _throttleCommands.add(new ThrottleSetting(ts));
               if (cmd.equals(Command.NOOP) && ts.getBean().equals(bean)) {
                     break;
                }
            } else {
                _throttleCommands.add(new ThrottleSetting(ts));
            }
        }
        _shareRouteBox.setSelected(warrant.getShareRoute());
        _warrant.setShareRoute(warrant.getShareRoute());
        _addTracker.setSelected(warrant.getAddTracker());
        _warrant.setAddTracker(warrant.getAddTracker());
        _haltStartBox.setSelected(warrant.getHaltStart());
        _warrant.setHaltStart(warrant.getHaltStart());
        _noRampBox.setSelected(warrant.getNoRamp());
        _warrant.setNoRamp(warrant.getNoRamp());
        _runETOnlyBox.setSelected(warrant.getRunBlind());
        _warrant.setRunBlind(warrant.getRunBlind());
        setTrainName(warrant.getTrainName());
        _warrant.setTrainName(warrant.getTrainName());

        SpeedUtil spU = warrant.getSpeedUtil();
        setSpeedUtil(_warrant.getSpeedUtil());
        _speedUtil.setDccAddress(spU.getDccAddress());
        _speedUtil.setRosterId(spU.getRosterId());
        if (_speedUtil.getDccAddress() != null) {
            setTrainInfo(warrant.getTrainName());
        } else {
            setTrainName(warrant.getTrainName());
        }
        return entranceSpeed;
    }

    private void init() {
        _commandModel = new ThrottleTableModel();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));

        contentPane.add(makeTopPanel(), BorderLayout.NORTH);

        _tabbedPane = new JTabbedPane();
        _tabbedPane.addTab(Bundle.getMessage("MakeRoute"), makeFindRouteTabPanel());
        _tabbedPane.addTab(Bundle.getMessage("RecordPlay"), makeSetPowerTabPanel());
        contentPane.add(_tabbedPane, BorderLayout.CENTER);

        contentPane.add(makeEditableButtonPanel(), BorderLayout.SOUTH);
        if (_orders != null && !_orders.isEmpty()) {
            _tabbedPane.setSelectedIndex(1);
        }
        if (!_throttleCommands.isEmpty()) {
            _showScript.setSelected(true);
        }
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (askClose()) {
                    WarrantTableAction.getDefault().closeWarrantFrame();
                }
            }
        });

        makeMenus();
        setTitle(Bundle.getMessage("editing", _warrant.getDisplayName()));
        setContentPane(contentPane);
        setVisible(true);
        _parameterPanel.setMaximumSize(_parameterPanel.getPreferredSize());
        _dirty = false;
        pack();
        getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Component c = (Component) e.getSource();
                int height = c.getHeight();
                _viewPortDim.height = (_rowHeight * 10) + height - 541;
                _throttlePane.getViewport().setPreferredSize(_viewPortDim);
                _throttlePane.invalidate();
                _commandTable.invalidate();
            }
        });
        speedUnitsAction();
    }

    public boolean askClose() {
        if (_dirty) {
            // if runMode != MODE_NONE, this is probably a panic shutdown. Don't
            // halt it.
            if (JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("saveOrClose", _warrant.getDisplayName()),
                    Bundle.getMessage("QuestionTitle"), JmriJOptionPane.YES_NO_OPTION,
                    JmriJOptionPane.QUESTION_MESSAGE) == JmriJOptionPane.YES_OPTION) {
                if (!isRunning()) {
                    save();
                }
            }
        }
        _dirty = false;
        return true;
    }

    private JPanel makeTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
        panel.add( sysNameLabel );
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        if (_saveWarrant != null) {
            _sysNameBox = new JTextField(_saveWarrant.getSystemName());
            _sysNameBox.setEditable(false);
            _userNameBox = new JTextField(_saveWarrant.getUserName());
        } else {
            _sysNameBox = new JTextField(_warrant.getSystemName());
            _userNameBox = new JTextField(_warrant.getUserName());
        }
        sysNameLabel.setLabelFor(_sysNameBox);
        _sysNameBox.setBackground(Color.white);
        panel.add(_sysNameBox);

        panel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
        userNameLabel.setLabelFor( _userNameBox );
        panel.add( userNameLabel );
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add( _userNameBox );

        panel.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        topPanel.add(panel);
        topPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        return topPanel;
    }

    private JPanel makeFindRouteTabPanel() {
        JPanel tab1 = new JPanel();
        tab1.setLayout(new BoxLayout(tab1, BoxLayout.LINE_AXIS));
        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel topLeft = new JPanel();
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.PAGE_AXIS));

        topLeft.add(makeBlockPanels(false));

        topLeft.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        tab1.add(topLeft);

        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel topRight = new JPanel();
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.LINE_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        panel.add(calculatePanel(true));
        panel.add(Box.createVerticalStrut(2 * STRUT_SIZE));
        panel.add(searchDepthPanel(true));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(makeTextBoxPanel(true, _searchStatus, "SearchRoute", null));
        _searchStatus.setEditable(false);
        p.add(Box.createVerticalGlue());
        panel.add(p);

        _searchStatus.setBackground(Color.white);
        _searchStatus.setEditable(false);
        panel.add(Box.createRigidArea(new Dimension(10,
                topLeft.getPreferredSize().height - panel.getPreferredSize().height)));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(Box.createVerticalGlue());
        topRight.add(panel);
        topRight.add(Box.createHorizontalStrut(STRUT_SIZE));

        PickListModel<OBlock> pickListModel = PickListModel.oBlockPickModelInstance();
        topRight.add(new JScrollPane(pickListModel.makePickTable()));
        Dimension dim = topRight.getPreferredSize();
        topRight.setMinimumSize(dim);
        tab1.add(topRight);
        tab1.add(Box.createHorizontalStrut(STRUT_SIZE));
        return tab1;
    }

    private JPanel makeSetPowerTabPanel() {
        JPanel tab2 = new JPanel();
        tab2.setLayout(new BoxLayout(tab2, BoxLayout.PAGE_AXIS));
        tab2.add(makeTabMidPanel());

        _parameterPanel = new JPanel();
        _parameterPanel.setLayout(new BoxLayout(_parameterPanel, BoxLayout.LINE_AXIS));

        _parameterPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _parameterPanel.add(makeBorderedTrainPanel());
        _parameterPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel typePanel = makeTypePanel();
        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("SelectType"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(typePanel);
        _parameterPanel.add(edge);
        _parameterPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel scParamPanel = makeSCParamPanel();
        edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("SetSCParameters"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(scParamPanel);
        _parameterPanel.add(edge);
        _parameterPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel learnPanel = makeRecordPanel();
        edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("LearnMode"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(learnPanel);
        _parameterPanel.add(edge);
        _parameterPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel paramsPanel = makeRunParmsPanel();
        edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("RunParameters"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(paramsPanel);
        _parameterPanel.add(edge);
        _parameterPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel runPanel = makePlaybackPanel();
        edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("RunTrain"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(runPanel);
        _parameterPanel.add(edge);
        _parameterPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _parameterPanel.setPreferredSize(_parameterPanel.getPreferredSize());
        tab2.add(_parameterPanel);

        _isSCWarrant.addActionListener((ActionEvent e) -> {
            setPanelEnabled(scParamPanel, true);
            setPanelEnabled(learnPanel, false);
            setPanelEnabled(paramsPanel, false);
            setPanelEnabled(runPanel, false);
        });
        if ( _saveWarrant instanceof SCWarrant) {
            setPanelEnabled(scParamPanel, true);
            setPanelEnabled(learnPanel, false);
            setPanelEnabled(paramsPanel, false);
            setPanelEnabled(runPanel, false);
            _isSCWarrant.setVisible(true);
        }

        _isWarrant.addActionListener((ActionEvent e) -> {
            setPanelEnabled(scParamPanel, false);
            setPanelEnabled(learnPanel, true);
            setPanelEnabled(paramsPanel, true);
            setPanelEnabled(runPanel, true);
        });

        JPanel panel = new JPanel();
        panel.add(makeTextBoxPanel(false, _statusBox, "Status", null));
        _statusBox.setEditable(false);
        _statusBox.setMinimumSize(new Dimension(300, _statusBox.getPreferredSize().height));
        _statusBox.setMaximumSize(new Dimension(900, _statusBox.getPreferredSize().height));
        panel.add(_statusBox);
        tab2.add(panel);

        return tab2;
    }

    private void setPanelEnabled(@Nonnull JPanel panel, Boolean isEnabled) {
        panel.setEnabled(isEnabled);

        Component[] components = panel.getComponents();

        for (Component component : components) {
            if ( component == null ) {
                continue;
            }
            if ( component instanceof JPanel ) {
                setPanelEnabled((JPanel) component, isEnabled);
            }
            component.setEnabled(isEnabled);
        }
    }

    private JPanel makeBorderedTrainPanel() {
        JPanel trainPanel = makeTrainIdPanel(null);

        JPanel edge = new JPanel();
        edge.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK),
                Bundle.getMessage("SetPower"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP));
        edge.add(trainPanel);
        return edge;
    }

    private JPanel makeTypePanel() {
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.LINE_AXIS));
        typePanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel wTypePanel = new JPanel();
        wTypePanel.setLayout(new BoxLayout(wTypePanel, BoxLayout.PAGE_AXIS));
        wTypePanel.add(Box.createVerticalStrut(STRUT_SIZE));
        ButtonGroup group = new ButtonGroup();
        group.add(_isSCWarrant);
        group.add(_isWarrant);
        _isSCWarrant.setToolTipText(Bundle.getMessage("SCW_Tooltip"));
        _isWarrant.setToolTipText(Bundle.getMessage("W_Tooltip"));
        wTypePanel.add(_isSCWarrant);
        wTypePanel.add(_isWarrant);
        typePanel.add(wTypePanel);
        return typePanel;
    }

    private void addSpeeds() {
        float speed = 0.0f;
        for (ThrottleSetting ts : _throttleCommands) {
            CommandValue cmdVal = ts.getValue();
            ValueType valType = cmdVal.getType();
            switch (valType) {
                case VAL_FLOAT:
                    speed = _speedUtil.getTrackSpeed(cmdVal.getFloat());
                    break;
                case VAL_TRUE:
                    _speedUtil.setIsForward(true);
                    break;
                case VAL_FALSE:
                    _speedUtil.setIsForward(false);
                    break;
                default:
            }
            ts.setTrackSpeed(speed);
        }
        _commandModel.fireTableDataChanged();
        showCommands(true);
    }

    private JPanel makeSCParamPanel() {
        JPanel scParamPanel = new JPanel();
        scParamPanel.setLayout(new BoxLayout(scParamPanel, BoxLayout.PAGE_AXIS));
        scParamPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        scParamPanel.add(_runForward);
        _runForward.setSelected(_forward);

        JPanel ttpPanel = new JPanel();
        ttpPanel.setLayout(new BoxLayout(ttpPanel, BoxLayout.LINE_AXIS));
        JLabel ttp_l = new JLabel(Bundle.getMessage("TTP"));
        _TTPtextField.setValue(_TTP);
        _TTPtextField.setColumns(6);
        ttp_l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        _TTPtextField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        ttpPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        ttpPanel.add(ttp_l);
        ttpPanel.add(_TTPtextField);
        ttpPanel.setToolTipText(Bundle.getMessage("TTPtoolTip"));
        scParamPanel.add(ttpPanel);

        JPanel sfPanel = new JPanel();
        sfPanel.setLayout(new BoxLayout(sfPanel, BoxLayout.LINE_AXIS));
        JLabel sf_l = new JLabel(Bundle.getMessage("SF"));
        _speedFactorTextField.setValue((long) (100 * _speedFactor));
        _speedFactorTextField.setColumns(3);
        sf_l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        _speedFactorTextField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        sfPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        sfPanel.add(sf_l);
        sfPanel.add(_speedFactorTextField);
        sfPanel.setToolTipText(Bundle.getMessage("sfToolTip"));
        scParamPanel.add(sfPanel);

        if (_isWarrant.isSelected()) {
            setPanelEnabled(scParamPanel, false);
        }
        return scParamPanel;
    }

    private JPanel makeRecordPanel() {
        JPanel learnPanel = new JPanel();
        learnPanel.setLayout(new BoxLayout(learnPanel, BoxLayout.LINE_AXIS));
        learnPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel startStopPanel = new JPanel();
        startStopPanel.setLayout(new BoxLayout(startStopPanel, BoxLayout.PAGE_AXIS));
        startStopPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JButton startButton = new JButton(Bundle.getMessage("Start"));
        startButton.addActionListener((ActionEvent e) -> {
            clearTempWarrant();
            _tabbedPane.setSelectedIndex(1);
            showCommands(true);
            runLearnModeTrain();
        });
        JButton stopButton = new JButton(Bundle.getMessage("Stop"));
        stopButton.addActionListener((ActionEvent e) -> {
            stopRunTrain(false);
        });
        startButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        startStopPanel.add(startButton);
        startStopPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        startStopPanel.add(stopButton);
        startStopPanel.add(Box.createRigidArea(new Dimension(30 + stopButton.getPreferredSize().width, 10)));
        learnPanel.add(startStopPanel);

        return learnPanel;
    }

    private JPanel makeRunParmsPanel() {
        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.LINE_AXIS));
        paramsPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeTextBoxPanel(_shareRouteBox, "ShareRoute", "ToolTipShareRoute"));
        panel.add(makeTextBoxPanel(_addTracker, "AddTracker", "ToolTipAddTracker"));
        panel.add(makeTextBoxPanel(_noRampBox, "NoRamping", "ToolTipNoRamping"));
        panel.add(makeTextBoxPanel(_haltStartBox, "HaltAtStart", null));
        panel.add(makeTextBoxPanel(_runETOnlyBox, "RunETOnly", "ToolTipRunETOnly"));

        paramsPanel.add(panel);
        return paramsPanel;
    }

    private JPanel makePlaybackPanel() {
        JPanel runPanel = new JPanel();
        runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.LINE_AXIS));
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        runPanel.add(panel);
        runPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

        JRadioButton run = new JRadioButton(Bundle.getMessage("ARun"), false);
        JRadioButton halt = new JRadioButton(Bundle.getMessage("Stop"), false);
        JRadioButton resume = new JRadioButton(Bundle.getMessage("Resume"), false);
        JRadioButton eStop = new JRadioButton(Bundle.getMessage("EStop"), false);
        JRadioButton abort = new JRadioButton(Bundle.getMessage("Abort"), false);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        ButtonGroup group = new ButtonGroup();
        group.add(run);
        group.add(halt);
        group.add(resume);
        group.add(eStop);
        group.add(abort);
        group.add(_invisible);
        panel.add(run);
        panel.add(halt);
        panel.add(resume);
        panel.add(eStop);
        panel.add(abort);
        runPanel.add(panel);

        run.addActionListener((ActionEvent e) -> {
            runTrain();
        });
        halt.addActionListener((ActionEvent e) -> {
            doControlCommand(Warrant.HALT);
        });
        resume.addActionListener((ActionEvent e) -> {
            doControlCommand(Warrant.RESUME);
        });
        eStop.addActionListener((ActionEvent e) -> {
            doControlCommand(Warrant.ESTOP);
        });
        abort.addActionListener((ActionEvent e) -> {
            doControlCommand(Warrant.ABORT);
        });
        runPanel.add(panel);
        return runPanel;
    }

    private JPanel makeTabMidPanel() {
        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.PAGE_AXIS));

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.LINE_AXIS));
        tablePanel.add(Box.createHorizontalStrut(5));
        _routePanel = makeRouteTablePanel();
        tablePanel.add(_routePanel);
        tablePanel.add(makeThrottleTablePanel());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        ButtonGroup group = new ButtonGroup();
        group.add(_showRoute);
        group.add(_showScript);
        buttonPanel.add(_showRoute);
        buttonPanel.add(_showScript);
        boolean show = (!_throttleCommands.isEmpty());
        showCommands(show);
        _showScript.setSelected(show);
        _showRoute.addActionListener((ActionEvent e) -> {
            showCommands(false);
        });
        _showScript.addActionListener((ActionEvent e) -> {
            showCommands(true);
        });

        if (_saveWarrant != null && _saveWarrant instanceof SCWarrant) {
            _showRoute.setSelected(true);
            showCommands(false);
            setPanelEnabled(buttonPanel, false);
        }
        _isSCWarrant.addActionListener((ActionEvent e) -> {
            _showRoute.setSelected(true);
            showCommands(false);
            setPanelEnabled(buttonPanel, false);
        });
        _isWarrant.addActionListener((ActionEvent e) -> {
            setPanelEnabled(buttonPanel, true);
        });

        midPanel.add(buttonPanel);
        midPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        midPanel.add(tablePanel);
        midPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        return midPanel;
    }

    private void showCommands(boolean setCmds) {
        _routePanel.setVisible(!setCmds);
        _commandPanel.setVisible(setCmds);
    }

    private void speedUnitsAction() {
        switch (_displayPref) {
            case MPH:
                _displayPref = Display.KPH;
                _speedConversion = _scale * 3.6f;
                setFormatter("kph");
                break;
            case KPH:
                _displayPref = Display.MMPS;
                _speedConversion = 1000;
                _unitsLabel.setText(Bundle.getMessage("trackSpeed"));
                setFormatter("mmps");
                break;
            case MMPS:
                _displayPref = Display.INPS;
                _speedConversion = 39.37f;
                setFormatter("inps");
                break;
            case INPS:
            default:
                _displayPref = Display.MPH;
                _speedConversion = 2.23694f * _scale;
                _unitsLabel.setText(Bundle.getMessage("scaleSpeed"));
                setFormatter("mph");
                break;
        }
        _speedUnits.setDisplayPref(_displayPref);
        addSpeeds();
    }

    private void setFormatter(String title) {
        JTableHeader header = _commandTable.getTableHeader();
        TableColumnModel colMod = header.getColumnModel();
        TableColumn tabCol = colMod.getColumn(ThrottleTableModel.SPEED_COLUMN);
        tabCol.setHeaderValue(Bundle.getMessage(title));
        header.repaint();
    }

    private JPanel makeThrottleTablePanel() {
        _commandTable = new JTable(_commandModel);
        DefaultCellEditor ed = (DefaultCellEditor) _commandTable.getDefaultEditor(String.class);
        ed.setClickCountToStart(1);

        TableColumnModel columnModel = _commandTable.getColumnModel();
        for (int i = 0; i < _commandModel.getColumnCount(); i++) {
            int width = _commandModel.getPreferredWidth(i);
            columnModel.getColumn(i).setPreferredWidth(width);
        }
        TableColumn cmdColumn = columnModel.getColumn(ThrottleTableModel.COMMAND_COLUMN);
        cmdColumn.setCellEditor(new CommandCellEditor(new JComboBox<>()));
        cmdColumn.setCellRenderer(new CommandCellRenderer());
        cmdColumn.setMinWidth(40);

        TableColumn valueColumn = columnModel.getColumn(ThrottleTableModel.VALUE_COLUMN);
        valueColumn.setCellEditor(new ValueCellEditor(new JTextField()));

        _throttlePane = new JScrollPane(_commandTable);
        _viewPortDim = _commandTable.getPreferredSize();
        _rowHeight = _commandTable.getRowHeight();
        _viewPortDim.height = _rowHeight * 10;
        _throttlePane.getViewport().setPreferredSize(_viewPortDim);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.add(Box.createVerticalStrut(2 * STRUT_SIZE));

        JButton insertButton = new JButton(Bundle.getMessage("buttonInsertRow"));
        insertButton.addActionListener((ActionEvent e) -> {
            insertRow();
        });
        buttonPanel.add(insertButton);
        buttonPanel.add(Box.createVerticalStrut(2 * STRUT_SIZE));

        JButton deleteButton = new JButton(Bundle.getMessage("buttonDeleteRow"));
        deleteButton.addActionListener((ActionEvent e) -> {
            deleteRow();
        });
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createVerticalStrut(2 * STRUT_SIZE));

        if (_displayPref.equals(Display.MMPS) || _displayPref.equals(Display.INPS)) {
            _unitsLabel = new JLabel(Bundle.getMessage("trackSpeed"));
        } else {
            _unitsLabel = new JLabel(Bundle.getMessage("scaleSpeed"));
        }
        _unitsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        _speedUnits = new DisplayButton(_displayPref);
        FontMetrics fm = _speedUnits.getFontMetrics(_speedUnits.getFont());
        int width = Math.max(fm.stringWidth(Display.KPH.toString()),
                Math.max(fm.stringWidth(Display.MPH.toString()),
                        fm.stringWidth(Display.MMPS.toString())));
        Dimension d = _speedUnits.getPreferredSize();
        d.width = width + 40;
        _speedUnits.setMaximumSize(d);
        _speedUnits.setMinimumSize(d);
        _speedUnits.setPreferredSize(d);
        _speedUnits.addActionListener((ActionEvent evt) -> speedUnitsAction());

        buttonPanel.add(_unitsLabel);
        buttonPanel.add(_speedUnits);

        _commandPanel = new JPanel();
        _commandPanel.setLayout(new BoxLayout(_commandPanel, BoxLayout.PAGE_AXIS));
        JLabel title = new JLabel(Bundle.getMessage("CommandTableTitle"));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        JPanel p = new JPanel();
        p.add(_throttlePane);
        panel.add(p);
        buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        panel.add(buttonPanel);
        buttonPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        _commandPanel.add(title);
        _commandPanel.add(panel);
        _commandPanel.add(Box.createGlue());
        _displayPref = Display.KPH;
        return _commandPanel;
    }

    private void insertRow() {
        int row = _commandTable.getSelectedRow();
        if (row < 0) {
            showWarning(Bundle.getMessage("selectRow"));
            return;
        }
        row++;
        _throttleCommands.add(row, new ThrottleSetting());
        _commandModel.fireTableDataChanged();
        _commandTable.setRowSelectionInterval(row, row);
    }

    private void deleteRow() {
        int row = _commandTable.getSelectedRow();
        if (row < 0) {
            showWarning(Bundle.getMessage("selectRow"));
            return;
        }
        ThrottleSetting cmd = _throttleCommands.get(row);
        if (cmd != null && cmd.getCommand() != null) {
            if (cmd.getCommand().equals(Command.NOOP)) {
                showWarning(Bundle.getMessage("cannotDeleteNoop"));
                return;
            }
            long time = cmd.getTime();
            if ((row + 1) < _throttleCommands.size()) {
                time += _throttleCommands.get(row + 1).getTime();
                _throttleCommands.get(row + 1).setTime(time);
            }
        }
        _throttleCommands.remove(row);
        _dirty = true;
        _commandModel.fireTableDataChanged();
    }

    /**
     * Save, Cancel, Delete buttons
     */
    private JPanel makeEditableButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalStrut(10 * STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
        saveButton.addActionListener((ActionEvent e) -> {
            if (save()) {
                WarrantTableAction.getDefault().closeWarrantFrame();
            }
        });
        panel.add(saveButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3 * STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JButton copyButton = new JButton(Bundle.getMessage("ButtonCopy"));
        copyButton.addActionListener((ActionEvent e) -> {
            WarrantTableAction.getDefault().makeWarrantFrame(_saveWarrant, null);
        });
        panel.add(copyButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3 * STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener((ActionEvent e) -> {
            close();
        });
        panel.add(cancelButton);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        buttonPanel.add(panel);
        buttonPanel.add(Box.createHorizontalStrut(3 * STRUT_SIZE));

        buttonPanel.add(Box.createHorizontalGlue());
        return buttonPanel;
    }

    private void doControlCommand(int cmd) {
        if (log.isDebugEnabled()) {
            log.debug("actionPerformed on doControlCommand  cmd= {}", cmd);
        }
        int runMode = _warrant.getRunMode();
        if (runMode == Warrant.MODE_NONE) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NotRunning", _warrant.getDisplayName()),
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
        } else if (runMode == Warrant.MODE_LEARN && cmd != Warrant.ABORT) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("LearnInvalidControl", _warrant.getDisplayName()),
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
        } else {
            _warrant.controlRunTrain(cmd);
        }
        _invisible.setSelected(true);
    }

    private void makeMenus() {
        setTitle(Bundle.getMessage("TitleWarrant", _warrant.getDisplayName()));
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.add(new jmri.configurexml.StoreMenu());
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.CreateEditWarrant", true);
    }

    private void clearCommands() {
        _throttleCommands = new ArrayList<>();
        _commandModel.fireTableDataChanged();
        _searchStatus.setText("");
    }

    @Override
    protected void selectedRoute(ArrayList<BlockOrder> orders) {
        clearCommands();
        _tabbedPane.setSelectedIndex(1);
    }

    /**
     * Sets address and block orders and does checks Non-null return is fatal
     */
    private String checkTrainId() {
        String msg = setAddress(); // sets SpeedUtil address in 'this'
                                   // (WarrantRoute)
        if (msg == null) {
            msg = routeIsValid();
        }
        if (msg == null) {
            _warrant.setBlockOrders(getOrders());
            msg = _warrant.checkforTrackers();
        }
        if (msg == null) {
            msg = checkLocoAddress();
        }
        return msg;
    }

    private String checkThrottleCommands() {
        if (_throttleCommands.size() <= getOrders().size() + 1) {
            return Bundle.getMessage("NoCommands", _warrant.getDisplayName());
        }
        float lastSpeed = 0.0f;
        for (int i = 0; i < _throttleCommands.size(); i++) {
            ThrottleSetting ts = _throttleCommands.get(i);
            Command cmd = ts.getCommand();
            CommandValue val = ts.getValue();
            if (val == null || cmd == null) {
                return Bundle.getMessage("BadThrottleSetting", i + 1);
            }
            ValueType valType = val.getType();
            if (valType == null) {
                return Bundle.getMessage("BadThrottleSetting", i + 1);
            }
            switch (cmd) {
                case SPEED:
                    if (valType != ValueType.VAL_FLOAT) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    lastSpeed = ts.getValue().getFloat();
                    if (lastSpeed > 1) {
                        return Bundle.getMessage("badSpeed", lastSpeed);
                    } else if (lastSpeed < 0) { // EStop OK only in the last
                                                // block
                        OBlock blk = getOrders().get(getOrders().size() - 1).getBlock();
                        if ( !blk.getSystemName().equals(ts.getBeanSystemName())) {
                            return Bundle.getMessage("badSpeed", lastSpeed);
                        }
                    }
                    break;
                case NOOP:
                    if (valType != ValueType.VAL_NOOP) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    break;
                case FORWARD:
                    if (valType != ValueType.VAL_TRUE && valType != ValueType.VAL_FALSE) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    break;
                case FKEY:
                case LATCHF:
                    if (valType != ValueType.VAL_ON && valType != ValueType.VAL_OFF) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    break;
                case SET_SENSOR:
                case WAIT_SENSOR:
                    if (valType != ValueType.VAL_ACTIVE && valType != ValueType.VAL_INACTIVE) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    String msg = ts.getBeanDisplayName();
                    if (msg == null) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    msg = WarrantFrame.checkBeanName(cmd, ts.getBeanDisplayName());
                    if (msg != null) {
                        return msg +
                                '\n' +
                                Bundle.getMessage("badThrottleCommand",
                                        i + 1, cmd.toString(), valType.toString());
                    }
                    break;
                case RUN_WARRANT:
                    if (valType != ValueType.VAL_INT) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    msg = ts.getBeanDisplayName();
                    if (msg == null) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    msg = WarrantFrame.checkBeanName(cmd, ts.getBeanDisplayName());
                    if (msg != null) {
                        return msg +
                                '\n' +
                                Bundle.getMessage("badThrottleCommand",
                                        i + 1, cmd.toString(), valType.toString());
                    }
                    break;
                case SPEEDSTEP:
                    if (valType != ValueType.VAL_STEP) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    break;
                case SET_MEMORY:
                    if (valType != ValueType.VAL_TEXT) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    msg = ts.getBeanDisplayName();
                    if (msg == null) {
                        return Bundle.getMessage("badThrottleCommand",
                                i + 1, cmd.toString(), valType.toString());
                    }
                    msg = WarrantFrame.checkBeanName(cmd, ts.getBeanDisplayName());
                    if (msg != null) {
                        return msg +
                                '\n' +
                                Bundle.getMessage("badThrottleCommand",
                                        i + 1, cmd.toString(), valType.toString());
                    }
                    break;
                default:
                    return Bundle.getMessage("BadThrottleSetting", i + 1);
            }
        }
        if (lastSpeed > 0.0f) {
            return Bundle.getMessage("BadLastSpeed", lastSpeed);
        }
        return null;
    }

    static String checkBeanName(Command command, String beanName) {
        switch (command) {
            case SET_SENSOR:
            case WAIT_SENSOR:
                if (InstanceManager.sensorManagerInstance().getSensor(beanName) == null) {
                    return Bundle.getMessage("BadSensor", beanName);
                }
                break;
            case RUN_WARRANT:
                if (InstanceManager.getDefault(WarrantManager.class).getWarrant(beanName) == null) {
                    return Bundle.getMessage("BadWarrant", beanName);
                }
                break;
            case SET_MEMORY:
                if (InstanceManager.getDefault(jmri.MemoryManager.class).getMemory(beanName) == null) {
                    return Bundle.getMessage("BadMemory", beanName);
                }
                break;
            default:
                if (InstanceManager.getDefault(OBlockManager.class).getOBlock(beanName) == null) {
                    return Bundle.getMessage("BlockNotFound", beanName);
                }
                break;
        }
        return null;
    }

    private void runLearnModeTrain() {
        _warrant.setSpeedUtil(_speedUtil); // transfer SpeedUtil to warrant
        String msg = null;
        if (isRunning()) {
            msg = Bundle.getMessage("CannotRun", _warrant.getDisplayName(),
                    Bundle.getMessage("TrainRunning", _warrant.getTrainName()));
        }
        if (msg == null) {
            _warrant.setBlockOrders(getOrders());
            msg = checkTrainId();
        }
        if (msg == null) {
            msg = _warrant.checkRoute();
        }
        if (msg == null) {
            msg = WarrantTableFrame.getDefault().getModel().checkAddressInUse(_warrant);
        }
        if (msg == null) {
            msg = _warrant.allocateRoute(false, getOrders());
        }
        toFront();

        if (msg != null) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("LearnError", msg),
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            _warrant.deAllocate();
            setStatus(msg, Color.red);
            return;
        }

        if (!_throttleCommands.isEmpty()) {
            if (JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("deleteCommand"),
                    Bundle.getMessage("QuestionTitle"), JmriJOptionPane.YES_NO_OPTION,
                    JmriJOptionPane.QUESTION_MESSAGE) != JmriJOptionPane.YES_OPTION ) {
                return;
            }
            _throttleCommands = new ArrayList<>();
            _commandModel.fireTableDataChanged();
        }

        msg = _warrant.checkStartBlock();
        if (msg != null) {
            if (msg.equals("warnStart")) {
                msg = Bundle.getMessage("warnStart", getTrainName(), _warrant.getCurrentBlockName());
                JmriJOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
                setStatus(msg, Color.red);
                return;
            } else if (msg.equals("BlockDark")) {
                msg = Bundle.getMessage("BlockDark", _warrant.getCurrentBlockName(), getTrainName());
                if (JmriJOptionPane.YES_OPTION != JmriJOptionPane.showConfirmDialog(this,
                        Bundle.getMessage("OkToRun", msg), Bundle.getMessage("QuestionTitle"),
                        JmriJOptionPane.YES_NO_OPTION, JmriJOptionPane.WARNING_MESSAGE)) {
                    stopRunTrain(true);
                    setStatus(msg, Color.red);
                    return;
                }
            }
            setStatus(msg, Color.black);
        }

        if (_learnThrottle == null) {
            _learnThrottle = new LearnThrottleFrame(this);
        } else {
            _learnThrottle.setVisible(true);
        }

        _warrant.setTrainName(getTrainName());
        _startTime = System.currentTimeMillis();
        _speed = 0.0f;

        _warrant.addPropertyChangeListener(this);

        msg = _warrant.setRunMode(Warrant.MODE_LEARN, _speedUtil.getDccAddress(), _learnThrottle,
                _throttleCommands, _runETOnlyBox.isSelected());
        if (msg != null) {
            stopRunTrain(true);
            JmriJOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"),
                    JmriJOptionPane.WARNING_MESSAGE);
            setStatus(msg, Color.red);
        }
    }

    private long lastClicktime; // keep double clicks from showing dialogs

    protected void runTrain() {
        long time = System.currentTimeMillis();
        if (time - lastClicktime < 1000) {
            return;
        }
        lastClicktime = time;

        _warrant.setSpeedUtil(_speedUtil); // transfer SpeedUtil to warrant
        String msg = null;
        if (isRunning()) {
            msg = Bundle.getMessage("CannotRun", _warrant.getDisplayName(),
                Bundle.getMessage("TrainRunning", _warrant.getTrainName()));
        }
        if (msg == null) {
            _warrant.setTrainName(getTrainName());
            _warrant.setShareRoute(_shareRouteBox.isSelected());
            _warrant.setAddTracker(_addTracker.isSelected());
            _warrant.setHaltStart(_haltStartBox.isSelected());
            _warrant.setNoRamp(_noRampBox.isSelected());
        }
        if (msg == null) {
            msg = checkTrainId();
        }
        if (msg == null) {
            msg = checkThrottleCommands();
            if (msg == null && !_warrant.hasRouteSet() && _runETOnlyBox.isSelected()) {
                msg = Bundle.getMessage("BlindRouteNotSet", _warrant.getDisplayName());
            }
        }
        if (msg == null) {
            WarrantTableModel model = WarrantTableFrame.getDefault().getModel();
            msg = model.checkAddressInUse(_warrant);
        }

        if (msg != null) {
            JmriJOptionPane.showMessageDialog(this, msg, Bundle.getMessage("WarningTitle"),
                JmriJOptionPane.WARNING_MESSAGE);

            setStatus(msg, Color.black);
            return;
        }
        if (_warrant.getRunMode() != Warrant.MODE_NONE) {
            return;
        }
        _warrant.addPropertyChangeListener(this);

        msg = _warrant.setRunMode(Warrant.MODE_RUN, _speedUtil.getDccAddress(), null,
                _throttleCommands, _runETOnlyBox.isSelected());
        if (msg != null) {
            clearWarrant();
            JmriJOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            setStatus(msg, Color.red);
            return;
        }

        msg = _warrant.checkStartBlock();
        if (msg != null) {
            if (msg.equals("warnStart")) {
                msg = Bundle.getMessage("warnStart", _warrant.getTrainName(), _warrant.getCurrentBlockName());
            } else if (msg.equals("BlockDark")) {
                msg = Bundle.getMessage("BlockDark", _warrant.getCurrentBlockName(), _warrant.getTrainName());
            }
            if (JmriJOptionPane.YES_OPTION != JmriJOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("OkToRun", msg), Bundle.getMessage("QuestionTitle"),
                    JmriJOptionPane.YES_NO_OPTION, JmriJOptionPane.WARNING_MESSAGE)) {
                clearWarrant();
                setStatus(msg, Color.red);
            } else {
                setStatus(_warrant.getRunningMessage(), myGreen);
            }
        }
    }

    /*
     * Stop a MODE_LEARN warrant, i.e. non-registered member _warrant
     */
    private void stopRunTrain(boolean aborted) {
        if (_learnThrottle != null) {
            _learnThrottle.dispose();
            _learnThrottle = null;
        }
        if (_warrant == null) {
            return;
        }

        if (_warrant.getRunMode() == Warrant.MODE_LEARN) {
            List<BlockOrder> orders = getOrders();
            if (orders != null && orders.size() > 1) {
                BlockOrder bo = _warrant.getCurrentBlockOrder();
                if (bo != null) {
                    OBlock lastBlock = orders.get(orders.size() - 1).getBlock();
                    OBlock currentBlock = bo.getBlock();
                    if (!lastBlock.equals(currentBlock)) {
                        if ((lastBlock.getState() & OBlock.UNDETECTED) != 0 &&
                                currentBlock.equals(orders.get(orders.size() - 2).getBlock())) {
                            setThrottleCommand("NoOp", Bundle.getMessage("Mark"), lastBlock.getDisplayName());
                            setStatus(Bundle.getMessage("LearningStop"), myGreen);
                        } else if (!aborted) {
                            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("IncompleteScript", lastBlock),
                                    Bundle.getMessage("WarningTitle"),
                                    JmriJOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        setStatus(Bundle.getMessage("LearningStop"), myGreen);
                    }
                }
            }
        }
        clearWarrant();
    }

    private void clearWarrant() {
        if (_warrant != null) {
            _warrant.stopWarrant(false, true);
            _warrant.removePropertyChangeListener(this);
        }
    }

    protected Warrant getWarrant() {
        return _warrant;
    }

    private void setStatus(String msg, Color c) {
        ThreadingUtil.runOnGUIEventually(() -> {
            _statusBox.setForeground(c);
            _statusBox.setText(msg);
        });
    }

    @Override
    protected void maxThrottleEventAction() {
    }

    /**
     * Property names from Warrant: "runMode" - from setRunMode "controlChange"
     * - from controlRunTrain "blockChange" - from goingActive "allocate" - from
     * allocateRoute, deAllocate "setRoute" - from setRoute, goingActive
     * Property names from Engineer: "Command" - from run "SpeedRestriction" -
     * ThrottleRamp run Property names from RouteFinder: "RouteSearch" - from
     * run
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (property.equals("DnDrop")) {
            doAction(e.getSource());
        } else if (e.getSource() instanceof Warrant && _warrant.equals(e.getSource())) {
            if (log.isDebugEnabled()) {
                log.debug("propertyChange \"{}\" old= {} new= {} source= {}",
                    property, e.getOldValue(), e.getNewValue(), e.getSource().getClass().getName());
            }
            String msg = null;
            Color color = myGreen;
            switch (_warrant.getRunMode()) {
                case Warrant.MODE_NONE:
                    _warrant.removePropertyChangeListener(this);
                    if (property.equals("StopWarrant")) {
                        String blkName = (String) e.getOldValue();
                        String bundleKey = (String) e.getNewValue();
                        if (blkName == null) {
                            msg = Bundle.getMessage(bundleKey,
                                    _warrant.getTrainName(), _warrant.getDisplayName());
                            color =  Color.red;
                        } else {
                            msg = Bundle.getMessage(bundleKey,
                                    _warrant.getTrainName(), _warrant.getDisplayName(),
                                    blkName);
                            color = myGreen;
                        }
                    }
                    break;
                case Warrant.MODE_LEARN:
                    switch (property) {
                        case "blockChange":
                            OBlock oldBlock = (OBlock) e.getOldValue();
                            OBlock newBlock = (OBlock) e.getNewValue();
                            if (newBlock == null) {
                                stopRunTrain(true);
                                msg = Bundle.getMessage("ChangedRoute",
                                        _warrant.getTrainName(),
                                        oldBlock.getDisplayName(),
                                        _warrant.getDisplayName());
                                color = Color.red;
                            } else {
                                setThrottleCommand("NoOp", Bundle.getMessage("Mark"),
                                        ((OBlock) e.getNewValue()).getDisplayName());
                                msg = Bundle.getMessage("TrackerBlockEnter",
                                        _warrant.getTrainName(),
                                        newBlock.getDisplayName());
                            }
                            break;
                        case "abortLearn":
                            stopRunTrain(true);
                            int oldIdx = ((Integer) e.getOldValue());
                            int newIdx = ((Integer) e.getNewValue());
                            if (oldIdx > newIdx) {
                                msg = Bundle.getMessage("LearnAbortOccupied",
                                        _warrant.getBlockAt(oldIdx),
                                        _warrant.getDisplayName());
                                color = Color.red;
                            } else {
                                msg = Bundle.getMessage("warrantAbort",
                                        _warrant.getTrainName(),
                                        _warrant.getDisplayName());
                                color = Color.red;
                            }
                            break;
                        default:
                            msg = Bundle.getMessage("Learning", _warrant.getCurrentBlockName());
                            color = Color.black;
                            break;
                    }
                    break;
                case Warrant.MODE_RUN:
                case Warrant.MODE_MANUAL:
                    if (e.getPropertyName().equals("blockChange")) {
                        OBlock oldBlock = (OBlock) e.getOldValue();
                        OBlock newBlock = (OBlock) e.getNewValue();
                        if (newBlock == null) {
                            msg = Bundle.getMessage("ChangedRoute",
                                    _warrant.getTrainName(),
                                    oldBlock.getDisplayName(),
                                    _warrant.getDisplayName());
                            color = Color.red;
                        } else {
                            msg = Bundle.getMessage("TrackerBlockEnter",
                                    _warrant.getTrainName(),
                                    newBlock.getDisplayName());
                        }
                    } else if (e.getPropertyName().equals("ReadyToRun")) {
                        msg = _warrant.getRunningMessage();
                    } else if (e.getPropertyName().equals("SpeedChange")) {
                        msg = _warrant.getRunningMessage();
                        color = Color.black;
                    } else if (property.equals("SignalOverrun")) {
                        String name = (String) e.getOldValue();
                        String speed = (String) e.getNewValue();
                        msg = Bundle.getMessage("SignalOverrun",
                                _warrant.getTrainName(), speed, name);
                        color = Color.red;
                    } else if (property.equals("OccupyOverrun")) {
                        String blockName = (String) e.getOldValue();
                        OBlock occuppier = (OBlock) e.getNewValue();
                        msg = Bundle.getMessage("OccupyOverrun",
                                _warrant.getTrainName(), blockName, occuppier);
                        color = Color.red;
                    } else if (property.equals("WarrantOverrun")) {
                        String blkName = (String) e.getOldValue();
                        OBlock warName = (OBlock) e.getNewValue();
                        msg = Bundle.getMessage("WarrantOverrun",
                                _warrant.getTrainName(), blkName, warName);
                        color = Color.red;
                    } else if (e.getPropertyName().equals("WarrantStart")) {
                        msg = Bundle.getMessage("warrantStart",
                                _warrant.getTrainName(), _warrant.getDisplayName(),
                                _warrant.getCurrentBlockName());
                        if (_warrant.getState() == Warrant.HALT) {
                            JmriJOptionPane.showMessageDialog(this, _warrant.getRunningMessage(),
                                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
                        }
                    } else if (e.getPropertyName().equals("controlChange")) {
                        int newCntrl = ((Integer) e.getNewValue());
                        msg = Bundle.getMessage("controlChange",
                                _warrant.getTrainName(),
                                Bundle.getMessage(Warrant.CNTRL_CMDS[newCntrl]),
                                _warrant.getCurrentBlockName());
                        color = Color.black;
                    } else if (e.getPropertyName().equals("throttleFail")) {
                        msg = Bundle.getMessage("ThrottleFail",
                                _warrant.getTrainName(), e.getNewValue());
                        color = Color.red;
                    } else {
                        return;
                    }
                    break;
                default:
            }
            setStatus(msg, color);
        }
        invalidate();
    }

    protected void setThrottleCommand(String cmd, String value) {
        String bName = Bundle.getMessage("NoBlock");
        BlockOrder bo = _warrant.getCurrentBlockOrder();
        if (bo != null) {
            bName = bo.getBlock().getDisplayName();
        }
        /*
         * if (cmd.equals("Forward")) {
         * _speedUtil.setIsForward(Boolean.parseBoolean(value)); }
         */
        setThrottleCommand(cmd, value, bName);
    }

    protected void setSpeedCommand(float speed) {
        if (_warrant.getSpeedUtil().profileHasSpeedInfo()) {
            _speed = _warrant.getSpeedUtil().getTrackSpeed(speed); // mm/ms
        } else {
            _speed = 0.0f;
        }
        setThrottleCommand("speed", Float.toString(speed));
    }

    private void setThrottleCommand(String cmd, String value, String bName) {
        long endTime = System.currentTimeMillis();
        long time = endTime - _startTime;
        _startTime = endTime;
        ThrottleSetting ts = new ThrottleSetting(time, cmd, value, bName, _speed);
        log.debug("setThrottleCommand= {}", ts);
        _throttleCommands.add(ts);
        _commandModel.fireTableDataChanged();

        scrollCommandTable(_commandModel.getRowCount());
    }

    private void scrollCommandTable(int row) {
        JScrollBar bar = _throttlePane.getVerticalScrollBar();
        bar.setValue(row * _rowHeight);
        bar.invalidate();
    }

    /**
     * Called by WarrantTableAction before closing the editing of this warrant
     *
     * @return true if this warrant or its pre-editing version is running
     */
    protected boolean isRunning() {
        return _warrant._runMode != Warrant.MODE_NONE ||
            (_saveWarrant != null && _saveWarrant._runMode != Warrant.MODE_NONE);
    }

    /**
     * Verify that commands are correct
     *
     * @return true if commands are OK
     */
    private boolean save() {
        boolean fatal = false;
        String msg = null;
        if (isRunning()) {
            msg = Bundle.getMessage("CannotEdit", _warrant.getDisplayName());
        }
        if (msg == null) {
            msg = routeIsValid();
        }
        if (msg != null) {
            msg = Bundle.getMessage("SaveError", msg);
            fatal = true;
        }
        if (msg == null) {
            msg = checkLocoAddress();
        }
        if (msg == null && !_isSCWarrant.isSelected()) {
            msg = checkThrottleCommands();
            if (msg != null) {
                msg = Bundle.getMessage("BadData", msg);
                fatal = true;
            }
        }

        WarrantManager mgr = InstanceManager.getDefault(WarrantManager.class);
        if (msg == null) {
            if (_saveWarrant != null) {
                if ((_saveWarrant instanceof SCWarrant && !_isSCWarrant.isSelected()) ||
                        (!(_saveWarrant instanceof SCWarrant) && _isSCWarrant.isSelected())) {
                    // _saveWarrant already registered, but is not the correct
                    // class.
                    mgr.deregister(_saveWarrant);
                    _warrant = mgr.createNewWarrant(
                            _sysNameBox.getText(), _userNameBox.getText(), _isSCWarrant.isSelected(),
                            (long) _TTPtextField.getValue());
                } else {
                    String uName = _userNameBox.getText();
                    if (uName.length() > 0 &&
                            !uName.equals(_saveWarrant.getUserName()) &&
                            mgr.getWarrant(uName) != null) {
                        fatal = true;
                        msg = Bundle.getMessage("WarrantExists", _userNameBox.getText());
                    } else {
                        _warrant = _saveWarrant; // update registered warrant
                    }
                }
            } else {
                if (_warrant == null) {
                    _warrant = mgr.createNewWarrant(
                            _sysNameBox.getText(), _userNameBox.getText(),
                            _isSCWarrant.isSelected(), (long) _TTPtextField.getValue());
                }
            }
        }
        if (_warrant == null) { // find out why
            if (_userNameBox.getText().length() > 0 && mgr.getByUserName(_userNameBox.getText()) != null) {
                msg = Bundle.getMessage("WarrantExists", _userNameBox.getText());
            } else if (mgr.getBySystemName(_sysNameBox.getText()) != null) {
                msg = Bundle.getMessage("WarrantExists", _sysNameBox.getText());
            } else {
                msg = Bundle.getMessage("IWSystemName", _sysNameBox.getText());
            }
            fatal = true;
        }
        if (msg == null && _userNameBox.getText().length() == 0) {
            msg = Bundle.getMessage("NoUserName", _sysNameBox.getText());
        }
        if (msg != null) {
            if (fatal) {
                JmriJOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
                return false;
            }
            int result = JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("SaveQuestion", msg),
                    Bundle.getMessage("QuestionTitle"),
                    JmriJOptionPane.YES_NO_OPTION, JmriJOptionPane.QUESTION_MESSAGE);
            if (result != JmriJOptionPane.YES_OPTION ) {
                if (_warrant != null) {
                    mgr.deregister(_warrant);
                }
                return false;
            }
        }

        if (_saveWarrant != null) {
            _warrant = _saveWarrant;
            if ((_saveWarrant instanceof SCWarrant && !_isSCWarrant.isSelected()) ||
                    (!(_saveWarrant instanceof SCWarrant) && _isSCWarrant.isSelected())) {
                // _saveWarrant already registered, but is not the correct class.
                InstanceManager.getDefault(WarrantManager.class).deregister(_saveWarrant);
                _warrant = InstanceManager.getDefault(WarrantManager.class).createNewWarrant(
                        _sysNameBox.getText(), _userNameBox.getText(), _isSCWarrant.isSelected(), (long)_TTPtextField.getValue());
            }
        } else {
            _warrant = InstanceManager.getDefault(WarrantManager.class).createNewWarrant(
                    _sysNameBox.getText(), _userNameBox.getText(), _isSCWarrant.isSelected(), (long)_TTPtextField.getValue());
        }

        if (_isSCWarrant.isSelected()) {
            ((SCWarrant) _warrant).setForward(_runForward.isSelected());
            ((SCWarrant) _warrant).setTimeToPlatform((long) _TTPtextField.getValue());
            long sf = (long) _speedFactorTextField.getValue();
            float sf_float = sf;
            ((SCWarrant) _warrant).setSpeedFactor(sf_float / 100);
        }
        _warrant.setTrainName(getTrainName());
        _warrant.setRunBlind(_runETOnlyBox.isSelected());
        _warrant.setShareRoute(_shareRouteBox.isSelected());
        _warrant.setAddTracker(_addTracker.isSelected());
        _warrant.setNoRamp(_noRampBox.isSelected());
        _warrant.setHaltStart(_haltStartBox.isSelected());
        _warrant.setUserName(_userNameBox.getText());

        _warrant.setViaOrder(getViaBlockOrder());
        _warrant.setAvoidOrder(getAvoidBlockOrder());
        _warrant.setBlockOrders(getOrders());
        _warrant.setThrottleCommands(_throttleCommands);
        _warrant.setSpeedUtil(_speedUtil); // transfer SpeedUtil to warrant
        if (_saveWarrant == null) {
            try {
                mgr.register(_warrant);
            } catch (jmri.NamedBean.DuplicateSystemNameException dsne) {
                // ignore
            }
            _saveWarrant = _warrant;
        }

        if (log.isDebugEnabled()) {
            log.debug("warrant {} saved _train {} name= {}",
                _warrant.getDisplayName(), _speedUtil.getRosterId(), getTrainName());
        }
        WarrantTableAction.getDefault().updateWarrantMenu();
        WarrantTableFrame.getDefault().getModel().fireTableDataChanged();
        _dirty = false;
        return true;
    }

    protected List<ThrottleSetting> getThrottleCommands() {
        return _throttleCommands;
    }

    protected void close() {
        _dirty = false;
        clearTempWarrant();
        if (_warrant.getRunMode() != Warrant.MODE_NONE) {
            stopRunTrain(true);
        }
        closeProfileTable();
        dispose();
    }

    // =============== Throttle Command Table ==========================\\
    // =============== VALUE_COLUMN editing/rendering ==================\\

    static final String[] TRUE_FALSE = {ValueType.VAL_TRUE.toString(), ValueType.VAL_FALSE.toString()};
    static final String[] ON_OFF = {ValueType.VAL_ON.toString(), ValueType.VAL_OFF.toString()};
    static final String[] SENSOR_STATES = {ValueType.VAL_ACTIVE.toString(), ValueType.VAL_INACTIVE.toString()};

    private class ValueCellEditor extends DefaultCellEditor {

        private ComboDialog editorDialog;
        private TextDialog textDialog;
        private String currentText;

        ValueCellEditor(JTextField textField) {
            super(textField);
            setClickCountToStart(1);
            log.debug("valueCellEditor Ctor");
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int col) {
            log.debug("getValueCellEditorComponent: row= {}, column= {} selected = {} value= {}",
                row, col, isSelected, value);
            currentText = value.toString();
            editorComponent = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, col);
            Command cmd = (Command) _commandModel.getValueAt(row, ThrottleTableModel.COMMAND_COLUMN);
            Rectangle cellRect = table.getCellRect(row, col, false);
            Dimension dim = new Dimension(cellRect.width, cellRect.height);

            if (cmd == null) {
                showTextDialog(dim);
            } else {
                switch (cmd) {
                    case FORWARD:
                        showComboDialog(TRUE_FALSE, dim);
                        break;
                    case FKEY:
                    case LATCHF:
                        showComboDialog(ON_OFF, dim);
                        break;
                    case SET_SENSOR:
                    case WAIT_SENSOR:
                        showComboDialog(SENSOR_STATES, dim);
                        break;
                    default:
                        // includes cases SPEED: and RUN_WARRANT:
                        // SPEEDSTEP and NOOP not included in ComboBox
                        showTextDialog(dim);
                        break;
                }
            }
            return editorComponent;
        }

        void showTextDialog(Dimension dim) {
            log.debug("valueCellEditor.showTextDialog");
            textDialog = new TextDialog();
            textDialog._textField.setText(currentText);

            class CellMaker implements Runnable {
                Dimension dim;

                CellMaker(Dimension d) {
                    dim = d;
                }

                @Override
                public void run() {
                    log.debug("Run valueCellEditor.TextDialog");
                    Point p = editorComponent.getLocationOnScreen();
                    textDialog.setLocation(p.x, p.y);
                    textDialog.setPreferredSize(dim);
                    textDialog.pack();
                    textDialog.setVisible(true);
                }
            }
            CellMaker t = new CellMaker(dim);
            javax.swing.SwingUtilities.invokeLater(t);
        }

        class TextDialog extends JDialog implements FocusListener {
            JTextField _textField;
            TextDialog _this;

            TextDialog() {
                super((JFrame) null, false);
                _this = this;
                _textField = new JTextField();
                _textField.addFocusListener(TextDialog.this);
                _textField.setForeground(Color.RED);
                getContentPane().add(_textField);
                setUndecorated(true);
            }

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                currentText = _textField.getText();
                ((JTextField)editorComponent).setText(currentText);
                fireEditingStopped();
                _this.dispose();
            }
        }

        void showComboDialog(String[] items, Dimension dim) {
            editorDialog = new ComboDialog(items);
            log.debug("valueCellEditor.showComboDialog");

            class CellMaker implements Runnable {
                Dimension dim;

                CellMaker(Dimension d) {
                    dim = d;
                }

                @Override
                public void run() {
                    log.debug("Run valueCellEditor.showDialog");
                    Point p = editorComponent.getLocationOnScreen();
                    editorDialog.setLocation(p.x, p.y);
                    editorDialog.setPreferredSize(dim);
                    editorDialog.pack();
                    editorDialog.setVisible(true);
                }
            }
            CellMaker t = new CellMaker(dim);
            javax.swing.SwingUtilities.invokeLater(t);
        }

        class ComboDialog extends JDialog implements ItemListener, FocusListener {
            JComboBox<String> _comboBox;
            ComboDialog _this;

            ComboDialog(String[] items) {
                super((JFrame) null, false);
                _this = this;
                _comboBox = new JComboBox<>();
                _comboBox.addItemListener(ComboDialog.this);
                _comboBox.addFocusListener(ComboDialog.this);
                _comboBox.setForeground(Color.RED);
                for (String item : items) {
                    _comboBox.addItem(item);
                }
                _comboBox.removeItem(Command.NOOP.toString());
                getContentPane().add(_comboBox);
                setUndecorated(true);
            }

            @Override
            public void itemStateChanged(ItemEvent e) {
                currentText = (String) _comboBox.getSelectedItem();
                ((JTextField)editorComponent).setText(currentText);
                fireEditingStopped();
                _this.dispose();
            }

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                currentText = (String) _comboBox.getSelectedItem();
                ((JTextField)editorComponent).setText(currentText);
                fireEditingStopped();
                _this.dispose();
            }
        }
    }

    // =============== COMMAND_COLUMN editing/rendering ===============\\

    private class CommandCellEditor extends DefaultCellEditor {
        CommandCellEditor(JComboBox<Command> comboBox) {
            super(comboBox);
            log.debug("New JComboBox<String> CommandCellEditor");
        }

        @SuppressWarnings("unchecked") // getComponent call requires an
                                       // unchecked cast
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            log.debug("getTableCellEditorComponent: row= {}, column= {} selected = {}",
                row, column, isSelected);

            JComboBox<Command> comboBox = (JComboBox<Command>) getComponent();
            cellPt = MouseInfo.getPointerInfo().getLocation();
            comboBox.removeAllItems();
            for (Command cmd : Command.values()) {
                if (!cmd.name().equals("NOOP") && !cmd.name().equals("SPEEDSTEP")) {
                    comboBox.addItem(cmd);
                }
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }

    private Point cellPt; // point to display key

    private class CommandCellRenderer extends DefaultTableCellRenderer {
        public CommandCellRenderer() {
            super();
            log.debug("New JComboBox<String> CommandCellRenderer");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Command cmd = (Command) value;
            int key = _throttleCommands.get(row).getKeyNum();
            if (null == cmd) {
                setText(null);
            } else switch (cmd) {
                case FKEY:
                    setText(Bundle.getMessage("FKey", key));
                    break;
                case LATCHF:
                    setText(Bundle.getMessage("FKeyMomemtary", key));
                    break;
                default:
                    setText(cmd.toString());
                    break;
            }
            return this;
        }
    }

    private static class EditDialog extends JDialog {
        SpinnerNumberModel _keyNumModel;
        ThrottleSetting _ts;
        Command _cmd;

        EditDialog(JFrame frame, ThrottleSetting ts, Command cmd) {
            super(frame, true);
            _ts = ts;
            _cmd = cmd;
            int key = ts.getKeyNum();
            if (key < 0) {
                key = 0;
            }
            _keyNumModel = new SpinnerNumberModel(key, 0, 28, 1);
            JSpinner keyNums = new JSpinner(_keyNumModel);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(new JLabel(Bundle.getMessage("editFunctionKey")), BorderLayout.NORTH);
            panel.add(keyNums, BorderLayout.CENTER);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JButton doneButton;
            doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener((ActionEvent a) -> done());
            p.add(doneButton);

            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener((ActionEvent a) -> this.dispose());
            p.add(cancelButton);
            panel.add(p, BorderLayout.SOUTH);
            getContentPane().add(panel);
            setUndecorated(true);
        }

        public void done() {
            int i = (Integer) _keyNumModel.getValue();
            _ts.setKeyNum(i);
            _ts.setCommand(_cmd);
            this.dispose();
        }

    }

    void makeEditWindow(ThrottleSetting ts, Command cmd) {
        JDialog dialog = new EditDialog(this, ts, cmd);
        dialog.setLocation(cellPt);
        dialog.pack();
        dialog.setVisible(true);
        log.debug("makeEditWindow: pt at ({}, {})", cellPt.x, cellPt.y);
    }

    private static java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");

    /************************* Throttle Table ******************************/
    private class ThrottleTableModel extends AbstractTableModel {

        public static final int ROW_NUM = 0;
        public static final int TIME_COLUMN = 1;
        public static final int COMMAND_COLUMN = 2;
        public static final int VALUE_COLUMN = 3;
        public static final int BLOCK_COLUMN = 4;
        public static final int SPEED_COLUMN = 5;
        public static final int NUMCOLS = 6;

        JComboBox<Integer> keyNums = new JComboBox<>();

        ThrottleTableModel() {
            super();
            for (int i = 0; i < 29; i++) {
                keyNums.addItem(i);
            }
        }

        @Override
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
        public int getRowCount() {
            return _throttleCommands.size();
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case ROW_NUM:
                    return "#";
                case TIME_COLUMN:
                    return Bundle.getMessage("TimeCol");
                case COMMAND_COLUMN:
                    return Bundle.getMessage("CommandCol");
                case VALUE_COLUMN:
                    return Bundle.getMessage("ValueCol");
                case BLOCK_COLUMN:
                    return Bundle.getMessage("BlockCol");
                case SPEED_COLUMN:
                    return Bundle.getMessage("trackSpeed");
                default:
                    // fall through
                    break;
            }
            return "";
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return !(col == ROW_NUM || col == SPEED_COLUMN);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == COMMAND_COLUMN) {
                return JComboBox.class;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case ROW_NUM:
                    return new JTextField(3).getPreferredSize().width;
                case TIME_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case COMMAND_COLUMN:
                case VALUE_COLUMN:
                    return new JTextField(18).getPreferredSize().width;
                case BLOCK_COLUMN:
                    return new JTextField(45).getPreferredSize().width;
                case SPEED_COLUMN:
                    return new JTextField(10).getPreferredSize().width;
                default:
                    return new JTextField(12).getPreferredSize().width;
            }
        }

        @Override
        public Object getValueAt(int row, int col) {
            // some error checking
            if (row >= _throttleCommands.size()) {
                log.debug("row {} is greater than throttle command size {}",
                    row, _throttleCommands.size());
                return "";
            }
            ThrottleSetting ts = _throttleCommands.get(row);
            if (ts == null) {
                log.debug("Throttle setting is null!");
                return "";
            }
            switch (col) {
                case ROW_NUM:
                    return row + 1;
                case TIME_COLUMN:
                    return ts.getTime();
                case COMMAND_COLUMN:
                    return ts.getCommand();
                case VALUE_COLUMN:
                    CommandValue cmdVal = ts.getValue();
                    if (cmdVal == null) {
                        return "";
                    }
                    return cmdVal.showValue();
                case BLOCK_COLUMN:
                    return ts.getBeanDisplayName();
                case SPEED_COLUMN:
                    return twoDigit.format(ts.getTrackSpeed() * _speedConversion);
                default:
                    return "";
            }
        }

        @Override
        @SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                justification = "put least likely cases last for efficiency")
        public void setValueAt(Object value, int row, int col) {
            if (row >= _throttleCommands.size()) {
                return;
            }
            ThrottleSetting ts = _throttleCommands.get(row);
            String msg = null;
            switch (col) {
                case TIME_COLUMN:
                    try {
                        long time = Long.parseLong((String) value);
                        if (time < 0) {
                            msg = Bundle.getMessage("InvalidTime", (String) value);
                        } else {
                            ts.setTime(time);
                            _dirty = true;
                        }
                    } catch (NumberFormatException nfe) {
                        msg = Bundle.getMessage("InvalidTime", (String) value);
                    }
                    break;
                case COMMAND_COLUMN:
                    Command cmd = ((Command) value);
                    if (cmd == null) {
                        break;
                    }
                    Command prCmd = ts.getCommand();
                    if (prCmd != null) {
                        if (prCmd.equals(Command.NOOP)) {
                            break;
                        }
                        if (!cmd.hasBlockName() && prCmd.hasBlockName()) {
                            ts.setNamedBeanHandle(null);
                        }
                    }
                    switch (cmd) {
                        case FKEY:
                        case LATCHF:
                            class CellMaker implements Runnable {
                                ThrottleSetting ts;
                                Command cmd;

                                CellMaker(ThrottleSetting t, Command c) {
                                    ts = t;
                                    cmd = c;
                                }

                                @Override
                                public void run() {
                                    makeEditWindow(ts, cmd);
                                }
                            }
                            CellMaker t = new CellMaker(ts, cmd);
                            javax.swing.SwingUtilities.invokeLater(t);
                            break;
                        case NOOP:
                            msg = Bundle.getMessage("cannotEnterNoop", cmd.toString());
                            break;
                        case SPEED:
                        case FORWARD:
                        case SET_SENSOR:
                        case WAIT_SENSOR:
                        case RUN_WARRANT:
                        case SPEEDSTEP:
                        case SET_MEMORY:
                            ts.setCommand(cmd);
                            _dirty = true;
                            break;
                        default:
                            msg = Bundle.getMessage("badCommand", cmd.toString());
                    }
                    break;
                case VALUE_COLUMN:
                    if (value == null || ((String) value).length() == 0) {
                        break;
                    }
                    if (ts == null || ts.getCommand() == null) {
                        msg = Bundle.getMessage("nullValue", Bundle.getMessage("CommandCol"));
                        break;
                    }
                    Command command = ts.getCommand();
                    if (command.equals(Command.NOOP)) {
                        break;
                    }
                    try {
                        CommandValue val = ThrottleSetting.getValueFromString(command, (String) value);
                        if (!val.equals(ts.getValue())) {
                            _dirty = true;
                            ts.setValue(val);
                        }
                    } catch (jmri.JmriException je) {
                        msg = je.getMessage();
                        break;
                    }
                    if (command.hasBlockName()) {
                        NamedBeanHandle<?> bh = getPreviousBlockHandle(row);
                        ts.setNamedBeanHandle(bh);
                    }
                    break;
                case BLOCK_COLUMN:
                    if (ts == null || ts.getCommand() == null) {
                        msg = Bundle.getMessage("nullValue", Bundle.getMessage("CommandCol"));
                        break;
                    }
                    command = ts.getCommand();
                    if (command == null) {
                        break;
                    }
                    if (!command.hasBlockName()) {
                        msg = ts.setNamedBean(command, (String) value);
                    } else if (command.equals(Command.NOOP)) {
                        if (!((String) value).equals(ts.getBeanDisplayName())) {
                            msg = Bundle.getMessage("cannotChangeBlock", (String) value);
                        }
                    } else {
                        NamedBeanHandle<?> bh = getPreviousBlockHandle(row);
                        if (bh != null) {
                            String name = bh.getBean().getDisplayName();
                            if (!name.equals(value)) {
                                msg = Bundle.getMessage("commandInBlock", name);
                                ts.setNamedBeanHandle(bh);
                                _dirty = true;
                            }
                        }
                    }
                    break;
                case SPEED_COLUMN:
                    break;
                default:
            }
            if (msg != null) {
                showWarning(msg);
            } else {
                fireTableRowsUpdated(row, row);
            }
        }

        private NamedBeanHandle<? extends NamedBean> getPreviousBlockHandle(int row) {
            for (int i = row; i > 0; i--) {
                NamedBeanHandle<? extends NamedBean> bh = _throttleCommands.get(i - 1).getNamedBeanHandle();
                if (bh != null && (bh.getBean() instanceof OBlock)) {
                    return bh;
                }
            }
            return null;
        }

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WarrantFrame.class);

}
