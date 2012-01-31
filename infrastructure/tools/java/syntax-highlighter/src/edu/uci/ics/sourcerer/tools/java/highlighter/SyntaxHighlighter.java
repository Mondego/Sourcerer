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

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class SyntaxHighlighter {
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
    
    keywords.add("true");
    keywords.add("false");
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

  private static enum TagState {
    START,
    IN_TAG,
    IN_FIELD_HIGHLIGHT,
    WAITING_FOR_IMPORT,
    WAITING_FOR_IMPORT_FQN,
    IN_METHOD,
  }

  public static String highlightSyntax(String code, TagInfo info) {
    State state = State.START;
    TagState tagState = TagState.START;

    HighlightBuilder builder = new HighlightBuilder();

    TagLocation nextTag = null;

    Iterator<TagLocation> iter = info.getLinks().iterator();
    if (iter.hasNext()) {
      nextTag = iter.next();
    }

    int mainAnchor = -1;
    if (info.getMainAnchor() != null) {
      mainAnchor = info.getMainAnchor();
    }
    int wordStart = -1;
    int endtag = -1;

    for (int i = 0; i < code.length(); i++) {
      char c = code.charAt(i);

      if (i == mainAnchor) {
        builder.appendHtmlTag("<a id=\"main\" name=\"main\"></a>");
      }
      if (tagState == TagState.START && nextTag.getOffset() == i) {
        if (state == State.START) {
          if (nextTag.getType() == TagType.TYPE_LINK || nextTag.getType() == TagType.FIELD_LINK) {
            tagState = TagState.IN_TAG;
            builder.appendTag(nextTag);
            endtag = nextTag.getOffset() + nextTag.getLength();
          } else if (nextTag.getType() == TagType.IMPORT_LINK) {
            tagState = TagState.WAITING_FOR_IMPORT;
          } else if (nextTag.getType() == TagType.COLOR) {
            tagState = TagState.IN_FIELD_HIGHLIGHT;
            builder.appendTag(nextTag);
            endtag = nextTag.getOffset() + nextTag.getLength();
          } else if (nextTag.getType() == TagType.METHOD_LINK) {
            tagState = TagState.IN_METHOD;
            builder.appendTag(nextTag);
          } else {
            logger.log(Level.SEVERE, "Unexpected type of link " + nextTag.getLink() + " in " + state.name() + ": " + nextTag.getType());
          }
        } else if (state == State.IN_JAVADOC_COMMENT) {
          if (nextTag.getType() == TagType.TYPE_LINK) {
            tagState = TagState.IN_TAG;
            builder.appendTag(nextTag);
            endtag = nextTag.getOffset() + nextTag.getLength();
          } else {
            logger.log(Level.SEVERE, "Unexpected type of link " + nextTag.getLink() + " in " + state.name() + ": " + nextTag.getType());
          }
        } else if (state == State.IN_ANNOTATION) {
          if (nextTag.getType() == TagType.TYPE_LINK) {
            tagState = TagState.IN_TAG;
            builder.appendTag(nextTag);
            endtag = nextTag.getOffset() + nextTag.getLength();
          } else {
            logger.log(Level.SEVERE, "Unexpected type of link " + nextTag.getLink() + " in " + state.name() + ": " + nextTag.getType());
          }
        } else {
          logger.log(Level.SEVERE, "Unexpected state type for link " + nextTag.getTitle() + ": " + state);
          nextTag = iter.next();
        }
      } else if (endtag == i && tagState == TagState.IN_TAG) {
        if (state == State.IN_WORD) {
          // Make sure the word will end this round
          if (Character.isJavaIdentifierPart(c)) {
            logger.log(Level.SEVERE, "Link ending in middle of word! " + code.substring(wordStart, i));
            builder.appendEndTag(nextTag);
            nextTag = null;
            while (nextTag == null) {
              nextTag = iter.next();
              if (nextTag.getOffset() <= i) {
                logger.log(Level.SEVERE, "Conflicting link " + nextTag.getLink() + " at " + nextTag.getOffset());
                nextTag = null;
              }
            }
            tagState = TagState.START;
            endtag = -1;
          }
        } else {
          builder.appendEndTag(nextTag);
          nextTag = null;
          while (nextTag == null) {
            nextTag = iter.next();
            if (nextTag.getOffset() <= i) {
              logger.log(Level.SEVERE, "Conflicting link " + code.substring(nextTag.getOffset(), nextTag.getOffset() + nextTag.getLength()) + " at " + nextTag.getOffset());
              nextTag = null;
            }
          }
          tagState = TagState.START;
          endtag = -1;
        }
      }

      if (state == State.START) {
        if (c == '/') {
          if (code.length() > i + 1) {
            char c2 = code.charAt(i + 1);
            if (c2 == '/') {
              state = State.IN_LINE_COMMENT;
              builder.appendHtmlTag("<span class=\"comment\">");
              builder.append(c);
            } else if (c2 == '*') {
              if (code.length() > i + 2) {
                char c3 = code.charAt(i + 2);
                if (c3 == '*') {
                  state = State.IN_JAVADOC_COMMENT;
                  builder.appendHtmlTag("<span class=\"javadoc-comment\">");
                  builder.append(c);
                } else {
                  state = State.IN_BLOCK_COMMENT;
                  builder.appendHtmlTag("<span class=\"comment\">");
                  builder.append(c);
                }
              } else {
                logger.log(Level.SEVERE, "Invalid syntax! Opening comment too close to end of file: " + code.substring(i));
                state = State.IN_BLOCK_COMMENT;
                builder.appendHtmlTag("<span class=\"comment\">");
                builder.append(c);
              }
            } else {
              builder.append(c);
            }
          } else {
            logger.log(Level.SEVERE, "Invalid syntax! / At end of file" + code.substring(i));
            builder.append(c);
          }
        } else if (c == '"') {
          state = State.IN_STRING_LITERAL;
          builder.appendHtmlTag("<span class=\"string\">");
          builder.append(c);
        } else if (c == '\'') {
          state = State.IN_CHARACTER_LITERAL;
          builder.appendHtmlTag("<span class=\"character\">");
          builder.append(c);
        } else if (Character.isJavaIdentifierPart(c)) {
          state = State.IN_WORD;
          wordStart = i;
          if (tagState == TagState.WAITING_FOR_IMPORT_FQN) {
            tagState = TagState.IN_TAG;
            builder.appendTag(nextTag);
            endtag = nextTag.getOffset() + nextTag.getLength() - 1;
          }
        } else if (c == '@') {
          state = State.IN_ANNOTATION;
          builder.appendHtmlTag("<span class=\"annotation\">");
          builder.append(c);
        } else {
          builder.append(c);
        }
      } else if (state == State.IN_LINE_COMMENT) {
        if (c == '\n') {
          builder.appendHtmlTag("</span>");
          state = State.START;
        }
        builder.append(c);
      } else if (state == State.IN_BLOCK_COMMENT) {
        builder.append(c);
        if (c == '/' && code.charAt(i - 1) == '*') {
          builder.appendHtmlTag("</span>");
          state = State.START;
        }
      } else if (state == State.IN_JAVADOC_COMMENT) {
        if (c == '/' && code.charAt(i - 1) == '*') {
          builder.append(c);
          builder.appendHtmlTag("</span>");
          state = State.START;
        } else if (c == '@' && Character.isWhitespace(code.charAt(i - 1))) {
          state = State.IN_JAVADOC_TAG;
          builder.appendHtmlTag("<span class=\"javadoc-tag\">");
          builder.append(c);
        } else {
          builder.append(c);
        }
      } else if (state == State.IN_JAVADOC_TAG) {
        if (Character.isJavaIdentifierPart(c)) {
          builder.append(c);
        } else {
          builder.appendHtmlTag("</span>");
          state = State.IN_JAVADOC_COMMENT;
          i--;
        }
      } else if (state == State.IN_WORD) {
        if (!Character.isJavaIdentifierPart(c)) {
          String word = code.substring(wordStart, i);
          if (keywords.contains(word)) {
            if (tagState == TagState.WAITING_FOR_IMPORT) {
              if (word.equals("import")) {
                tagState = TagState.WAITING_FOR_IMPORT_FQN;
              } else {
                logger.log(Level.SEVERE, "Was expecting import, instead got: " + word);
                tagState = TagState.START;
                nextTag = iter.next();
              }
            }
            builder.appendHtmlTag("<span class=\"keyword\">");
            builder.appendWord(word);
            builder.appendHtmlTag("</span>");
          } else {
            builder.appendWord(word);
          }
          if (tagState == TagState.IN_FIELD_HIGHLIGHT || tagState == TagState.IN_METHOD) {
            tagState = TagState.IN_TAG;
            endtag = i;
          }
          state = State.START;
          i--;
        }
      } else if (state == State.IN_STRING_LITERAL) {
        builder.append(c);
        if (c == '"' && code.charAt(i - 1) != '\\') {
          builder.appendHtmlTag("</span>");
          state = State.START;
        }
      } else if (state == State.IN_CHARACTER_LITERAL) {
        builder.append(c);
        if (c == '\'' && code.charAt(i - 1) != '\\') {
          builder.appendHtmlTag("</span>");
          state = State.START;
        }
      } else if (state == State.IN_ANNOTATION) {
        if (Character.isJavaIdentifierPart(c)) {
          builder.append(c);
        } else {
          builder.appendHtmlTag("</span>");
          state = State.START;
          i--;
        }
      } else {
        logger.log(Level.SEVERE, "Invalid state! " + state);
      }
    }

    return builder.toString();
  }
}
