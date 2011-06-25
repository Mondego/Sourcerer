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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class SyntaxHighlighter {
  public static final int TAB_WIDTH = 4;
  public static final int MINIMUM_TAB_WIDTH = 0;
  
  private static Collection<String> keywords = null;
  static {
    keywords = Helper.newHashSet();
    keywords.add("abstract");
    keywords.add("assert");
    keywords.add("boolean");
    keywords.add("break");
    keywords.add("byte");
    keywords.add("case");
    keywords.add("catch");
    keywords.add("char");
    keywords.add("class");
    keywords.add("const");
    keywords.add("continue");
    keywords.add("default");
    keywords.add("do");
    keywords.add("double");
    keywords.add("else");
    keywords.add("enum");
    keywords.add("extends");
    keywords.add("final");
    keywords.add("finally");
    keywords.add("float");
    keywords.add("for");
    keywords.add("goto");
    keywords.add("if");
    keywords.add("implements");
    keywords.add("import");
    keywords.add("instanceof");
    keywords.add("int");
    keywords.add("interface");
    keywords.add("long");
    keywords.add("native");
    keywords.add("new");
    keywords.add("package");
    keywords.add("private");
    keywords.add("protected");
    keywords.add("public");
    keywords.add("return");
    keywords.add("short");
    keywords.add("static");
    keywords.add("strictfp");
    keywords.add("super");
    keywords.add("switch");
    keywords.add("synchronized");
    keywords.add("this");
    keywords.add("throw");
    keywords.add("throws");
    keywords.add("transient");
    keywords.add("try");
    keywords.add("void");
    keywords.add("volatile");
    keywords.add("while");
  }

//  private static Collection<String> tags = null;
//  static {
//    tags = Helper.newHashSet();
//    tags.add("@author");
//    tags.add("@category");
//    tags.add("@deprecated");
//    tags.add("@exception");
//    tags.add("@param");
//    tags.add("@return");
//    tags.add("@see");
//    tags.add("@serial");
//    tags.add("@serialData");
//    tags.add("@serialField");
//    tags.add("@since");
//    tags.add("@throws");
//    tags.add("@vesion");
//  }
  public static String highlightSyntax(String code) {
    return highlightSyntax(code, Collections.<LinkLocation> emptyList());
  }

  public static String highlightSyntax(String code, LinkLocationSet links) {
    return highlightSyntax(code, links.getLinks());
  }

  private static enum State {
    START,
    IN_WORD,
    IN_ANNOTATION,
    IN_LINE_COMMENT,
    IN_BLOCK_COMMENT,
    IN_JAVADOC_COMMENT,
    IN_JAVADOC_TAG,
    IN_STRING_LITERAL,
    IN_CHARACTER_LITERAL,
  }

  private static String highlightSyntax(String code, Collection<LinkLocation> links) {
    EscapeBuilder builder = new EscapeBuilder();

    Iterator<LinkLocation> iter = links.iterator();
    LinkLocation next = null;
    if (iter.hasNext()){
      next = iter.next();
    }

    int endLink = -1;
    
    int charsIntoLine = 0;
    int wordStart = -1;

    State state = State.START;
    for (int i = 0; i < code.length(); i++) {
      char c = code.charAt(i);

      if (endLink == -1 && next != null && next.getOffset() == i) {
        if (state != State.START) {
          logger.log(Level.SEVERE, "Unexpected state type for link: " + state);
          next = null;
          while (next == null && iter.hasNext()) {
            next = iter.next();
            if (next.getOffset() <= i) {
              logger.log(Level.SEVERE, "Conflicting links: " + next.getLink());
              next = null;
            }
          }
        } else {
          builder.append("<a href=\"");
          builder.append(next.getLink());
          builder.append("\">");
          endLink = next.getOffset() + next.getLength();
        }
      } else if (endLink == i && state != State.IN_WORD) {
        builder.append("</a>");
        next = null;
        while (next == null && iter.hasNext()) {
          next = iter.next();
          if (next.getOffset() <= i) {
            logger.log(Level.SEVERE, "Conflicting links: " + next.getLink());
            next = null;
          }
        }
        endLink = -1;
      }
      
      if (state == State.START) {
        if (Character.isWhitespace(c)) {
          if (c == '\t') {
            int remaining = charsIntoLine % TAB_WIDTH;
            if (remaining <= MINIMUM_TAB_WIDTH) {
              remaining += 4;
            }
            while (remaining-- > 0) {
              builder.append("&nbsp;");
            }
            charsIntoLine = 0;
          } else if (c == 'r') {
          } else if (c == '\n') {
            builder.append("<br>\n");
            charsIntoLine = 0;
          } else {
            builder.append("&nbsp;");
            charsIntoLine++;
          }
        } else if (c == '/') {
          if (code.length() > i + 1) {
            char c2 = code.charAt(i + 1);
            if (c2 == '/') {
              builder.append("<span class=\"comment\">");
              builder.append(c);
              charsIntoLine++;
              state = State.IN_LINE_COMMENT;
            } else if (c2 == '*') {
              if (code.length() > i + 2) {
                char c3 = code.charAt(i + 2);
                if (c3 == '*') {
                  builder.append("<span class=\"javadoc-comment\">");
                  builder.append(c);
                  charsIntoLine++;
                  state = State.IN_JAVADOC_COMMENT;
                } else {
                  builder.append("<span class=\"comment\">");
                  builder.append(c);
                  charsIntoLine++;
                  state = State.IN_BLOCK_COMMENT;
                }
              } else {
                logger.log(Level.SEVERE, "Invalid syntax! " + code.substring(i));
                builder.append("<span class=\"comment\">");
                builder.append(c);
                charsIntoLine++;
                state = State.IN_BLOCK_COMMENT;
              }
              
            } else {
              logger.log(Level.SEVERE, "Invalid syntax! " + code.substring(i));
              builder.append(c);
              charsIntoLine++;
            }
          } else {
            logger.log(Level.SEVERE, "Invalid syntax! " + code.substring(i));
            builder.append(c);
            charsIntoLine++;
          }
        } else if (c == '"') {
          builder.append("<span class=\"string\">");
          builder.append(c);
          charsIntoLine++;
          state = State.IN_STRING_LITERAL;
        } else if (c == '\'') {
          builder.append("<span class=\"character\">");
          builder.append(c);
          charsIntoLine++;
          state = State.IN_CHARACTER_LITERAL;
        } else if (Character.isJavaIdentifierPart(c)) {
          wordStart = i;
          state = State.IN_WORD;
        } else if (c == '@') {
          builder.append("<span class=\"annotation\">");
          builder.append(c);
          charsIntoLine++;
          state = State.IN_ANNOTATION;
        } else {
          builder.append(c);
          charsIntoLine++;
        }
      } else if (state == State.IN_LINE_COMMENT) {
        if (Character.isWhitespace(c)) {
          if (c == '\t') {
            int remaining = charsIntoLine % TAB_WIDTH;
            if (remaining <= MINIMUM_TAB_WIDTH) {
              remaining += 4;
            }
            while (remaining-- > 0) {
              builder.append("&nbsp;");
            }
            charsIntoLine = 0;
          } else if (c == 'r') {
          } else if (c == '\n') {
            builder.append("</span><br>");
            state = State.START;
            charsIntoLine = 0;
          } else {
            builder.append("&nbsp;");
            charsIntoLine++;
          }
        } else {
          builder.append(c);
          charsIntoLine++;
        }
      } else if (state == State.IN_BLOCK_COMMENT) {
        if (Character.isWhitespace(c)) {
          if (c == '\t') {
            int remaining = charsIntoLine % TAB_WIDTH;
            if (remaining <= MINIMUM_TAB_WIDTH) {
              remaining += 4;
            }
            while (remaining-- > 0) {
              builder.append("&nbsp;");
            }
            charsIntoLine = 0;
          } else if (c == 'r') {
          } else if (c == '\n') {
            builder.append("<br>\n");
            charsIntoLine = 0;
          } else {
            builder.append("&nbsp;");
            charsIntoLine++;
          }
        } else if (c == '/' && code.charAt(i - 1) == '*') {     
          builder.append(c);
          charsIntoLine++;
          builder.append("</span>");
          state = State.START;
        } else {
          builder.append(c);
          charsIntoLine++;
        }
      } else if (state == State.IN_JAVADOC_COMMENT) {
        if (Character.isWhitespace(c)) {
          if (c == '\t') {
            int remaining = charsIntoLine % TAB_WIDTH;
            if (remaining <= MINIMUM_TAB_WIDTH) {
              remaining += 4;
            }
            while (remaining-- > 0) {
              builder.append("&nbsp;");
            }
            charsIntoLine = 0;
          } else if (c == 'r') {
          } else if (c == '\n') {
            builder.append("<br>\n");
            charsIntoLine = 0;
          } else {
            builder.append("&nbsp;");
            charsIntoLine++;
          }
        } else if (c == '/' && code.charAt(i - 1) == '*') {     
          builder.append(c);
          charsIntoLine++;
          builder.append("</span>");
          state = State.START;
        } else if (c == '@' && Character.isWhitespace(code.charAt(i - 1))) {
          builder.append("<span class=\"javadoc-tag\">");
          builder.append(c);
          charsIntoLine++;
          state = State.IN_JAVADOC_TAG;
        } else {
          builder.append(c);
          charsIntoLine++;
        }
      } else if (state == State.IN_JAVADOC_TAG) {
        if (Character.isJavaIdentifierPart(c)) {
          builder.append(c);
          charsIntoLine++;
        } else {
          builder.append("</span>");
          state = State.IN_JAVADOC_COMMENT;
          i--;
        }
      } else if (state == State.IN_WORD) {
        if (!Character.isJavaIdentifierPart(c)) {
          String word = code.substring(wordStart, i);
          if (keywords.contains(word)) {
            builder.append("<span class=\"keyword\">");
            builder.append(word);
            builder.append("</span>");
          } else {
            builder.append(word);
            builder.append("</span>");
          }
          charsIntoLine += word.length();
          state = State.START;
          i--;
        }
      } else if (state == State.IN_ANNOTATION) {
        if (Character.isJavaIdentifierPart(c)) {
          builder.append(c);
          charsIntoLine++;
        } else {
          builder.append("</span>");
          state = State.START;
          i--;
        }
      } else if (state == State.IN_STRING_LITERAL) {
        if (Character.isWhitespace(c)) {
          if (c == '\t') {
            int remaining = charsIntoLine % TAB_WIDTH;
            if (remaining <= MINIMUM_TAB_WIDTH) {
              remaining += 4;
            }
            while (remaining-- > 0) {
              builder.append("&nbsp;");
            }
            charsIntoLine = 0;
          } else if (c == 'r') {
          } else if (c == '\n') {
            builder.append("<br>\n");
            charsIntoLine = 0;
          } else {
            builder.append("&nbsp;");
            charsIntoLine++;
          }
        } else {
          builder.append(c);
          charsIntoLine++;
          if (c == '"' && code.charAt(i - 1) != '\\') {
            builder.append("</span>");
            state = State.START;
          }
        }
      } else if (state == State.IN_CHARACTER_LITERAL) {
        if (Character.isWhitespace(c)) {
          if (c == '\t') {
            int remaining = charsIntoLine % TAB_WIDTH;
            if (remaining <= MINIMUM_TAB_WIDTH) {
              remaining += 4;
            }
            while (remaining-- > 0) {
              builder.append("&nbsp;");
            }
            charsIntoLine = 0;
          } else if (c == 'r') {
          } else if (c == '\n') {
            builder.append("<br>\n");
            charsIntoLine = 0;
          } else {
            builder.append("&nbsp;");
            charsIntoLine++;
          }
        } else {
          builder.append(c);
          charsIntoLine++;
          if (c == '\'' && code.charAt(i - 1) != '\\') {
            builder.append("</span>");
          }
        }
      } else {
        logger.log(Level.SEVERE, "Invalid state! " + state);
      }
      
      if (endLink == i) {
        builder.append("</a>");
        next = null;
        while (next == null && iter.hasNext()) {
          next = iter.next();
          if (next.getOffset() <= i) {
            logger.log(Level.SEVERE, "Conflicting links: " + next.getLink());
            next = null;
          }
        }
        endLink = -1;
      }

    } 
    return builder.toString();
  }
}
