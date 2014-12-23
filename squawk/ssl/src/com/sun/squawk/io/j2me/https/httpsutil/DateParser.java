/*
 * Copyright 1999-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.io.j2me.https.httpsutil;

import java.util.Hashtable;

/**
 * This class implements somewhat of a subset of the J2SE Date class.
 * However, since the semantics of parse() is slightly different
 * (DateParser will not handle dates prior to 1/1/1970, amd to
 * be able to provide methods that will set timezone and DST
 * information, it is called DateParser.
 */
public class DateParser {
	protected int year;
	protected int month;
	protected int day;
	protected int hour;
	protected int minute;
	protected int second;
	protected int milli;
    protected int tzoffset;
    protected static int local_tz;

	/**
	 * Allocates a <code>DateParser</code> object and initializes it so that
	 * it represents the instant at the start of the second specified
	 * by the <code>year</code>, <code>month</code>, <code>date</code>,
	 * <code>hrs</code>, <code>min</code>, and <code>sec</code> arguments,
	 * in the local time zone.
	 *
	 * @param   year    the year, >= 1583.
	 * @param   month   the month between 0-11.
	 * @param   day     the day of the month between 1-31.
	 * @param   hour    the hours between 0-23.
	 * @param   minute  the minutes between 0-59.
	 * @param   second  the seconds between 0-59.
	 */
	 DateParser(int year, int month, int day,
							  int hour, int minute, int second) {
		if (year < 1583
			|| month < 0 || month > 11
			|| day < 0 || (day > days_in_month[month]
			&& !(month == 1 && day == 29 && year % 4 == 0))
			|| hour < 0 || hour > 23
			|| minute < 0 || minute > 59
			|| second < 0 || second > 59) {
			throw new IllegalArgumentException();
		}

		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		milli = 0;
	}

	/**
	 * Allocates a <code>DateParser</code> object and initializes it so that
	 * it represents the date and time indicated by the string
	 * <code>s</code>, which is interpreted as if by the
	 * {@link DateParser#parse} method.
	 *
	 * @param   s   a string representation of the date.
	 */
	DateParser(String s) {
		internalParse(s);
	}

	/**
	 * Set the local time zone for the DateParser class.
	 * <code>tz</code> must in abbreviated format, e.g. "PST"
	 * for Pacific Standard Time.
	 */
	static void setTimeZone(String tz) {
		if (timezones.get(tz) == null) return;
		local_tz = ((Integer)timezones.get(tz)).intValue();
	}

