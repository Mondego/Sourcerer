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
package edu.uci.ics.sourcerer.tools.java.utilization.model.fqn;

import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractFqnNode<T extends AbstractFqnNode<T>> {
  protected final String name;
  protected final T parent;
  protected T sibling;
  protected T firstChild;
  
  protected AbstractFqnNode(String name, T parent) {
    this.name = name;
    this.parent = parent;
  }
  
  protected abstract T create(String name, AbstractFqnNode<?> parent);
    
  protected final T getChild(String name) {
    T previousChild = null;
    for (T child = firstChild; child != null; child = child.sibling) {
      int cmp = child.name.compareTo(name);
      if (cmp == 0) {
        return child;
      } else if (cmp > 0) {
        T newChild = create(name, this);
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
    T newChild = create(name, this);
    if (previousChild == null) {
      firstChild = newChild;
    } else {
      previousChild.sibling = newChild;
    }
    return newChild;
  }
  
  @SuppressWarnings("unchecked")
  public final T getChild(String fqn, char sep) {
    T fragment = (T) this;
    int start = 0;
    int sepIdx = fqn.indexOf(sep);
    while (sepIdx != -1) {
      fragment = fragment.getChild(fqn.substring(start, sepIdx));
      start = sepIdx + 1;
      sepIdx = fqn.indexOf(sep, start);
    }
    return fragment.getChild(fqn.substring(start));
  }
  
  public final T getParent() {
    return parent;
  }
  
  public final boolean hasChildren() {
    return firstChild != null;
  }
  
  public final Iterable<T> getChildren() {
    if (firstChild == null) {
      return Collections.emptyList();
    } else {
      return new Iterable<T>() {
        @Override
        public Iterator<T> iterator() {
          return new Iterator<T>() {
            T child = firstChild;
            @Override
            public boolean hasNext() {
              return child != null;
            }

            @Override
            public T next() {
              T next = child;
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
  
  public final String getName() {
    return name;
  }
  
  public final String getFqn() {
    if (parent == null) {
      return null;
    } else {
      Deque<T> stack = new LinkedList<>();
      T node = parent;
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
  
  public Iterable<T> getPostOrderIterable() {
    return new Iterable<T>() {
      @SuppressWarnings("unchecked")
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          T node = null;
          {
            for (node = (T) AbstractFqnNode.this; node.firstChild != null; node = node.firstChild);
          }
          
          @Override
          public boolean hasNext() {
            return node != null;
          }

          @Override
          public T next() {
            T next = node;
            if (node == AbstractFqnNode.this) {
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
