package io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractReceiver extends AbstractIO {
  private int rate = -1;
  private List<ReceiverDispatcher> dispatchers = new ArrayList<>();

  /**
   * HashMap containing the protocol specific headers for that
	 * receiver.
	 */
	private HashMap<String,String> headers = new HashMap<String,String>();

	/**
	 * Add a protocol header key and value.
	 * Add a key and value that will be used by the protocol implementation every
	 * time a publication update is being made.
	 * @param key Protocol-specific key
	 * @param value Protocol-specific value
	 */
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	/**
	 * Query current set of protocol headers.
	 * @return The list of protocol headers
	 */
	public HashMap<String, String> getHeaders() {
		return headers;
	}

  /**
   * Set the rate at which values should be polled, for protocols
   * that require polling.
   * @param rate Rate in milliseconds, negative to turn off
   */
  public void setPoll(int rate) {
    this.rate = rate;
  }

  /**
   * Get the polling rate
   * @return the polling rate.
   */
  public int getPoll() {
    return rate;
  }


  protected ReceiverDispatcher addDispatcher(IMessage msg) {
      //System.out.println("addDispatcher IMessage");
      //System.out.println(" length " + dispatchers.size());
    ReceiverDispatcher d = new ReceiverDispatcher(msg);
    dispatchers.add(d);
    //System.out.println(" length " + dispatchers.size());
    return d;
  }

  protected ReceiverDispatcher addDispatcher(String ptn, int grp, IMessage msg) {
      //System.out.println("addDispatcher IMessage ptn grp");
    ReceiverDispatcher d = new ReceiverDispatcher(ptn, grp, msg);
    dispatchers.add(d);
    return d;
  }

  protected ReceiverDispatcher addDispatcher(String ptn, IMessage msg) {
      //System.out.println("addDispatcher IMessage (" + ptn + ")");
    return addDispatcher(ptn, -1, msg);
  }

  protected class ReceiverDispatcher implements IMessage {
    /* Turn this on to get additional debugging information on the standard
     * output.
     */
    private boolean debug = false;

    /* This is the regular expression associated to the dispatcher, it will be
     * used to extract sub-text from responses sent by servers.
     */
    protected Pattern ptn = null;

    /* This is the group within the regular expression that you want to extract
     * */
    protected int grp = -1;

    /* This is where to forward (the extracted) message content when new
     * messages have been received and filtered */
    protected IMessage fwd;

    /** Create a dispatcher that will not filter incoming messages and will pass
     * further their contents "as is" */
    public ReceiverDispatcher(IMessage msg) {
      this.ptn = null;
      this.grp = -1;
      this.fwd = msg;
    }

    /**
     * Create a dispatcher that will filter incoming messages through a regular
     * expression and, possibly, extract the text of one its sub-groups.
     * @param ptn Compiled regular expression
     * @param grp Sub-group for text extraction, negative or 0 for all expression.
     * @param msg Where to send the filtered text.
     */
    public ReceiverDispatcher(Pattern ptn, int grp, IMessage msg) {
      this.ptn = ptn;
      this.grp = grp;
      this.fwd = msg;
    }

    /**
     * Create a dispatcher that will filter incoming messages through a regular
     * expression and, possibly, extract the text of one its sub-groups.
     * @param ptn Regular expression to be compiled
     * @param grp Sub-group for text extraction, negative or 0 for all expression.
     * @param msg Where to send the filtered text.
     */
    public ReceiverDispatcher(String ptn, int grp, IMessage msg) {
      this(Pattern.compile(ptn), grp, msg);
    }

    /**
     * Receive and filter a message.  If a regular expression is associated to
     * this dispatcher, it will be applied to the content of the message and
     * the specified sub-group will be the content of the filtered message.
     * When the sub-group is less or equal to zero, the whole matching content
     * of the regular expression will be the result
     * @param msg Text of the message to be filtered.
     */
    @Override
    public void receive(String msg) {
	//System.out.println("AbstractReceiver receive (" + msg + ")");
      if (ptn == null) {
        /* No regular expression, just pass "as-is". */
        if (debug) {
          System.out.println("Forwarding " + msg + "(no rx)");
        }
        if (fwd != null)
          fwd.receive(msg);
      } else {
        /* We have a regular expression, match it against the incoming text */
        Matcher m = ptn.matcher(msg);

        /* For every match, either we push back the match or the sub-group */
        while (m.find()) {
          if (grp >= 0 && grp <= m.groupCount()) {
            if (debug) {
              System.out.println("Forwarding '" + m.group(grp) + "' (match group " + grp + " in rx " + ptn + ")");
            }
            if (fwd != null)
              fwd.receive(m.group(grp));
          }
          if (grp < 0) {
            if (debug) {
              System.out.println("Forwarding '" + m.group() + "' (match in rx " + ptn + ")");
            }
            if (fwd != null)
              fwd.receive(m.group());
          }
        }
      }
    }
  }
}
