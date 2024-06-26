package jmri.jmrix.sprog;

import java.util.Vector;

import jmri.util.JUnitUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the SprogTrafficController class
 *
 * @author Bob Jacobsen
 */
public class SprogTrafficControlScaffold extends SprogTrafficController {

    private boolean useTestReplies = false;
    
    public SprogTrafficControlScaffold(SprogSystemConnectionMemo memo) {
       super(memo);
    }

    public synchronized void setTestReplies(boolean state) {
        useTestReplies = state;
    }
    
    // override some SprogTrafficController methods for test purposes
    @Override
    public boolean status() {
        return isTcThreadAlive();
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<SprogMessage> outbound = new Vector<>();  // public OK here, so long as this is a test class

    @Override
    public void sendSprogMessage(SprogMessage m) {
        log.debug("Send Sprog Message [{}] id {}", m, m.getId());
        // save a copy
        outbound.addElement(m);
        // we don't return an echo so that the processing before the echo can be
        // separately tested
    }

    @Override
    public synchronized void sendSprogMessage(SprogMessage m, SprogListener replyTo) {

        // notify all _other_ listeners
        notifyMessage(m, replyTo);
        this.sendSprogMessage(m);
        
        if (!useTestReplies) {
            // For now we just reply with the SPROG prompt
            // If we add tests that require replies with data the we will need to
            // decode the message and construct a suitable reply
            // Set the id to match the message just sent
            final SprogReply r = new SprogReply("P> ");
            r.setId(m.getId());
            log.debug("Notify reply [{}} id {}", r, r.getId() );
            notifyReply(r, replyTo);
        }
    }

    // test control member functions

    /**
     * forward a message to the listeners, e.g. test receipt
     */
    protected void sendTestMessage(SprogMessage m, SprogListener l) {
        // forward a test message to NceListeners
        log.debug("sendTestMessage    [{}]", m);
        notifyMessage(m, l);
    }

    /**
     * forward a message to the listeners, e.g. test receipt
     */
    protected void sendTestReply(SprogReply m) {
        // forward a test message to NceListeners
        log.debug("sendTestReply [{}]", m);
        notifyReply(m);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            log.debug("Thread interrupted while sleeping");
        }

    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    @Override
    public void dispose() {
        super.dispose();
        JUnitUtil.waitFor(() -> { return status() == false; },"Sprog STCS Thread did not terminate.");
    }

    private final static Logger log = LoggerFactory.getLogger(SprogTrafficControlScaffold.class);

}
