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
package edu.uci.ics.sourcerer.tools.java.highlighter;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.logging.Level;
/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class HighlightBuilder {
  public static final int TAB_WIDTH = 4;
  public static final int MINIMUM_TAB_WIDTH = 0;
  
  private StringBuilder builder = new StringBuilder();
  
  private int charsIntoLine = 0;
  
  protected HighlightBuilder() {}
  
  public void appendTag(TagLocation tag) {
    if (tag.getType() == TagType.COLOR) {
      builder.append("<span class=\"");
      builder.append(tag.getKlass());
      builder.append("\">");
    } else if (tag.getType().isLinkType()) {
      builder.append("<a href=\"");
      builder.append(tag.getLink());
      builder.append("\" class=\"");
      builder.append(tag.getKlass());
      if (tag.getTitle() != null) {
        builder.append("\" title=\"");
        builder.append(tag.getTitle());
      }
      builder.append("\">");
    } else {
      logger.log(Level.SEVERE, "Unknown tag type: " + tag.getType());
    }
  }
  
  public void appendEndTag(TagLocation tag) {
    if (tag.getType() == TagType.COLOR) {
      builder.append("</span>");
    } else if (tag.getType().isLinkType()) {
      builder.append("</a>");
    } else {
      logger.log(Level.SEVERE, "Unknown tag type: " + tag.getType());
    }
  }
  
  public void appendHtmlTag(String s) {
    builder.append(s);
  }
  
  public void appendWord(String s) {
    builder.append(s);
    charsIntoLine += s.length();
  }
  
  public void append(char c) {
    switch (c) {
      case '<':
        builder.append("&lt;");
        charsIntoLine++;
        break;
      case '>':
        builder.append("&gt;");
        charsIntoLine++;
        break;
      case '&':
        builder.append("&amp;");
        charsIntoLine++;
        break;
      case '\t':
        int remaining = charsIntoLine % TAB_WIDTH;
        if (remaining <= MINIMUM_TAB_WIDTH) {
          remaining += 4;
        }
        while (remaining-- > 0) {
          builder.append("&nbsp;");
        }
        charsIntoLine = 0;
        break;
      case '\r':
        break;
      case '\n':
        builder.append("<br>\n");
        charsIntoLine = 0;
        break;
      default:
        if (Character.isWhitespace(c)) {
          builder.append("&nbsp;");
        } else {
          builder.append(c);
        }
        charsIntoLine++;
    }
  }
  
  public String toString() {
    return builder.toString();
  }
}
