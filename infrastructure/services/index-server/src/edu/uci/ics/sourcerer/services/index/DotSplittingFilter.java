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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DotSplittingFilter extends AbstractSplittingTokenFilter {
  public DotSplittingFilter(Version matchVersion, TokenStream input) {
    super(matchVersion, input);
  }

  @Override
  protected boolean readNextToken() throws IOException {
    if (input.incrementToken()) {
      char[] buff = termAtt.buffer();
      int length = termAtt.length();
      int start = 0;
      for (int i = 0; i < length; i++) {
        if (buff[i] == '.') {
          addToken(buff, start, i - start);
          start = i + 1;
        }
      }
      addToken(buff, start, length - start);
      return true;
    } else {
      return false;
    }
  }
}
