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
package edu.uci.ics.sourcerer.search.analysis;

import java.io.IOException; // import java.io.Reader;
// import java.io.StringWriter;
import java.util.Arrays;
import java.util.Stack;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;


/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jun 22, 2009
 * 
 */
public final class FqnFilter extends TokenFilter {

	final int extractSig;
	final int shortNamesOnly; // will void the effect of fragment*

	final String UNRESOLVED_LITERAL = "_UNRESOLVED_.";
	final String UNKNOWN_LITERAL = "\\(UNKNOWN\\)";
	final String UNKNOWN_LITERAL2 = "\\(1UNKNOWN\\)";
	final char TYPED_PARAMETER_LITERAL = 'T';

	public FqnFilter(TokenStream in, int extractSig, int shortNamesOnly) {
		super(in);
		this.extractSig = extractSig;
		this.shortNamesOnly = shortNamesOnly;
	}

	private String processAngularBrackets(String term) {
		Stack<Integer> _stack = new Stack<Integer>();
		char[] chars = term.toCharArray();

		for (int i = 0; i < chars.length; i++) {

			if (chars[i] == '<') {
				_stack.push(new Integer(i));
			} else if (chars[i] == '>') {
				int _start = _stack.pop().intValue();
				if (_stack.empty()) {
					int _end = i;
					boolean preserveAngularBrackets = false;
					// handle angular brackets
					// here '<' and '>' are the outermost occurrences of angular
					// brackets
					// in (possibly nested) expressions in the FQNs
					// Example: In <<T>,<K>,<V>> the expression after removing
					// the outermost
					// angular brackets is <T>,<K>,<V>

					// TYPE_VARIABLEs appear as
					// <..>
					// currently don't index TYPE_VARIABLES, thus do nothing
					if (_start == 0 && _end == chars.length) {
						preserveAngularBrackets = true;
					} else if (_start > 0 && _end < chars.length) {
						// handle constructors and initializations
						// .<clinit>( OR .<init>(
						if (chars[_start - 1] == '.' && chars[_end + 1] == '(') {
							if (_end - _start == 5) {
								if (chars[_start + 1] == 'i'
										&& chars[_start + 2] == 'n'
										&& chars[_start + 3] == 'i'
										&& chars[_start + 4] == 't') {
									preserveAngularBrackets = true;
								}
							} else if (_end - _start == 7) {
								if (chars[_start + 1] == 'c'
										&& chars[_start + 2] == 'l'
										&& chars[_start + 3] == 'i'
										&& chars[_start + 4] == 'n'
										&& chars[_start + 5] == 'i'
										&& chars[_start + 6] == 't') {
									preserveAngularBrackets = true;
								}
							}
						}

						// handle type params in method signatures
						// (<..>, OR ,<..>, OR ,<..>) OR (<..>) OR (<..>[ OR
						// ,<..>[
						else if ((chars[_start - 1] == '(' && chars[_end + 1] == ',')
								|| (chars[_start - 1] == ',' && chars[_end + 1] == ',')
								|| (chars[_start - 1] == ',' && chars[_end + 1] == ')')
								|| (chars[_start - 1] == '(' && chars[_end + 1] == ')')
								|| (chars[_start - 1] == '(' && chars[_end + 1] == '[')
								|| (chars[_start - 1] == ',' && chars[_end + 1] == '[')) {

							// preserve the angular brackets and insert 'T' ->
							// '<T>'
							Arrays.fill(chars, _start + 1, _end, ' ');
							chars[_start + 1] = TYPED_PARAMETER_LITERAL;
							preserveAngularBrackets = true;

						}

					}

					if (!preserveAngularBrackets) {
						// parameterized types are stripped of angular brackets
						// and information therein
						Arrays.fill(chars, _start, _end + 1, ' ');
					}

				}
			}
		}

		return new String(chars).replaceAll(" ", "");
	}

	public final Token next(final Token reusableToken) throws IOException {
		assert reusableToken != null;

		Token token = input.next(reusableToken);

		if (null == token || null == token.termBuffer()
				|| token.termLength() == 0) {
			return token;
		}

		String tokenTerm = token.term();

		// remove all typed parameters, and constructor info from fqn
		// these would be pulled in the fields that denote usage
		String newTerm = tokenTerm.replaceAll(UNRESOLVED_LITERAL, "");
		newTerm = newTerm.replaceAll(UNKNOWN_LITERAL, "");
		newTerm = newTerm.replaceAll(UNKNOWN_LITERAL2, "");
		
		// remove all whitespace from fqn
		newTerm = newTerm.replaceAll("\\s", "");
		newTerm = processAngularBrackets(newTerm);

		token.setTermLength(newTerm.length());
		token.setTermBuffer(newTerm);
		token.setEndOffset(newTerm.length());
		token.setStartOffset(0);

		// extracting method signatures
		if (extractSig > 0) {

			if (shortNamesOnly > 0) {

				token = JavaFqnTokenizer.extractMethodSigArgsInShortName(token);

			} else {

				token = JavaFqnTokenizer.extractMethodSigArgsInFQN(token);
			}

		}
		// extracting fqns
		else {

			if (shortNamesOnly > 0) {

				token = JavaFqnTokenizer.extractShortName(token);

			} else { // full fqn

				token = JavaFqnTokenizer.extractFQN(token);

			}
		}

		return token;
	}

}
