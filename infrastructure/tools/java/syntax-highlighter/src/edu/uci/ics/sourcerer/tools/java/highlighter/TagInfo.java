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

import java.util.Collection;
import java.util.TreeMap;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class TagInfo {
  private Integer mainAnchor;
  private TreeMap<Integer, TagLocation> links;
  
  private TagInfo() {
    links = Helper.newTreeMap();
    addLinkLocation(TagType.TYPE_LINK, Integer.MAX_VALUE, 0, null, null, null);
  }
  
  public static TagInfo make() {
    return new TagInfo();
  }

  public boolean containsLink(Integer offset, Integer length) {
    TagLocation ll = links.get(offset);
    if (ll == null) {
      return false;
    } else {
      return ll.getLength().equals(length);
    }
  }
  
  public void setMainAnchorLocation(Integer offset) {
    mainAnchor = offset;
  }
  
  protected Integer getMainAnchor() {
    return mainAnchor;
  }
  
  public void addColorLocation(Integer offset, Integer length, String klass) {
    if (links.containsKey(offset)) {
      throw new IllegalArgumentException("Set already contains link at offset: " + offset);
    } else {
      links.put(offset, new TagLocation(TagType.COLOR, offset, length, klass, null, null));
    }
  }
  
  public void addLinkLocation(TagType type, Integer offset, Integer length, String klass, String link, String title) {
    if (links.containsKey(offset)) {
      throw new IllegalArgumentException("Set already contains link at offset: " + offset + " " + type.name() + "(" + link + ") vs " + links.get(offset).getType().name() + "(" + links.get(offset).getLink() + ")");
    } else if (type.isLinkType()) {
      links.put(offset, new TagLocation(type, offset, length, klass, link, title));
    } else {
      throw new IllegalArgumentException(type + " is not a link type");
    }
  }
  
  protected Collection<TagLocation> getLinks() {
    return links.values();
  }
}
