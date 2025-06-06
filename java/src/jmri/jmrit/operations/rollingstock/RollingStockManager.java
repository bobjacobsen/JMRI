package jmri.jmrit.operations.rollingstock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;

/**
 * Base class for rolling stock managers car and engine.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 * @param <T> the type of RollingStock managed by this manager
 */
public abstract class RollingStockManager<T extends RollingStock> extends PropertyChangeSupport implements PropertyChangeListener {

    public static final String NONE = "";

    // RollingStock
    protected Hashtable<String, T> _hashTable = new Hashtable<>();

    public static final String LISTLENGTH_CHANGED_PROPERTY = "RollingStockListLength"; // NOI18N
    
    abstract public RollingStock newRS(String road, String number);

    public RollingStockManager() {
    }

    /**
     * Get the number of items in the roster
     *
     * @return Number of rolling stock in the Roster
     */
    public int getNumEntries() {
        return _hashTable.size();
    }

    public void dispose() {
        deleteAll();
    }

    /**
     * Get rolling stock by id
     *
     * @param id The string id.
     *
     * @return requested RollingStock object or null if none exists
     */
    public T getById(String id) {
        return _hashTable.get(id);
    }

    /**
     * Get rolling stock by road and number
     *
     * @param road   RollingStock road
     * @param number RollingStock number
     * @return requested RollingStock object or null if none exists
     */
    public T getByRoadAndNumber(String road, String number) {
        String id = RollingStock.createId(road, number);
        return getById(id);
    }

    /**
     * Get a rolling stock by type and road. Used to test that rolling stock
     * with a specific type and road exists.
     *
     * @param type RollingStock type.
     * @param road RollingStock road.
     * @return the first RollingStock found with the specified type and road.
     */
    public T getByTypeAndRoad(String type, String road) {
        Enumeration<String> en = _hashTable.keys();
        while (en.hasMoreElements()) {
            T rs = getById(en.nextElement());
            if (rs.getTypeName().equals(type) && rs.getRoadName().equals(road)) {
                return rs;
            }
        }
        return null;
    }

    /**
     * Get a rolling stock by Radio Frequency Identification (RFID)
     *
     * @param rfid RollingStock's RFID.
     * @return the RollingStock with the specific RFID, or null if not found
     */
    public T getByRfid(String rfid) {
        Enumeration<String> en = _hashTable.keys();
        while (en.hasMoreElements()) {
            T rs = getById(en.nextElement());
            if (rs.getRfid().equals(rfid)) {
                return rs;
            }
        }
        return null;
    }

