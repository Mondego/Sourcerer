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
package edu.uci.ics.sourcerer.tools.java.component.model.fqn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import edu.uci.ics.sourcerer.util.io.InvalidFileFormatException;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractFqnNode<T extends AbstractFqnNode<T>> implements Comparable<T> {
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
  
  protected final T lookupChild(String name) {
    for (T child = firstChild; child != null; child = child.sibling) {
      int cmp = child.name.compareTo(name);
      if (cmp == 0) {
        return child;
      } else if (cmp > 0) {
        return null;
      }
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public final T lookup(String fqn, char sep) {
    T fragment = (T) this;
    int start = 0;
    int sepIdx = fqn.indexOf(sep);
    while (sepIdx != -1 && fragment != null) {
      fragment = fragment.getChild(fqn.substring(start, sepIdx));
      start = sepIdx + 1;
      sepIdx = fqn.indexOf(sep, start);
    }
    if (fragment == null) {
      return null;
    } else { 
      return fragment.getChild(fqn.substring(start));
    }
  }
  
  @SuppressWarnings("unchecked")
  public final T lookup(AbstractFqnNode<?> fqn) {
    Deque<String> stack = new LinkedList<>();
    for (AbstractFqnNode<?> fragment = fqn; fragment != null; fragment = fragment.parent) {
      stack.push(fragment.name);
    } 
    T fragment = (T) this;
    while (!stack.isEmpty() && fragment != null) {
      fragment = fragment.lookupChild(stack.pop());
    }
    return fragment;
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
      return "(root)";
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
  
  public Iterable<T> getLeavesIterable() {
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
            if (node == null) {
              throw new NoSuchElementException();
            } else {
              T next = node;
              // We're currently at a leaf
              if (node.sibling != null) {
                // Does this leaf have a sibling?
                // Look at the sibling, find the lowest child
                for (node = node.sibling; node.firstChild != null; node = node.firstChild);
              } else {
                // This leaf doesn't have a sibling, so we need to walk up the tree
                // find the first parent with a sibling
                for (node = node.parent; node != null && node.sibling == null; node = node.parent);
                if (node != null) {
                  // find the first child of this sibling
                  for (node = node.sibling; node.firstChild != null; node = node.firstChild);
                }
              }
              return next;
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }};
  }
  
  public Iterable<T> getPreOrderIterable() {
    return new Iterable<T>() {
      @SuppressWarnings("unchecked")
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          T node = (T) AbstractFqnNode.this;

          @Override
          public boolean hasNext() {
            return node != null;
          }

          @Override
          public T next() {
            if (node == null) {
              throw new NoSuchElementException();
            } else {
              T next = node;
              if (node.firstChild != null) {
                node = node.firstChild;
              } else {
                for (; node != null; node = node.parent) {
                  if (node.sibling != null) {
                    node = node.sibling;
                    break;
                  }
                }
              }
              return next;
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
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
            if (node == null) {
              throw new NoSuchElementException();
            } else {
              T next = node;
              if (node.sibling != null) {
                for (node = node.sibling; node.firstChild != null; node = node.firstChild);
              } else {
                node = node.parent;
              }
              return next;
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public abstract class Saver {
    protected Saver() {}

    protected abstract void save(BufferedWriter writer, T node) throws IOException;
    
    public void save(BufferedWriter writer) throws IOException {
      Map<T, Integer> nodes = new HashMap<>();
      int count = 0;
      for (T node : getPreOrderIterable()) {
        nodes.put(node, count);
        // Write the node name, and it's parent's id
        writer.write(node.name + " " + (node.parent == null ? "null" : nodes.get(node.parent)));
        // Save the extra node information
        save(writer, node);
        writer.newLine();
        count++;
      }
    }
  }
 
  public abstract class Loader {
    protected Loader() {}
    
    protected abstract void load(Scanner scanner, T node);
    
    @SuppressWarnings("unchecked")
    public void load(BufferedReader reader) throws IOException {
      TaskProgressLogger task = TaskProgressLogger.get();
      task.start("Loading prefix tree", "nodes loaded", 1_000_000);
      ArrayList<T> nodes = new ArrayList<>();
      T lastNode = null;
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        Scanner lineScanner = new Scanner(line);
        T node = null;
        // Special check for the root node
        if (nodes.isEmpty()) {
          // Get the name
          String next = lineScanner.next();
          if ("null".equals(next)) {
            node = (T) AbstractFqnNode.this;
          } else {
            throw new InvalidFileFormatException("Expected null for root name, received " + name);
          }
          // Check the parent info
          next = lineScanner.next();
          if (!"null".equals(next)) {
            throw new InvalidFileFormatException("Expected null for root parent, received " + name);
          }
        } else {
          String name = lineScanner.next();
          T parent = null;
          if (lineScanner.hasNextInt()) {
            parent = nodes.get(lineScanner.nextInt());
          } else {
            throw new InvalidFileFormatException("Expected number for node parent, received " + lineScanner.next());
          }
          node = create(name, parent);
          // Hook it up properly
          if (lastNode.parent == parent) {
            lastNode.sibling = node;
          } else if (parent.firstChild == null){
            parent.firstChild = node;
          } else {
            for (lastNode = parent.firstChild; lastNode.sibling != null; lastNode = lastNode.sibling);
            lastNode.sibling = node;
          }
        }
        lastNode = node;
        nodes.add(node);
        load(lineScanner, node);
        task.progress();
      }
      task.finish();
    }
  }

  @Override
  public String toString() {
    return getFqn();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public int compareTo(T other) {
//    return getFqn().compareTo(other.getFqn());
    // Find the first overlapping parent
    // Can do this poorly, since the tree is never deep
    if (this == other) {
      return 0;
    } else {
      T previousMe = null;
      for (T me = (T) this; me != null; me = me.parent) {
        T previousHim = null;
        for (T him = other; him != null; him= him.parent) {
          if (me == him) {
            if (previousMe == null) {
              return -1;
            } else if (previousHim == null) {
              return 1;
            }
            // Which child comes first in the tree?
            for (T child = me.firstChild; child != null; child = child.sibling) {
              if (child == previousMe) {
                return -1;
              } else if (child == previousHim) {
                return 1;
              }
            }
            throw new IllegalStateException("Impossible: " + this + " and " + other);
          }
          previousHim = him;
        }
        previousMe = me;
      }
      throw new IllegalStateException("Uncomparable: " + this + " and " + other);
    }
  }
}
