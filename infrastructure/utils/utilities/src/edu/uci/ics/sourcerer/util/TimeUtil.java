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
package edu.uci.ics.sourcerer.util;

/**
 * @author <a href="mailto:bajracharya@gmail.com">Sushil Bajracharya</a>
 */
public class TimeUtil {

	public static String formatMs(long elapsedTime){
		    
		    String format = String.format("%%0%dd", 2);
		    String ms = String.format(format, elapsedTime % 1000);
		    elapsedTime = elapsedTime / 1000;
		    String seconds = String.format(format, elapsedTime % 60);
		    String minutes = String.format(format, (elapsedTime % 3600) / 60);
		    String hours = String.format(format, elapsedTime / 3600);
		    String time =  hours + ":" + minutes + ":" + seconds + ":" + ms;
		    return time;
	}

}