    /**
     * Load RollingStock.
     *
     * @param rs The RollingStock to load.
     */
    public void register(T rs) {
        if (!_hashTable.containsKey(rs.getId())) {
            int oldSize = _hashTable.size();
            rs.addPropertyChangeListener(this);
            _hashTable.put(rs.getId(), rs);
            firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, _hashTable.size());
        } else {
            log.error("Duplicate rolling stock id: ({})", rs.getId());
            rs.dispose();
        }
    }

    /**
     * Unload RollingStock.
     *
     * @param rs The RollingStock to delete.
     */
    public void deregister(T rs) {
        rs.removePropertyChangeListener(this);
        rs.dispose();
        int oldSize = _hashTable.size();
        _hashTable.remove(rs.getId());
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, _hashTable.size());
    }

    /**
     * Remove all RollingStock from roster
     */
    public void deleteAll() {
        int oldSize = _hashTable.size();
        Enumeration<String> en = _hashTable.keys();
        while (en.hasMoreElements()) {
            T rs = getById(en.nextElement());
            rs.dispose();
            _hashTable.remove(rs.getId());
        }
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, _hashTable.size());
    }

    public void resetMoves() {
        resetMoves(getList());
    }

    public void resetMoves(List<T> list) {
        for (RollingStock rs : list) {
            rs.setMoves(0);
        }
    }

    /**
     * Returns a list (no order) of RollingStock.
     *
     * @return list of RollingStock
     */
    public List<T> getList() {
        return new ArrayList<>(_hashTable.values());
    }

    /**
     * Sort by rolling stock id
     *
     * @return list of RollingStock ordered by id
     */
    public List<T> getByIdList() {
        Enumeration<String> en = _hashTable.keys();
        String[] arr = new String[_hashTable.size()];
        List<T> out = new ArrayList<>();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i++] = en.nextElement();
        }
        Arrays.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(getById(arr[i]));
        }
        return out;
    }

    /**
     * Sort by rolling stock road name
     *
     * @return list of RollingStock ordered by road name
     */
    public List<T> getByRoadNameList() {
        return getByList(getByIdList(), BY_ROAD);
    }

    private static final int PAGE_SIZE = 64;
    private static final int NOT_INTEGER = -999999999; // flag when RS number isn't an Integer

    /**
     * Sort by rolling stock number, number can be alphanumeric. RollingStock
     * number can also be in the format of nnnn-N, where the "-N" allows the
     * user to enter RollingStock with similar numbers.
     *
     * @return list of RollingStock ordered by number
     */
    public List<T> getByNumberList() {
        // first get by road list
        List<T> sortIn = getByRoadNameList();
        // now re-sort
        List<T> out = new ArrayList<>();
        int rsNumber = 0;
        int outRsNumber = 0;

        for (T rs : sortIn) {
            boolean rsAdded = false;
            try {
                rsNumber = Integer.parseInt(rs.getNumber());
                rs.number = rsNumber;
            } catch (NumberFormatException e) {
                // maybe rolling stock number in the format nnnn-N
                try {
                    String[] number = rs.getNumber().split(TrainCommon.HYPHEN);
                    rsNumber = Integer.parseInt(number[0]);
                    rs.number = rsNumber;
                } catch (NumberFormatException e2) {
                    rs.number = NOT_INTEGER;
                    // sort alphanumeric numbers at the end of the out list
                    String numberIn = rs.getNumber();
                    // log.debug("rolling stock in road number ("+numberIn+") isn't a number");
                    for (int k = (out.size() - 1); k >= 0; k--) {
                        String numberOut = out.get(k).getNumber();
                        try {
                            Integer.parseInt(numberOut);
                            // done, place rolling stock with alphanumeric
                            // number after rolling stocks with real numbers.
                            out.add(k + 1, rs);
                            rsAdded = true;
                            break;
                        } catch (NumberFormatException e3) {
                            if (numberIn.compareToIgnoreCase(numberOut) >= 0) {
                                out.add(k + 1, rs);
                                rsAdded = true;
                                break;
                            }
                        }
                    }
                    if (!rsAdded) {
                        out.add(0, rs);
                    }
                    continue;
                }
            }

            int start = 0;
            // page to improve sort performance.
            int divisor = out.size() / PAGE_SIZE;
            for (int k = divisor; k > 0; k--) {
                outRsNumber = out.get((out.size() - 1) * k / divisor).number;
                if (outRsNumber == NOT_INTEGER) {
                    continue;
                }
                if (rsNumber >= outRsNumber) {
                    start = (out.size() - 1) * k / divisor;
                    break;
                }
            }
            for (int j = start; j < out.size(); j++) {
                outRsNumber = out.get(j).number;
                if (outRsNumber == NOT_INTEGER) {
                    try {
                        outRsNumber = Integer.parseInt(out.get(j).getNumber());
                    } catch (NumberFormatException e) {
                        try {
                            String[] number = out.get(j).getNumber().split(TrainCommon.HYPHEN);
                            outRsNumber = Integer.parseInt(number[0]);
                        } catch (NumberFormatException e2) {
                            // force add
                            outRsNumber = rsNumber + 1;
                        }
                    }
                }
                if (rsNumber < outRsNumber) {
                    out.add(j, rs);
                    rsAdded = true;
                    break;
                }
            }
            if (!rsAdded) {
                out.add(rs);
            }
        }
        // log.debug("end rolling stock sort by number list");
        return out;
    }

    /**
     * Sort by rolling stock type names
     *
     * @return list of RollingStock ordered by RollingStock type
     */
    public List<T> getByTypeList() {
        return getByList(getByRoadNameList(), BY_TYPE);
    }

    /**
     * Return rolling stock of a specific type
     *
     * @param type type of rolling stock
     * @return list of RollingStock that are specific type
     */
    public List<T> getByTypeList(String type) {
        List<T> typeList = getByTypeList();
        List<T> out = new ArrayList<>();
        for (T rs : typeList) {
            if (rs.getTypeName().equals(type)) {
                out.add(rs);
            }
        }
        return out;
    }

    /**
     * Sort by rolling stock color names
     *
     * @return list of RollingStock ordered by RollingStock color
     */
    public List<T> getByColorList() {
        return getByList(getByTypeList(), BY_COLOR);
    }

    /**
     * Sort by rolling stock location
     *
     * @return list of RollingStock ordered by RollingStock location
     */
    public List<T> getByLocationList() {
        return getByList(getByNumberList(), BY_LOCATION);
    }

    /**
     * Sort by rolling stock destination
     *
     * @return list of RollingStock ordered by RollingStock destination
     */
    public List<T> getByDestinationList() {
        return getByList(getByLocationList(), BY_DESTINATION);
    }

    /**
     * Sort by rolling stocks in trains
     *
     * @return list of RollingStock ordered by trains
     */
    public List<T> getByTrainList() {
        List<T> byDest = getByList(getByIdList(), BY_DESTINATION);
        List<T> byLoc = getByList(byDest, BY_LOCATION);
        return getByList(byLoc, BY_TRAIN);
    }

    /**
     * Sort by rolling stock moves
     *
     * @return list of RollingStock ordered by RollingStock moves
     */
    public List<T> getByMovesList() {
        return getByList(getList(), BY_MOVES);
    }

    /**
     * Sort by when rolling stock was built
     *
     * @return list of RollingStock ordered by RollingStock built date
     */
    public List<T> getByBuiltList() {
        return getByList(getByIdList(), BY_BUILT);
    }

    /**
     * Sort by rolling stock owner
     *
     * @return list of RollingStock ordered by RollingStock owner
     */
    public List<T> getByOwnerList() {
        return getByList(getByIdList(), BY_OWNER);
    }

    /**
     * Sort by rolling stock value
     *
     * @return list of RollingStock ordered by value
     */
    public List<T> getByValueList() {
        return getByList(getByIdList(), BY_VALUE);
    }

    /**
     * Sort by rolling stock RFID
     *
     * @return list of RollingStock ordered by RFIDs
     */
    public List<T> getByRfidList() {
        return getByList(getByIdList(), BY_RFID);
    }

    /**
     * Get a list of all rolling stock sorted last date used
     *
     * @return list of RollingStock ordered by last date
     */
    public List<T> getByLastDateList() {
        return getByList(getByIdList(), BY_LAST);
    }
    
    public List<T> getByCommentList() {
        return getByList(getByIdList(), BY_COMMENT);
    }

    /**
     * Sort a specific list of rolling stock last date used
     *
     * @param inList list of rolling stock to sort.
     * @return list of RollingStock ordered by last date
     */
    public List<T> getByLastDateList(List<T> inList) {
        return getByList(inList, BY_LAST);
    }

    protected List<T> getByList(List<T> sortIn, int attribute) {
        List<T> out = new ArrayList<>(sortIn);
        out.sort(getComparator(attribute));
        return out;
    }

    // The various sort options for RollingStock
    // see CarManager and EngineManger for other values
    protected static final int BY_NUMBER = 0;
    protected static final int BY_ROAD = 1;
    protected static final int BY_TYPE = 2;
    protected static final int BY_COLOR = 3;
    protected static final int BY_LOCATION = 4;
    protected static final int BY_DESTINATION = 5;
    protected static final int BY_TRAIN = 6;
    protected static final int BY_MOVES = 7;
    protected static final int BY_BUILT = 8;
    protected static final int BY_OWNER = 9;
    protected static final int BY_RFID = 10;
    protected static final int BY_VALUE = 11;
    protected static final int BY_LAST = 12;
    protected static final int BY_BLOCKING = 13;
    protected static final int BY_COMMENT = 14;

    protected java.util.Comparator<T> getComparator(int attribute) {
        switch (attribute) {
            case BY_NUMBER:
                return (r1, r2) -> (r1.getNumber().compareToIgnoreCase(r2.getNumber()));
            case BY_ROAD:
                return (r1, r2) -> (r1.getRoadName().compareToIgnoreCase(r2.getRoadName()));
            case BY_TYPE:
                return (r1, r2) -> (r1.getTypeName().compareToIgnoreCase(r2.getTypeName()));
            case BY_COLOR:
                return (r1, r2) -> (r1.getColor().compareToIgnoreCase(r2.getColor()));
            case BY_LOCATION:
                return (r1, r2) -> (r1.getStatus() + r1.getLocationName() + r1.getTrackName())
                        .compareToIgnoreCase(r2.getStatus() + r2.getLocationName() + r2.getTrackName());
            case BY_DESTINATION:
                return (r1, r2) -> (r1.getDestinationName() + r1.getDestinationTrackName())
                        .compareToIgnoreCase(r2.getDestinationName() + r2.getDestinationTrackName());
            case BY_TRAIN:
                return (r1, r2) -> (r1.getTrainName().compareToIgnoreCase(r2.getTrainName()));
            case BY_MOVES:
                return (r1, r2) -> (r1.getMoves() - r2.getMoves());
            case BY_BUILT:
                return (r1,
                        r2) -> (convertBuildDate(r1.getBuilt()).compareToIgnoreCase(convertBuildDate(r2.getBuilt())));
            case BY_OWNER:
                return (r1, r2) -> (r1.getOwnerName().compareToIgnoreCase(r2.getOwnerName()));
            case BY_RFID:
                return (r1, r2) -> (r1.getRfid().compareToIgnoreCase(r2.getRfid()));
            case BY_VALUE:
                return (r1, r2) -> (r1.getValue().compareToIgnoreCase(r2.getValue()));
            case BY_LAST:
                return (r1, r2) -> (r1.getLastMoveDate().compareTo(r2.getLastMoveDate()));
            case BY_BLOCKING:
                return (r1, r2) -> (r1.getBlocking() - r2.getBlocking());
            case BY_COMMENT:
                return (r1, r2) -> (r1.getComment().compareToIgnoreCase(r2.getComment()));
            default:
                return (r1, r2) -> ((r1.getRoadName() + r1.getNumber())
                        .compareToIgnoreCase(r2.getRoadName() + r2.getNumber()));
        }
    }

    /*
     * Converts build date into consistent String. Three build date formats; Two
     * digits YY becomes 19YY. MM-YY becomes 19YY. MM-YYYY becomes YYYY.
     */
    public static String convertBuildDate(String date) {
        String[] built = date.split("-");
        if (built.length == 2) {
            try {
                int d = Integer.parseInt(built[1]);
                if (d < 100) {
                    d = d + 1900;
                }
                return Integer.toString(d);
            } catch (NumberFormatException e) {
                log.debug("Unable to parse built date ({})", date);
            }
        } else {
            try {
                int d = Integer.parseInt(date);
                if (d < 100) {
                    d = d + 1900;
                }
                return Integer.toString(d);
            } catch (NumberFormatException e) {
                log.debug("Unable to parse built date ({})", date);
            }
        }
        return date;
    }

    /**
     * Get a list of rolling stocks assigned to a train ordered by location
     *
     * @param train The Train.
     *
     * @return List of RollingStock assigned to the train ordered by location
     */
    public List<T> getByTrainList(Train train) {
        return getByList(getList(train), BY_LOCATION);
    }

    /**
     * Returns a list (no order) of RollingStock in a train.
     *
     * @param train The Train.
     *
     * @return list of RollingStock
     */
    public List<T> getList(Train train) {
        List<T> out = new ArrayList<>();
        getList().stream().filter((rs) -> {
            return rs.getTrain() == train;
        }).forEachOrdered((rs) -> {
            out.add(rs);
        });
        return out;
    }

    /**
     * Returns a list (no order) of RollingStock at a location.
     *
     * @param location location to search for.
     * @return list of RollingStock
     */
    public List<T> getList(Location location) {
        List<T> out = new ArrayList<>();
        getList().stream().filter((rs) -> {
            return rs.getLocation() == location;
        }).forEachOrdered((rs) -> {
            out.add(rs);
        });
        return out;
    }

    /**
     * Returns a list (no order) of RollingStock on a track.
     *
     * @param track Track to search for.
     * @return list of RollingStock
     */
    public List<T> getList(Track track) {
        List<T> out = new ArrayList<>();
        getList().stream().filter((rs) -> {
            return rs.getTrack() == track;
        }).forEachOrdered((rs) -> {
            out.add(rs);
        });
        return out;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Xml.ID)) {
            @SuppressWarnings("unchecked")
            T rs = (T) evt.getSource(); // unchecked cast to T  
            _hashTable.remove(evt.getOldValue());
            if (_hashTable.containsKey(rs.getId())) {
                log.error("Duplicate rolling stock id: ({})", rs.getId());
                rs.dispose();
            } else {
                _hashTable.put(rs.getId(), rs);
            }
            // fire so listeners that rebuild internal lists get signal of change in id, even without change in size
            firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, _hashTable.size(), _hashTable.size());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RollingStockManager.class);

}
