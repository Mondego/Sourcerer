/*
 * Sourcerer: An infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package edu.uci.ics.sourcerer.search;

import java.text.MessageFormat;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Mar 29, 2010
 *
 */
public class Timeutil {
	public static String formatElapsedTime(long elapsedTime) {
        long millis = elapsedTime;
        millis = millis>1000?elapsedTime % 1000:millis;
        long seconds = millis / 1000;
        seconds = seconds > 60 ? seconds % 60 : seconds;
        long minutes = seconds / 60;
        Object[] PARAMS = {
              new Long(minutes), new Long(seconds), new Long(millis)
        };

        String format = "{2} msec";
        if ( seconds > 0 ) format = "{1} seconds "+format;
        if ( minutes > 0 ) format = "{0} minutes"+format;
        return MessageFormat.format(format, PARAMS);
    }
}
