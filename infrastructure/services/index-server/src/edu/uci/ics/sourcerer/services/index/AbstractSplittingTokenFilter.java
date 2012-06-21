/* 
 * Sourcerer: an infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.ics.sourcerer.services.index;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.CharacterUtils;
import org.apache.lucene.util.Version;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractSplittingTokenFilter extends TokenFilter {
  private final LinkedList<String> tokens;
  protected final CharTermAttribute termAtt;
  protected final CharacterUtils charUtils;
  
  protected AbstractSplittingTokenFilter(Version matchVersion, TokenStream input) {
    super(input);
    tokens = new LinkedList<>();
    termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
    charUtils = CharacterUtils.getInstance(matchVersion);
  }
  
  protected void addToken(char[] buff, int offset, int length) {
    if (length > 0) {
      tokens.add(new String(buff, offset, length));
    }
  }
  
  protected abstract boolean readNextToken() throws IOException;
  
  @Override
  public final boolean incrementToken() throws IOException {
    if (!tokens.isEmpty() || readNextToken()) {
      termAtt.setLength(0);
      termAtt.append(tokens.pollFirst());
      return true;
    } else {
      return false;
    }
  }
}