	/**
	 * Attempts to interpret the string <tt>s</tt> as a representation
	 * of a date and time. If the attempt is successful, the time
	 * indicated is returned represented as teh distance, measured in
	 * milliseconds, of that time from the epoch (00:00:00 GMT on
	 * January 1, 1970). If the attempt fails, an
	 * <tt>IllegalArgumentException</tt> is thrown.
	 * <p>
	 * It accepts many syntaxes; in particular, it recognizes the IETF
	 * standard date syntax: "Sat, 12 Aug 1995 13:30:00 GMT". It also
	 * understands the continental U.S. time-zone abbreviations, but for
	 * general use, a time-zone offset should be used: "Sat, 12 Aug 1995
	 * 13:30:00 GMT+0430" (4 hours, 30 minutes west of the Greenwich
	 * meridian). If no time zone is specified, the local time zone is
	 * assumed. GMT and UTC are considered equivalent.
	 * <p>
	 * The string <tt>s</tt> is processed from left to right, looking for
	 * data of interest. Any material in <tt>s</tt> that is within the
	 * ASCII parenthesis characters <tt>(</tt> and <tt>)</tt> is ignored.
	 * Parentheses may be nested. Otherwise, the only characters permitted
	 * within <tt>s</tt> are these ASCII characters:
	 * <blockquote><pre>
	 * abcdefghijklmnopqrstuvwxyz
	 * ABCDEFGHIJKLMNOPQRSTUVWXYZ
	 * 0123456789,+-:/</pre></blockquote>
	 * and whitespace characters.<p>
	 * A consecutive sequence of decimal digits is treated as a decimal
	 * number:<ul>
	 * <li>If a number is preceded by <tt>+</tt> or <tt>-</tt> and a year
	 *     has already been recognized, then the number is a time-zone
	 *     offset. If the number is less than 24, it is an offset measured
	 *     in hours. Otherwise, it is regarded as an offset in minutes,
	 *     expressed in 24-hour time format without punctuation. A
	 *     preceding <tt>-</tt> means a westward offset. Time zone offsets
	 *     are always relative to UTC (Greenwich). Thus, for example,
	 *     <tt>-5</tt> occurring in the string would mean "five hours west
	 *     of Greenwich" and <tt>+0430</tt> would mean "four hours and
	 *     thirty minutes east of Greenwich." It is permitted for the
	 *     string to specify <tt>GMT</tt>, <tt>UT</tt>, or <tt>UTC</tt>
	 *     redundantly-for example, <tt>GMT-5</tt> or <tt>utc+0430</tt>.
	 * <li>If a number is greater than 70, it is regarded as a year number.
	 *     It must be followed by a space, comma, slash, or end of string.
	 * <li>If the number is followed by a colon, it is regarded as an hour,
	 *     unless an hour has already been recognized, in which case it is
	 *     regarded as a minute.
	 * <li>If the number is followed by a slash, it is regarded as a month
	 *     (it is decreased by 1 to produce a number in the range <tt>0</tt>
	 *     to <tt>11</tt>), unless a month has already been recognized, in
	 *     which case it is regarded as a day of the month.
	 * <li>If the number is followed by whitespace, a comma, a hyphen, or
	 *     end of string, then if an hour has been recognized but not a
	 *     minute, it is regarded as a minute; otherwise, if a minute has
	 *     been recognized but not a second, it is regarded as a second;
	 *     otherwise, it is regarded as a day of the month. </ul><p>
	 * A consecutive sequence of letters is regarded as a word and treated
	 * as follows:<ul>
	 * <li>A word that matches <tt>AM</tt>, ignoring case, is ignored (but
	 *     the parse fails if an hour has not been recognized or is less
	 *     than <tt>1</tt> or greater than <tt>12</tt>).
	 * <li>A word that matches <tt>PM</tt>, ignoring case, adds <tt>12</tt>
	 *     to the hour (but the parse fails if an hour has not been
	 *     recognized or is less than <tt>1</tt> or greater than <tt>12</tt>).
	 * <li>Any word that matches any prefix of <tt>SUNDAY, MONDAY, TUESDAY,
	 *     WEDNESDAY, THURSDAY, FRIDAY</tt>, or <tt>SATURDAY</tt>, ignoring
	 *     case, is ignored. For example, <tt>sat, Friday, TUE</tt>, and
	 *     <tt>Thurs</tt> are ignored.
	 * <li>Otherwise, any word that matches any prefix of <tt>JANUARY,
	 *     FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER,
	 *     OCTOBER, NOVEMBER</tt>, or <tt>DECEMBER</tt>, ignoring case, and
	 *     considering them in the order given here, is recognized as
	 *     specifying a month and is converted to a number (<tt>0</tt> to
	 *     <tt>11</tt>). For example, <tt>aug, Sept, april</tt>, and
	 *     <tt>NOV</tt> are recognized as months. So is <tt>Ma</tt>, which
	 *     is recognized as <tt>MARCH</tt>, not <tt>MAY</tt>.
	 * <li>Any word that matches <tt>GMT, UT</tt>, or <tt>UTC</tt>, ignoring
	 *     case, is treated as referring to UTC.
	 * <li>Any word that matches <tt>EST, CST, MST</tt>, or <tt>PST</tt>,
	 *     ignoring case, is recognized as referring to the time zone in
	 *     North America that is five, six, seven, or eight hours west of
	 *     Greenwich, respectively. Any word that matches <tt>EDT, CDT,
	 *     MDT</tt>, or <tt>PDT</tt>, ignoring case, is recognized as
	 *     referring to the same time zone, respectively, during daylight
	 *     saving time.</ul><p>
	 * Once the entire string s has been scanned, it is converted to a time
	 * result in one of two ways. If a time zone or time-zone offset has been
	 * recognized, then the year, month, day of month, hour, minute, and
	 * second are interpreted in UTC and then the time-zone offset is
	 * applied. Otherwise, the year, month, day of month, hour, minute, and
	 * second are interpreted in the local time zone.
	 *
	 * @param   s   a string to be parsed as a date.
	 * @return  the distance in milliseconds from January 1, 1970, 00:00:00 GMT
	 *          represented by the string argument. Note that this method will
	 *          throw an <code>IllegalArgumentException</code> if the year
	 *          indicated in <code>s</code> is less than 1583.
	 */
	public static long parse(String s) {
		return (new DateParser(s)).getTime();
	}

