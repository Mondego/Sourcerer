package edu.uci.ics.sourcerer.clusterer.stats;

import java.io.BufferedWriter;
import java.io.IOException;

public class FqnFragment {
  public String name;
  public FqnFragment parent;
  public FqnFragment[] children;
  public int[] projects;
  
  private FqnFragment() {}
  
  private FqnFragment(String name, FqnFragment parent) {
    this.name = name;
    this.parent = parent;
  }
  
  public static FqnFragment getRootFragment() {
    return new FqnFragment();
  }

  private FqnFragment getChild(String name) {
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
  
  protected FqnFragment addChild(String name, int project) {
    FqnFragment child = getChild(name);
    child.addProject(project);
    return child;
  }

  private void addProject(int project) {
    if (projects == null) {
      projects = new int[1];
      projects[0] = project;
    } else {
      for (int i = 0; i < projects.length; i++) {
        if (projects[i] == 0) {
          projects[i] = project;
          return;
        } else if (projects[i] == project) {
          return;
        }
      }
      int[] newArray = new int[(projects.length * 3) / 2 + 1];
      int i = 0;
      for (; i < projects.length; i++) {
        newArray[i] = projects[i];
      }
      newArray[i] = project;
      projects = newArray;
    }
  }
  
//  protected FqnFragment addChild(String name) {
//    FqnFragment child = children.get(name);
//    if (child == null) {
//      child = new FqnFragment();
//      child.name = name;
//      child.parent = this;
//      children.put(name, child);
//    }
//    return child;
//  }
  
  public FqnFragment getParent() {
    return parent;
  }
  
  public void writeToDisk(BufferedWriter bw) throws IOException {
    if (name != null) {
      bw.write(name);
      for (int project : projects) {
        bw.write(" " + project);
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
}