package engine.android.util;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Exception for problems with Out Of Memory type of exception 
 * based on RegExp from IOException<br>
 * which comments contains "space" or "memory"<br>
 * Note: 未经检验
 * 
 * @author Daimon
 * @version 3.0
 * @since 6/12/2013
 * Daimon:IOException
 */

public class OOMException extends IOException {

    private static final long serialVersionUID = 1L;

    /** words: 'memory' or 'space' should appeared in middle of a sentence */
    private static final Pattern OOMPattern
    = Pattern.compile("(.*\\smemory\\s.*)|(.*\\sspace\\s.*)", Pattern.CASE_INSENSITIVE);

    /**
     * Checking whether IOException should be treated as Out Of Memory
     * exception.
     */

    public static boolean isOOMException(IOException e) {
        if (e == null || e.getMessage() == null)
        {
            return false;
        }

        return OOMPattern.matcher(e.getMessage()).matches();
    }

    /**
     * Return OOMException when e(IOException) should be treated as Out Of
     * Memory exception. Otherwise return Null.
     */

    public static OOMException obtainOOMException(IOException e) {
        if (isOOMException(e))
        {
            return new OOMException(e);
        }

        return null;
    }

    public OOMException() {
        super();
    }

    public OOMException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * this constructor is supported by superclass only from 
     * API level 9(2.3version) so we have to find another way 
     * to implement for backward compatibility
     */

    public OOMException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public OOMException(Throwable cause) {
        this(cause == null ? null : cause.toString(), cause);
    }
}