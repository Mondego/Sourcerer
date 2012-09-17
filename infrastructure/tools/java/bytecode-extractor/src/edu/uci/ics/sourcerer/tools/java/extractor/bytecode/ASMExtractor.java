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
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import edu.uci.ics.sourcerer.tools.java.model.extracted.io.EntityWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.FileWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.FindBugsRunner;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.LocalVariableWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.RelationWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;
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
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Extracting class files", "class files extracted", 500);
    try (JarFile jar = new JarFile(file)) {
      Enumeration<JarEntry> en = jar.entries();
      while (en.hasMoreElements()) {
        JarEntry entry = en.nextElement();
        if (entry.getName().endsWith(".class")) {
//          String pkgFqn = convertNameToFqn(entry.getName());
//          location = new Location(pkgFqn, null, null, null);
//          int last = pkgFqn.lastIndexOf('.', pkgFqn.lastIndexOf('.') - 1);
//          String name = pkgFqn.substring(last + 1);
//          if (last == -1) {
//            logger.severe("No pkg for: " + entry.getName());
//            pkgFqn = "default";
//          } else {
//            pkgFqn = pkgFqn.substring(0, last);
//          }
          
//          entityWriter.writeEntity(Entity.PACKAGE, pkgFqn, 0, null, null);
          
//          fqnStack.push(pkgFqn, Entity.PACKAGE);
          try {
            ClassReader reader = new ClassReader(jar.getInputStream(entry));
            reader.accept(classVisitor, 0);
          } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading class file: " + entry.getName(), e);
          }
          
          fqnStack.pop();
          location = null;
          task.progress();
        }
      }
      task.finish();
    } catch (Exception e) {
      task.exception(e);
    }

    FindBugsRunner.runFindBugs(file, writers.getOutput());
  }
  
  public void extract(byte[] bytes) {
//    location = new Location(pkg + "." + name, null, null, null);
//    entityWriter.writeEntity(Entity.PACKAGE, pkg, 0, null, null);
    
//    fqnStack.push(pkg, Entity.PACKAGE);
    
    ClassReader reader = new ClassReader(bytes);
    reader.accept(classVisitor, 0);
    
//    fileWriter.writeFile(File.CLASS, name, null, location.getClassFile());
    location = null;
    
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
      // Make sure location is initialized
      if (location == null) {
        String fileName = null;
        String classFile = name.replace('/', '.') + ".class";
        location = new Location(classFile, null, null, null);
        
        int idx = name.lastIndexOf('/');
        if (idx >= 0) {
          fileName = name.substring(idx + 1) + ".class";
          String pkg = classFile.substring(0, idx);
          entityWriter.writeEntity(Entity.PACKAGE, pkg, 0, null, null);
          fqnStack.push(pkg, Entity.PACKAGE);
        } else {
          fileName = name + ".class";
          entityWriter.writeEntity(Entity.PACKAGE, "default", 0, null, null);
          fqnStack.push("default", Entity.PACKAGE);
        }
        
        fileWriter.writeFile(File.CLASS, fileName, null, location.getClassFile());
      }
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
      relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fqnStack.getFqn(), location);
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
//      logger.info(attr.type + " : " + attr);
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
      
      // Write the contains relation
      relationWriter.writeRelation(Relation.CONTAINS, fqnStack.getFqn(), fqn, location);
      
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
      } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
        // drop synthetic methods
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
        
        methodVisitor.init(type, methodSignatureVisitor.getFqn(), methodSignatureVisitor.getSignature(), null, access & METHOD_ACCESS_MASK);
      } else {
        new SignatureReader(signature).accept(methodSignatureVisitor.init(type, name));
        String fqn = methodSignatureVisitor.getFqn();
        String sig = methodSignatureVisitor.getSignature();
        new SignatureReader(desc).accept(methodSignatureVisitor.init());
        String rawSig = methodSignatureVisitor.getSignature();
        if (sig.equals(rawSig)) {
          methodVisitor.init(type, fqn, sig, null, access & METHOD_ACCESS_MASK);
        } else {
          methodVisitor.init(type, fqn, sig, rawSig, access & METHOD_ACCESS_MASK);
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
          relationWriter.writeRelation(Relation.CONTAINS, fqnStack.getFqn(), fqn, location);
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
    private Entity type;
    private String fqn;
    private String sig;
    private String rawSig;
    private int mods;
    
    // Metric related things
    private int instructionCount;
    private int statementCount;
    private Set<Label> offsets = new HashSet<>();
    private Set<String> operators = new HashSet<>();
    private Set<String> operands = new HashSet<>();

    public MethodVisitorImpl() {
      super(Opcodes.V1_7);
    }

    private void init(Entity type, String fqn, String sig, String rawSig, int mods) {
      this.type = type;
      this.fqn = fqn;
      this.sig = sig;
      this.rawSig = rawSig;
      this.mods = mods;
      
      instructionCount = 0;
      statementCount = 0;
      offsets.clear();
      operators.clear();
      operands.clear();
    }
    
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
//      logger.info("foo");
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
      instructionCount++;
      if (name.indexOf('$') == -1) {
        switch (opcode) {
          case Opcodes.GETFIELD:
          case Opcodes.GETSTATIC:
            relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), convertNameToFqn(owner) + "." + name, location);
            operands.add(owner + name);
            break;
          case Opcodes.PUTFIELD:
          case Opcodes.PUTSTATIC:
            relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), convertNameToFqn(owner) + "." + name, location);
            statementCount++;
            operands.add(owner + name);
            break;
          default:
            logger.severe("Unknown field instruction: " + opcode);
        }
      }
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      instructionCount++;
      statementCount++;
      operators.add("$op84");
      operands.add("$var" + var);
      operands.add("$int" + increment);
    }

    @Override
    public void visitInsn(int opcode) {
      switch (opcode) {
        case Opcodes.ACONST_NULL: operands.add("$null"); break;
        case Opcodes.ICONST_M1: operands.add("$int-1"); break;
        case Opcodes.ICONST_0: operands.add("$int0"); break;
        case Opcodes.ICONST_1: operands.add("$int1"); break;
        case Opcodes.ICONST_2: operands.add("$int2"); break;
        case Opcodes.ICONST_3: operands.add("$int3"); break;
        case Opcodes.ICONST_4: operands.add("$int4"); break;
        case Opcodes.ICONST_5: operands.add("$int5"); break;
        case Opcodes.LCONST_0: operands.add("$long0"); break;
        case Opcodes.LCONST_1: operands.add("$long1"); break;
        case Opcodes.FCONST_0: operands.add("$float0"); break;
        case Opcodes.FCONST_1: operands.add("$float1"); break;
        case Opcodes.FCONST_2: operands.add("$float2"); break;
        case Opcodes.DCONST_0: operands.add("$double0"); break;
        case Opcodes.DCONST_1: operands.add("$double1"); break;
        case Opcodes.IALOAD:
        case Opcodes.LALOAD:
        case Opcodes.FALOAD:
        case Opcodes.DALOAD:
        case Opcodes.AALOAD:
        case Opcodes.BALOAD:
        case Opcodes.CALOAD:
        case Opcodes.SALOAD:
        case Opcodes.IASTORE:
        case Opcodes.LASTORE:
        case Opcodes.FASTORE:
        case Opcodes.DASTORE:
        case Opcodes.AASTORE:
        case Opcodes.BASTORE:
        case Opcodes.CASTORE:
        case Opcodes.SASTORE:
          operators.add("$arr-deref");
          break;
        case Opcodes.IADD:
        case Opcodes.LADD:
        case Opcodes.FADD:
        case Opcodes.DADD:
          operators.add("$add");
          break;
        case Opcodes.ISUB:
        case Opcodes.LSUB:
        case Opcodes.FSUB:
        case Opcodes.DSUB:
          operators.add("$sub");
          break;
        case Opcodes.IMUL:
        case Opcodes.LMUL:
        case Opcodes.FMUL:
        case Opcodes.DMUL:
          operators.add("$mul");
          break;
        case Opcodes.IDIV:
        case Opcodes.LDIV:
        case Opcodes.FDIV:
        case Opcodes.DDIV:
          operators.add("$div");
          break;
        case Opcodes.IREM:
        case Opcodes.LREM:
        case Opcodes.FREM:
        case Opcodes.DREM:
          operators.add("$rem");
          break;
        case Opcodes.INEG:
        case Opcodes.LNEG:
        case Opcodes.FNEG:
        case Opcodes.DNEG:
          operators.add("$neg");
          break;
        case Opcodes.ISHL:
        case Opcodes.LSHL:
          operators.add("$shl");
          break;
        case Opcodes.ISHR:
        case Opcodes.LSHR:
          operators.add("$shr");
          break;
        case Opcodes.IUSHR:
        case Opcodes.LUSHR:
          operators.add("$ushr");
          break;
        case Opcodes.IAND:
        case Opcodes.LAND:
          operators.add("$bw-and");
          break;
        case Opcodes.IOR:
        case Opcodes.LOR:
          operators.add("$bw-or");
          break;
        case Opcodes.IXOR:
        case Opcodes.LXOR:
          operators.add("$bw-xor");
          break;
        case Opcodes.I2L:
        case Opcodes.F2L:
        case Opcodes.D2L:
          operators.add("long");
          break;
        case Opcodes.I2F:
        case Opcodes.L2F:
        case Opcodes.D2F:
          operators.add("float");
          break;
        case Opcodes.I2D:
        case Opcodes.L2D:
        case Opcodes.F2D:
          operators.add("double");
          break;
        case Opcodes.L2I:
        case Opcodes.F2I:
        case Opcodes.D2I:
          operators.add("int");
          break;
        case Opcodes.I2B:
          operators.add("byte");
          break;
        case Opcodes.I2C:
          operators.add("char");
          break;
        case Opcodes.I2S:
          operators.add("short");
          break;
        case Opcodes.LCMP:
        case Opcodes.FCMPL:
        case Opcodes.FCMPG:
        case Opcodes.DCMPL:
        case Opcodes.DCMPG:
          operators.add("$cmp");
          break;
        case Opcodes.IRETURN:
        case Opcodes.LRETURN:
        case Opcodes.FRETURN:
        case Opcodes.DRETURN:
        case Opcodes.ARETURN:
        case Opcodes.RETURN:
          statementCount++;
          operators.add("$return");
          break;
        case Opcodes.ARRAYLENGTH:
          operators.add("$length");
          break;
        case Opcodes.ATHROW:
          statementCount++;
          operators.add("throw");
          break;
        case Opcodes.MONITORENTER:
        case Opcodes.MONITOREXIT:
          operators.add("synchronized");
          break;
        default:
          break;
      }
      instructionCount++;
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
      switch (opcode) {
        case Opcodes.BIPUSH:
        case Opcodes.SIPUSH:
          operands.add("$int" + operand);
          break;
        case Opcodes.NEWARRAY:
          operators.add("$new-arr");
          break;
        default:
          logger.severe("Unknown int instruction: " + opcode);
          break;
      }
      instructionCount++;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      switch (opcode) {
        case Opcodes.IFEQ:
        case Opcodes.IFNE:
        case Opcodes.IFLT:
        case Opcodes.IFGE:
        case Opcodes.IFGT:
        case Opcodes.IFLE:
        case Opcodes.IF_ICMPEQ:
        case Opcodes.IF_ICMPNE:
        case Opcodes.IF_ICMPLT:
        case Opcodes.IF_ICMPGE:
        case Opcodes.IF_ICMPGT:
        case Opcodes.IF_ICMPLE:
        case Opcodes.IF_ACMPEQ:
        case Opcodes.IF_ACMPNE:
        case Opcodes.JSR:
          operators.add("$op" + opcode);
          break;
        case Opcodes.GOTO:
          operators.add("$op" + opcode);
          break;
        case Opcodes.IFNULL:
        case Opcodes.IFNONNULL:
          operators.add("$op" + opcode);
          operands.add("$null");
          break;
        default:
          logger.severe("Unknown jump instruction: " + opcode);
          break;
      }
      instructionCount++;
      statementCount++;
    }

    @Override
    public void visitLdcInsn(Object cst) {
      if (cst instanceof Integer) {
        operands.add("$int" + cst);
      } else if (cst instanceof Float) {
        operands.add("$float" + cst);
      } else if (cst instanceof Long) {
        operands.add("$long" + cst);
      } else if (cst instanceof Double) {
        operands.add("$double" + cst);
      } else if (cst instanceof String) {
        operands.add("$string" + cst);
      } else if (cst instanceof Type) {
        operands.add("$type" + cst);
      } else {
        logger.severe("Unknown constant type: " + cst);
      }
      instructionCount++;
    }
    
    @Override
    public void visitTypeInsn(int opcode, String desc) {
      instructionCount++;
      switch (opcode) {
        case Opcodes.INSTANCEOF:
          operators.add("$op" + opcode);
          operands.add(desc);
          relationWriter.writeRelation(Relation.CHECKS, fqnStack.getFqn(), convertNameToFqn(desc), location);
          statementCount++;
          break;
        case Opcodes.CHECKCAST:
          operators.add("$op" + opcode);
          operands.add(desc);
          relationWriter.writeRelation(Relation.CASTS, fqnStack.getFqn(), convertNameToFqn(desc), location);
          statementCount++;
          break;
        case Opcodes.NEW:
          operators.add("$op" + opcode);
          relationWriter.writeRelation(Relation.INSTANTIATES, fqnStack.getFqn(), convertNameToFqn(desc), location);
          break;
        case Opcodes.ANEWARRAY:
          operators.add("$new-arr");
          break;
        default:
          logger.severe("Unknown type instruction: " + opcode);
      }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      operands.add("$var" + var);
      instructionCount++;
      switch (opcode) {
        case Opcodes.ILOAD:
        case Opcodes.LLOAD:
        case Opcodes.FLOAD:
        case Opcodes.DLOAD:
        case Opcodes.ALOAD:
          break;
        case Opcodes.ISTORE:
        case Opcodes.LSTORE:
        case Opcodes.FSTORE:
        case Opcodes.DSTORE:
        case Opcodes.ASTORE:
          statementCount++;
      }
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      operators.add("$switch");
      for (int key : keys) {
        operands.add("int" + key);
      }
      instructionCount++;
      statementCount++;
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      instructionCount++;
      operands.add(owner + name + desc);
      if (!name.startsWith("access$")) {
        new SignatureReader(desc).accept(methodSignatureVisitor.init(owner, name));
        String fqn = methodSignatureVisitor.getReferenceFqn();
        relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, location);
      }
      statementCount++;
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
      instructionCount++;
      operators.add("$new-arr");
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label ... labels) {
      operators.add("$switch");
      instructionCount++;
      statementCount++;
    }

    @Override
    public void visitLabel(Label label) {
      offsets.add(label);
    }
    
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
      operators.add("$try");
      operators.add("$catch");
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
      operands.add(desc);
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
      Metrics metrics = new Metrics();
      metrics.addMetric(Metric.BC_CYCLOMATIC_COMPLEXITY, 1 + offsets.size());
      metrics.addMetric(Metric.BC_NUMBER_OF_STATEMENTS, statementCount);
      metrics.addMetric(Metric.BC_NUMBER_OF_INSTRUCTIONS, instructionCount);
      metrics.addMetric(Metric.BC_VOCABULARY_SIZE, operators.size() + operands.size());
      entityWriter.writeEntity(type, fqn, sig, rawSig, mods, metrics, location);
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
    private Map<String, Collection<String>> bounds = new HashMap<>();
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
      currentBound = new LinkedList<>();
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
    private Map<String, Collection<String>> bounds = new HashMap<>();
    private Collection<String> currentBound;
    private Relation currentType;
    
    private Collection<String> paramTypes = new LinkedList<>();

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
      } else if (currentType == Relation.CONTAINS) {
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
      currentBound = new LinkedList<>();
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
      currentType = Relation.CONTAINS;
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
          relationWriter.writeRelation(Relation.CONTAINS, fqnStack.getFqn(), getReferenceFqn(), location);
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
