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
package edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal;

import java.io.File;

import edu.uci.ics.sourcerer.tools.java.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.CommentWriter;
import edu.uci.ics.sourcerer.tools.java.model.types.Comment;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentWriterImpl extends AbstractExtractorWriter<CommentEX> implements CommentWriter {
  public CommentWriterImpl(File output) {
    super(new File(output, CommentEX.COMMENT_FILE.getValue()), CommentEX.class);
  }
  
  public void writeComment(CommentEX comment) {
    write(comment);
  }
  
  private CommentEX trans = new CommentEX();
  @Override
  public void writeComment(Comment type, Location location) {
    write(trans.update(type, location));
  }
  
  @Override
  public void writeComment(Comment type, String fqn, Location location) {
    write(trans.update(type, fqn, location));
  }
}
