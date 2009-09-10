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
package edu.uci.ics.sourcerer.scs.client;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 31, 2009
 */
public class JavaNameUtil {
	public static String extractShortName(String fqn){
		int i = getDotIndexBeforeSname(fqn);
		if(i<0) i = 0;
		return extractShortName(fqn, i);
	}

	public static String extractContainerPartFromFQN(String fqn, int lastIndexOfDot) {

		// int lastIndexOfDot = dotIndexBeforeSname(fqn);

		if (lastIndexOfDot > 0)
			fqn = fqn.substring(0, lastIndexOfDot);
		else
			return null;

		return fqn;
	}

	public static int getDotIndexBeforeSname(String fqn) {

		int lastIndexOfOpenBracket = fqn.lastIndexOf('(');
		int lastIndexOfDot = -1;

		if (lastIndexOfOpenBracket > 0)
			fqn = fqn.substring(0, lastIndexOfOpenBracket);

		int firstIndexOfAngOpenBracket = fqn.indexOf('<');
		if (firstIndexOfAngOpenBracket > 0) {
			if (fqn.charAt(firstIndexOfAngOpenBracket - 1) != '.') {
				// not .<init => has type parameter
				lastIndexOfDot = fqn.lastIndexOf('.',
						firstIndexOfAngOpenBracket);
			} else {
				lastIndexOfDot = fqn.lastIndexOf('.');
			}
		} else if (firstIndexOfAngOpenBracket < 0) {

			lastIndexOfDot = fqn.lastIndexOf('.');
		} else
			/* first character is '<'; it has no container */
			return -1;

		if (lastIndexOfDot > 0)
			return lastIndexOfDot;
		else
			return -1;

	}
	
	
	public static String extractShortName(String fqn, int lastIndexOfDot) {

		// remove method signature
		int indexOfOpenBracket = fqn.indexOf('(');
		if (indexOfOpenBracket > 0) {
			fqn = fqn.substring(0, indexOfOpenBracket);
		}

		// int lastIndexOfDot = dotIndexBeforeSname(fqn);

		if (lastIndexOfDot > 0 && lastIndexOfDot < fqn.length())
			return fqn.substring(lastIndexOfDot + 1, fqn.length());
		else
			return fqn;

	}

	public static String[] extractMethodSigArgsInFQN(String fqn) {

		String[] _fragments = fqn.split("[(]");

		if (_fragments == null || _fragments.length <= 1)
			return null;

		return _fragments[1].replace(")", "").split("[,]");

	}

//	// copied from old edu.uci.ics.sourcerer.lucene.JavaTermExtractor
//
//	/**
//	 * Extracts the true FQN for a method, i.e; removes the method arguments
//	 * 
//	 * @param fqn
//	 *            input of form: org.foo.Cl.m(arg)
//	 * @return method's fqn of form: orf.foo.Cl.m
//	 */
//	private String removeMethodArguments(String fqn) {
//		return fqn.split("[(]")[0];
//	}

	public static boolean isJavaPrimitive(String name) {
		if (name.equals("byte") || name.equals("short") || name.equals("int")
				|| name.equals("long") || name.equals("float")
				|| name.equals("double") || name.equals("char")
				|| name.equals("boolean") || name.equals("void")

				|| name.startsWith("byte[") || name.startsWith("short[")
				|| name.startsWith("int[") || name.startsWith("long[")
				|| name.startsWith("float") || name.startsWith("double[")
				|| name.startsWith("char[") || name.startsWith("boolean[")) {
			return true;
		}

		return false;
	}
}