	/** Return the year represented by this date. */
	int getYear() {
		return year;
	}

	/** Return the month represented by this date. */
	int getMonth() {
		return month;
	}

	/** Return the day of the month represented by this date. */
	int getDay() {
		return day;
	}

	/** Return the hour represented by this date. */
	int getHour() {
		return hour;
	}

	/** Return the minute represented by this date. */
	int getMinute() {
		return minute;
	}

	/** Return the second represented by this date. */
	int getSecond() {
		return second;
	}

	/** Return the number of milliseconds since 1/1/1970 represented by this date. */
	long getTime() {
		long julianDay = computeJulianDay(year, month, day);
		long millis = julianDayToMillis(julianDay);

		int millisInDay = 0;
		millisInDay += hour;
		millisInDay *= 60;
		millisInDay += minute; // now have minutes
		millisInDay *= 60;
		millisInDay += second; // now have seconds
		millisInDay *= 1000;
		millisInDay += milli; // now have millis

		return millis + millisInDay - tzoffset;
	}


	private final long computeJulianDay(int year, int month, int day) {
		int y;
		long millis = 0;

		boolean isLeap = year%4 == 0;
		y = year - 1;
		long julianDay = 365L*y + floorDivide(y, 4) + (JAN_1_1_JULIAN_DAY - 3);

		isLeap = isLeap && ((year%100 != 0) || (year%400 == 0));
		// Add 2 because Gregorian calendar starts 2 days after Julian calendar
		julianDay += floorDivide(y, 400) - floorDivide(y, 100) + 2;
		julianDay += isLeap ? LEAP_NUM_DAYS[month] : NUM_DAYS[month];
		julianDay += day;

		return julianDay;
	}

	/**
	 * Divide two long integers, returning the floor of the quotient.
	 * <p>
	 * Unlike the built-in division, this is mathematically well-behaved.
	 * E.g., <code>-1/4</code> => 0
	 * but <code>floorDivide(-1,4)</code> => -1.
	 * @param numerator the numerator
	 * @param denominator a divisor which must be > 0
	 * @return the floor of the quotient.
	 */
	private static final long floorDivide(long numerator, long denominator) {
		// We do this computation in order to handle
		// a numerator of Long.MIN_VALUE correctly
		return (numerator >= 0) ?
			numerator / denominator :
			((numerator + 1) / denominator) - 1;
	}

	// public String toString() {
	// 	return "" + month + "/" + day + "/" + year
	// 		+ " " + hour + ":" + minute + ":" + second;
	// }

	private long julianDayToMillis(long julian) {
		return (julian - julianDayOffset) * millisPerDay;
	}

