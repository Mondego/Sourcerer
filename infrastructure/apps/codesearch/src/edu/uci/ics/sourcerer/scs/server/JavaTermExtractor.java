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
package edu.uci.ics.sourcerer.scs.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

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


/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class JavaTermExtractor {

	/**
	 * a valid whitespace as separator for terms
	 */
	public static String TERM_SEPARATOR = " ";

	public static String FQN_SPLIT_CHARS = "[^A-Za-z]";

	
	public static String[] extractShortNameFragments(String fqn){
		
		SplitCamelCaseIdentifier splitter = new SplitCamelCaseIdentifier(extractShortName(fqn));

		Collection<String> _strCol = splitter.split();
		
		String[] terms = new String[_strCol.size()];
		
		int i = 0;
		for(String s: _strCol){
			terms[i++] = s;
		}
		
		return terms;
	}
	
	
	/**
	 * extracts the short name from FQN works both for fqn w/o method arguments
	 * or for fqn with method arguments
	 * 
	 * @param fqn
	 * @return
	 */
	public static String extractShortName(String fqn) {

		// try to strip anything after first '<'
		int idx = fqn.indexOf('<');
		if(idx>-1){
			fqn = fqn.substring(0, idx);
		}
		
		// try to strip anything after first '('
		idx = fqn.indexOf('(');
		if(idx>-1){
			fqn = fqn.substring(0, idx);
		}
		
		// fqn = removeMethodArguments(fqn);
		
		String[] _fragments = fqn.split("[.]");
		int _lastIndex = _fragments.length;
		
		return _fragments[_lastIndex - 1];
	}

	/**
	 * splits the fqn to fragments using "." as separator and returns a single
	 * string where all the fqn fragments are appended with TERM_SEPARATOR in
	 * between
	 * 
	 * @param fqn
	 * @return
	 */
	public static String extractFQNFragments(String fqn) {
		return mergeTerms(fqn.split("[.]"));
	}

	/**
	 * Extracts the true FQN for a method, i.e; removes the method arguments
	 * 
	 * @param fqn
	 *            input of form: org.foo.Cl.m(arg)
	 * @return method's fqn of form: orf.foo.Cl.m
	 */
	public static String removeMethodArguments(String fqn) {
		return fqn.split("[(]")[0];
	}

	/**
	 * @param fqn
	 * @return comma separated fqns of method arguments in format
	 *         foo.arg1,x.y.arg2 no whitespaces
	 */
	public static String extractMethodSigArgsInFQN(String fqn) {

		String[] _fragments = fqn.split("[(]");

		if (_fragments == null || _fragments.length <= 1)
			return "";

		return _fragments[1].replaceAll("\\s", "").replace(")", "");

	}
	
	/**
	 * @param fqn 
	 * @return int value indicating how many arguments this method has
	 */
	public static int extractMethodArgsArityFromFQN(String fqn){
		
		String _argsInSingleString = JavaTermExtractor.extractMethodSigArgsInFQN(fqn);
		if (_argsInSingleString.equals(""))
			return 0;
		
		String[] _args = _argsInSingleString.split(",");
		return _args.length;
	}

	/**
	 * 
	 * @param fqn
	 * @return comma separted short names of the method arguments in format
	 *            arg1,arg2
	 */
	public static String extractMethodSigArgsInShortName(String fqn) {
		
		String _fqn = extractMethodSigArgsInFQN(fqn);
		
		if (fqn.equals("")) return "";
	
		String[] _argsFqn = _fqn.split(",");
		StringBuffer _buf = new StringBuffer("");
		
		for(String s: _argsFqn){
			String _sname = extractShortName(s);
			// don't think this check is needed
			if(!_sname.equals("")){
				_buf.append(_sname);
				_buf.append(" ");
			}
		}
		
		String _sigArgsSname = _buf.toString().trim().replace(" ", ",");
		
		return _sigArgsSname;
	}

	private static String getFQNFragmentTermsAsString(String fqnFragment) {

		SplitCamelCaseIdentifier splitter = new SplitCamelCaseIdentifier(
				fqnFragment);

		Collection<String> _strCol = splitter.split();

		return mergeTerms(_strCol);

	}

	/**
	 * Returns a single string representation of the FQN terms separated by
	 * TERM_SEPARATOR
	 * 
	 * @param fqn
	 *            Fully Qualified Name
	 * @return String representing the terms separated by TERM_SEPARATOR
	 */
	public static String getFQNTermsAsString(String fqn) {

		String[] fragments = fqn.split(FQN_SPLIT_CHARS);

		if (fragments == null || fragments.length <= 0)
			return "";

		Collection<String> _strCol = new LinkedList<String>();

		for (String _fragment : fragments) {
			_strCol.add(getFQNFragmentTermsAsString(_fragment));
		}

		return mergeTerms(_strCol);

	}

	/**
	 * Takes a Collection of String and returns a single string that has all the
	 * strings in the colletion separated with the TERM_SEPARATOR
	 * 
	 * @param strCol
	 * @return
	 */
	public static String mergeTerms(Collection<String> strCol) {

		String retVal = "";
		if (strCol == null || strCol.size() <= 0)
			return retVal;
		else {
			StringBuffer _buf = new StringBuffer();
			for (String s : strCol) {
				_buf.append(s);
				_buf.append(TERM_SEPARATOR);
			}
			retVal = _buf.toString().trim();
		}

		return retVal;
	}

	public static String mergeTerms(String[] strCol) {

		String retVal = "";
		if (strCol == null || strCol.length <= 0)
			return retVal;
		else {
			StringBuffer _buf = new StringBuffer();
			for (String s : strCol) {
				_buf.append(s);
				_buf.append(TERM_SEPARATOR);
			}
			retVal = _buf.toString().trim();
		}

		return retVal;
	}

}

/**
 * Sliced Document sliced from sourcerer, query: camel case split
 */
class SplitCamelCaseIdentifier {

	/* fields */
	private String ident;

	/* constructors */
	public SplitCamelCaseIdentifier(String ident) {
		this.ident = ident;
	}

	/* methods */
	public Collection<String> split() {
		String s = ident;
		Set<String> result = new HashSet<String>();

		while (s.length() > 0) {
			StringBuffer buf = new StringBuffer();

			char first = s.charAt(0);
			buf.append(first);
			int i = 1;

			if (s.length() > 1) {
				boolean camelWord;
				if (Character.isLowerCase(first)) {
					camelWord = true;
				} else {
					char next = s.charAt(i++);
					buf.append(next);
					camelWord = Character.isLowerCase(next);
				}

				while (i < s.length()) {
					char c = s.charAt(i);
					if (Character.isUpperCase(c)) {
						if (camelWord)
							break;
					} else if (!camelWord) {
						break;
					}
					buf.append(c);
					++i;
				}

				if (!camelWord && i < s.length()) {
					buf.deleteCharAt(buf.length() - 1);
					--i;
				}
			}

			result.add(buf.toString().toLowerCase(Locale.US));
			s = s.substring(i);
		}

		return result;
	}
}