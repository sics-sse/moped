package common;

public class MopedException extends Exception {

    //	private static final long serialVersionUID = 1L;

    private String m_msg;
    private Exception m_orig;

    public MopedException(String msg) {
	m_msg = msg;
	m_orig = null;
    }

    public MopedException(String msg, Exception e) {
	m_msg = msg;
	m_orig = e;
    }

    public String getMsg() {
	return m_msg;
    }

    public Exception getOriginalException() {
	return m_orig;
    }

}
