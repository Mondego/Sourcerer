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
package edu.uci.ics.sourcerer.extractor.io.internal;

import java.io.File;

import edu.uci.ics.sourcerer.extractor.io.CommentWriter;
import edu.uci.ics.sourcerer.model.Comment;
import edu.uci.ics.sourcerer.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentWriterImpl extends ExtractorWriter implements CommentWriter {
  public CommentWriterImpl(File output, IFileSet input) {
    super(new File(output, Extracted.COMMENT_FILE.getValue()), input);
  }

  public void writeLineComment(String containingFile, int startPos, int length) {
    write(CommentEX.getUnlinkedLine(Comment.LINE, convertToRelativePath(containingFile), startPos, length));
  }
  
  public void writeBlockComment(String containingFile, int startPos, int length) {
    write(CommentEX.getUnlinkedLine(Comment.BLOCK, convertToRelativePath(containingFile), startPos, length));
  }
  
  public void writeUnassociatedJavadocComment(String containingFile, int startPos, int length) {
    write(CommentEX.getUnlinkedLine(Comment.UJAVADOC, convertToRelativePath(containingFile), startPos, length));
  }
  
  public void writeJavadocComment(String containingFqn, String containingFile, int startPos, int length) {
    write(CommentEX.getLinkedLine(Comment.JAVADOC, containingFqn, convertToRelativePath(containingFile), startPos, length));
  }
}
