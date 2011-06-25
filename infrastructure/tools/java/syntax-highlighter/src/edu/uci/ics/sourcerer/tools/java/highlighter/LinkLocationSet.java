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
public class LinkLocationSet {
  private TreeMap<Integer, LinkLocation> links;
  
  private LinkLocationSet() {
    links = Helper.newTreeMap();
  }
  
  public static LinkLocationSet make() {
    return new LinkLocationSet();
  }
  
  public void addLinkLocation(Integer offset, Integer length, String link) {
    if (links.containsKey(offset)) {
      LinkLocation other = links.get(offset);
      if (other.getLength().equals(length)) {
        throw new IllegalArgumentException("Set already contains link at offset: " + offset);
      } else {
        if (other.getLength() < length) {
          Integer newOffset = offset += other.getLength();
          Integer newLength = length - other.getLength();
          if (links.containsKey(newOffset)) {
            throw new IllegalArgumentException("Set already contains link at offset: " + offset);
          } else {
            links.put(newOffset, new LinkLocation(newOffset, newLength, link));
          }
        } else {
          Integer newOffset = offset + length;
          Integer newLength = other.getLength() - length;
          if (links.containsKey(newOffset)) {
            throw new IllegalArgumentException("Set already contains link at offset: " + offset);
          } else {
            links.put(newOffset, new LinkLocation(newOffset, newLength, other.getLink()));
            links.put(offset, new LinkLocation(offset, length, link));
          }
        }
      }
    } else {
      links.put(offset, new LinkLocation(offset, length, link));
    }
  }
  
  protected Collection<LinkLocation> getLinks() {
    return links.values();
  }
}
