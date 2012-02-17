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
package edu.uci.ics.sourcerer.tools.java.extractor.bytecode;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import edu.uci.ics.sourcerer.tools.java.extractor.io.EntityWriter;
import edu.uci.ics.sourcerer.tools.java.extractor.io.FileWriter;
import edu.uci.ics.sourcerer.tools.java.extractor.io.LocalVariableWriter;
import edu.uci.ics.sourcerer.tools.java.extractor.io.RelationWriter;
import edu.uci.ics.sourcerer.tools.java.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ASMExtractor implements Closeable {
  private WriterBundle writers;
  
  private FileWriter fileWriter;
  private EntityWriter entityWriter;
  private RelationWriter relationWriter;
  private LocalVariableWriter parameterWriter;
  
  private ClassVisitorImpl classVisitor = new ClassVisitorImpl();
  private AnnotationVisitorImpl annotationVisitor = new AnnotationVisitorImpl();
  private MethodVisitorImpl methodVisitor = new MethodVisitorImpl();
  private ClassSignatureVisitorImpl classSignatureVisitor = new ClassSignatureVisitorImpl();
  private MethodSignatureVisitorImpl methodSignatureVisitor = new MethodSignatureVisitorImpl();
  
  private FqnStack fqnStack;
  private Location location;
  
  public ASMExtractor(WriterBundle writers) {
    this.writers = writers;
    this.fileWriter = writers.getFileWriter();
    this.entityWriter = writers.getEntityWriter();
    this.relationWriter = writers.getRelationWriter();
    this.parameterWriter = writers.getLocalVariableWriter();
    
    fqnStack = new FqnStack();
  }
  
  @Override
  public void close() {
    IOUtils.close(writers);
  }
  
  public void extractJar(java.io.File file) {
    try (JarFile jar = new JarFile(file)) {
      TaskProgressLogger task = TaskProgressLogger.get();
      task.start("Extracting class files", "class files extracted", 500);
      Enumeration<JarEntry> en = jar.entries();
      while (en.hasMoreElements()) {
        JarEntry entry = en.nextElement();
        if (entry.getName().endsWith(".class")) {
          String pkgFqn = convertNameToFqn(entry.getName());
          location = new Location(pkgFqn, null, null, null);
          int last = pkgFqn.lastIndexOf('.', pkgFqn.lastIndexOf('.') - 1);
          String name = pkgFqn.substring(last + 1);
          pkgFqn = pkgFqn.substring(0, last);
          
          entityWriter.writeEntity(Entity.PACKAGE, pkgFqn, 0, null, null);
          
          fqnStack.push(pkgFqn, Entity.PACKAGE);
          
          ClassReader reader = new ClassReader(jar.getInputStream(entry));
          reader.accept(classVisitor, 0);
          
          fileWriter.writeFile(File.CLASS, name, null, location.getClassFile());
          
          fqnStack.pop();
          location = null;
          task.progress();
        }
      }
      task.finish();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading jar file.", e);
    }
  }
  
  public void extract(String pkg, String name, byte[] bytes) {
    location = new Location(pkg + "." + name, null, null, null);
    entityWriter.writeEntity(Entity.PACKAGE, pkg, 0, null, null);
    
    fqnStack.push(pkg, Entity.PACKAGE);
    
    ClassReader reader = new ClassReader(bytes);
    reader.accept(classVisitor, 0);
    
    fileWriter.writeFile(File.CLASS, name, null, location.getClassFile());
    
    fqnStack.pop();
  }

  private class FqnStack {
    private Deque<StackItem> stack = Helper.newStack();
    
    public void push(String fqn, Entity type) {
      stack.push(new StackItem(type, fqn));
    }
    
    public void pop() {
      stack.pop();
    } 
    
    public String getFqn() {
      return stack.peek().fqn;
    }
    
    public Entity getType() {
      return stack.peek().type;
    }
    
    public void outputInside() {
      stack.peek().insideOutput = true;
    }
    
    public boolean insideOutput() {
      return stack.peek().insideOutput;
    }
    
    private class StackItem {
      private Entity type;
      private String fqn;
      private boolean insideOutput = false;
      
      public StackItem(Entity type, String fqn) {
        this.type = type;
        this.fqn = fqn;
      }
      
      @Override
      public String toString() {
        return type + " " + fqn;
      }
    }
  }
  
  private String convertNameToFqn(String name) {
    if (name.charAt(0) == '[') {
      TypeSignatureVisitorImpl visitor = new TypeSignatureVisitorImpl();
      new SignatureReader(name).acceptType(visitor);
      return visitor.getResult();
    } else {
      return name.replace('/', '.');
    }
  }
    
  private String convertBaseType(char type) {
    switch (type) {
      case 'B':
        return "byte";
      case 'C':
        return "char";
      case 'D':
        return "double";
      case 'F':
        return "float";
      case 'I':
        return "int";
      case 'J':
        return "long";
      case 'S':
        return "short";
      case 'Z':
        return "boolean";
      case 'V':
        return "void";
      default:
        logger.log(Level.SEVERE, "Unexpected type name: " + type);
        return "" + type;
    }
  }

  private static final int CLASS_ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC;
  private static final int FIELD_ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_VOLATILE | Opcodes.ACC_TRANSIENT | Opcodes.ACC_SYNTHETIC;
  private static final int METHOD_ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNCHRONIZED | Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_STRICT | Opcodes.ACC_SYNTHETIC;
  private class ClassVisitorImpl extends ClassVisitor {
    private int access;
    
    private ClassVisitorImpl() {
      super(Opcodes.V1_7);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      // Get the type
      Entity type = null;
      if (access == 0x1600) {
        // package-info file
      } else if ((access & Opcodes.ACC_INTERFACE) != 0) {
        if ((access & Opcodes.ACC_ANNOTATION) != 0) {
          type = Entity.ANNOTATION;
        } else {
          type = Entity.INTERFACE;
        }
      } else {
        if ((access & Opcodes.ACC_ENUM) != 0) {
          type = Entity.ENUM;
        } else {
          type = Entity.CLASS;
        }
      }
      
      // Get the fqn
      String fqn = convertNameToFqn(name);
      
      // Write the entity
      if (type == null) {
        fqnStack.push(fqn, null);
      } else {
        this.access = access;
        fqnStack.push(fqn, type);
        
        // Write the type variables
        if (signature == null) {
          // Write the extends relation
          if (superName == null) {
            if (!"java/lang/Object".equals(name)) {
              logger.log(Level.SEVERE, "Missing supertype for " + fqn);
            }
          } else {
            relationWriter.writeRelation(Relation.EXTENDS, fqn, convertNameToFqn(superName), location);
          }
          
          // Write the implements relations
          for (String face : interfaces) {
            relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, convertNameToFqn(face), location);
          }
        } else {
          new SignatureReader(signature).accept(classSignatureVisitor);
        }
      }
    }
  
    @Override
    public void visitSource(String source, String debug) {}
    
    @Override
    public void visitOuterClass(String owner, String name, String desc) {
      // Write the inside relation
      String parentFqn = null;
      if (name == null) {
        parentFqn = convertNameToFqn(owner);
      } else {
        new SignatureReader(desc).accept(methodSignatureVisitor.init(owner, name));
        parentFqn = methodSignatureVisitor.getReferenceFqn();
      }
      relationWriter.writeRelation(Relation.INSIDE, fqnStack.getFqn(), parentFqn, location);
      fqnStack.outputInside();
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      if (fqnStack.getType() == null) {
        return null;
      } else {
        TypeSignatureVisitorImpl visitor = new TypeSignatureVisitorImpl();
        new SignatureReader(desc).acceptType(visitor);
        relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn(), visitor.getResult(), location);
        return annotationVisitor;
      }
    }
  
    @Override
    public void visitAttribute(Attribute attr) {
      logger.info(attr.type + " : " + attr);
    }
    
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
      if (innerName != null && fqnStack.getFqn().equals(convertNameToFqn(name))) {
        this.access = access;
      }
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      Entity type = null;
      // Drop the synthetic fields
      if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
        return null;
      } else if ((access & Opcodes.ACC_ENUM) != 0) {
        type = Entity.ENUM_CONSTANT;
      } else {
        type = Entity.FIELD;
      }
      
      // Get the fqn
      String fqn = fqnStack.getFqn() + "." + name;
      
      // Write the entity
      entityWriter.writeEntity(type, fqn, access & FIELD_ACCESS_MASK, null, location);
      
      // Write the inside relation
      relationWriter.writeRelation(Relation.INSIDE, fqn, fqnStack.getFqn(), location);
      
      // Write the holds relation
      TypeSignatureVisitorImpl visitor = new TypeSignatureVisitorImpl();
      if (signature == null) {
        new SignatureReader(desc).acceptType(visitor);
      } else {
        new SignatureReader(signature).acceptType(visitor);
      }
      relationWriter.writeRelation(Relation.HOLDS, fqn, visitor.getResult(), location);

      // Write the writes relation
      if (value != null) {
        relationWriter.writeRelation(Relation.WRITES, fqn, fqn, location);
      }
      return null;
    }
  
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      // Get the type
      Entity type = null;
      if ((access & (Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC)) == (Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC)) {
        // ignore bridge method
        return null;
      } else if ("<clinit>".equals(name)) {
        type = Entity.INITIALIZER;
      } else if ("<init>".equals(name)) {
        type = Entity.CONSTRUCTOR;
      } else if (fqnStack.getType() == Entity.ANNOTATION){
        type = Entity.ANNOTATION_ELEMENT;
      } else {
        type = Entity.METHOD;
      }

      if (signature == null) {
        // Get the fqn
        new SignatureReader(desc).accept(methodSignatureVisitor.init(type, name));
        // Write the throws relations
        if (exceptions != null) {
          for (String exception : exceptions) {
            relationWriter.writeRelation(Relation.THROWS, fqnStack.getFqn(), convertNameToFqn(exception), location);
          }
        }
        
        // Write the entity
        entityWriter.writeEntity(type, methodSignatureVisitor.getFqn(), methodSignatureVisitor.getSignature(), null, access & METHOD_ACCESS_MASK, null, location);
      } else {
        new SignatureReader(signature).accept(methodSignatureVisitor.init(type, name));
        String fqn = methodSignatureVisitor.getFqn();
        String sig = methodSignatureVisitor.getSignature();
        new SignatureReader(desc).accept(methodSignatureVisitor.init());
        String rawSig = methodSignatureVisitor.getSignature();
        if (sig.equals(rawSig)) {
          entityWriter.writeEntity(type, fqn, sig, null, access & METHOD_ACCESS_MASK, null, location);
        } else {
          entityWriter.writeEntity(type, fqn, sig, methodSignatureVisitor.getSignature(), access & METHOD_ACCESS_MASK, null, location);
        }
      }

      return methodVisitor;
    }
    
    @Override
    public void visitEnd() {
      if (fqnStack.getType() == null) {
        fqnStack.pop();
      } else {
        entityWriter.writeEntity(fqnStack.getType(), fqnStack.getFqn(), access & CLASS_ACCESS_MASK, null, location);
        if (fqnStack.insideOutput()) {
          fqnStack.pop();
        } else {
          String fqn = fqnStack.getFqn();
          fqnStack.pop();
    
          // Write the inside relation
          int dot = fqn.lastIndexOf('.');
          if (dot != -1) {
            String parentFqn = fqn.substring(0, dot);
            if (!parentFqn.equals(fqnStack.getFqn())) {
              logger.log(Level.SEVERE, "Mismatch between " + parentFqn + " and " + fqnStack.getFqn());
            }
          }
          relationWriter.writeRelation(Relation.INSIDE, fqn, fqnStack.getFqn(), location);
        }
      }
    }
  }
  
  private class AnnotationVisitorImpl extends AnnotationVisitor {
    public AnnotationVisitorImpl() {
      super(Opcodes.V1_7);
    }

    @Override
    public void visit(String name, Object value) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      logger.info("nested!");
      return null;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
      return annotationVisitor;
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
      TypeSignatureVisitorImpl visitor = new TypeSignatureVisitorImpl();
      new SignatureReader(desc).acceptType(visitor);
      relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), visitor.getResult() + "." + value, location);
    }
  }
  
  private class MethodVisitorImpl extends MethodVisitor {
    public MethodVisitorImpl() {
      super(Opcodes.V1_7);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
      logger.info("foo");
      return null;
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      TypeSignatureVisitorImpl visitor = new TypeSignatureVisitorImpl();
      new SignatureReader(desc).acceptType(visitor);
      relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn(), visitor.getResult(), location);
      return annotationVisitor;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      TypeSignatureVisitorImpl visitor = new TypeSignatureVisitorImpl();
      new SignatureReader(desc).acceptType(visitor);
      relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn() + "#" + parameter, visitor.getResult(), location);
      return annotationVisitor;
    }

    @Override
    public void visitAttribute(Attribute attr) {
    }

    @Override
    public void visitCode() {
    }
    
    @Override
    public void visitFrame(int type, int nLoacl, Object[] local, int nStack, Object[] stack) {
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
//      if (!name.startsWith("this$") && name.charAt(0) != '$') {
      if (name.indexOf('$') == -1) {
        if (opcode == 0xb4 || opcode == 0xb2) {
          relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), convertNameToFqn(owner) + "." + name, location);
        } else if (opcode == 0xb5 || opcode == 0xb3) {
          relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), convertNameToFqn(owner) + "." + name, location);
        }
      }
    }

    @Override
    public void visitIincInsn(int var, int increment) {
    }

    @Override
    public void visitInsn(int opcode) {
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
    }

    @Override
    public void visitLdcInsn(Object cst) {
    }
    
    @Override
    public void visitTypeInsn(int opcode, String desc) {
      if (opcode == 0xc1) {
        relationWriter.writeRelation(Relation.CHECKS, fqnStack.getFqn(), convertNameToFqn(desc), location);
      } else if (opcode == 0xc0) {
        relationWriter.writeRelation(Relation.CASTS, fqnStack.getFqn(), convertNameToFqn(desc), location);
      } else if (opcode == 0xbb) {
        relationWriter.writeRelation(Relation.INSTANTIATES, fqnStack.getFqn(), convertNameToFqn(desc), location);
      }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      new SignatureReader(desc).accept(methodSignatureVisitor.init(owner, name));
      String fqn = methodSignatureVisitor.getReferenceFqn();
      relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, location);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label ... label) {
    }

    @Override
    public void visitLabel(Label label) {
    }
    
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
//      logger.info("local : " + name + " : " + desc + " : " + signature);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
    }

    @Override
    public void visitEnd() {
      fqnStack.pop();
    }
  }
  
  private abstract class AbstractSignatureVisitor extends SignatureVisitor {
    public AbstractSignatureVisitor() {
      super(Opcodes.V1_7);
    }
    
    public abstract void add(String type);
  }
  
  private class ClassSignatureVisitorImpl extends AbstractSignatureVisitor {
    private Map<String, Collection<String>> bounds = Helper.newHashMap();
    private Collection<String> currentBound;
    private Relation currentType;
    
    @Override
    public void add(String type) {
      if (currentType == Relation.EXTENDS || currentType == Relation.IMPLEMENTS) {
        relationWriter.writeRelation(currentType, fqnStack.getFqn(), type, location);
        currentType = null;
      } else if (currentBound != null) {
        currentBound.add(type);
      }
    }
    
    @Override
    public void visitFormalTypeParameter(String name) {
      currentBound = Helper.newLinkedList();
      bounds.put(name, currentBound);
    }

    @Override
    public SignatureVisitor visitClassBound() {
      return new TypeSignatureVisitorImpl(this);
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
      return new TypeSignatureVisitorImpl(this);
    }
    
    private void resolveFormalParameters() {
      for (Map.Entry<String, Collection<String>> entry : bounds.entrySet()) {
        StringBuilder builder = new StringBuilder();
        builder.append('<').append(entry.getKey()).append('+');
        for (String arg : entry.getValue()) {
          builder.append(arg).append(',');
        }
        builder.setCharAt(builder.length() - 1, '>');
        relationWriter.writeRelation(Relation.PARAMETRIZED_BY, fqnStack.getFqn(), builder.toString(), location);
      }
      bounds.clear();
      currentBound = null;
    }
    
    @Override
    public SignatureVisitor visitSuperclass() {
      resolveFormalParameters();
      currentType = Relation.EXTENDS;
      return new TypeSignatureVisitorImpl(this);
    }

    @Override
    public SignatureVisitor visitInterface() {
      currentType = Relation.IMPLEMENTS;
      return new TypeSignatureVisitorImpl(this);
    }
  }

  private class MethodSignatureVisitorImpl extends AbstractSignatureVisitor {
    private Entity type;
    private String fqn;
    private StringBuilder signature;
    private Map<String, Collection<String>> bounds = Helper.newHashMap();
    private Collection<String> currentBound;
    private Relation currentType;
    
    private Collection<String> paramTypes = Helper.newLinkedList();

    public MethodSignatureVisitorImpl init() {
      this.type = null;
      fqn = null;
      signature = new StringBuilder();
      signature.append("(");
      return this;
    }
    
    public MethodSignatureVisitorImpl init(String owner, String name) {
      this.type = null;
      fqn = convertNameToFqn(owner) + '.' + name;
      signature = new StringBuilder();
      signature.append("(");
      return this;
    }
    
    public MethodSignatureVisitorImpl init(Entity type, String name) {
      this.type = type;
      fqn = fqnStack.getFqn() + '.' + name;
      signature = new StringBuilder();
      signature.append("(");
      return this;
    }

    @Override
    public void add(String type) {
      if (currentType == null) {
        currentBound.add(type);
      } else if (currentType == Relation.INSIDE) {
        paramTypes.add(type);
        currentType = null;
      } else if (this.type != null) { 
        if (currentType == Relation.RETURNS) {
          if (this.type != Entity.CONSTRUCTOR && this.type != Entity.INITIALIZER) {
            relationWriter.writeRelation(currentType, fqnStack.getFqn(), type, location);
          }
        } else if (currentType == Relation.THROWS) {
          relationWriter.writeRelation(currentType, fqnStack.getFqn(), type, location);
        } else {
          throw new IllegalStateException(currentType + " is not valid");
        }
        currentType = null;
      }
    }
    
    public String getFqn() {
      return fqn;
    }
    
    public String getReferenceFqn() {
      return fqn + signature.toString();
    }
    
    public String getSignature() {
      return signature.toString();
    }
    
    @Override
    public void visitFormalTypeParameter(String name) {
      currentBound = Helper.newLinkedList();
      bounds.put(name, currentBound);
    }

    @Override
    public SignatureVisitor visitClassBound() {
      return new TypeSignatureVisitorImpl(this);
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
      return new TypeSignatureVisitorImpl(this);
    }
    
    private void resolveFormalParameters() {
      for (Map.Entry<String, Collection<String>> entry : bounds.entrySet()) {
        StringBuilder builder = new StringBuilder();
        builder.append('<').append(entry.getKey()).append('+');
        for (String arg : entry.getValue()) {
          builder.append(arg).append(',');
        }
        builder.setCharAt(builder.length() - 1, '>');
        if (type != null) {
          relationWriter.writeRelation(Relation.PARAMETRIZED_BY, fqnStack.getFqn(), builder.toString(), location);
        }
      }
      bounds.clear();
      currentBound = null;
    }

    @Override
    public SignatureVisitor visitParameterType() {
      currentType = Relation.INSIDE;
      return new TypeSignatureVisitorImpl(this);
    }
    
    private void resolveFqn() {
      for (String param : paramTypes) {
        signature.append(param).append(',');
      }
      if (paramTypes.size() == 0) {
        signature.append(')');
      } else {
        signature.setCharAt(signature.length() - 1, ')');
      }
      if (fqn != null) {
        if (type != null) {
          relationWriter.writeRelation(Relation.INSIDE, getReferenceFqn(), fqnStack.getFqn(), location);
          fqnStack.push(getReferenceFqn(), type);
          int i = 0;
          for (String param : paramTypes) {
            parameterWriter.writeLocalVariable(LocalVariable.PARAM, "arg" + i, 0, param, location, fqnStack.getFqn(), i, location);
            i++;
          }
        }
      }
      paramTypes.clear();
    }

    @Override
    public SignatureVisitor visitReturnType() {
      resolveFqn();
      resolveFormalParameters();
      currentType = Relation.RETURNS;
      return new TypeSignatureVisitorImpl(this);
    }
   
    @Override
    public SignatureVisitor visitExceptionType() {
      currentType = Relation.THROWS;
      return new TypeSignatureVisitorImpl(this);
    }
  }
  
  private class TypeSignatureVisitorImpl extends AbstractSignatureVisitor {
    private AbstractSignatureVisitor parent;
    private StringBuilder result;
    private Collection<String> args;
    private String wildcard;
    private int arrayCount = 0;

    public TypeSignatureVisitorImpl() {
      result = new StringBuilder();
      args = new LinkedList<>();
    }
    
    public TypeSignatureVisitorImpl(AbstractSignatureVisitor parent) {
      this.parent = parent;
      result = new StringBuilder();
      args = new LinkedList<>();
    }
    
    public void add(String type) {
      if (wildcard == null) {
        args.add(type);
      } else {
        args.add(wildcard + type + ">");
        wildcard = null;
      }
    }
    
    @Override
    public void visitBaseType(char descriptor) {
      result.append(convertBaseType(descriptor));
      visitEnd();
    }

    @Override
    public void visitTypeVariable(String name) {
      result.append("<" + name + ">");
      visitEnd();
    }

    @Override
    public SignatureVisitor visitArrayType() {
      arrayCount++;
      return this;
    }
 
    @Override
    public void visitClassType(String name) {
      result.append(convertNameToFqn(name));
    }

    @Override
    public void visitTypeArgument() {
      add("<?>");
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
      if (wildcard == INSTANCEOF) {
        this.wildcard = null;
      } else {
        this.wildcard = "<?" + wildcard;
      }
      return new TypeSignatureVisitorImpl(this);
    }

    @Override
    public void visitInnerClassType(String name) {
      addArgs();
      result.append("$").append(name);
    }
    
    private void addArgs() {
      if (args.size() != 0) {
        result.append("<");
        for (String arg : args) {
          result.append(arg).append(",");
        }
        result.setCharAt(result.length() - 1, '>');
      }
      args.clear();
    }
    
    private void addArray() {
      while (arrayCount-- > 0) {
        result.append("[]");
      }
    }

    
    @Override
    public void visitEnd() {
      addArgs();
      addArray();
      if (parent != null) {
        parent.add(result.toString());
      }
    }
    
    public String getResult() {
      return result.toString();
    }
  }
}
