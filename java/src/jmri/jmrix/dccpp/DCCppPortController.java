package jmri.jmrix.dccpp;

/**
 * interface for DCCppPortController objects.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author      Mark Underwood Copyright (C) 2015
  *
 * Based on XNetPortController by Paul Bender
 */
public interface DCCppPortController extends jmri.jmrix.PortAdapter {

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    @Override
    boolean status();

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     * @return true if OK to send, else false.
     */
    boolean okToSend();

    /**
     * We need a way to say if the output buffer is empty or not
     * @param s true to set buffer empty, else false.
     */
    void setOutputBufferEmpty(boolean s);

}



