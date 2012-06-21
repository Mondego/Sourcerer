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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ArrayFilter extends TokenFilter {
  private CharTermAttribute termAtt;
  
  protected ArrayFilter(TokenStream input) {
    super(input);
    termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      char[] buff = termAtt.buffer();
      int length = termAtt.length();
      for (int i = length - 1; i >= 0; i--) {
        if (buff[i] != '[' && buff[i] != ']') {
          termAtt.setLength(i + 1);
          return true;
        }
      }
      termAtt.setEmpty();
      return false;
    } else {
      return false;
    }
  }
}
