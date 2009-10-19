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
package edu.uci.ics.sourcerer.extractor.io.file;

import edu.uci.ics.sourcerer.extractor.io.ICommentWriter;
import edu.uci.ics.sourcerer.model.Comment;
import edu.uci.ics.sourcerer.model.extracted.CommentExParser;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentWriter extends ExtractorWriter implements ICommentWriter {
  public static final Property<String> COMMENT_FILE = new StringProperty("comment-file", "comments.txt", "Extractor Output", "Filename for extracted comments.");
  
  public CommentWriter(Repository input) {
    super(input, COMMENT_FILE);
  }

  public void writeLineComment(String containingFile, int startPos, int length) {
    write(CommentExParser.getLine(Comment.LINE, convertToRelativePath(containingFile), startPos, length));
  }
  
  public void writeBlockComment(String containingFile, int startPos, int length) {
    write(CommentExParser.getLine(Comment.BLOCK, convertToRelativePath(containingFile), startPos, length));
  }
  
  public void writeUnassociatedJavadocComment(String containingFile, int startPos, int length) {
    write(CommentExParser.getLine(Comment.UJAVADOC, convertToRelativePath(containingFile), startPos, length));
  }
  
  public void writeJavadocComment(String containingFqn, int startPos, int length) {
    write(CommentExParser.getLine(Comment.JAVADOC, containingFqn, startPos, length));
  }
}
