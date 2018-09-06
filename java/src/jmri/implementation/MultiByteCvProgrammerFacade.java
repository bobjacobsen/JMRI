package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade allowing upstream code to treat a CV as up to 64-bits wide (signed)
 * while breaking it up into individual byte-wide CVs for layout operations.
 * <p>
 * CV addresses of the form MvCV(ad1).(ad2).(adN) will be operated on; all others
 * will be passed through. (ad1) through (adN) contain CV addresses that are 
 * understood by downstream operations, for example (12) or (53.9.1) or (T2CV.11.12).
 * There can be two through eight addresses (zero or one would be pointless; nine or more don't fit).
 * The left-most address holds the most significant byte and is done first; the right-most 
 * address is the least significant address and is done last.  
 * 
 * <p>Cautions and limitations:
 * <ul>
 * <li>This assumes each value is an 8-bit byte; it may not work right with downstream types that
 *      assume something else.  (That T2CV.11.12 example may not be a good one). This
 *      could be extended in the figure with e.g. (T2CV.11.12:16) to indicate width.
 * <li>The parsing assumes that "(" and ")" don't appear in the underlying CV names.
 *      That could also be extended with a smarter (Polish) parser.
 * </ul>
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2013
 */
public class MultiByteCvProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param prog       the programmer associated with this facade
     */
    public MultiByteCvProgrammerFacade(Programmer prog) {
        super(prog);
        _prog = prog;
        log.debug("Created with {}", prog);
    }

    int top;
    String addrCVhigh;
    String addrCVlow;
    String valueCV;
    int modulo;
    Programmer _prog;

    // members for handling the programmer interface
    int _val; // remember the value being read/written for confirmative reply
    int _cv; // remember the cv being read/written

    // programming interface
    @Override
    public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("start writeCV");
        _cv = Integer.parseInt(CV);
        _val = val;
        useProgrammer(p);
        if (prog.getCanWrite(CV) || _cv <= top) {
            state = ProgState.PROGRAMMING;
            prog.writeCV(CV, val, this);
        } else {
            // write index first
            state = ProgState.WRITELOWWRITE;
            prog.writeCV(addrCVhigh, _cv / modulo, this);
        }
    }

    @Override
    public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("start readCV");
        _cv = Integer.parseInt(CV);
        useProgrammer(p);
        if (prog.getCanRead(CV) || _cv <= top) {
            state = ProgState.PROGRAMMING;
            prog.readCV(CV, this);
        } else {
            // write index first
            state = ProgState.WRITELOWREAD;
            prog.writeCV(addrCVhigh, _cv / modulo, this);
        }
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            log.info("programmer already in use by {}", _usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    enum ProgState {
        /**
         * A pass-through operation, waiting reply, when done the entire
         * operation is done
         */
        PROGRAMMING,
        /**
         * Wrote 1st index on a read operation, waiting for reply
         */
        WRITELOWREAD,
        /**
         * Wrote 1st index on a write operation, waiting for reply
         */
        WRITELOWWRITE,
        /**
         * Wrote 2nd index on a read operation, waiting for reply
         */
        FINISHREAD,
        /**
         * Wrote 2nd index on a write operation, waiting for reply
         */
        FINISHWRITE,
        /**
         * nothing happening, no reply expected
         */
        NOTPROGRAMMING
    }
    ProgState state = ProgState.NOTPROGRAMMING;

    // get notified of the final result
    // Note this assumes that there's only one phase to the operation
    @Override
    public void programmingOpReply(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }

        if (status != OK) {
            // pass abort up
            log.debug("Reset and pass abort up");
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null; // done
            state = ProgState.NOTPROGRAMMING;
            temp.programmingOpReply(value, status);
            return;
        }

        if (_usingProgrammer == null) {
            log.error("No listener to notify, reset and ignore");
            state = ProgState.NOTPROGRAMMING;
            return;
        }

        switch (state) {
            case PROGRAMMING:
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                jmri.ProgListener temp = _usingProgrammer;
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                temp.programmingOpReply(value, status);
                break;
            case WRITELOWREAD:
                try {
                    state = ProgState.FINISHREAD;
                    prog.writeCV(addrCVlow, _cv % modulo, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case WRITELOWWRITE:
                try {
                    state = ProgState.FINISHWRITE;
                    prog.writeCV(addrCVlow, _cv % modulo, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final write", e);
                }
                break;
            case FINISHREAD:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.readCV(valueCV, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case FINISHWRITE:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.writeCV(valueCV, _val, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final write", e);
                }
                break;
            default:
                log.error("Unexpected state on reply: " + state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;

        }

    }

    // Access to full address space provided by this.
    @Override
    public boolean getCanRead() {
        return _prog.getCanRead();
    }

    @Override
    public boolean getCanRead(String addr) {
        return _prog.getCanRead() && (Integer.parseInt(addr) <= 1024);
    }

    @Override
    public boolean getCanWrite() {
        return _prog.getCanWrite();
    }

    @Override
    public boolean getCanWrite(String addr) {
        return _prog.getCanWrite() && (Integer.parseInt(addr) <= 1024);
    }

    private final static Logger log = LoggerFactory.getLogger(MultiByteCvProgrammerFacade.class);

}
