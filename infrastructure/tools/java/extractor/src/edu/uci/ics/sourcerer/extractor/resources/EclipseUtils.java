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
package edu.uci.ics.sourcerer.extractor.resources;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

import edu.uci.ics.sourcerer.repo.IndexedJar;
import edu.uci.ics.sourcerer.repo.base.IJarFile;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EclipseUtils {
  private static final String projectName = "_EXTRACTOR_";
  
  private static IFolder srcFolder = null;
  private static IProject project = null;
  private static IJavaProject javaProject = null;
  private static IProgressMonitor monitor = new NullProgressMonitor();

  public static void initialize() {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceDescription desc = workspace.getDescription();
    desc.setAutoBuilding(false);
    try {
      workspace.setDescription(desc);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    
    IWorkspaceRoot root = workspace.getRoot();
    project = root.getProject(projectName);
    
    try {
      if (!project.exists()) {
        project.create(monitor);
        project.open(monitor);
        IProjectDescription description = project.getDescription();
        String[] prevNatures= description.getNatureIds();
        String[] newNatures= new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length]= JavaCore.NATURE_ID;
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
        project.setDefaultCharset("US-ASCII", monitor);
      } else {
        project.open(monitor);
      }
      
      javaProject = JavaCore.create(project);
    } catch (CoreException e) {
      logger.log(Level.SEVERE, "Error in project initialization", e);
    }
  }
  
  public static Collection<IPath> getLibraryJars() {
    Collection<IPath> libraryJars = Helper.newLinkedList();
    
    IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
    for (LibraryLocation location : JavaRuntime.getLibraryLocations(vmInstall)) {
      libraryJars.add(location.getSystemLibraryPath());
    }
    
    return libraryJars;
  }
  
  public static void initializeJarProject(IndexedJar jar) {
    try {
      IClasspathEntry entries[] = new IClasspathEntry[1];
      entries[0] = JavaCore.newLibraryEntry(new Path(jar.getPath()), null, null);
      javaProject.setRawClasspath(entries, monitor);
    } catch (JavaModelException e) {
      logger.log(Level.SEVERE, "Unable to initialize jar project", e);
    }
  }
  
  public static void initializeLibraryProject(IPath libraryJar) {
    try {
      IClasspathEntry entries[] = new IClasspathEntry[1];
      entries[0] = JavaCore.newLibraryEntry(libraryJar, null, null);
      javaProject.setRawClasspath(entries, monitor);
    } catch (JavaModelException e) {
      logger.log(Level.SEVERE, "Unable to initialize jar project", e);
    }
  }
  
  public static void initializeProject(Iterable<IJarFile> jarFiles) {
    try {
      srcFolder = project.getFolder("src");
      cleanProject();

      if (!srcFolder.exists()) {
        srcFolder.create(true, true, null);
      }
      
      IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
      List<IClasspathEntry> entries = Helper.newArrayList();//new IClasspathEntry[locations.length + jarFiles.size() + 1];
      for (LibraryLocation location : JavaRuntime.getLibraryLocations(vmInstall)) {
        entries.add(JavaCore.newLibraryEntry(location.getSystemLibraryPath(), location.getSystemLibrarySourcePath(), null));
      }
      for (IJarFile file : jarFiles) {
        entries.add(JavaCore.newLibraryEntry(new Path(file.getPath()), null, null));
      }
      entries.add(JavaCore.newSourceEntry(srcFolder.getFullPath()));
      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
    } catch (CoreException e) {
      logger.log(Level.SEVERE, "Error in project initialization", e);
    }
  }
  
//  public static void buildProject() {
//    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//    IProject project = root.getProject(projectName);
//    try {
//      project.build(IncrementalProjectBuilder.FULL_BUILD, null);
//    } catch (CoreException e) {
//      logger.log(Level.SEVERE, "Unable to build project", e);
//    }
//  }
  
  public static Collection<IFile> loadFilesIntoProject(Iterable<IJavaFile> files) {
    Collection<IFile> set = Helper.newLinkedList();
    int fileCount = 0;
    for (IJavaFile file : files) {
      IFile iFile = loadFileIntoProject(file, false);
      if (iFile != null) {
        if (++fileCount % 1000 == 0) {
          logger.info(fileCount + " files loaded.");
        }
        set.add(iFile);
      } else {
        logger.log(Level.SEVERE, "Unable to load: " + file.getPath());
      }
    }
    logger.info(fileCount + " files loaded.");
    return set;
  }
  
  public static IFile loadFileIntoProject(IJavaFile file, boolean replace) {
    try {
      IFile newFile = getIFileFromFile(file);
      if (newFile != null) {
        newFile.createLink(new Path(file.getPath()), replace ? IFile.REPLACE : IFile.NONE, null);
        return newFile;
      } else {
        return null;
      }
    } catch (CoreException e) {
      return null;
    }
  }
  
  private static IFile getIFileFromFile(IJavaFile file) {
    IFolder folder = getFolderFromPackage(srcFolder, file.getPackage());
    if (folder == null) {
      return null;
    } else {
      return folder.getFile(file.getName());
    }
  }
  
  private static IFolder getFolderFromPackage(IFolder root, String pkg) {
    if (pkg == null) {
      return srcFolder;
    } else {
      for (String part : pkg.split("\\.")) {
        root = root.getFolder(part);
        if (!root.exists()) {
          try {
            root.create(true, true, null);
          } catch (CoreException e) {
            e.printStackTrace();
          }
        }
      }
      return root;
    }
  }
  
  public static Collection<IClassFile> getClassFiles() {
    try {
      Collection<IClassFile> classFiles = Helper.newLinkedList();
      Deque<IPackageFragment> fragments = Helper.newStack();
      
      for (IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
        for (IJavaElement child : root.getChildren()) {
          if (child.getElementType() == IJavaProject.PACKAGE_FRAGMENT) {
            fragments.push((IPackageFragment)child); 
          } else if (child.getElementType() == IJavaProject.CLASS_FILE) {
            classFiles.add((IClassFile)child);
          }
        }
      }
      
      while (!fragments.isEmpty()) {
        for (IJavaElement child : fragments.pop().getChildren()) {
          if (child.getElementType() == IJavaProject.PACKAGE_FRAGMENT) {
            fragments.push((IPackageFragment)child); 
          } else if (child.getElementType() == IJavaProject.CLASS_FILE) {
            classFiles.add((IClassFile)child);
          }
        }
      }
      
      return classFiles;
    } catch (JavaModelException e) {
      logger.log(Level.SEVERE, "Unable to get class files", e);
      return Collections.emptySet();
    }
  }

//  public static CompilationUnit getCompilationUnit(JavaFile file, boolean doPPA) {
//    return getCompilationUnit(getIFileFromFile(file), doPPA);
//  }
  
//  public static CompilationUnit getCompilationUnit(IFile file, boolean doPPA) {
//    if (doPPA) {
//      return null;
////      return PPAUtil.getCU(file, new PPAOptions());
//    } else {
//      ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
//      ASTParser parser = ASTParser.newParser(AST.JLS3);
//      parser.setStatementsRecovery(true);
//      parser.setResolveBindings(true);
//      parser.setSource(icu);
//      ASTNode node = parser.createAST(null);
//      if (node.getNodeType() == ASTNode.COMPILATION_UNIT) {
//        return (CompilationUnit) node;
//      } else {
//        return null;
//      }
//    } 
//  }
  
//  public static Collection<IFile> getIFilesFromJavaFiles(Iterable<JavaFile> files) {
//    List<IFile> fileSet = Helper.newLinkedList();
//    for (JavaFile file : files) {
//      IFile iFile = getIFileFromFile(file);
//      if (iFile != null) {
//        fileSet.add(iFile);
//      } else {
//        logger.log(Level.SEVERE, "Unable to get iFile for " + file.getPath());
//      }
//    }
//    return fileSet;
//  }
  
//  public static ReferenceExtractorASTRequestor getCompilationUnits(Collection<IFile> files, boolean doPPA) {
//    return new ReferenceExtractorASTRequestor(files, doPPA);
//    if (doPPA) {
//      ReferenceExtractorASTRequestor requestor = new ReferenceExtractorASTRequestor(files, doPPA);
//      
//      for (IFile file : files) {
//        CompilationUnit cu = PPAUtil.getCU(file, new PPAOptions());
//        requestor.acceptAST((ICompilationUnit)cu.getJavaElement(), cu);
//      }
//
//      return requestor;
//    } else {
//      ICompilationUnit[] icus = new ICompilationUnit[files.size()];
//      int index = 0;
//      for (IFile file : files) {
//        icus[index++] = JavaCore.createCompilationUnitFrom(file);
//      }
//      
//      ReferenceExtractorASTRequestor requestor = new ReferenceExtractorASTRequestor();
//      
//      ASTParser parser = ASTParser.newParser(AST.JLS3);
//      parser.setStatementsRecovery(true);
//      parser.setResolveBindings(true);
//      parser.setBindingsRecovery(true);
//      parser.setProject(icus[0].getJavaProject());
//      parser.createASTs(icus, new String[0], requestor, null);
//      
//      return requestor;
//    }
//  }

//  public static void cleanAll() {
//    try {
//      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//      IProject project = root.getProject(projectName);
//      if (project.exists()) {
//        project.refreshLocal(IResource.DEPTH_INFINITE, null);
//        project.delete(true, true, null);
//      }
//    } catch (CoreException e) {
//      logger.log(Level.SEVERE, "Error in project deletion", e);
//    }
//  }
//  
  public static void cleanProject() {
    try {
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject project = root.getProject(projectName);
      if (project.exists()) {
        final Set<IResource> resources = Helper.newHashSet();
        IFolder folder = project.getFolder("src");
        if (folder.exists()) {
          project.getFolder("src").accept(new IResourceVisitor() {
            @Override
            public boolean visit(IResource resource) throws CoreException {
              if (resource.getType() == IResource.FILE || resource.getType() == IResource.FOLDER) {
                resources.add(resource);
                return false;
              } else {
                return true;
              }
            }
          });
          for (IResource resource : resources) {
            resource.delete(true, null);
          }
        }
//        srcFolder.create(true, true, null);
      }
    } catch (CoreException e) {
      logger.log(Level.SEVERE, "Error in project cleaning", e);
    }
  }
}
