package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;

import jmri.Sensor;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MultiSensorIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.swing.JmriJOptionPane;

public class MultiSensorItemPanel extends TableItemPanel<Sensor> {

    JPanel _multiSensorPanel;
    MultiSensorSelectionModel _selectionModel;
    boolean _upDown = false;

    public MultiSensorItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<Sensor> model) {
        super(parentFrame, type, family, model);
        setToolTipText(Bundle.getMessage("ToolTipDragSelection"));
    }

    @Override
    protected JPanel initTablePanel(PickListModel<Sensor> model) {
        _table = model.makePickTable();
        TableColumn column = new TableColumn(PickListModel.POSITION_COL);
        column.setHeaderValue("Position");
        _table.addColumn(column);
        _selectionModel = new MultiSensorSelectionModel(model);
        _table.setSelectionModel(_selectionModel);
        _table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel(model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        _scrollPane = new JScrollPane(_table);
        topPanel.add(_scrollPane, BorderLayout.CENTER);
        _scrollPane.getVerticalScrollBar().setMaximum(100);
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));

        JPanel panel = new JPanel();
        _addTableButton = new JButton(Bundle.getMessage("CreateNewItem"));
        _addTableButton.addActionListener(a -> makeAddToTableWindow());
        _addTableButton.setToolTipText(Bundle.getMessage("ToolTipAddToTable"));
        panel.add(_addTableButton);

        int size = 6;
        if (_family != null) {
            HashMap<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
            if (map != null) {
                size = map.size();
            }
        }
        _selectionModel.setPositionRange(size - 3);
        JButton clearSelectionButton = new JButton(Bundle.getMessage("ClearSelection"));
        clearSelectionButton.addActionListener(a -> clearSelections());
        clearSelectionButton.setToolTipText(Bundle.getMessage("ToolTipClearSelection"));
        panel.add(clearSelectionButton);
        topPanel.add(panel, BorderLayout.SOUTH);
        _table.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        _scrollPane.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        return topPanel;
    }

    public void clearSelections() {
        _selectionModel.clearSelection();
        int size = 6;
        HashMap<String, NamedIcon> map = getIconMap();
        if (map != null) {
            size = map.size();
        }
        _selectionModel.setPositionRange(size - 3);
    }

    @Override
    protected String getDisplayKey() {
        return "second";
    }

    @Override
    protected void initIconFamiliesPanel() {
        super.initIconFamiliesPanel();
        if (_multiSensorPanel == null) {
            makeMultiSensorPanel();
            _iconFamilyPanel.add(_multiSensorPanel); // Panel containing up-dn, le-ri radio buttons
        }
    }

    private void makeMultiSensorPanel() {
        _multiSensorPanel = new JPanel();
        _multiSensorPanel.setLayout(new BoxLayout(_multiSensorPanel, BoxLayout.Y_AXIS));
        JPanel panel2 = new JPanel();
        ButtonGroup group2 = new ButtonGroup();
        JRadioButton button = new JRadioButton(Bundle.getMessage("LeftRight"));
        button.addActionListener(e -> _upDown = false);
        group2.add(button);
        panel2.add(button);
        button.setSelected(true);
        button = new JRadioButton(Bundle.getMessage("UpDown"));
        button.addActionListener(e -> _upDown = true);
        group2.add(button);
        panel2.add(button);
        _multiSensorPanel.add(panel2);
        _multiSensorPanel.repaint();
    }

    @Override
    protected void setFamily(String family) {
        super.setFamily(family);
        if (_multiSensorPanel == null) {
            makeMultiSensorPanel();
            _iconFamilyPanel.add(_multiSensorPanel);
        }
        _iconFamilyPanel.repaint();
        updateFamiliesPanel();
        setSelections();
    }

    protected void setSelections() {
        int[] positions = _selectionModel.getPositions();
        clearSelections();
        int len = Math.min(positions.length, _selectionModel.getPositions().length);
        for (int i = 0; i < len; i++) {
            if (positions[i] > -1) {
                _selectionModel.setSelectionInterval(positions[i], positions[i]);
            }
        }
    }

    /*
     * Used by Panel Editor to make updates the icon(s) into the user's Panel.
     */
    public ArrayList<Sensor> getTableSelections() {
        return _selectionModel.getSelections();
    }

    public int[] getPositions() {
        return _selectionModel.getPositions();
    }

    public boolean getUpDown() {
        return _upDown;
    }

    @Override
    public void setSelection(Sensor bean) {
        int row = _model.getIndexOf(bean);
        if (row >= 0) {
            _selectionModel.setSelectionInterval(row, row);
            JScrollBar bar = _scrollPane.getVerticalScrollBar();
            int numRows = _model.getRowCount();
            bar.setValue((int)((float)(row * bar.getMaximum())/numRows));
        } else {
            valueChanged(null);
        }
    }

    public void setUpDown(boolean upDown) {
        _upDown = upDown;
    }

    static final String[] POSITION = {"first", "second", "third", "fourth", "fifth",
        "sixth", "seventh", "eighth", "nineth", "tenth", "eleventh", "twelfth"};

    static public String getPositionName(int index) {
        return POSITION[index];
    }

    protected class MultiSensorSelectionModel extends DefaultListSelectionModel {
        ArrayList<Sensor> _selections;
        int[] _positions;
        int _nextPosition;
        PickListModel<Sensor> _tableModel;

        MultiSensorSelectionModel(PickListModel<Sensor> tableModel) {
            super();
            _tableModel = tableModel;
            setPositionRange(0);
        }

        protected ArrayList<Sensor> getSelections() {
            if (log.isDebugEnabled()) {
                log.debug("getSelections: size = {}, _nextPosition = {}", _selections.size(), _nextPosition);
            }
            return _selections;
        }

        protected int[] getPositions() {
            int[] positions = new int[_positions.length];
            System.arraycopy(_positions, 0, positions, 0, _positions.length);
            return positions;
        }

        protected int getNextPosition() {
            return _nextPosition;
        }

        protected void setPositionRange(int size) {
            if (log.isDebugEnabled()) {
                log.debug("setPositionRange: size= {}", size);
            }
            if (size > POSITION.length) {
                size = POSITION.length;
            }
            if (size < 0) {
                size = 0;
            }
            _positions = new int[size];
            for (int i = 0; i < size; i++) {
                _positions[i] = -1;
            }
            _selections = new ArrayList<>(size);
            _nextPosition = 0;
        }

        /**
         * ************* DefaultListSelectionModel overrides *******************
         */
        @Override
        public boolean isSelectedIndex(int index) {
            for (int position : _positions) {
                if (position == index) {
                    log.debug("isSelectedIndex({}) returned true", index);
                    return true;
                }
            }
            log.debug("isSelectedIndex({}) returned false", index);
            return false;
        }

        @Override
        public void clearSelection() {
            log.debug("clearSelection()");
            for (int i = 0; i < _positions.length; i++) {
                if (_positions[i] >= 0) {
                    _table.setValueAt(null, _positions[i], PickListModel.POSITION_COL);
                    super.setSelectionInterval(_positions[i], _positions[i]);
                    super.clearSelection();
                    _positions[i] = -1;
                }
            }
            _selections = new ArrayList<>(_positions.length);
            _nextPosition = 0;
        }

        @Override
        public void addSelectionInterval(int index0, int index1) {
            log.debug("addSelectionInterval({}), {}) - stubbed", index0, index1);
            // super.addSelectionInterval(index0, index1);
        }

        @Override
        public void setSelectionInterval(int row, int index1) {
            if (_nextPosition >= _positions.length) {
                JmriJOptionPane.showMessageDialog(_frame,
                        Bundle.getMessage("NeedIcon", _selectionModel.getPositions().length),
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("setSelectionInterval({}, {})", row, index1);
            }
            Sensor bean = _tableModel.getBySystemName((String) _table.getValueAt(row, 0));
            if (bean == null) {
                return;
            }
            String position = (String) _tableModel.getValueAt(row, PickListModel.POSITION_COL);
            if (position != null && position.length() > 0) {
                JmriJOptionPane.showMessageDialog(_frame,
                        Bundle.getMessage("DuplicatePosition", bean.getDisplayName(), position),
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            } else {
                _table.setValueAt(Bundle.getMessage(POSITION[_nextPosition]), row, PickListModel.POSITION_COL);
                _selections.add(_nextPosition, bean);
                _positions[_nextPosition] = row;
                _nextPosition++;
                super.setSelectionInterval(row, row);
            }
        }
    }

    @Override
    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
        return new IconDragJLabel(flavor, map, icon);
    }

    public boolean oktoUpdate() {
        ArrayList<Sensor> selections = _selectionModel.getSelections();
        if (selections == null || selections.isEmpty()) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("noRowSelected"),
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (selections.size() < _selectionModel.getPositions().length) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NeedPosition", _selectionModel.getPositions().length),
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            return false;
        }
        return getIconMap() != null;
    }


    protected class IconDragJLabel extends DragJLabel {

        HashMap<String, NamedIcon> iconMap;

        public IconDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
            super(flavor, icon);
            iconMap = new HashMap<>(map);
        }

        @Override
        protected boolean okToDrag() {
            return oktoUpdate();
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (iconMap == null) {
                log.error("IconDragJLabel.getTransferData: iconMap is null!");
                return null;
            }
            ArrayList<Sensor> selections = _selectionModel.getSelections();
            if (selections == null || selections.size() < _selectionModel.getPositions().length) {
                return null;
            }

            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                if (_itemType.equals("MultiSensor")) {
                    MultiSensorIcon ms = new MultiSensorIcon(_frame.getEditor());
                    ms.setInactiveIcon(new NamedIcon(iconMap.get("SensorStateInactive")));
                    ms.setInconsistentIcon(new NamedIcon(iconMap.get("BeanStateInconsistent")));
                    ms.setUnknownIcon(new NamedIcon(iconMap.get("BeanStateUnknown")));
                    for (int i = 0; i < selections.size(); i++) {
                        ms.addEntry(selections.get(i).getDisplayName(), new NamedIcon(iconMap.get(POSITION[i])));
                    }
                    _selectionModel.clearSelection();
                    ms.setFamily(_family);
                    ms.setUpDown(_upDown);
                    ms.setLevel(Editor.SENSORS);
                    return ms;
                }
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icons for ");
                for (int i = 0; i < selections.size(); i++) {
                    sb.append(selections.get(i).getDisplayName());
                    if (i < selections.size()-1) {
                        sb.append(", ");
                    }
                }
                return  sb.toString();
            }

            return null;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MultiSensorItemPanel.class);

}
