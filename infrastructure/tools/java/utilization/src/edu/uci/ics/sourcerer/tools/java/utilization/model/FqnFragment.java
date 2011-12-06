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
package edu.uci.ics.sourcerer.tools.java.utilization.model;

import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnFragment {
  private final String name;
  private final FqnFragment parent;
  private FqnFragment sibling;
  private FqnFragment firstChild;
  private JarSetMap jars;
  
  private FqnFragment(String name, FqnFragment parent) {
    this.name = name;
    this.parent = parent;
    this.jars = JarSetMap.makeEmpty();
  }
  
  static FqnFragment makeRoot() {
    return new FqnFragment(null, null);
  }
  
  FqnFragment getFragment(String fqn, char sep) {
    FqnFragment fragment = this;
    int start = 0;
    int sepIdx = fqn.indexOf(sep);
    while (sepIdx != -1) {
      fragment = fragment.getChild(fqn.substring(start, sepIdx));
      start = sepIdx + 1;
      sepIdx = fqn.indexOf(sep, start);
    }
    return fragment.getChild(fqn.substring(start));
  }
  
  private FqnFragment getChild(String name) {
    FqnFragment previousChild = null;
    for (FqnFragment child = firstChild; child != null; child = child.sibling) {
      int cmp = child.name.compareTo(name);
      if (cmp == 0) {
        return child;
      } else if (cmp > 0) {
        FqnFragment newChild = new FqnFragment(name, this);
        newChild.sibling = child;
        if (previousChild == null) {
          firstChild = newChild;
        } else {
          previousChild.sibling = newChild;
        }
        return newChild;
      }
      previousChild = child;
    }
    FqnFragment newChild = new FqnFragment(name, this);
    if (previousChild == null) {
      firstChild = newChild;
    } else {
      previousChild.sibling = newChild;
    }
    return newChild;
  }
  
  void addJar(Jar jar, Fingerprint fingerprint) {
    jars = jars.add(jar, fingerprint);
  }
  
  public FqnFragment getParent() {
    return parent;
  }
  
  public boolean hasChildren() {
    return firstChild != null;
  }
  
  public Iterable<FqnFragment> getChildren() {
    if (firstChild == null) {
      return Collections.emptyList();
    } else {
      return new Iterable<FqnFragment>() {
        @Override
        public Iterator<FqnFragment> iterator() {
          return new Iterator<FqnFragment>() {
            FqnFragment child = firstChild;
            @Override
            public boolean hasNext() {
              return child != null;
            }

            @Override
            public FqnFragment next() {
              FqnFragment next = child;
              child = child.sibling;
              return next;
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
        }
      };
    }
  }
  
  public String getName() {
    return name;
  }
  
  public JarSetMap getJars() {
    return jars;
  }
  
  public String getFqn() {
    if (parent == null) {
      return null;
    } else {
      Deque<FqnFragment> stack = new LinkedList<>();
      FqnFragment node = parent;
      while (node.parent != null) {
        stack.push(node);
        node = node.parent;
      }
      StringBuilder fqn = new StringBuilder();
      while (!stack.isEmpty()) {
        fqn.append(stack.pop().name).append(".");
      }
      fqn.append(name);
      return fqn.toString();
    }
  }
  
  public Iterable<FqnFragment> getPostOrderIterable() {
    return new Iterable<FqnFragment>() {
      @Override
      public Iterator<FqnFragment> iterator() {
        return new Iterator<FqnFragment>() {
          FqnFragment node = null;
          {
            for (node = FqnFragment.this; node.firstChild != null; node = node.firstChild);
          }
          
          @Override
          public boolean hasNext() {
            return node != null;
          }

          @Override
          public FqnFragment next() {
            FqnFragment next = node;
            if (node == FqnFragment.this) {
              node = null;
            } else if (node.sibling != null) {
              for (node = node.sibling; node.firstChild != null; node = node.firstChild);
            } else {
              node = node.parent;
            }
            return next;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
  
  @Override
  public String toString() {
    return getFqn();
  }
}
