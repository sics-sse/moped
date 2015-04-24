/*
 * Copyright 2011-2012 Oracle. All Rights Reserved.
 */

package tests;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.sun.squawk.security.HexEncoding;

/**
 *
 * @author vgupta
 */
public class Utilities {
    private static final char[] hc = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Encodes a string as follows:
     * - The ASCII characters 'a' through 'z', 'A' through 'Z', '0' through
     *   '9', and ".", "-", "*", "_" remain the same.
     * - The space character ' ' is converted into a plus sign '+'.
     * - All other characters are converted into the 3-character string "%xy",
     *   where xy is the two-digit hexadecimal representation of the lower
     *   8-bits of the character.
     */
    public static String URLEncode(String in) {
        char[] chars = in.toCharArray();
        char c = ' ';
        StringBuffer out = new StringBuffer();

        for (int i = 0; i < chars.length; i++) {
            c = chars[i];
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')) {
                out.append(c);
            } else if (c == ' ') {
                out.append('+');
            } else {
                out.append('%');
                out.append(hc[(c >> 4) & 0x0F]);
                out.append(hc[c & 0x0F]);
            }
        }

        return out.toString();
    }

    /**
     * Get current UTC time in ISO 8601 format, i.e. 'yyyy-mm-ddThh:mm:ss.sssZ'
     * @return a string containing the current UTC time in ISO 8601 format
     */
    public static String getUTCTimestamp() {
        return getUTCTimestamp(System.currentTimeMillis());
    }

    /**
     * Convert the specified milliseconds (since midnight Jan 1, 1970 UTC)
     * into UTC time in ISO 8601 format, i.e. 'yyyy-mm-ddThh:mm:ss.sssZ'
     * @return a string containing UTC time in ISO 8601 format for the
     *          specified number of milliseconds
     */
    public static String getUTCTimestamp(long millis) {
        String result = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date(millis));
        int year = cal.get(Calendar.YEAR);
        int month = 1 + cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int ms = cal.get(Calendar.MILLISECOND);

        result = year + "-" +
                ((month < 10)? "0": "") + month + "-" +
                ((day < 10) ? "0" : "") + day + "T" +
                ((hour < 10) ? "0" : "") + hour + ":" +
                ((min < 10) ? "0" : "") + min + ":" +
                ((sec < 10) ? "0" : "") + sec + ".";

        if (ms < 10) {
            result += "00" + ms;
        } else if (ms < 100) {
            result += "0" + ms;
        } else {
            result += "" + ms;
        }

        return (result + "Z");
    }
    
       public final static int UTC_TIME_LEN = 24;
    
    private static StringBuffer appendPadded(StringBuffer buffer, int num, boolean threeDigits) {
        if (threeDigits && num < 100) {
            buffer.append("0");
        }
        if (num < 10) {
            buffer.append("0");
        }
        buffer.append(num);
        return buffer;
    }
    
    final static TimeZone UTC = TimeZone.getTimeZone("UTC");
    final static Calendar cal;
    
    static {
        cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * Convert the specified milliseconds (since midnight Jan 1, 1970 UTC)
     * into UTC time in ISO 8601 format, i.e. 'yyyy-mm-ddThh:mm:ss.sssZ'
     * @return a string containing UTC time in ISO 8601 format for the
     *          specified number of milliseconds
     */
    public static String getUTCTimestamp2(long millis) {
        StringBuffer result = new StringBuffer(UTC_TIME_LEN);
        int year, month, day, hour, min, sec, ms;
        synchronized (cal) {
            cal.setTime(new Date(millis));
            year = cal.get(Calendar.YEAR);
            month = 1 + cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            hour = cal.get(Calendar.HOUR_OF_DAY);
            min = cal.get(Calendar.MINUTE);
            sec = cal.get(Calendar.SECOND);
            ms = cal.get(Calendar.MILLISECOND);
        }
        result.append(year).append('-');
        appendPadded(result, month, false).append('-');
        appendPadded(result, day, false).append('T');
        appendPadded(result, hour, false).append(':');
        appendPadded(result, min, false).append(':');
        appendPadded(result, sec, false).append('.');
        appendPadded(result, ms, true).append('Z');

        return result.toString();
    }

        /**
     * Convert the specified milliseconds (since midnight Jan 1, 1970 UTC)
     * into UTC time in ISO 8601 format, i.e. 'yyyy-mm-ddThh:mm:ss.sssZ'
     * @return a string containing UTC time in ISO 8601 format for the
     *          specified number of milliseconds
     */
    public static String getUTCTimestamp3(long millis) {
        StringBuffer result = new StringBuffer(UTC_TIME_LEN);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC);
        //cal.setTime(new Date(millis));
        int year = cal.get(Calendar.YEAR);
        int month = 1 + cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int ms = cal.get(Calendar.MILLISECOND);

        result.append(year).append('-');
        appendPadded(result, month, false).append('-');
        appendPadded(result, day, false).append('T');
        appendPadded(result, hour, false).append(':');
        appendPadded(result, min, false).append(':');
        appendPadded(result, sec, false).append('.');
        appendPadded(result, ms, true).append('Z');

        return result.toString();
    }

    /**
     * Convert a string with UTC timestamp in ISO 8601 format,
     * i.e. 'yyyy-mm-ddThh:mm:ss.sssZ' to milliseconds since midnight
     * Jan 1, 1970 UTC.
     */
    public static long UTCTimestampToMillis(String timestamp) {
        if (timestamp == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            cal.set(Calendar.YEAR,
                    Integer.parseInt(timestamp.substring(0, 4), 10));
            // Note that we need to subtract 1 for the month
            cal.set(Calendar.MONTH,
                    Integer.parseInt(timestamp.substring(5, 7), 10) - 1);
            cal.set(Calendar.DAY_OF_MONTH,
                    Integer.parseInt(timestamp.substring(8, 10), 10));
            cal.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(timestamp.substring(11, 13), 10));
            cal.set(Calendar.MINUTE,
                    Integer.parseInt(timestamp.substring(14, 16), 10));
            cal.set(Calendar.SECOND,
                    Integer.parseInt(timestamp.substring(17, 19), 10));
            cal.set(Calendar.MILLISECOND,
                    Integer.parseInt(timestamp.substring(20, 23), 10));
        } catch (Exception e) {
            System.err.println("UTCTimestampToMillis caught "
                    + e.getMessage() + " while parsing "
                    + timestamp);
            return 0;
        }
        return cal.getTime().getTime();
    }

    /**
     * Converts a duration from milliseconds to days, hours, minutes and
     * seconds.
     * @param millis Number of milliseconds in the duration
     * @return a human-friendly string indicating the duration in the format
     *  D days, h hours m min s sec
     */
    public static String getDuration(long millis) {
        String out = "";
        long tmp = millis/1000;

        if (tmp > 86400) {
            out += tmp/86400 + "d";
            tmp = tmp % 86400;
        }

        if (tmp > 3600) {
             out += tmp/3600 + "h";
            tmp = tmp % 3600;
        }

        if (tmp > 60) {
            out += tmp/60 + "m";
            tmp = tmp % 60;
        }

        out += tmp + "s";
        return out;
    }

    /*
     * Parses a string containing comma-separated values and inserts key-value
     * pairs into the properties passed as the first argument. Keys are
     * specified in the the third argument.
     *
     * @param props Properties object into which key value pairs are added
     * @param toParse String containing comma-separated values
     * @param prefix This is the prefix, e.g. "$GPGGA," that marks the start
     * of comma separated values in the first argument.
     * @param suffix This is the suffix, e.g. "\r\n" that marks the end
     * of comma separated values in the first argument.
     * @param keys An array of keys for the values included in the first argument
     * @return a set of key-value pairs where the values are parsed from the
     * first argument
     */
    public static void addCSVtoProperties(Properties props, String toParse,
            String prefix, String suffix, String[] keys) {
        String tmp = toParse;

        // Look for the comma separated values between the prefix and "\r\n"
        int sidx = tmp.indexOf(prefix);
        int eidx = tmp.indexOf(suffix);
        if ((sidx >= 0) && (eidx > sidx)) {
            tmp = tmp.substring(sidx + prefix.length(), eidx);
            // parse this comma-separated list of values
            int previdx = -1;
            int newidx = 0;
            int i = 0;
            String val = "";
            // Note that if val is "", then we do not add the key/val pair.
            while ((i < keys.length) && (newidx >= 0)) {
                newidx = tmp.indexOf(',', previdx + 1);
                if (newidx < 0) {
                    val = tmp.substring(previdx + 1);
                } else {
                    val = tmp.substring(previdx + 1, newidx);
                }
                if (!val.equals("")) {
                    props.put(keys[i], val);
                }
                previdx = newidx;
                i++;
            }
        }
    }

    /*
     * Parses a string containing a list of key-value pairs in the format
     * key1:val1 key2:val2 key3:val3 ...
     * and inserts them into the properties passed as the first argument.
     * The key name used in the properties are specified in the the third
     * argument.
     *
     * @param props Properties object into which key value pairs are added
     * @param toParse String containing key value pairs
     * @param prefix This is the prefix, e.g. " BSIC:" that marks the key value
     * pairs
     * @param suffix This is the suffix, e.g. "\r\n" that marks the end
     * of comma separated values in the first argument.
     * @param keys An array of keys for the values included in the first argument
     * @return a set of key-value pairs where the values are parsed from the
     * first argument
     */
    public static void addCVtuplestoProperties(Properties props, String toParse,
            String prefix, String suffix, String[] keys) {
        String tmp = toParse;

        // Look for the key-value tuples between the prefix and "\r\n"
        int sidx = tmp.indexOf(prefix);
        int eidx = tmp.indexOf(suffix);
        if ((sidx >= 0) && (eidx > sidx)) {
            tmp = tmp.substring(sidx + prefix.length(), eidx);
            // parse this list of values
            int previdx = -1;
            int newidx = 0;
            int i = 0;
            String val = "";
            // Note that if val is "", then we do not add the key/val pair.
            while ((i < keys.length) && (newidx >= 0)) {
//                System.out.println("prevIdx=" + previdx + ", newIdx=" + newidx);
                newidx = tmp.indexOf(' ', previdx + 1);
                if (newidx < 0) {
                    val = tmp.substring(previdx + 1);
                } else {
                    val = tmp.substring(previdx + 1, newidx);
                }
//                System.out.println("Val is: " + val);
                if (!val.equals("")) {
                    props.put(keys[i], val);
                }
                previdx = tmp.indexOf(':', newidx);
                i++;
            }
        }
    }

    /**
     * Extracts an error message from the result of an AT command. For example,
     * it will extract the part 'no network service' from an AT command result
     * that looks like:
     * 
     * AT#SERVINFO
     * +CME ERROR: no network service
     *
     * @param errorResponse contains the result of an AT command
     * @return a succint description of the error by getting a substring between
     * "CME ERROR: " and "\r\n"
     */
    public static String extractErrorMessage(String errorResponse) {
        int start = -1;
        int end = -1;

        if (errorResponse == null) return null;
        start = errorResponse.indexOf("CME ERROR: ");
        if (start < 0) return null;
        end = errorResponse.indexOf("\r\n", start);
        if (end < 0) return null;

        return errorResponse.substring(start + ("CME ERROR: ").length(), end);
    }

    // Removes all occurences of a charcater in the specfied string.
    public static String removeChar(String str, char charToRemove) {
        StringBuffer sb = new StringBuffer();
        char c;

        for (int i = 0; i < str.length(); i++) {
            if ((c = str.charAt(i)) != charToRemove) sb.append(c);
        }

        return sb.toString();
    }

    public static String hexEncode(byte[] buf) {
        return HexEncoding.hexEncode(buf);
    }

    private Utilities() {
    }
}
