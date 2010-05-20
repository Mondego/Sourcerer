package edu.uci.ics.sourcerer.clusterer.stats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.util.Helper;

public class FqnFragment {
  public String name;
  public FqnFragment parent;
  public Map<String, FqnFragment> children;
  public Set<Project> projects;

  public FqnFragment() {
    projects = Helper.newHashSet();
  }

  protected FqnFragment addChild(String name, Project project) {
    if (children == null) {
      children = Helper.newHashMap();
    }
    FqnFragment child = children.get(name);
    if (child == null) {
      child = new FqnFragment();
      child.name = name;
      child.parent = this;
      children.put(name, child);
    }
    child.addProject(project);
    return child;
  }

  protected void addProject(Project project) {
    projects.add(project);
    project.addFqn(this);
  }
  
  protected FqnFragment addChild(String name) {
    FqnFragment child = children.get(name);
    if (child == null) {
      child = new FqnFragment();
      child.name = name;
      child.parent = this;
      children.put(name, child);
    }
    return child;
  }
  
  public FqnFragment getParent() {
    return parent;
  }
  
  public void writeToDisk(BufferedWriter bw) throws IOException {
    if (name != null) {
      bw.write(name);
      for (Project project : projects) {
        bw.write(" " + project.getID());
      }
      bw.write("\n");
    }
    if (!children.isEmpty()) {
      bw.write("+\n");
      for (FqnFragment child : children.values()) {
        child.writeToDisk(bw);
      }
      bw.write("-\n");
    }
  }
}