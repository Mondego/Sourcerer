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
package edu.uci.ics.sourcerer.tools.java.extractor.eclipse;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import edu.uci.ics.sourcerer.tools.java.model.extracted.io.EntityWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.FileWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.LocalVariableWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.RelationWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClassFileExtractor {
  private FileWriter fileWriter;
  private EntityWriter entityWriter;
  private LocalVariableWriter localVariableWriter;
  private RelationWriter relationWriter;
  
  private String name;
  private String path;
  
  private Deque<String> fqnStack;
  
  public ClassFileExtractor(WriterBundle writers) {
    fileWriter = writers.getFileWriter();
    entityWriter = writers.getEntityWriter();
    localVariableWriter = writers.getLocalVariableWriter();
    relationWriter = writers.getRelationWriter();
    fqnStack = new LinkedList<>();
  }
  
  public static boolean isTopLevelOrAnonymous(IClassFile classFile) {
    if (classFile.getType() == null) {
      logger.log(Level.SEVERE, "Null type!" + classFile);
      return false;
    } else {
      IType type = classFile.getType();
      try {
        if (type.isMember() || type.isLocal()) {
          return false;
        } else {
          return true;
        }
      } catch (JavaModelException e) {
        logger.log(Level.SEVERE, "Error determining if class is top level", e);
        return true;
      }
    }
//    return !classFile.getType().getFullyQualifiedName().contains("$");
//    try {
//      IType declaring = classFile.getType();
//      boolean topLevel = true;
//
//      while (declaring != null) {
//        if (declaring.isMember() || declaring.isLocal() || declaring.isAnonymous()) {
//          topLevel = false;
//          break;
//        }
//        declaring = declaring.getDeclaringType();
//      }
//      
//      if (topLevel) {
//        // check if there is any $ in the fqn
//        if (classFile.getType().getFullyQualifiedName().indexOf('$') == -1) { 
//          return true;
//        } else {
//          logger.log(Level.SEVERE, "isTopLevel thinks " + classFile.getType().getFullyQualifiedName() + " is top-level");
//          return true;
//        }
//      }
//    } catch (JavaModelException e) {
//      logger.log(Level.SEVERE, "Error in determining toplevel", e);
//      return false;
//    }
  }
  
  public void extractClassFile(IClassFile classFile) {
    // Verify that it's a top-level type, or a subtype of a top-level type
//    try {
//      IType declaring = classFile.getType();
//      while (declaring != null) {
//        if (declaring.isLocal() || declaring.isAnonymous()) {
//          return;
//        }
//        declaring = declaring.getDeclaringType();
//      }
//    } catch (JavaModelException e) {
//      logger.log(Level.SEVERE, "Error in extracting class file", e);
//      return;
//    }

    IJavaElement parent = classFile.getParent();
    while (true) {
      if (parent == null) {
        logger.log(Level.SEVERE, "Unable to find package for: " + classFile.getElementName());
        break;
      } else if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
        // Write the class file
        name = classFile.getElementName();
        path = parent.getElementName() + "." + name;
        fileWriter.writeFile(File.CLASS, name, null, path);

        try {
          if (classFile.getType().isAnonymous()) {
            String fqn = classFile.getType().getFullyQualifiedName();
            String containingFqn = fqn.substring(0, fqn.lastIndexOf('$'));
            relationWriter.writeRelation(Relation.CONTAINS, containingFqn, classFile.getType().getFullyQualifiedName(), new Location(classFile.getType().getFullyQualifiedName(), path, null, null));
          } else {
            relationWriter.writeRelation(Relation.CONTAINS, parent.getElementName(), classFile.getType().getFullyQualifiedName(), new Location(classFile.getType().getFullyQualifiedName(), path, null, null));
            entityWriter.writeEntity(Entity.PACKAGE, parent.getElementName(), 0, null, null);
          }
        } catch (JavaModelException e) {
          logger.log(Level.SEVERE, "Error in extracting class file", e);
        }
        break;
      } else {
        logger.log(Level.SEVERE, classFile.getType().getFullyQualifiedName() + " should be top-level!");
        parent = parent.getParent();
      }
    }
    
    extractIType(classFile.getType());
    name = null;
  }
  
  private void extractIType(IType type) {
    try {
      String fqn = type.getFullyQualifiedName();
      Location location = new Location(fqn, path, null, null);
      // Write the entity
      if (type.isClass()) {
        entityWriter.writeEntity(Entity.CLASS, fqn, type.getFlags(), null, location);
        
        // Write the superclass
        String superSig = type.getSuperclassTypeSignature();
        if (superSig != null) {
          relationWriter.writeRelation(Relation.EXTENDS, fqn, typeSignatureToFqn(superSig), location);
        }
      } else if (type.isAnnotation()) {
        entityWriter.writeEntity(Entity.ANNOTATION, fqn, type.getFlags(), null, location);
      } else if (type.isInterface()) {
        entityWriter.writeEntity(Entity.INTERFACE, fqn, type.getFlags(), null, location);
      }  else if (type.isEnum()) {
        entityWriter.writeEntity(Entity.ENUM, fqn, type.getFlags(), null, location);
      }
      
      // Write the superinterfaces
      for (String superIntSig : type.getSuperInterfaceTypeSignatures()) {
        relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, typeSignatureToFqn(superIntSig), location);
      }
      
      if (!fqnStack.isEmpty()) {
        relationWriter.writeRelation(Relation.CONTAINS, fqnStack.peek(), type.getFullyQualifiedName(), location);
      }
      
      fqnStack.push(type.getFullyQualifiedName());
      
//      for (IType child : type.getTypes()) {
//        extractIType(child);
//      }
      
      for (IField field : type.getFields()) {
        if (!Flags.isSynthetic(field.getFlags())) {
          extractIField(field);
        }
      }
      
      for (IMethod method : type.getMethods()) {
        if (!Flags.isSynthetic(method.getFlags()) || (Flags.isSynthetic(method.getFlags()) && method.isConstructor() && method.getParameterTypes().length == 0)) {
          extractIMethod(method, type.isAnnotation());
        }
      }
      
      for (ITypeParameter param : type.getTypeParameters()) {
        relationWriter.writeRelation(Relation.PARAMETRIZED_BY, fqn, getTypeParam(param), location);
      }
      
      fqnStack.pop();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error in extracting class file", e);
    }
  }
  
  private void extractIField(IField field) {
    try {
      Location location = new Location(fqnStack.peek(), path, null, null);
      String fqn = fqnStack.peek() + "." + field.getElementName();
      // Write the entity
      if (field.isEnumConstant()) {
        entityWriter.writeEntity(Entity.ENUM_CONSTANT, fqn, field.getFlags(), null, location);
      } else {
        entityWriter.writeEntity(Entity.FIELD, fqn, field.getFlags(), null, location);
      }
      
      // Write the contains relation
      relationWriter.writeRelation(Relation.CONTAINS, fqnStack.peek(), fqn, location);
      
      // Write the holds relation
      relationWriter.writeRelation(Relation.HOLDS, fqn, typeSignatureToFqn(field.getTypeSignature()), location);
    } catch(JavaModelException e) {
      logger.log(Level.SEVERE, "Error in extracting class file", e);
    }
  }
  
  private void extractIMethod(IMethod method, boolean annotationElement) {
    try {
      Location location = new Location(fqnStack.peek(), path, null, null);
      StringBuilder basicFqnBuilder = new StringBuilder(fqnStack.peek());
      String name = null;
      if (method.isConstructor()) {
        name = "<init>";
        
      } else {
        name = method.getElementName();
      }
      basicFqnBuilder.append('.').append(name);
      StringBuilder paramsBuilder = new StringBuilder("(");
      StringBuilder rawParamsBuilder = new StringBuilder("(");
      boolean first = true;
      for (String param : method.getParameterTypes()) {
        if (first) {
          first = false;
        } else {
          paramsBuilder.append(',');
          rawParamsBuilder.append(',');
        }
        paramsBuilder.append(typeSignatureToFqn(param));
        rawParamsBuilder.append(erasedTypeSignatureToFqn(param));
      }
      paramsBuilder.append(')');
      rawParamsBuilder.append(')');
      
      String referentialFqn = basicFqnBuilder.toString() + paramsBuilder.toString();
      String params = paramsBuilder.toString();
      String rawParams = rawParamsBuilder.toString();
      if (params.equals(rawParams)) {
        rawParams = null;
      }
      // Write the entity
      if (annotationElement) {
        entityWriter.writeEntity(Entity.ANNOTATION_ELEMENT, basicFqnBuilder.toString(), params, rawParams, method.getFlags(), null, location);
      } else if (method.isConstructor()) {
        entityWriter.writeEntity(Entity.CONSTRUCTOR, basicFqnBuilder.toString(), params, rawParams, method.getFlags(), null, location);
      } else if (method.getElementName().equals("<clinit>()")) {
        entityWriter.writeEntity(Entity.INITIALIZER, basicFqnBuilder.toString(), params, rawParams, method.getFlags(), null, location);
      } else {
        entityWriter.writeEntity(Entity.METHOD, basicFqnBuilder.toString(), params, rawParams, method.getFlags(), null, location);
      }
      
      // Write the contains relation
      relationWriter.writeRelation(Relation.CONTAINS, fqnStack.peek(), referentialFqn, location);
      
      // Write the returns relation
      relationWriter.writeRelation(Relation.RETURNS, referentialFqn, typeSignatureToFqn(method.getReturnType()), location);
      
      // Write the receives relation
      String[] paramTypes = method.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++) {
        localVariableWriter.writeLocalVariable(LocalVariable.PARAM, "arg" + i, 0, typeSignatureToFqn(paramTypes[i]), location, referentialFqn, i, location);
//        relationWriter.writeReceives(fqn, typeSignatureToFqn(paramTypes[i]), "arg" + i, i);
      }
      
      for (ITypeParameter param : method.getTypeParameters()) {
        relationWriter.writeRelation(Relation.PARAMETRIZED_BY, referentialFqn, getTypeParam(param), location);
      }
    } catch(JavaModelException e) {
      logger.log(Level.SEVERE, "Error in extracting class file", e);
    }
  }
  
  private static final String brackets = "[][][][][][][][][][][][][][][][]";
  private static String typeSignatureToFqn(String signature) {
    try {
      switch (Signature.getTypeSignatureKind(signature)) {
        case Signature.ARRAY_TYPE_SIGNATURE:
          return typeSignatureToFqn(Signature.getElementType(signature)) + brackets.substring(0, 2 * Signature.getArrayCount(signature)); 
        case Signature.CLASS_TYPE_SIGNATURE:
          if (signature.indexOf('<') == -1) {
            int firstDollar = signature.indexOf('$');
            if (firstDollar == -1) {
              return Signature.getSignatureQualifier(signature) + "." + Signature.getSignatureSimpleName(signature);
            } else {
              String shortSig = signature.substring(0, firstDollar) + ";";
              return Signature.getSignatureQualifier(shortSig) + "." + Signature.getSignatureSimpleName(shortSig) + signature.substring(firstDollar, signature.length() - 1);
            }
          } else {
            StringBuilder fqnBuilder = new StringBuilder();
            StringBuilder current = new StringBuilder();
            int depth = 0;
            boolean dollar = false;
            for (char c : signature.toCharArray()) {
              switch (c) {
                case '<':
                  if (fqnBuilder.length() == 0) {
                    current.append(';');
                    fqnBuilder.append(typeSignatureToFqn(current.toString()));
                    current.setLength(current.length() - 1);
                  }
                  current.append(c);
                  depth++;
                  break;
                case '>':
                  current.append(c);
                  if (--depth == 0) {
                    dollar = true;
                    fqnBuilder.append('<');
                    boolean first = true;
                    current.append(";");
                    for (String arg : Signature.getTypeArguments(current.toString())) {
                      if (first) {
                        first = false;
                      } else {
                        fqnBuilder.append(',');
                      }
                      fqnBuilder.append(typeSignatureToFqn(arg));
                    }
                    current.setLength(current.length() - 1);
                    fqnBuilder.append('>');
                  }
                  break;
                case '.':
                  if (dollar && depth == 0) {
                    fqnBuilder.append('$');
                  }
                  current.append(c);
                  break;
                default:
                  if (dollar && depth == 0) {
                    fqnBuilder.append(c);
                  }
                  current.append(c);
                  break;
              }
            }
            fqnBuilder.setLength(fqnBuilder.length() - 1);
            return fqnBuilder.toString();
          }
        case Signature.BASE_TYPE_SIGNATURE:
          return Signature.getSignatureSimpleName(signature);
        case Signature.TYPE_VARIABLE_SIGNATURE:
          return "<" + Signature.getSignatureSimpleName(signature) + ">";
        case Signature.WILDCARD_TYPE_SIGNATURE:
          if (signature.startsWith("+")) {
            return "<?+" + typeSignatureToFqn(signature.substring(1)) + ">";
          } else if (signature.startsWith("-")) {
            return "<?-" + typeSignatureToFqn(signature.substring(1)) + ">";
          } else {
            return "<?>";
          }
        case Signature.CAPTURE_TYPE_SIGNATURE:
          System.out.println("eek");
          return "";
        default:
          throw new IllegalArgumentException("Not a valid type signature");
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("bad");
      return null;
    }
   }
  
  private static String erasedTypeSignatureToFqn(String signature) {
    return typeSignatureToFqn(Signature.getTypeErasure(signature));
  }
  
  private String getTypeParam(ITypeParameter typeParam) {
    try {
      StringBuilder builder = new StringBuilder();
      builder.append('<').append(typeParam.getElementName());
      boolean first = true;
      
      for (String bound : typeParam.getBoundsSignatures()) {
        if (first) {
          first = false;
          builder.append('+');
        } else {
          builder.append('&');
        }
        builder.append(typeSignatureToFqn(bound));
      }
      
      builder.append('>');
      return builder.toString();
    } catch (JavaModelException e) {
      e.printStackTrace();
      System.out.println("bad");
      return null;
    }
  }
}
