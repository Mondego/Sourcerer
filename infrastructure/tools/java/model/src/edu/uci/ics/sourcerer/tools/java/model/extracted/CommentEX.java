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
package edu.uci.ics.sourcerer.tools.java.model.extracted;

import edu.uci.ics.sourcerer.tools.java.model.types.Comment;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentEX implements SimpleSerializable {
  public static final Argument<String> COMMENT_FILE = new StringArgument("comment-file", "comments.txt", "Filename for the extracted comments.").permit();
  
  private Comment type;
  private String fqn;
  private Location location;
  
  public CommentEX() {}
  
  public CommentEX(Comment type, Location location) {
   this.type = type;
   this.location = location;
  }
  
  public CommentEX(Comment type, String fqn, Location location) {
    this.type = type;
    this.fqn = fqn;
    this.location = location;
  }
  
  public CommentEX update(Comment type, Location location) {
    this.type = type;
    this.fqn = null;
    this.location = location;
    return this;
  }
  
  public CommentEX update(Comment type, String fqn, Location location) {
    this.type = type;
    this.fqn = fqn;
    this.location = location;
    return this;
  }

  public Comment getType() {
    return type;
  }
  
  public String getFqn() {
    return fqn;
  }

  public Location getLocation() {
    return location;
  }
  
  public String toString() {
    return type.name() + " " + location;
  }
}
