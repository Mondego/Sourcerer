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
package edu.uci.ics.sourcerer.extractor.ast;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Deque;
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

import edu.uci.ics.sourcerer.extractor.io.IJarEntityWriter;
import edu.uci.ics.sourcerer.extractor.io.IJarRelationWriter;
import edu.uci.ics.sourcerer.extractor.io.ILocalVariableWriter;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClassFileExtractor {
  private IJarEntityWriter jarEntityWriter;
  private ILocalVariableWriter localVariableWriter;
  private IJarRelationWriter relationWriter;
  private Deque<String> fqnStack;
  
  public ClassFileExtractor(WriterBundle writers) {
    jarEntityWriter = writers.getJarEntityWriter();
    localVariableWriter = writers.getLocalVariableWriter();
    relationWriter = writers.getJarRelationWriter();
    fqnStack = Helper.newStack();
  }
  
  public static boolean isTopLevel(IClassFile classFile) {
    return !classFile.getType().getFullyQualifiedName().contains("$");
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
    try {
      IType declaring = classFile.getType();
      while (declaring != null) {
        if (declaring.isLocal() || declaring.isAnonymous()) {
          return;
        }
        declaring = declaring.getDeclaringType();
      }
    } catch (JavaModelException e) {
      logger.log(Level.SEVERE, "Error in extracting class file", e);
      return;
    }
    extractIType(classFile.getType());
    
    IJavaElement parent = classFile.getParent();
    while (true) {
      if (parent == null) {
        logger.log(Level.SEVERE, "Unable to find package for: " + classFile.getElementName());
        break;
      } else if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
        relationWriter.writeInside(classFile.getType().getFullyQualifiedName(), parent.getElementName());
        jarEntityWriter.writePackage(parent.getElementName());
        break;
      } else {
        parent = parent.getParent();
      }
    }
  }
  
  private void extractIType(IType type) {
    try {
      String fqn = type.getFullyQualifiedName();
      
      // Write the entity
      if (type.isClass()) {
        jarEntityWriter.writeClass(fqn, type.getFlags());
        
        // Write the superclass
        String superSig = type.getSuperclassTypeSignature();
        if (superSig != null) {
          relationWriter.writeExtends(fqn, typeSignatureToFqn(superSig));
        }
      } else if (type.isAnnotation()) {
        jarEntityWriter.writeAnnotation(fqn, type.getFlags());
      } else if (type.isInterface()) {
        jarEntityWriter.writeInterface(fqn, type.getFlags());
      }  else if (type.isEnum()) {
        jarEntityWriter.writeEnum(fqn, type.getFlags());
      }
      
      // Write the superinterfaces
      for (String superIntSig : type.getSuperInterfaceTypeSignatures()) {
        relationWriter.writeImplements(fqn, typeSignatureToFqn(superIntSig));
      }
      
      if (!fqnStack.isEmpty()) {
        relationWriter.writeInside(type.getFullyQualifiedName(), fqnStack.peek());
      }
      
      fqnStack.push(type.getFullyQualifiedName());
      
      for (IType child : type.getTypes()) {
        extractIType(child);
      }
      
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
      
      int pos = 0;
      for (ITypeParameter param : type.getTypeParameters()) {
        relationWriter.writeParametrizedBy(fqn, getTypeParam(param), pos++);
      }
      
      fqnStack.pop();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error in extracting class file", e);
    }
  }
  
  private void extractIField(IField field) {
    try {
      String fqn = fqnStack.peek() + "." + field.getElementName();
      // Write the entity
      if (field.isEnumConstant()) {
        jarEntityWriter.writeEnumConstant(fqn, field.getFlags());
      } else {
        jarEntityWriter.writeField(fqn, field.getFlags());
      }
      
      // Write the inside relation
      relationWriter.writeInside(fqn, fqnStack.peek());
      
      // Write the holds relation
      relationWriter.writeHolds(fqn, typeSignatureToFqn(field.getTypeSignature()));
    } catch(JavaModelException e) {
      logger.log(Level.SEVERE, "Error in extracting class file", e);
    }
  }
  
  private void extractIMethod(IMethod method, boolean annotationElement) {
    try {
      StringBuilder fqnBuilder = new StringBuilder(fqnStack.peek());
      if (method.isConstructor()) {
        fqnBuilder.append('.').append("<init>");
      } else {
        fqnBuilder.append('.').append(method.getElementName());
      }
      fqnBuilder.append('(');
      boolean first = true;
      for (String param : method.getParameterTypes()) {
        if (first) {
          first = false;
        } else {
          fqnBuilder.append(',');
        }
        String sig = typeSignatureToFqn(param);
        fqnBuilder.append(sig);
      }
      fqnBuilder.append(')');
      
      String fqn = fqnBuilder.toString();
      
      // Write the entity
      if (annotationElement) {
        jarEntityWriter.writeAnnotationElement(fqn, method.getFlags());
      } else if (method.isConstructor()) {
        jarEntityWriter.writeConstructor(fqn, method.getFlags());
      } else {
        jarEntityWriter.writeMethod(fqn, method.getFlags());
      }
      
      // Write the inside relation
      relationWriter.writeInside(fqn, fqnStack.peek());
      
      // Write the returns relation
      relationWriter.writeReturns(fqn, typeSignatureToFqn(method.getReturnType()));
      
      // Write the receives relation
      String[] paramTypes = method.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++) {
        localVariableWriter.writeJarParameter("arg" + i, typeSignatureToFqn(paramTypes[i]), fqn, i);
//        relationWriter.writeReceives(fqn, typeSignatureToFqn(paramTypes[i]), "arg" + i, i);
      }
      
      int pos = 0;
      for (ITypeParameter param : method.getTypeParameters()) {
        relationWriter.writeParametrizedBy(fqn, getTypeParam(param), pos++);
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
          String args[] = Signature.getTypeArguments(signature);
          if (args.length == 0) {
            return Signature.getSignatureQualifier(signature) + "." + Signature.getSignatureSimpleName(signature).replace('.', '$');
          } else {
            StringBuilder fqnBuilder = new StringBuilder(typeSignatureToFqn(Signature.getTypeErasure(signature)));
            fqnBuilder.append('<');
            boolean first = true;
            for (String arg : args) {
              if (first) {
                first = false;
              } else {
                fqnBuilder.append(',');
              }
              fqnBuilder.append(typeSignatureToFqn(arg));
            }
            fqnBuilder.append('>');
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
  
  private String getTypeParam(ITypeParameter typeParam) {
    try {
      StringBuilder builder = new StringBuilder();
      builder.append('<').append(typeParam.getElementName());
      boolean first = true;
    
      for (String bound : typeParam.getBounds()) {
        if (first) {
          first = false;
          builder.append('+');
        } else {
          builder.append('&');
        }
        builder.append(bound);
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
