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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class TagLocation {
  private TagType type;
  private Integer offset;
  private Integer length;
  private String klass;
  private String link;
  private String title;
  
  TagLocation(TagType type, Integer offset, Integer length, String klass, String link, String title) {
    this.type = type;
    this.offset = offset;
    this.length = length;
    this.klass = klass;
    this.link = link;
    this.title = title;
  }
  
  TagType getType() {
    return type;
  }

  Integer getOffset() {
    return offset;
  }

  Integer getLength() {
    return length;
  }
  
  String getKlass() {
    return klass;
  }
  
  String getLink() {
    return link;
  }
  
  String getTitle() {
    return title;
  }
}
