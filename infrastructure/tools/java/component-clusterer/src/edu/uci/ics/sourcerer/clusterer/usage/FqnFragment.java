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
package edu.uci.ics.sourcerer.clusterer.usage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;

public class FqnFragment {
  private String name;
  private FqnFragment parent;
  private FqnFragment[] children;
  private int[] ids;
  private int idCount;
  private int referenceCount;
  
  private FqnFragment() {
    idCount = 0;
    referenceCount = 0;
  }
  
  private FqnFragment(String name, FqnFragment parent) {
    idCount = 0;
    referenceCount = 0;
    this.name = name;
    this.parent = parent;
  }
  
  public static FqnFragment getRootFragment() {
    return new FqnFragment();
  }

  protected FqnFragment getChild(String name) {
    if (children == null) {
      children = new FqnFragment[1];
      children[0] = new FqnFragment(name, this);
      return children[0];
    } else {
      for (int i = 0; i < children.length; i++) {
        if (children[i] == null) {
          children[i] = new FqnFragment(name, this);
          return children[i];
        } else if (children[i].name.equals(name)) {
          return children[i];
        }
      }
      FqnFragment[] newArray = new FqnFragment[(children.length * 3) / 2 + 1];
      int i = 0;
      for (; i < children.length; i++) {
        newArray[i] = children[i];
      }
      newArray[i] = new FqnFragment(name, this);
      children = newArray;
      return children[i];
    }
  }
  
  protected FqnFragment addChild(String name, int id) {
    FqnFragment child = getChild(name);
    child.addID(id);
    return child;
  }
  
  protected void addID(int id) {
    referenceCount++;
    if (ids == null) {
      ids = new int[1];
      ids[0] = id;
    } else {
      for (int i = 0; i < ids.length; i++) {
        if (ids[i] == 0) {
          ids[i] = id;
          idCount++;
          return;
        } else if (ids[i] == id) {
          return;
        }
      }
      int[] newArray = new int[(ids.length * 3) / 2 + 1];
      int i = 0;
      for (; i < ids.length; i++) {
        newArray[i] = ids[i];
      }
      newArray[i] = id;
      ids = newArray;
      idCount++;
    }
  }
  
  protected void setReferenceCount(int count) {
    referenceCount = count;
  }
  
  public int getReferenceCount() {
    return referenceCount;
  }
    
  public FqnFragment getParent() {
    return parent;
  }
  
  public FqnFragment[] getChildren() {
    return children;
  }
  
  public void writeToDisk(BufferedWriter bw) throws IOException {
    if (name != null) {
      bw.write(name);
      bw.write(" " + referenceCount);
      for (int id : ids) {
        bw.write(" " + id);
      }
      bw.write("\n");
    }
    if (children != null) {
      bw.write("+\n");
      for (FqnFragment child : children) {
        if (child != null) {
          child.writeToDisk(bw);
        }
      }
      bw.write("-\n");
    }
  }

  public String getFqn() {
    if (parent == null) {
      if (name == null) {
        return "";
      } else {
        return name;
      }
    } else if (parent.name == null) {
      return name;
    } else {
      return parent.getFqn() + "." + name;
    }
  }
  
  public static Comparator<FqnFragment> getReferenceComparator() {
    return new Comparator<FqnFragment>() {
      @Override
      public int compare(FqnFragment o1, FqnFragment o2) {
        if (o1 == o2) {
          return 0;
        } else if (o1.referenceCount == o2.referenceCount) {
          return o1.name.compareTo(o2.name);
        } else {
          return o1.referenceCount - o2.referenceCount;
        }
      }};
  }
}