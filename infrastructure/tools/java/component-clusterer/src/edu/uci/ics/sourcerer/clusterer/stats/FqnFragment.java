package edu.uci.ics.sourcerer.clusterer.stats;

import java.io.BufferedWriter;
import java.io.IOException;

public class FqnFragment implements Comparable<FqnFragment> {
  private String name;
  private FqnFragment parent;
  private FqnFragment[] children;
  private int[] projects;
  private int projectCount; 
  
  private FqnFragment() {
    projectCount = 0;
  }
  
  private FqnFragment(String name, FqnFragment parent) {
    this.name = name;
    this.parent = parent;
  }
  
  public static FqnFragment getRootFragment() {
    return new FqnFragment();
  }

  protected FqnFragment addChild(String name) {
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
    FqnFragment child = addChild(name);
    child.addProject(project);
    return child;
  }
  
  protected void addProject(int project) {
    if (projects == null) {
      projects = new int[1];
      projects[0] = project;
      projectCount++;
    } else {
      for (int i = 0; i < projects.length; i++) {
        if (projects[i] == 0) {
          projects[i] = project;
          projectCount++;
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
      projectCount++;
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
  
  public FqnFragment[] getChildren() {
    return children;
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

  public String getFqn() {
    if (parent.name == null) {
      return name;
    } else {
      return parent.getFqn() + "." + name;
    }
  }
  
  public int getProjectCount() {
    return projectCount;
  }
  
  public boolean isTopLevelClass() {
    return children == null && name.indexOf('$') == -1;
  }
  
  @Override
  public int compareTo(FqnFragment o) {
    return projectCount - o.projectCount;
  }
}