	private void internalParse(String s) {
		int year = -1;
		int mon = -1;
		int mday = -1;
		int hour = -1;
		int min = -1;
		int sec = -1;
		int millis = -1;
		int c = -1;
		int i = 0;
		int n = -1;
		int wst = -1;
		int tzoffset = -1;
		int prevc = 0;
	syntax: {
		if (s == null)
		break syntax;
		int limit = s.length();
		while (i < limit) {
		c = s.charAt(i);
		i++;
		if (c <= ' ' || c == ',')
			continue;
		if (c == '(') {	// skip comments
			int depth = 1;
			while (i < limit) {
			c = s.charAt(i);
			i++;
			if (c == '(') depth++;
			else if (c == ')')
				if (--depth <= 0)
					break;
			}
			continue;
		}
		if ('0' <= c && c <= '9') {
			n = c - '0';
			while (i < limit && '0' <= (c = s.charAt(i)) && c <= '9') {
			n = n * 10 + c - '0';
			i++;
			}
			if (prevc == '+' || prevc == '-' && year>=0) {
			// timezone offset
			if (n < 24)
				n = n * 60;	// EG. "GMT-3"
			else
				n = n % 100 + n / 100 * 60;	// eg "GMT-0430"
			if (prevc == '+')	// plus means east of GMT
				n = -n;
			if (tzoffset != 0 && tzoffset != -1)
				break syntax;
			tzoffset = n;
			} else if (n >= 70)
			if (year >= 0)
				break syntax;
			else if (c <= ' ' || c == ',' || c == '/' || i >= limit)
				// year = n < 1900 ? n : n - 1900;
				year = n < 100 ? n + 1900 : n;
			else
				break syntax;
			else if (c == ':')
			if (hour < 0)
				hour = (byte) n;
			else if (min < 0)
				min = (byte) n;
			else
				break syntax;
			else if (c == '/')
			if (mon < 0)
				mon = (byte) (n - 1);
			else if (mday < 0)
				mday = (byte) n;
			else
				break syntax;
			else if (i < limit && c != ',' && c > ' ' && c != '-')
			break syntax;
			else if (hour >= 0 && min < 0)
			min = (byte) n;
			else if (min >= 0 && sec < 0)
			sec = (byte) n;
			else if (mday < 0)
			mday = (byte) n;
			else
			break syntax;
			prevc = 0;
		} else if (c == '/' || c == ':' || c == '+' || c == '-')
			prevc = c;
		else {
			int st = i - 1;
			while (i < limit) {
			c = s.charAt(i);
			if (!('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z'))
				break;
			i++;
			}
			if (i <= st + 1)
			break syntax;
			int k;
			for (k = wtb.length; --k >= 0;)
			if (wtb[k].regionMatches(true, 0, s, st, i - st)) {
				int action = ttb[k];
				if (action != 0) {
				if (action == 1) {	// pm
					if (hour > 12 || hour < 1)
					break syntax;
					else if (hour < 12)
										hour += 12;
								} else if (action == 14) {  // am
					if (hour > 12 || hour < 1)
					break syntax;
					else if (hour == 12)
										hour = 0;
								} else if (action <= 13) {	// month!
					if (mon < 0)
					mon = (byte) (action - 2);
					else
					break syntax;
								} else {
					tzoffset = action - 10000;
								}
							}
				break;
			}
			if (k < 0)
			break syntax;
			prevc = 0;
		}
		}
		if (year < 1583 || mon < 0 || mday < 0)
		break syntax;
		if (sec < 0)
		sec = 0;
		if (min < 0)
		min = 0;
		if (hour < 0)
		hour = 0;

		this.year = year;
		month = mon;
		day = mday;
		this.hour = hour;
        this.tzoffset = -tzoffset * 60 * 1000;
		minute = min;
		second = sec;
		milli = 0;
		return;
	}
		// syntax error
		throw new IllegalArgumentException();
	}

	private static Hashtable timezones;

	private int[] days_in_month = {31, 28, 31, 30, 31, 30, 31,
									31, 30, 31, 30, 31};
	private String[] month_shorts = {"Jan", "Feb", "Mar", "Apr",
		"May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	private String[] weekday_shorts = {"Mon", "Tue", "Wed", "Thu",
										"Fri", "Sat", "Sun"};

	private static long julianDayOffset = 2440588;
	private static int millisPerHour = 60 * 60 * 1000;
	private static int millisPerDay = 24 * millisPerHour;
	private static final int JAN_1_1_JULIAN_DAY = 1721426; // January 1, year 1 (Gregorian)

	private final static String wtb[] = {
		"am", "pm",
		"monday", "tuesday", "wednesday", "thursday", "friday",
		"saturday", "sunday",
		"january", "february", "march", "april", "may", "june",
		"july", "august", "september", "october", "november", "december",
		"gmt", "ut", "utc", "est", "edt", "cst", "cdt",
		"mst", "mdt", "pst", "pdt"
		// this time zone table needs to be expanded
	};

	private final static int ttb[] = {
		14, 1, 0, 0, 0, 0, 0, 0, 0,
		2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
		10000 + 0, 10000 + 0, 10000 + 0,	// GMT/UT/UTC
		10000 + 5 * 60, 10000 + 4 * 60,	// EST/EDT
		10000 + 6 * 60, 10000 + 5 * 60,
		10000 + 7 * 60, 10000 + 6 * 60,
		10000 + 8 * 60, 10000 + 7 * 60
	};

	private static final int NUM_DAYS[]
		= {0,31,59,90,120,151,181,212,243,273,304,334};
	private static final int LEAP_NUM_DAYS[]
		= {0,31,60,91,121,152,182,213,244,274,305,335};

	static {
		timezones = new Hashtable();
		timezones.put("GMT", new Integer(0 * millisPerHour));
		timezones.put("UT", new Integer(0 * millisPerHour));
		timezones.put("UTC", new Integer(0 * millisPerHour));
		timezones.put("PST", new Integer(-8 * millisPerHour));
		timezones.put("PDT", new Integer(-7 * millisPerHour));
		timezones.put("JST", new Integer(9 * millisPerHour));

		local_tz = ((Integer)timezones.get("PST")).intValue();
	}
}
