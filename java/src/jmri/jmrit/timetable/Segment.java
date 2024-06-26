package jmri.jmrit.timetable;

/**
 * Define the content of a Segment record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Segment {

    /**
     * Create a new segment with default values.
     * @param layoutId The parent layout id.
     * @throws IllegalArgumentException SEGMENT_ADD_FAIL
     */
    public Segment(int layoutId) {
        if (_dm.getLayout(layoutId) == null) {
            throw new IllegalArgumentException(TimeTableDataManager.SEGMENT_ADD_FAIL);
        }
        _segmentId = _dm.getNextId("Segment");  // NOI18N
        _layoutId = layoutId;
        _dm.addSegment(_segmentId, this);
    }

    public Segment(int segmentId, int layoutId, String segmentName) {
        _segmentId = segmentId;
        _layoutId = layoutId;
        setSegmentName(segmentName);
    }

    TimeTableDataManager _dm = TimeTableDataManager.getDataManager();

    private final int _segmentId;
    private final int _layoutId;
    private String _segmentName = Bundle.getMessage("NewSegmentName");  // NOI18N

    /**
     * Make a copy of the segment.
     * @param layoutId The new layoutId, if zero use the current layout id.
     * @return a new segment instance.
     */
    public Segment getCopy(int layoutId) {
        if (layoutId == 0) layoutId = getLayoutId();
        Segment copy = new Segment(layoutId);
        copy.setSegmentName(Bundle.getMessage("DuplicateCopyName", _segmentName));
        return copy;
    }

    public int getSegmentId() {
        return _segmentId;
    }

    public int getLayoutId() {
        return _layoutId;
    }

    public String getSegmentName() {
        return _segmentName;
    }

    public void setSegmentName(String newName) {
        _segmentName = newName;
    }

    @Override
    public String toString() {
        return _segmentName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Segment.class);

}
