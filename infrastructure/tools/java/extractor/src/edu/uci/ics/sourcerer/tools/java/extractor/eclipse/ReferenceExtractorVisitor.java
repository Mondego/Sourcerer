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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import static com.google.common.base.Preconditions.*;

import edu.uci.ics.sourcerer.tools.java.extractor.metrics.LinesOfCode;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.CommentWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.EntityWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.FileWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ImportWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.LocalVariableWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ProblemWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.RelationWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Comment;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;
import edu.uci.ics.sourcerer.tools.java.model.types.Problem;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ReferenceExtractorVisitor extends ASTVisitor {
  private static final String UNKNOWN = "1_UNKNOWN_";
  
  private FileWriter fileWriter;
  private ProblemWriter problemWriter;
  private ImportWriter importWriter;
  private CommentWriter commentWriter;
  private EntityWriter entityWriter;
  private LocalVariableWriter localVariableWriter;
  private RelationWriter relationWriter;
  
  private String compilationUnitName = null;
  private String compilationUnitPath = null;
  private String compilationUnitSource = null;
  private JavaFile javaFile = null;

  private boolean inLhsAssignment = false;
  
  private boolean bindingFree = false;
  
  private NamingAdvisor advisor;
  
  private FQNStack fqnStack = new FQNStack();

  public ReferenceExtractorVisitor(WriterBundle writers) {
    fileWriter = writers.getFileWriter();
    problemWriter = writers.getProblemWriter();
    importWriter = writers.getImportWriter();
    commentWriter = writers.getCommentWriter();
    entityWriter = writers.getEntityWriter();
    localVariableWriter = writers.getLocalVariableWriter();
    relationWriter = writers.getRelationWriter();
  }

  public void setBindingFreeMode(boolean bindingFree) {
    this.bindingFree = bindingFree;
  }
  
  public void setCompilationUnitSource(String source) {
    this.compilationUnitSource = source;
  }
  
  public void setJavaFile(JavaFile file) {
    this.javaFile = file;
  }
  
  public void setAdvisor(NamingAdvisor advisor) {
    this.advisor = advisor;
  }
  
  /**
   * This method writes:
   * <ul>
   * <li>File path to <code>IFileWriter</code>.</li>
   * <li>Problems associated with compilation of this file to <code>IProblemWriter</code>.</li>
   * <li>All the non-associated comments from the file to <code>ICommentWriter</code>.</li>
   * </ul>
   */
  @Override
  public boolean visit(CompilationUnit node) {
    fqnStack.clear();
    // Get the file path
    compilationUnitName = node.getJavaElement().getElementName();
    if (javaFile == null) {
      if (node.getPackage() == null) {
        compilationUnitPath = compilationUnitName;
      } else {
        compilationUnitPath = node.getPackage().getName() + "." + compilationUnitName;
      }
    } else {
      compilationUnitPath = javaFile.getFile().getRelativePath().toString();
    } 
    
    // Get the package fqn
    if (node.getPackage() == null) {
      entityWriter.writeEntity(Entity.PACKAGE, "default", 0, null, null);
      fqnStack.push("default", Entity.PACKAGE);
    } else {
      entityWriter.writeEntity(Entity.PACKAGE, node.getPackage().getName().getFullyQualifiedName(), 0, null, null);
      fqnStack.push(node.getPackage().getName().getFullyQualifiedName(), Entity.PACKAGE);
    }
    
    return true;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void endVisit(CompilationUnit node) {
    // Write the file path
    fileWriter.writeFile(File.SOURCE, compilationUnitName, createMetrics(node), compilationUnitPath);
    
    // Write the problems
    for (IProblem problem : node.getProblems()) {
      if (problem.isError()) {
        problemWriter.writeProblem(Problem.ERROR, compilationUnitPath, problem.getID(), problem.getMessage());
      } else {
        problemWriter.writeProblem(Problem.WARNING, compilationUnitPath, problem.getID(), problem.getMessage());
      }
    }
    
    // Visit the comments
    for (ASTNode comment : (List<ASTNode>)node.getCommentList()) {
      // Skip the associated comments
      if (comment.getParent() == null) {
        comment.accept(this);
      }
    }
    
    compilationUnitPath = null;
    fqnStack.pop();
  }

  /**
   * This method writes:
   * <ul>
   * <li>Import statement to <code>IImportWriter</code>.</li>
   * </ul>
   */
  @Override
  public boolean visit(ImportDeclaration node) {
    importWriter.writeImport(node.getName().getFullyQualifiedName(), node.isStatic(), node.isOnDemand(), createLocation(node));
    if (!node.isStatic() && !node.isOnDemand()) {
      advisor.addImport(node.getName().getFullyQualifiedName());
    }
    return false;
  }

  /**
   * This method writes:
   * <ul>
   *   <li>Non-anonymous class or interface entity to <code>IEntityWriter</code>.
   *   <ul>
   *     <li>Inside relation to <code>IRelationWriter</code>.</li>
   *     <li>Extends relation to <code>IRelationWriter</code>.</li>
   *     <li>Implements relation to <code>IRelationWriter</code>.</li>
   *     <li>Synthesized constructors to <code>IEntityWriter</code>.
   *     <ul>
   *       <li>Inside relation to <code>IRelationWriter</code>.</li>
   *       <li>Implicit superconstructor calls to <code>IRelationWriter</code>.</li>
   *     </ul></li>
   *   </ul></li>
   * </ul>
   * 
   * Class/interface fully qualified names (FQNs) adhere to the following format:
   * <ul>
   * <li>Top-level: package fqn + . + simple name</li>
   * <li>Member: parent fqn + $ + simple name</li>
   * <li>Local: parent fqn + $ + incrementing counter + simple name</li>
   * </ul>
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(TypeDeclaration node) {
    // Get the fqn
    String parentFqn = null;
    String fqn = null;
    Entity type = null;
    ITypeBinding binding = node.resolveBinding();
    if (binding == null && !bindingFree) {
      throw new IllegalStateException("Binding resolution appears to have failed!");
    }
    if (node.isPackageMemberTypeDeclaration()) {
      EnclosingPackage enc = fqnStack.peek(EnclosingPackage.class);
      fqn = enc.getTypeFqn(node.getName().getIdentifier());
      parentFqn = enc.getFqn();
    } else if (node.isMemberTypeDeclaration()) {
      if (node.getName().getIdentifier().length() > 0) {
        EnclosingDeclaredType enc = fqnStack.peek(EnclosingDeclaredType.class);
        fqn = enc.getMemberFqn(node.getName().getIdentifier());
        parentFqn = enc.getFqn();
      } else {
        throw new IllegalStateException("A type declaration should not declare an annonymous type!");
      }
    } else if (node.isLocalTypeDeclaration()) {
      EnclosingBlock enc = fqnStack.peek(EnclosingBlock.class);
      if (binding == null) {
        fqn = createUnknownFqn(node.getName().getIdentifier());
      } else {
        fqn = enc.getLocalFqn(node.getName().getIdentifier(), binding);
      }
      parentFqn = enc.getFqn();
    } else {
      throw new IllegalStateException("Unknown declaration: " + node);
    }
    
    if (node.isInterface()) {
      type = Entity.INTERFACE;
    } else {
      type = Entity.CLASS;
    }
    
    // Push the stack
    fqnStack.push(fqn, type);
    
    // Visit the children
    accept(node.getJavadoc());
    accept(node.modifiers());
    accept(node.typeParameters());
    accept(node.getSuperclassType());
    accept(node.superInterfaceTypes());
    accept(node.bodyDeclarations());
    
    // Write the contains relation
    relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fqn, createUnknownLocation());
    
    // Write the entity
    entityWriter.writeEntity(type, fqn, node.getModifiers(), createMetrics(node), createLocation(node));
    
    // Write the extends relation
    Type superType = node.getSuperclassType();
    String superFqn = null;
    if (superType == null) {
      if (!fqn.equals("java.lang.Object")) {
        superFqn = "java.lang.Object";
        relationWriter.writeRelation(Relation.EXTENDS, fqn, superFqn, createUnknownLocation());
      }
    } else {
      superFqn = getTypeFqn(superType);
      relationWriter.writeRelation(Relation.EXTENDS, fqn, superFqn, createLocation(superType));
    }
    
    // Write the implements relation
    List<Type> superInterfaceTypes = node.superInterfaceTypes();
    for (Type superInterfaceType : superInterfaceTypes) {
      String superInterfaceFqn = getTypeFqn(superInterfaceType);
      relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, superInterfaceFqn, createLocation(superInterfaceType));
    }
    
    if (binding != null) {
      if (binding.isAnonymous()) {
        logger.log(Level.SEVERE, "A type declaration should not declare an annonymous type!");
      } else {
        // Write out the synthesized constructors
        for (IMethodBinding method : binding.getDeclaredMethods()) {
          if (method.isDefaultConstructor()) {
            // Write the entity
            String constructorFqn = getMethodName(method, true);
            entityWriter.writeEntity(Entity.CONSTRUCTOR, constructorFqn, "()", null, method.getModifiers(), null, createUnknownLocation());
            constructorFqn += "()";
            // Write the contains relation
            relationWriter.writeRelation(Relation.CONTAINS, fqn, constructorFqn, createUnknownLocation());
            
            // Write the calls relation (implicit superconstructor call)
            if (superFqn == null) {
              relationWriter.writeRelation(Relation.CALLS, constructorFqn, "java.lang.Object.<init>()", createUnknownLocation());
            } else {
              if (superType == null) {
                relationWriter.writeRelation(Relation.CALLS, constructorFqn, superFqn + ".<init>()", createUnknownLocation());
              } else {
                relationWriter.writeRelation(Relation.CALLS, constructorFqn, getErasedTypeFqn(superType)+ ".<init>()", createUnknownLocation());
              }
            }
          }
        }
      }
    }

    fqnStack.pop();
    
    return false;
  }

  /**
   * This method writes:
   * <ul>
   *   <li>For anonymous classes:
   *   <ul>
   *     <li>Anonymous class entity to <code>IEntityWriter</code>.</li>
   *     <li>Inside relation to <code>IRelationWriter</code>.</li>
   *     <li>Extends relation to <code>IRelationWriter</code>.</li>
   *     <li>Implements relation to <code>IRelationWriter</code>.</li>
   *     <li>Synthesized constructor to <code>IEntityWriter</code>.
   *     <ul>
   *       <li>Inside relation to <code>IRelationWriter</code>.</li>
   *       <li>Parameters for the synthesized constructor to <code>ILocalVariableWriter</code>.</li>
   *       <li>Implicit superconstructor calls to <code>IRelationWriter</code>.</li>
   *     </ul></li>
   *   </ul></li>
   *   <li>Does nothing for enum constant declarations.</li>
   * </ul>
   * 
   * Anonymous class fully qualified names (FQNs) adhere to the following format:<br> 
   * parent fqn + $anonymous- + incrementing counter
   * 
   */
  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    // If it's an anonymous class
    if (node.getParent().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
      ClassInstanceCreation parent = (ClassInstanceCreation) node.getParent();
      
      ITypeBinding binding = node.resolveBinding();
      
      // Get the fqn
      String fqn = fqnStack.find(EnclosingDeclaredType.class).createAnonymousClassFqn(binding);
//      String fqn = null;
//      if (binding == null) {
//        fqn = fqnStack.find(EnclosingDeclaredType.class).getAnonymousClassFqn();
//      } else {
//        fqn = getTypeFqn(binding);
//      }
      
      // Push the stack
      String parentFqn = fqnStack.getFqn();
      Location parentLocation = createLocation(parent);
      fqnStack.push(fqn, Entity.CLASS);
      
      // Visit the children
      accept(node.bodyDeclarations());
      
      // Write the entity
      entityWriter.writeEntity(Entity.CLASS, fqn, 0, createMetrics(node), createLocation(node));
    
      // Write the contains relation
      relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fqn, createUnknownLocation());
      
      Type superType = parent.getType();
      ITypeBinding superBinding = safeResolve(superType);
      
      if (superBinding == null) {
        // Write the uses relation
        // Can't do extends/implements because unsure if it's an interface or class type
//        relationWriter.writeUses(fqn, getTypeFqn(superType), getLocation(superType));
      } else {
        if (superBinding.isInterface()) {
          // Write the implements relation
          relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, getTypeFqn(superBinding), createLocation(superType));
          
          relationWriter.writeRelation(Relation.EXTENDS, fqn, "java.lang.Object", createUnknownLocation());
        } else {
          // Write the extends relation
          relationWriter.writeRelation(Relation.EXTENDS, fqn, getTypeFqn(superBinding), createLocation(superType));
        }
      }
      
      if (binding != null) {
        // Write out the synthesized constructors
        for (IMethodBinding method : binding.getDeclaredMethods()) {
          if (method.isConstructor()) {
            // Write the entity
            String args = getMethodArgs(method);
            String rawArgs = getErasedMethodArgs(method);
            String basicFqn = fqn + ".<init>";
            String constructorFqn = basicFqn + args;
            if (args.equals(rawArgs)) {
              entityWriter.writeEntity(Entity.CONSTRUCTOR, basicFqn, args, null, method.getModifiers(), null, createUnknownLocation());
            } else {
              entityWriter.writeEntity(Entity.CONSTRUCTOR, basicFqn, args, rawArgs, method.getModifiers(), null, createUnknownLocation());
            }

            // Write the contains relation
            relationWriter.writeRelation(Relation.CONTAINS, fqn, constructorFqn, createUnknownLocation());

            // Write the instantiates relation
            relationWriter.writeRelation(Relation.INSTANTIATES, parentFqn, fqn, parentLocation);
            
            // Write the calls relation            
            relationWriter.writeRelation(Relation.CALLS, parentFqn, constructorFqn, parentLocation);

            // Write the parameters
            int count = 0;
            for (ITypeBinding param : method.getParameterTypes()) {
              localVariableWriter.writeLocalVariable(LocalVariable.PARAM, "(ANONYMOUS)", 0, getTypeFqn(param), createUnknownLocation(), constructorFqn, count++, createUnknownLocation());
            }
            
            // Reference the superconstructor
            ITypeBinding superClassBinding = binding.getSuperclass();
            if (superClassBinding != null) {
              superClassBinding = superClassBinding.getErasure();
            }
            if (superClassBinding != null) {
              String superFqn = getTypeFqn(superClassBinding);
              String superConstructorFqn = superFqn + ".<init>" + rawArgs;
              relationWriter.writeRelation(Relation.CALLS, constructorFqn, superConstructorFqn, createUnknownLocation());
            }
          }
        }
      }
      fqnStack.pop();
    } else if (node.getParent().getNodeType() == ASTNode.ENUM_CONSTANT_DECLARATION) {
      ITypeBinding binding = node.resolveBinding();
      
      // Get the fqn
      String parentFqn = fqnStack.getFqn();
      String fqn = fqnStack.find(EnclosingDeclaredType.class).createAnonymousClassFqn(binding);
      
      fqnStack.push(fqn, Entity.CLASS);
      
      // Write the entity
      entityWriter.writeEntity(Entity.CLASS, fqn, 0, createMetrics(node), createLocation(node));
    
      // Write the contains relation
      relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fqn, createUnknownLocation());
      
      // Write the extends relation
      relationWriter.writeRelation(Relation.EXTENDS, fqn, fqnStack.find(EnclosingDeclaredType.class).getFqn(), createUnknownLocation());
      
      if (binding != null) {
        // Write out the synthesized constructors
        for (IMethodBinding method : binding.getDeclaredMethods()) {
          if (method.isConstructor()) {
            // Write the entity
            String args = getMethodArgs(method);
            String rawArgs = getErasedMethodArgs(method);
            String basicFqn = fqn + ".<init>";
            String constructorFqn = basicFqn + args;
            if (args.equals(rawArgs)) {
              entityWriter.writeEntity(Entity.CONSTRUCTOR, basicFqn, args, null, method.getModifiers(), null, createUnknownLocation());
            } else {
              entityWriter.writeEntity(Entity.CONSTRUCTOR, basicFqn, args, rawArgs, method.getModifiers(), null, createUnknownLocation());
            }
            

            // Write the contains relation
            relationWriter.writeRelation(Relation.CONTAINS, fqn, constructorFqn, createUnknownLocation());

//            // Write the instantiates relation
//            relationWriter.writeRelation(Relation.INSTANTIATES, parentFqn, fqn, parentLocation);
//            
//            // Write the calls relation            
//            relationWriter.writeRelation(Relation.CALLS, parentFqn, constructorFqn, parentLocation);

            // Write the parameters
            int count = 0;
            for (ITypeBinding param : method.getParameterTypes()) {
              localVariableWriter.writeLocalVariable(LocalVariable.PARAM, "(ANONYMOUS)", 0, getTypeFqn(param), createUnknownLocation(), constructorFqn, count++, createUnknownLocation());
            }
            
            // Reference the superconstructor
            ITypeBinding superClassBinding = binding.getSuperclass();
            if (superClassBinding != null) {
              superClassBinding = superClassBinding.getErasure();
            }
            if (superClassBinding != null) {
              String superFqn = getTypeFqn(superClassBinding);
              String superConstructorFqn = superFqn + ".<init>" + args;
              relationWriter.writeRelation(Relation.CALLS, constructorFqn, superConstructorFqn, createUnknownLocation());
            }
          }
        }
      }
      fqnStack.pop();
    } else {
      throw new IllegalStateException("Unexpected parent node type: " + node);
    }
    
    return false;
  }

  /**
   * This method writes:
   * <ul>
   *   <li>Enum entity to <code>IEntityWriter</code>.
   *   <ul>
   *     <li>Inside relation to <code>IRelationWriter</code>.</li>
   *     <li>Implements relation to <code>IRelationWriter</code>.</li>
   *     <li>Parametrized by relation to <code>IRelationWriter</code>.</li>
   *     <li>Synthesized constructors to <code>IEntityWriter</code>.
   *     <ul>
   *       <li>Inside relation to <code>IRelationWriter</code>.</li>
   *     </ul></li>
   *   </ul></li>
   * </ul>
   * 
   * Enum qualified names (FQNs) adhere to the following format:
   * <ul>
   * <li>Top-level: package fqn + . + simple name</li>
   * <li>Member: parent fqn + $ + simple name</li>
   * </ul>
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(EnumDeclaration node) {
    // Get the fqn
    String fqn = null;
    if (node.isPackageMemberTypeDeclaration()) {
      fqn = fqnStack.peek(EnclosingPackage.class).getTypeFqn(node.getName().getIdentifier());
    } else if (node.isMemberTypeDeclaration()) {
      fqn = fqnStack.peek(EnclosingDeclaredType.class).getMemberFqn(node.getName().getIdentifier());
    } else if (node.isLocalTypeDeclaration()) {
      throw new IllegalStateException("Can't have local enums!");
    } else {
      logger.log(Level.SEVERE, "Unsure what type the declaration is!");
      fqn = "(ERROR)";
    }

    String parentFqn = fqnStack.getFqn();
    fqnStack.push(fqn, Entity.ENUM);
    
    // Visit the children
    accept(node.getJavadoc());
    accept(node.enumConstants());
    accept(node.bodyDeclarations());
    
    Location unknown = createUnknownLocation();
    
    // Write the entity
    entityWriter.writeEntity(Entity.ENUM, fqn, node.getModifiers(), createMetrics(node), createLocation(node));
    
    // Write the contains relation
    relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fqn, unknown);
    
    // Write the implements relation
    for (Type superInterfaceType : (List<Type>) node.superInterfaceTypes()) {
      relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, getTypeFqn(superInterfaceType), createLocation(superInterfaceType));
    }
    
    // Write the extends relation
    relationWriter.writeRelation(Relation.EXTENDS, fqn, "java.lang.Enum<" + fqn + ">", unknown);
    
    ITypeBinding binding = node.resolveBinding();
    if (binding != null) {
      // Write out the synthesized constructors
      for (IMethodBinding method : binding.getDeclaredMethods()) {
        if (method.isDefaultConstructor()) {
          // Write the entity
          String constructorFqn = getMethodName(method, true);
          entityWriter.writeEntity(Entity.CONSTRUCTOR, constructorFqn, "()", null, method.getModifiers(), createMetrics(node), unknown);
          constructorFqn += "()";

          // Write the contains relation
          relationWriter.writeRelation(Relation.CONTAINS, fqn, constructorFqn, unknown);
          
          // Write the calls relation
          relationWriter.writeRelation(Relation.CALLS, constructorFqn, "java.lang.Enum.<init>(java.lang.String,int)", unknown);
        }
      }
    }
    
    // Write the values method
    {
      String methodFqn = fqn + ".values";
      entityWriter.writeEntity(Entity.METHOD, methodFqn, "()", null, 9, null, unknown);
      methodFqn += "()";
      relationWriter.writeRelation(Relation.CONTAINS, fqn, methodFqn, unknown);
      relationWriter.writeRelation(Relation.RETURNS, methodFqn, fqn + "[]", unknown);
    }

    // Write the valueOf method
    {
      String methodFqn = fqn + ".valueOf";
      entityWriter.writeEntity(Entity.METHOD, methodFqn, "(java.lang.String)", null, 9, null, unknown);
      methodFqn += "(java.lang.String)";
      relationWriter.writeRelation(Relation.CONTAINS, fqn, methodFqn, unknown);
      relationWriter.writeRelation(Relation.RETURNS, methodFqn, fqn, unknown);
      localVariableWriter.writeLocalVariable(LocalVariable.PARAM, "name", 0, "java.lang.String", unknown, methodFqn, 0, unknown);
    }
    
    fqnStack.pop();
    return false;
  }
  
  /**
   * This method writes:
   * <ul>
   *   <li>Enum constant entity to <code>IEntityWriter</code>
   *   <ul>
   *     <li>Inside relation to <code>IRelationWriter</code></li>
   *     <li>Holds relation to <code>IRelationWriter</code></li>
   *     <li>Instantiated relation to<code>IRelationWriter</code></li>
   *   </ul></li>
   * </ul>
   * 
   * Enum constant fully qualified names (FQNs) adhere to the following format:<br> 
   * parent fqn + . + simple name
   */
  @Override
  public boolean visit(EnumConstantDeclaration node) {
    // Get the fqn
    String fqn = fqnStack.peek(EnclosingDeclaredType.class).getFieldFqn(node.getName().getIdentifier());
    
    // Write the inside relation
    relationWriter.writeRelation(Relation.CONTAINS, fqnStack.getFqn(), fqn, createUnknownLocation());
    
    // Write the holds relation
    relationWriter.writeRelation(Relation.HOLDS, fqn, fqnStack.getFqn(), createUnknownLocation());
    
    // Write the calls relation
    IMethodBinding methodBinding = node.resolveConstructorBinding();
    if (methodBinding == null) {
      String methodFqn = fqnStack.getFqn() + ".<init>" + getFuzzyMethodArgs(node.arguments());
      relationWriter.writeRelation(Relation.CALLS, fqn, methodFqn, createLocation(node.getName()));
    } else {
      relationWriter.writeRelation(Relation.CALLS, fqn, getMethodName(methodBinding, false) + getMethodArgs(methodBinding), createLocation(node.getName()));
    }
    
    // Write the instantiates relation
    relationWriter.writeRelation(Relation.INSTANTIATES, fqn, fqnStack.getFqn(), createLocation(node.getName()));
    
    // Visit the children
    fqnStack.push(fqn, Entity.ENUM_CONSTANT);
    accept(node.arguments());
    accept(node.getAnonymousClassDeclaration());
    
    // Write the entity
    entityWriter.writeEntity(Entity.ENUM_CONSTANT, fqn, 0, createMetrics(node), createLocation(node));
    
    fqnStack.pop();
    return false;
  }

  /**
   * This method writes:
   * <ul>
   *   <li>Initializer entity to <code>IEntityWriter</code>
   *   <ul>
   *     <li>Inside relation to <code>IRelationWriter</code></li>
   *   </ul></li>
   * </ul>
   * 
   * Enum constant fully qualified names (FQNs) adhere to the following format:<br> 
   * parent fqn + . + simple name
   */
  @Override
  public boolean visit(Initializer node) {
    // Get the fqn
    String fqn = fqnStack.peek(EnclosingDeclaredType.class).getInitializerFqn();

    String parentFqn = fqnStack.getFqn();
    
    // Visit the children
    fqnStack.push(fqn, Entity.INITIALIZER);
    accept(node.getJavadoc());
    accept(node.modifiers());
    accept(node.getBody());

    // Write the entity
    entityWriter.writeEntity(Entity.INITIALIZER, fqn, node.getModifiers(), createMetrics(node), createLocation(node));

    // Write the contains relation
    relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fqn, createUnknownLocation());
    
    fqnStack.pop();
    
    return false;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    // Javadoc and modifiers are handled by the childen
    accept(node.fragments());
    return false;
  }

  /**
   * This method writes:
   * <ul>
   *   <li>Parametrized by relation to <code>IRelationWriter</code>.</li>
   * </ul>
   */
  @Override
  public boolean visit(TypeParameter node) {
    // Write the parametrized by relation
    relationWriter.writeRelation(Relation.PARAMETRIZED_BY, fqnStack.getFqn(), getTypeParam(node), createLocation(node));
    
    accept(node.typeBounds());
    return false;
  }
  
  /**
   * This method writes:
   * <ul>
   *   <li>Local variable entity to <code>ILocalVariableWriter</code></li>
   *   <li>Uses relation to <code>IRelationWriter</code></li>
   * </ul>
   */
  @Override
  public boolean visit(SingleVariableDeclaration node) {
    IVariableBinding binding = node.resolveBinding();
    Type type = node.getType();

    if (binding != null) {
      String typeFqn = getTypeFqn(binding.getType());
      if (binding.isParameter()) {
        // Write the parameter
        int param = fqnStack.peek(EnclosingMethod.class).getNextParameterPos();
        localVariableWriter.writeLocalVariable(LocalVariable.PARAM, binding.getName(), binding.getModifiers(), typeFqn, createLocation(type), fqnStack.getFqn(), param, createLocation(node.getName()));
        fqnStack.push(fqnStack.getFqn() + "#" + param, Entity.PARAMETER);
        accept(node.modifiers());
        fqnStack.pop();
        accept(node.getType());
        accept(node.getName());
        accept(node.getInitializer());
        return false;
      } else {
        // Write the local variable
        localVariableWriter.writeLocalVariable(LocalVariable.LOCAL, binding.getName(), binding.getModifiers(), typeFqn, createLocation(type), fqnStack.getFqn(), null, createLocation(node.getName()));
        return true;
      }
    } else {
      return true;
    }
  }

  /**
   * This method writes:
   * <ul>
   *   <li>Field entity to <code>IEntityWriter</code>.
   *   <ul>
   *     <li>Inside relation to <code>IRelationWriter</code>.</li>
   *     <li>Holds relation to <code>IRelationWriter</code>.</li>
   *   </ul></li>
   *   <li>Local variable to <code>ILocalVariableWriter</code>.
   *   <ul>
   *     <li>Uses relation to <code>IRelationWriter</codE>.</li>
   *   </ul></li>
   * </ul>
   * 
   * Field fully qualified names (FQNs) adhere to the following format:<br> 
   * parent fqn + . + simple name
   */
  @Override
  public boolean visit(VariableDeclarationFragment node) {
    if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
      FieldDeclaration parent = (FieldDeclaration) node.getParent();
      
      // Get the fqn
      String fqn = fqnStack.peek(EnclosingDeclaredType.class).getFieldFqn(node.getName().getIdentifier());
      
      // Write the entity
      entityWriter.writeEntity(Entity.FIELD, fqn, parent.getModifiers(), createMetrics(node), createLocation(node));
      
      // Write the contains relation
      relationWriter.writeRelation(Relation.CONTAINS, fqnStack.getFqn(), fqn, createUnknownLocation());
      
      Type type = parent.getType();
      String typeFqn = getTypeFqn(type);
      
      // Write the holds relation
      relationWriter.writeRelation(Relation.HOLDS, fqn, typeFqn, createLocation(type));
      
      // Add the field to the fqnstack
      fqnStack.push(fqn, Entity.FIELD);
      
      // Write the uses relation
      accept(parent.getType());
      
      // Write the javadoc comment
      accept(parent.getJavadoc());
    } else if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
      VariableDeclarationStatement parent = (VariableDeclarationStatement) node.getParent();
      
      Type type = parent.getType();
      String typeFqn = getTypeFqn(type);
      
      // Write the local variable
      localVariableWriter.writeLocalVariable(LocalVariable.LOCAL, node.getName().getIdentifier(), parent.getModifiers(), typeFqn, createLocation(type), fqnStack.getFqn(), null, createLocation(node));
    } else if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
      VariableDeclarationExpression parent = (VariableDeclarationExpression)node.getParent();
      
      Type type = parent.getType();
      String typeFqn = getTypeFqn(type);
      
      // Write the local variable
      localVariableWriter.writeLocalVariable(LocalVariable.LOCAL, node.getName().getIdentifier(), parent.getModifiers(), typeFqn, createLocation(type), fqnStack.getFqn(), null, createLocation(node));
    } else {
      throw new IllegalStateException("Unknown parent for variable declaration fragment.");
    }
    
    if (node.getInitializer() != null) {
      inLhsAssignment = true;
      accept(node.getName());
      inLhsAssignment = false;
      accept(node.getInitializer());
    }
    
    return false;
  }
  
  @Override
  public void endVisit(VariableDeclarationFragment node) {
    if (node.getParent() instanceof FieldDeclaration) {
      fqnStack.pop();
    }
  }

  /**
   * This method writes:
   * <ul>
   *   <li>Constructor entity to <code>IEntityWriter</code>.
   *   <ul>
   *     <li>Inside relation to <code>IRelationWriter</code>.</li>
   *     <li>Throws relation to <code>IRelationWriter</code>.</li>
   *     
   *   </ul></li>
   *   <li>Method entity to <code>IEntityWriter</code>.
   *   <ul>
   *     <li>Inside relation to <code>IRelationWriter</code>.</li>
   *     <li>Returns relation to <code>IRelationWriter</code>.</li>
   *     <li>Throws relation to <code>IRelationWriter</code>.</li>
   *   </ul></li>
   *   <li>Default constructor call relation to <code>IRelationWriter</code>.</li>
   * </ul>
   * 
   * Method fully qualified names (FQNs) adhere to the following format:<br> 
   * parent fqn + . + simple name + ( + parameter list + )
   * <p>
   * Constructor FQNs adhere to the following format:<br>
   * parent fqn + . + <init> + ( + parameter list + )
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(MethodDeclaration node) {
    // Build the fqn and type
    String fqn = null;
    String fullFqn = null;
    Entity type = null;

    String parentFqn = fqnStack.getFqn();
    String signature = getMethodParams(node.parameters());
    String rawSignature = getErasedMethodParams(node.parameters());
    if (node.isConstructor()) {
      type = Entity.CONSTRUCTOR;
      fqn = parentFqn + ".<init>";
      fullFqn = fqn + signature;
    } else {
      type = Entity.METHOD;
      fqn = parentFqn + '.' + node.getName().getIdentifier();
      fullFqn = fqn + signature;
      
      // Write the returns relation
      Type returnType = node.getReturnType2();
      if (returnType == null) {
        logger.severe("Null return type for " + fullFqn);
      } else {
        relationWriter.writeRelation(Relation.RETURNS, fullFqn, getTypeFqn(returnType), createLocation(returnType));
      }
    }
         
    fqnStack.push(fullFqn, type);
    
    // Explore the children
    accept(node.getJavadoc());
    accept(node.modifiers());
    accept(node.typeParameters());
    accept(node.getReturnType2());
    accept(node.getName());
    accept(node.parameters());
    accept(node.thrownExceptions());
    accept(node.getBody());
    
    // Write the entity
    if (signature.equals(rawSignature)) {
      entityWriter.writeEntity(type, fqn, signature, null, node.getModifiers(), createMetrics(node), createLocation(node));
    } else {
      entityWriter.writeEntity(type, fqn, signature, rawSignature, node.getModifiers(), createMetrics(node), createLocation(node));
    }
    
    // Write the contains relation
    relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fullFqn, createUnknownLocation());

    // Write the throws relation
    for (Name name : (List<Name>)node.thrownExceptions()) {
      ITypeBinding exceptionBinding = name.resolveTypeBinding();
      if (exceptionBinding == null) {
        relationWriter.writeRelation(Relation.THROWS, fullFqn, createUnknownFqn(name.getFullyQualifiedName()), createLocation(node));
      } else {
        relationWriter.writeRelation(Relation.THROWS, fullFqn, getTypeFqn(exceptionBinding), createLocation(name));
      }
    }

    // Write the default constructor call if needed
    if (node.isConstructor() && !fqnStack.peek(EnclosingConstructor.class).wasSuperInvoked()) {
      IMethodBinding binding = node.resolveBinding();
      if (binding != null) {
        ITypeBinding declaring = binding.getDeclaringClass();
        if (declaring != null) {
          if (declaring.isEnum()) {
            relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), "java.lang.Enum.<init>(java.lang.String,int)", createUnknownLocation());
          } else {
            ITypeBinding parent = declaring.getSuperclass();
            if (parent == null) {
              relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), "java.lang.Object.<init>()", createUnknownLocation());
            } else {
              relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), getErasedTypeFqn(parent) + ".<init>()", createUnknownLocation());
            }
          }
        }
      }
    }
    
    fqnStack.pop();
    return false;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>Calls relation to <code>IRelationWriter</code>.</li>
   *</ul>
   */
  @Override
  public boolean visit(MethodInvocation node) {
    // Get the fqn
    String fqn = null;
    IMethodBinding binding = node.resolveMethodBinding();
    if (binding == null) {
      fqn = createUnknownMethodFqn(node);
    } else {
      fqn = getMethodName(binding, false) + getMethodArgs(binding);
    }

    // Write the calls relation
    relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, new Location(fqnStack.find(EnclosingDeclaredType.class).getFqn(), compilationUnitPath, node.getName().getStartPosition(), node.getLength() - (node.getName().getStartPosition() - node.getStartPosition())));
    
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>Calls relation to <code>IRelationWriter</code>.</li>
   *</ul>
   */
  @Override
  public boolean visit(SuperMethodInvocation node) {
    // Get the fqn
    String fqn = null;
    IMethodBinding binding = node.resolveMethodBinding();
    if (binding == null) {
      fqn = createUnknownSuperFqn(node.getName().getIdentifier()) + getFuzzyMethodArgs(node.arguments());
    } else {
      fqn = getMethodName(binding, false) + getMethodArgs(binding);
      
    } 
    
    // Write the calls relation
    relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, createLocation(node));

    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For non-anonymous class instantiations:
   *  <ul>
   *    <li>Instantiates relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(ClassInstanceCreation node) {
    if (node.getAnonymousClassDeclaration() == null) {
      {
        // Get the fqn
        String fqn = null;
        IMethodBinding binding = node.resolveConstructorBinding();
        if (binding == null) {
          fqn = getTypeFqn(node.getType()) + ".<init>" + getFuzzyMethodArgs(node.arguments());
        } else {
          fqn = getMethodName(binding, false) + getMethodArgs(binding);
        }
  
        // Write the calls relation
        relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, createLocation(node));
        // Write the instantiates relation
        relationWriter.writeRelation(Relation.INSTANTIATES, fqnStack.getFqn(), getTypeFqn(node.getType()), createLocation(node));
      }
    }
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For constructor invocations (not instantiations):
   *  <ul>
   *    <li>Calls relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(ConstructorInvocation node) {
    // Get the fqn
    String fqn = null;
    IMethodBinding binding = node.resolveConstructorBinding();
    if (binding == null) {
      fqn = fqnStack.getFqn() + ".<init>" + getFuzzyMethodArgs(node.arguments()); 
    } else {
      fqn = getMethodName(binding, false) + getMethodArgs(binding);
    }

    // Write the calls relation
    relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, createLocation(node));
    fqnStack.peek(EnclosingConstructor.class).reportSuperInvocation();
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For superconstructor invocations (not instantiations):
   *  <ul>
   *    <li>Calls relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(SuperConstructorInvocation node) {
    // Get the fqn
    String fqn = null;
    IMethodBinding binding = node.resolveConstructorBinding();
    if (binding == null) {
      fqn = createUnknownSuperFqn("<init>") + getFuzzyMethodArgs(node.arguments());
    } else {
      fqn = getMethodName(binding, false) + getMethodArgs(binding);
    }

    // Write the call relation
    relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, createLocation(node));

    fqnStack.peek(EnclosingConstructor.class).reportSuperInvocation();
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For cast expressions:
   *  <ul>
   *    <li>Casts relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(CastExpression node) {
    // Write the casts relation
    relationWriter.writeRelation(Relation.CASTS, fqnStack.getFqn(), getTypeFqn(node.getType()), createLocation(node));

    return true;
  }

  @Override
  public boolean visit(ThrowStatement node) {
    return true;
  }

  @Override
  public boolean visit(CatchClause node) {
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For field access expressions:
   *  <ul>
   *    <li>Writes relation to <code>IRelationWriter</code>.</li>
   *    <li>Reads relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(FieldAccess node) {
    // Get the fqn
    String fqn = null;
    IVariableBinding binding = node.resolveFieldBinding();
    if (binding == null) {
      fqn = createUnknownFqn(node.getName().getIdentifier());
    } else {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      if (declaringClass != null) {
        declaringClass = declaringClass.getErasure();
      }
      if (declaringClass == null) {
        if (binding.isRecovered()) {
          fqn = createUnknownFqn(binding.getName());
        } else if (node.getExpression().resolveTypeBinding().isArray() && binding.getName().equals("length")) {
          // Ignore array length
          return true;
        } else {
          logger.log(Level.SEVERE, "Non-recovered field binding without a declaring class that's not an array.length!");
        }
      } else {
        fqn = getTypeFqn(declaringClass) + "." + binding.getName();
      }
    }

    // Write the relation
    if (inLhsAssignment) {
      relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), fqn, createLocation(node.getName()));
    } else {
      relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), fqn, createLocation(node.getName()));
    }
    
    inLhsAssignment = false;
    
    node.getExpression().accept(this);
    return false;
  }
  
  @Override
  public boolean visit(Assignment node) {
    inLhsAssignment = true;
    node.getLeftHandSide().accept(this);
    inLhsAssignment = false;
    node.getRightHandSide().accept(this);
    return false;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For super field access expressions:
   *  <ul>
   *    <li>Writes relation to <code>IRelationWriter</code>.</li>
   *    <li>Reads relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(SuperFieldAccess node) {
    // Get the fqn
    String fqn = null;
    IVariableBinding binding = node.resolveFieldBinding();
    if (binding == null) {
      fqn = createUnknownSuperFqn(node.getName().getIdentifier());
    } else {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      if (declaringClass != null) {
        declaringClass = declaringClass.getErasure();
      }
      if (declaringClass == null) {
        fqn = createUnknownSuperFqn(binding.getName());
      } else {
        fqn = getTypeFqn(declaringClass) + "." + binding.getName();
      }
    }
    
    // Write the relation
    if (inLhsAssignment) {
      relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), fqn, createLocation(node));
    } else {
      relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), fqn, createLocation(node));
    }
    
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any type reference:
   *  <ul>
   *    <li>Uses relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(QualifiedName node) {
    IBinding binding = node.resolveBinding();

    if (binding instanceof IVariableBinding) {
      IVariableBinding varBinding = (IVariableBinding) binding;
      if (varBinding.isField()) {
        node.getName().accept(this);
        inLhsAssignment = false;
        node.getQualifier().accept(this);
      } else {
        logger.log(Level.SEVERE, "Unexpected type of qualified name variable binding: ", varBinding);
      }
    } else if (binding instanceof ITypeBinding) {
      ITypeBinding typeBinding = getBaseType((ITypeBinding) binding);
      IBinding qualifierBinding = node.getQualifier().resolveBinding();
      if (qualifierBinding instanceof IPackageBinding) {
        // Write the uses relation
        relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(typeBinding), createLocation(node));
      } else {
        // Write the uses relation
        relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(typeBinding), createLocation(node.getName()));
        node.getQualifier().accept(this);
      }
    } else if (binding instanceof IPackageBinding) {
    } else if (binding == null) { 
    } else {
      logger.log(Level.SEVERE, "Unknown binding type encountered:", binding);
    }
    return false;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any field:
   *  <ul>
   *    <li>Reads relation to <code>IRelationWriter</code>.</li>
   *    <li>Writes relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *  <li>For any type reference:
   *  <ul>
   *    <li>Uses relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(SimpleName node) {
    IBinding binding = node.resolveBinding();

    if (binding instanceof IVariableBinding) {
      IVariableBinding varBinding = (IVariableBinding) binding;
      if (varBinding.isField()) {
        ITypeBinding declaringClass = varBinding.getDeclaringClass();
        if (declaringClass != null) {
          declaringClass = declaringClass.getErasure();
        }
        String fqn = null;
        if (declaringClass == null) {
          if (binding.isRecovered()) {
            fqn = createUnknownFqn(binding.getName());
          } else if (binding.getName().equals("length")) {
            return true;
          } else {
            logger.log(Level.SEVERE, "Non-recovered field binding without a declaring class that's not an array.length!", node);
            return true;
          }
        } else {
          fqn = getTypeFqn(declaringClass) + "." + binding.getName();
        }
        // Write the relation
        if (inLhsAssignment) {
          relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), fqn, createLocation(node));
        } else {
          relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), fqn, createLocation(node));
        }
      }
    } else if (binding instanceof ITypeBinding) {
      ITypeBinding typeBinding = (ITypeBinding) binding;
      // Write the uses relation
      relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(getBaseType(typeBinding)), createLocation(node));
    }
    return true;
  }

  @Override
  public boolean visit(ParameterizedType node) {
    String typeFqn = getTypeFqn(node);
      
    // Write uses
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), typeFqn, createLocation(node));

    return true;
  }
  
  @Override
  public boolean visit(QualifiedType node) {
    ITypeBinding typeBinding = getBaseType(node.resolveBinding());
    IBinding qualifierBinding = node.getQualifier().resolveBinding();
    if (qualifierBinding instanceof IPackageBinding) {
      // Write the uses relation
      relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(typeBinding), createLocation(node));
    } else {
      // Write the uses relation
      relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(typeBinding), createLocation(node.getName()));
      node.getQualifier().accept(this);
    }
    return false;
  }
  
  @Override
  public boolean visit(SimpleType node) {
//    ITypeBinding binding = getBaseType(node.resolveBinding());
//    // Write the uses relation
//    relationWriter.writeUses(fqnStack.getFqn(), getTypeFqn(binding), getLocation(node));
    return true;
  }
  
  @Override
  public boolean visit(PrimitiveType node) {
    // Write the uses relation
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), node.toString(), createLocation(node));
    return false;
  }
  
  @Override
  public boolean visit(ArrayType node) {
    // Write the uses relation
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(node), createLocation(node));
    
    node.getElementType().accept(this);
    
    return false;
  }
  
  @Override
  public boolean visit(ArrayCreation node) {
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any instance of expression:
   *  <ul>
   *    <li>Checks relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(InstanceofExpression node) {
    // Write the checks relation
    relationWriter.writeRelation(Relation.CHECKS, fqnStack.getFqn(), getTypeFqn(node.getRightOperand()), createLocation(node));

    return true;
  }

  @Override
  public boolean visit(NullLiteral node) {
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any boolean literal:
   *  <ul>
   *    <li>Uses relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(BooleanLiteral node) {
    // Write the uses relation
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), "boolean", createLocation(node));
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any character literal:
   *  <ul>
   *    <li>Uses relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(CharacterLiteral node) {
    // Write the uses relation
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), "char", createLocation(node));
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any string literal:
   *  <ul>
   *    <li>Uses relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(StringLiteral node) {
    // Write the uses relation
//    relationWriter.writeUses(fqnStack.getFqn(), "java.lang.String", getLocation(node));
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any number literal:
   *  <ul>
   *    <li>Uses relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(NumberLiteral node) {
    // Write the uses relation
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), node.resolveTypeBinding().getQualifiedName(), createLocation(node));
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any block comment:
   *  <ul>
   *    <li>Comment to <code>ICommentWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(BlockComment node) {
    // Write the comment entity
    commentWriter.writeComment(Comment.BLOCK, createLocation(node));
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any line comment:
   *  <ul>
   *    <li>Comment to <code>ICommentWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(LineComment node) {
    // Write the comment entity
    commentWriter.writeComment(Comment.LINE, createLocation(node));
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any javadoc comment:
   *  <ul>
   *    <li>Comment to <code>ICommentWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(Javadoc node) {
    // Write the comment entity
    if (node.getParent() == null || node.getParent() instanceof PackageDeclaration) {
      commentWriter.writeComment(Comment.UJAVADOC,createLocation(node));
    } else {
      commentWriter.writeComment(Comment.JAVADOC, fqnStack.getFqn(), createLocation(node));
    }
    return false;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any annotation declaration:
   *  <ul>
   *    <li>Annotation entity to <code>IEntityWriter</code>.</li>
   *    <li>Inside relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    // Get the fqn
    String fqn = null;
    if (node.isPackageMemberTypeDeclaration()) {
      fqn = fqnStack.peek(EnclosingPackage.class).getTypeFqn(node.getName().getIdentifier());
    } else if (node.isMemberTypeDeclaration()) {
      if (node.getName().getIdentifier().length() > 0) {
        fqn = fqnStack.peek(EnclosingDeclaredType.class).getMemberFqn(node.getName().getIdentifier());
      } else {
        throw new IllegalStateException("A annotation declaration should not declare an annonymous type!");
      }
    } else if (node.isLocalTypeDeclaration()) {
      ITypeBinding binding = node.resolveBinding();
      if (binding == null) {
        fqn = createUnknownFqn(node.getName().getIdentifier());
      } else {
        fqn = fqnStack.peek(EnclosingBlock.class).getLocalFqn(node.getName().getIdentifier(), binding);
      }
    } else {
      throw new IllegalArgumentException("Unknown annotation type declaration type: " + node);
    }
    
    // Push the stack
    String parentFqn = fqnStack.getFqn();
    fqnStack.push(fqn, Entity.ANNOTATION);
    
    // Visit the children
    accept(node.getJavadoc());
    accept(node.modifiers());
    accept(node.bodyDeclarations());
    
    // Write the entity
    entityWriter.writeEntity(Entity.ANNOTATION, fqn, node.getModifiers(), createMetrics(node), createLocation(node));

    // Write the contains relation
    relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fqn, createUnknownLocation());

    // Write the extends relation
    relationWriter.writeRelation(Relation.EXTENDS, fqn, "java.lang.Object", createUnknownLocation());
    
    // Write the implements relation
    relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, "java.lang.annotation.Annotation", createUnknownLocation());

    fqnStack.pop();
    return false;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any annotation element declaration:
   *  <ul>
   *    <li>Annotation element entity to <code>IEntityWriter</code>.</li>
   *    <li>Inside relation to <code>IRelationWriter</code>.</li>
   *    <li>Returns relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    // Get the fqn
    String parentFqn = fqnStack.getFqn();
    String fqn = parentFqn + "." + node.getName().getIdentifier();
    String fullFqn = fqn + "()";

    // Push the stack
    fqnStack.push(fullFqn, Entity.ANNOTATION_ELEMENT);
    
    // Visit the children
    accept(node.getJavadoc());
    accept(node.modifiers());
    accept(node.getType());
    accept(node.getName());
    accept(node.getDefault());
    
    // Write the entity
    entityWriter.writeEntity(Entity.ANNOTATION_ELEMENT, fqn, "()", null, node.getModifiers(), createMetrics(node), createLocation(node));

    // Write the contains relation
    relationWriter.writeRelation(Relation.CONTAINS, parentFqn, fullFqn, createUnknownLocation());

    // Write the returns relation
    Type returnType = node.getType();
    relationWriter.writeRelation(Relation.RETURNS, fullFqn, getTypeFqn(returnType), createLocation(returnType));

    fqnStack.pop();
    return false;
  }
  
  /**
   * This method writes:
   *<ul>
   *  <li>For any marker annotation use:
   *  <ul>
   *    <li>Annotated by relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(MarkerAnnotation node) {
    // Get the fqn
    String fqn = null;
    IAnnotationBinding binding = node.resolveAnnotationBinding();
    if (binding == null) {
      fqn = createUnknownFqn(node.getTypeName().getFullyQualifiedName());
    } else {
      ITypeBinding typeBinding = binding.getAnnotationType();
      if (typeBinding == null) {
        fqn = createUnknownFqn(binding.getName());
      } else {
        fqn = getTypeFqn(typeBinding);
      }
    }
    
    // Write the annotates relation
    relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn(), fqn, createLocation(node));
    
    return true;
  }
  
  /**
   * This method writes:
   *<ul>
   *  <li>For any normal annotation use:
   *  <ul>
   *    <li>Annotated by relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(NormalAnnotation node) {
    // Get the fqn
    String fqn = null;
    IAnnotationBinding binding = node.resolveAnnotationBinding();
    if (binding == null) {
      fqn = createUnknownFqn(node.getTypeName().getFullyQualifiedName());
    } else {
      ITypeBinding typeBinding = binding.getAnnotationType();
      if (typeBinding == null) {
        fqn = createUnknownFqn(binding.getName());
      } else {
        fqn = getTypeFqn(typeBinding);
      }
    }    
    
    // Write the annotates relation
    relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn(), fqn, createLocation(node));
    
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>For any single member annotation use:
   *  <ul>
   *    <li>Annotated by relation to <code>IRelationWriter</code>.</li>
   *  </ul></li>
   *</ul>
   */
  @Override
  public boolean visit(SingleMemberAnnotation node) {
    // Get the fqn
    String fqn = null;
    IAnnotationBinding binding = node.resolveAnnotationBinding();
    if (binding == null) {
      fqn = createUnknownFqn(node.getTypeName().getFullyQualifiedName());
    } else {
      ITypeBinding typeBinding = binding.getAnnotationType();
      if (typeBinding == null) {
        fqn = createUnknownFqn(binding.getName());
      } else {
        fqn = getTypeFqn(typeBinding);
      }
    }    
    
    // Write the annotates relation
    relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn(), fqn, createLocation(node));
    
    return true;
  }
  
//  @SuppressWarnings("unchecked")
  
  @Override
  public boolean visit(ArrayAccess node) {
    return super.visit(node);
  }
  


  @Override
  public boolean visit(ArrayInitializer node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(AssertStatement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(Block node) {
    fqnStack.peek(EnclosingMethod.class).incrementLevel();
    accept(node.statements());
    fqnStack.peek(EnclosingMethod.class).decrementLevel();
    return false;
  }

  @Override
  public boolean visit(BreakStatement node) {
    fqnStack.find(EnclosingMethod.class).incrementUnconditionalJumps();
    return super.visit(node);
  }

  @Override
  public boolean visit(ConditionalExpression node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(ContinueStatement node) {
    fqnStack.find(EnclosingMethod.class).incrementUnconditionalJumps();
    return super.visit(node);
  }

  @Override
  public boolean visit(DoStatement node) {
    // The block will handle the nesting
    if (node.getBody().getNodeType() == ASTNode.BLOCK) {
      return true;
    } else {
      fqnStack.peek(EnclosingMethod.class).incrementLevel();
      accept(node.getBody());
      fqnStack.peek(EnclosingMethod.class).decrementLevel();
      accept(node.getExpression());
      return false;
    }
  }

  @Override
  public boolean visit(EmptyStatement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    // Write the uses relation for iterable
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), "java.lang.Iterable", createUnknownLocation());
    
    // The block will handle the nesting
    if (node.getBody().getNodeType() == ASTNode.BLOCK) {
      return true;
    } else {
      accept(node.getParameter());
      accept(node.getExpression());
      fqnStack.peek(EnclosingMethod.class).incrementLevel();
      accept(node.getBody());
      fqnStack.peek(EnclosingMethod.class).decrementLevel();
      return false;
    }
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(ForStatement node) {
    // The block will handle the nesting
    if (node.getBody().getNodeType() == ASTNode.BLOCK) {
      return true;
    } else {
      accept(node.initializers());
      accept(node.getExpression());
      accept(node.updaters());
      fqnStack.peek(EnclosingMethod.class).incrementLevel();
      accept(node.getBody());
      fqnStack.peek(EnclosingMethod.class).decrementLevel();
      return false;
    }
  }

  @Override
  public boolean visit(IfStatement node) {
    accept(node.getExpression());
    // The block will handle the nesting
    if (node.getThenStatement().getNodeType() == ASTNode.BLOCK) {
      fqnStack.peek(EnclosingMethod.class).incrementLevel();
      accept(node.getThenStatement());
      fqnStack.peek(EnclosingMethod.class).decrementLevel();
    } else {
      accept(node.getThenStatement());
    }
    // The block will handle the nesting
    if (node.getElseStatement() != null) {
      if (node.getElseStatement().getNodeType() == ASTNode.BLOCK) {
        fqnStack.peek(EnclosingMethod.class).incrementLevel();
        accept(node.getElseStatement());
        fqnStack.peek(EnclosingMethod.class).decrementLevel();
      } else {
        accept(node.getElseStatement());
      }
    }
    return false;
  }

  @Override
  public boolean visit(InfixExpression node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(LabeledStatement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(MemberRef node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(MemberValuePair node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(MethodRef node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(MethodRefParameter node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(Modifier node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(ParenthesizedExpression node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(PostfixExpression node) {
    // Count it as both a read and a write
    inLhsAssignment = true;
    accept(node.getOperand());
    inLhsAssignment = false;
    accept(node.getOperand());
    return false;
  }

  @Override
  public boolean visit(PrefixExpression node) {
    // If it's either ++ or --. count it as both a read an a write
    if (node.getOperator() == Operator.INCREMENT || node.getOperator() == Operator.DECREMENT) {
      inLhsAssignment = true;
      accept(node.getOperand());
      inLhsAssignment = false;
    }
    accept(node.getOperand());
    return false;
  }

  @Override
  public boolean visit(ReturnStatement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(SwitchCase node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(SwitchStatement node) {
    accept(node.getExpression());
    fqnStack.peek(EnclosingMethod.class).incrementLevel();
    accept(node.statements());
    fqnStack.peek(EnclosingMethod.class).decrementLevel();
    return false;
  }

  @Override
  public boolean visit(SynchronizedStatement node) {
    // The body is always a block
    return true;
  }

  @Override
  public boolean visit(TagElement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(TextElement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(ThisExpression node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(TryStatement node) {
    // The body is always a block
    return true;
  }

  @Override
  public boolean visit(TypeDeclarationStatement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(TypeLiteral node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    return super.visit(node);
  }

  @Override
  public boolean visit(WhileStatement node) {
    // The block will handle the nesting
    if (node.getBody().getNodeType() == ASTNode.BLOCK) {
      return true;
    } else {
      accept(node.getExpression());
      fqnStack.peek(EnclosingMethod.class).incrementLevel();
      accept(node.getBody());
      fqnStack.peek(EnclosingMethod.class).decrementLevel();
      return false;
    }
  }

  @Override
  public boolean visit(WildcardType node) {
    return super.visit(node);
  }
  
  private Location createLocation(ASTNode node) {
    EnclosingDeclaredType enc = fqnStack.find2(EnclosingDeclaredType.class);
    return new Location(enc == null ? null : enc.getFqn(), compilationUnitPath, node.getStartPosition(), node.getLength());
  }
  
  private Location createUnknownLocation() {
    EnclosingDeclaredType enc = fqnStack.find2(EnclosingDeclaredType.class);
    return new Location(enc == null ? null : enc.getFqn(), compilationUnitPath, null, null);
  }

  private Metrics createMetrics(ASTNode node) {
    if (compilationUnitSource == null || node.getStartPosition() == -1) {
      return null;
    } else {
      Metrics metrics = new Metrics();
      // Add the lines of code metrics
      try {
        LinesOfCode.computeLinesOfCode(compilationUnitSource.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()), metrics);
      } catch (StringIndexOutOfBoundsException e) {
        logger.log(Level.SEVERE, "Error in getting node source: " + node, e);
      }
      
      // Add the block metrics
      EnclosingMethod block = fqnStack.peek2(EnclosingMethod.class);
      if (block != null) {
        metrics.addMetric(Metric.NUMBER_OF_UNCONDITIONAL_JUMPS, block.getUnconditionalJumpCount());
        metrics.addMetric(Metric.NUMBER_OF_NESTED_LEVELS, block.getMaximumNesting());
      }
      return metrics;
    }
  }
  
  private String getMethodArgs(IMethodBinding binding) {
    StringBuilder builder = new StringBuilder();
    getMethodArgs(builder, binding.getMethodDeclaration());
    return builder.toString();
  }
  
  private void getMethodArgs(StringBuilder argBuilder, IMethodBinding binding) {
    argBuilder.append('(');
    boolean first = true;
    for (ITypeBinding paramType : binding.getParameterTypes()) {
      if (first) {
        first = false;
      } else {
        argBuilder.append(',');
      }
      argBuilder.append(getTypeFqn(paramType));
    }
    argBuilder.append(')');
  }
  
  private String getErasedMethodArgs(IMethodBinding binding) {
    StringBuilder builder = new StringBuilder();
    getErasedMethodArgs(builder, binding.getMethodDeclaration());
    return builder.toString();
  }
  
  private void getErasedMethodArgs(StringBuilder argBuilder, IMethodBinding binding) {
    argBuilder.append('(');
    boolean first = true;
    for (ITypeBinding paramType : binding.getParameterTypes()) {
      if (first) {
        first = false;
      } else {
        argBuilder.append(',');
      }
      argBuilder.append(getErasedTypeFqn(paramType));
    }
    argBuilder.append(')');
  }
  
  private String getFuzzyMethodArgs(Iterable<?> arguments) {
    StringBuilder builder = new StringBuilder();
    getFuzzyMethodArgs(builder, arguments);
    return builder.toString();
  }
  
  @SuppressWarnings("unchecked")
  private void getFuzzyMethodArgs(StringBuilder argBuilder, Iterable<?> arguments) {
    boolean first = true;
    argBuilder.append('(');
    for (Expression exp : (Iterable<Expression>) arguments) {
      ITypeBinding binding = exp.resolveTypeBinding();
      if (first) {
        first = false;
      } else {
        argBuilder.append(',');
      }
      if (binding == null) {
        argBuilder.append(UNKNOWN);
      } else {
        argBuilder.append(getErasedTypeFqn(binding));
      }
    }
    argBuilder.append(')');
  }
  
  private String getMethodParams(Iterable<SingleVariableDeclaration> parameters) {
    StringBuilder builder = new StringBuilder();
    getMethodParams(builder, parameters);
    return builder.toString();
  }
  
  private void getMethodParams(StringBuilder argBuilder, Iterable<SingleVariableDeclaration> parameters) {
    boolean first = true;
    argBuilder.append('(');
    for (SingleVariableDeclaration param : parameters) {
      if (first) {
        first = false;
      } else {
        argBuilder.append(',');
      }
      argBuilder.append(getTypeFqn(param.getType()));
      if (param.isVarargs()) {
        argBuilder.append("[]");
      } else if (param.getExtraDimensions() != 0) {
        argBuilder.append(BRACKETS.substring(0, 2 * param.getExtraDimensions()));
      }
    }
    argBuilder.append(')');
  }
  
  private String getErasedMethodParams(Iterable<SingleVariableDeclaration> parameters) {
    StringBuilder builder = new StringBuilder();
    getErasedMethodParams(builder, parameters);
    return builder.toString();
  }
  
  private void getErasedMethodParams(StringBuilder argBuilder, Iterable<SingleVariableDeclaration> parameters) {
    boolean first = true;
    argBuilder.append('(');
    for (SingleVariableDeclaration param : parameters) {
      if (first) {
        first = false;
      } else {
        argBuilder.append(',');
      }
      argBuilder.append(getErasedTypeFqn(param.getType()));
      if (param.isVarargs()) {
        argBuilder.append("[]");
      } else if (param.getExtraDimensions() != 0) {
        argBuilder.append(BRACKETS.substring(0, 2 * param.getExtraDimensions()));
      }
    }
    argBuilder.append(')');
  }
  
  private String getMethodName(IMethodBinding binding, boolean declaration) {
    binding = binding.getMethodDeclaration();
    StringBuilder fqnBuilder = new StringBuilder();
    ITypeBinding declaringClass = binding.getDeclaringClass();
    if (declaringClass == null) {
      logger.log(Level.SEVERE, "Unresolved declaring class for method!", binding);
    } else {
      if (declaration) {
        fqnBuilder.append(fqnStack.getFqn());
      } else {
        fqnBuilder.append(getTypeFqn(declaringClass));
      }
      fqnBuilder.append('.').append(binding.isConstructor() ? "<init>" : binding.getName());
    }
    return fqnBuilder.toString();
  }
 
  private static final String BRACKETS = "[][][][][][][][][][][][][][][][][][][][]";
 
  private String createUnknownMethodFqn(MethodInvocation node) {
    String fqn = advisor.adviseMethod(node);
    if (fqn == null) {
      return UNKNOWN + "." + node.getName().getFullyQualifiedName() + getFuzzyMethodArgs(node.arguments());
    } else {
      return fqn;
    }
  }
  
  private String createUnknownFqn(String name) {
    String fqn = advisor.advise(name);
    if (fqn == null) {
      return UNKNOWN + "." + name;
    } else {
      return fqn;
    }
  }
  
  private String createUnknownSuperFqn(String name) {
    return "1_SUPER_UNKNOWN_." + name;
  }
  
  private String getErasedTypeFqn(Type type) {
    if (type == null) {
      logger.log(Level.SEVERE, "Attempt to get type fqn of null type!");
      throw new NullPointerException("Attempt to get type fqn of null type!");
    }
    ITypeBinding binding = safeResolve(type);
    if (binding == null) {
      if (type.isPrimitiveType()) {
        return ((PrimitiveType)type).getPrimitiveTypeCode().toString();
      } else if (type.isSimpleType()) {
        return createUnknownFqn(((SimpleType)type).getName().getFullyQualifiedName());
      } else if (type.isArrayType()) {
        ArrayType arrayType = (ArrayType) type;
        Type elementType = arrayType.getElementType();
        if (elementType == null) {
          return createUnknownFqn(BRACKETS.substring(0, 2 * arrayType.getDimensions()));
        } else {
          return getErasedTypeFqn(elementType) + BRACKETS.substring(0, 2 * arrayType.getDimensions());
        }
      } else if (type.isParameterizedType()) {
        ParameterizedType pType = (ParameterizedType)type;
        return getErasedTypeFqn(pType.getType());
      } else {
        logger.log(Level.SEVERE, "Unexpected node type for unresolved type!" + type.toString());
        return UNKNOWN;
      }
    } else {
      return getErasedTypeFqn(binding);
    }
  }
  
  @SuppressWarnings("unchecked")
  private String getTypeFqn(Type type) {
    if (type == null) {
      logger.log(Level.SEVERE, "Attempt to get type fqn of null type!");
      throw new NullPointerException("Attempt to get type fqn of null type!");
    }
    ITypeBinding binding = safeResolve(type);
    if (binding == null || binding.isRecovered()) {
      if (type.isPrimitiveType()) {
        return ((PrimitiveType)type).getPrimitiveTypeCode().toString();
      } else if (type.isSimpleType()) {
        return createUnknownFqn(((SimpleType)type).getName().getFullyQualifiedName());
      } else if (type.isArrayType()) {
        ArrayType arrayType = (ArrayType) type;
        Type elementType = arrayType.getElementType();
        if (elementType == null) {
          return createUnknownFqn(BRACKETS.substring(0, 2 * arrayType.getDimensions()));
        } else {
          return getTypeFqn(elementType) + BRACKETS.substring(0, 2 * arrayType.getDimensions());
        }
      } else if (type.isParameterizedType()) {
        ParameterizedType pType = (ParameterizedType)type;
        StringBuilder fqn = new StringBuilder(getTypeFqn(pType.getType()));
        fqn.append("<");
        boolean isFirst = true;
        for (Type arg : (List<Type>)pType.typeArguments()) {
          if (isFirst) {
            isFirst = false;
          } else {
            fqn.append(",");
          }
          try {
            fqn.append(getTypeFqn(arg));
          } catch (NullPointerException e) {
            logger.log(Level.WARNING, "Eclipse NPE bug in parametrized type", e);
            fqn.append(UNKNOWN);              
          }
        }
        fqn.append(">");
        return fqn.toString();
      } else if (type.isWildcardType()) {
        WildcardType wType = (WildcardType)type;
        Type bound = wType.getBound();
        if (bound == null) {
          return "<?>";
        } else {
          return "<?" + (wType.isUpperBound() ? "+" : "-") + getTypeFqn(bound) + ">";
        }
      } else {
        logger.log(Level.SEVERE, "Unexpected node type for unresolved type!" + type.toString());
        return UNKNOWN;
      }
    } else {
      return getTypeFqn(binding);
    }
  }

  private ITypeBinding getBaseType(ITypeBinding binding) {
    if (binding.isParameterizedType()) {
      return binding.getErasure();
    } else {
      return binding;
    }
  }
  
  private String getErasedTypeFqn(ITypeBinding binding) {
    if (binding == null) {
      logger.log(Level.SEVERE, "Null type binding", new NullPointerException());
      return UNKNOWN;
    } else {
      return getTypeFqn(binding.getErasure());
    }
  }
  
  private String getTypeFqn(ITypeBinding binding) {
    if (binding == null) {
      logger.log(Level.SEVERE, "Null type binding", new NullPointerException());
      return UNKNOWN;
    } else if (binding.isTypeVariable()) {
      return "<" + binding.getQualifiedName() + ">";
    } else if (binding.isPrimitive()) {
      return binding.getQualifiedName();
    } else if (binding.isArray()) {
      if (2 * binding.getDimensions() > BRACKETS.length()) {
        StringBuilder builder = new StringBuilder(getTypeFqn(binding.getElementType()));
        for (int i = 0; i < binding.getDimensions(); i++) {
          builder.append("[]");
        }
        logger.log(Level.WARNING, "Really long array! " + builder.toString() + " from " + compilationUnitPath);
        return builder.toString();
      } else {
        return getTypeFqn(binding.getElementType()) + BRACKETS.substring(0, 2 * binding.getDimensions());
      }
    } else if (binding.isAnonymous()) {
      return fqnStack.findAnonymousFqn(binding);
    } else if (binding.isLocal()) {
      return fqnStack.find(EnclosingBlock.class).getLocalFqn(binding.getName(), binding);
    } else if (binding.isParameterizedType()) {
      StringBuilder fqn = new StringBuilder();
      if (binding.getErasure().isParameterizedType()) {
        logger.log(Level.SEVERE, "Parametrized type erasure is a parametrized type: " + binding.getQualifiedName());
        fqn.append(createUnknownFqn(binding.getQualifiedName()));
      } else {
        fqn.append(getTypeFqn(binding.getErasure()));
      }
      
      fqn.append("<");
      boolean isFirst = true;
      for (ITypeBinding arg : binding.getTypeArguments()) {
        if (isFirst) {
          isFirst = false;
        } else {
          fqn.append(",");
        }
        try {
          fqn.append(getTypeFqn(arg));
        } catch (NullPointerException e) {
          logger.log(Level.WARNING, "Eclipse NPE bug in parametrized type", e);
          fqn.append(UNKNOWN);              
        }
      }
      fqn.append(">");
      return fqn.toString();
    } else if (binding.isWildcardType()) {
      ITypeBinding bound = binding.getBound();
      if (bound == null) {
        return "<?>";
      } else {
        return "<?" + (binding.isUpperbound() ? "+" : "-") + getTypeFqn(bound) + ">";
      }
    } else {
      if (binding.isMember()) {
        return getTypeFqn(binding.getDeclaringClass()) + "$" + binding.getName();
      } else {
        if (binding.getName().equals("null")) {
          return "null";
        } else {
          String fqn = binding.getBinaryName();
          IPackageBinding pkgBinding = binding.getPackage();
          if (pkgBinding != null && pkgBinding.isUnnamed()) {
            fqn = "default." + fqn;
          }
          if (binding.isRecovered()) {
            if (binding.getDeclaringClass() == null || fqn == null) {
              return createUnknownFqn(binding.getName());
            } else {
              return fqn;
            }
          } else {
            return fqn;
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private String getTypeParam(TypeParameter typeParam) {
    StringBuilder builder = new StringBuilder();
    builder.append('<').append(typeParam.getName().getIdentifier());
    boolean first = true;
    for (Type bound : (List<Type>)typeParam.typeBounds()) {
      if (first) {
        first = false;
        builder.append('+');
      } else {
        builder.append('&');
      }
      builder.append(getTypeFqn(bound));
    }
    builder.append('>');
    return builder.toString();
  }
  
  private class FQNStack {
    private Deque<Enclosing> stack;

    private FQNStack() {
      stack = new LinkedList<>();
    }
    
    public void clear() {
      stack.clear();
    }

    public void push(String fqn, Entity type) {
      Enclosing next = null;
      switch (type) {
        case PACKAGE:
          next = new EnclosingPackage(fqn);
          break;
        case CLASS:
        case INTERFACE:
        case ANNOTATION:
        case ENUM:
          next = new EnclosingDeclaredType(fqn);
          break;
        case CONSTRUCTOR:
          next = new EnclosingConstructor(fqn);
          break;
        case METHOD:
        case ANNOTATION_ELEMENT:
        case INITIALIZER:
          next = new EnclosingMethod(fqn);
          break;
        case FIELD:
        case ENUM_CONSTANT:
        case PARAMETER:
          next = new EnclosingBlock(fqn);
          break;
        default:
          logger.severe("Invalid enclosing type: " + type);
      }
      stack.push(next);
    }

    public void pop() {
      stack.pop();
    }
    
    public <T extends Enclosing> T find(Class<T> type) {
      for (Enclosing enc : stack) {
        if (type.isInstance(enc)) {
          return type.cast(enc);
        }
      }
      throw new NoSuchElementException("No enclosing " + type.getName());
    }
    
    public <T extends Enclosing> T find2(Class<T> type) {
      for (Enclosing enc : stack) {
        if (type.isInstance(enc)) {
          return type.cast(enc);
        }
      }
      return null;
    }
    
    public <T extends Enclosing> T peek(Class<T> type) {
      Enclosing enc = stack.peek();
      if (enc == null) {
        throw new NoSuchElementException("Stack is empty");
      } else if (type.isInstance(enc)) {
        return type.cast(enc);
      } else {
        throw new ClassCastException("Top of stack is not of type " + type.getName());
      }
    }
    
    public <T extends Enclosing> T peek2(Class<T> type) {
      Enclosing enc = stack.peek();
      if (enc == null) {
        throw new NoSuchElementException("Stack is empty");
      } else if (type.isInstance(enc)) {
        return type.cast(enc);
      } else {
        return null;
      }
    }
    
    public String findAnonymousFqn(ITypeBinding binding) {
      for (Enclosing enc : stack) {
        if (enc instanceof EnclosingDeclaredType) {
          EnclosingDeclaredType dec = (EnclosingDeclaredType) enc;
          if (dec.anonymousClassMap != null) {
            String fqn = dec.anonymousClassMap.get(binding);
            if (fqn != null) {
              return fqn;
            }
          }
        }
      }
      return UNKNOWN;
    }
    
    public String getFqn() {
      return stack.peek().getFqn();
    }
  }

  private abstract class Enclosing {
    protected final String fqn;
    
    private Enclosing(String fqn) {
      this.fqn = fqn;
    }
    
    public String getFqn() {
      return fqn;
    }
  }
  
  private class EnclosingPackage extends Enclosing {
    private EnclosingPackage(String fqn) {
      super(fqn);
    }
    
    public String getTypeFqn(String identifier) {
      return fqn + "." + identifier;
    }
  }
  
  private class EnclosingDeclaredType extends Enclosing {
    private int initializerCount;
    
    private int nullAnonymousClassCount = 0;
    private Map<ITypeBinding, String> anonymousClassMap;
    private Map<String, String> localClassMap;

    private EnclosingDeclaredType(String fqn) {
      super(fqn);
    }
    
    public String getMemberFqn(String identifier) {
      return fqn + "$" + identifier;
    }
    
    public String getInitializerFqn() {
      return fqn + ".initializer-" + ++initializerCount;
    }
    
    public String createAnonymousClassFqn(ITypeBinding binding) {
      if (binding == null) {
        return fqn + "$" + --nullAnonymousClassCount;
      } else {
        if (anonymousClassMap == null) {
          anonymousClassMap = new HashMap<>();
        }
        String anonFqn = anonymousClassMap.get(binding);
        if (anonFqn == null) {
          anonFqn = fqn + "$" + (anonymousClassMap.size() + 1);
          anonymousClassMap.put(binding, anonFqn);
        } else {
          logger.severe("Attempt to create anonymous class fqn twice: " + binding.getBinaryName());
        }
        return anonFqn;
      }
    }
    
    public String createLocalClassFqn(String name, String uniqueID) {
      if (localClassMap == null) {
        localClassMap = new HashMap<>();
      }
      String retval = fqn + "$" + (localClassMap.size() + 1) + name;
      localClassMap.put(uniqueID, retval);
      return retval;
    }
    
    public String getLocalClassFqn(String name, String binaryName) {
      if (localClassMap == null) {
        localClassMap = new HashMap<>();
      }
      return localClassMap.get(binaryName);
    }
    
    public String getFieldFqn(String identifier) {
      return fqn + "." + identifier;
    }
  }
  
  private class EnclosingBlock extends Enclosing {
    private EnclosingBlock(String fqn) {
      super(fqn);
    }
    
    public String getLocalFqn(String identifier, ITypeBinding binding) {
      checkNotNull(binding);
      
      String uniqueID = binding.getBinaryName();
      if (uniqueID == null) {
        uniqueID = binding.getDeclaringClass().getBinaryName();
        if (binding.getDeclaringMethod() != null) {
          uniqueID += binding.getDeclaringMethod().getName();
        }
        uniqueID += identifier;
      }
      
      for (Enclosing enc : fqnStack.stack) {
        if (enc instanceof EnclosingDeclaredType) {
          String localFqn = ((EnclosingDeclaredType) enc).getLocalClassFqn(identifier, uniqueID);
          if (localFqn != null) {
            return localFqn;
          }
        }
      }
      for (Enclosing enc : fqnStack.stack) {
        if (enc instanceof EnclosingDeclaredType) {
          return ((EnclosingDeclaredType) enc).createLocalClassFqn(identifier, uniqueID);
        }
      }
      throw new IllegalStateException("Cannot have local declaration with no declared types on stack.");
    }
  }
  
  private class EnclosingMethod extends EnclosingBlock {
    private int parameterCount;
    private int unconditionalJumps;
    private int nesting = -1; // This accounts for the initial block
    private int maxNesting;
    
    private EnclosingMethod(String fqn) {
      super(fqn);
    }
    
    public int getNextParameterPos() {
      return parameterCount++;
    }
    
    public void incrementUnconditionalJumps() {
      unconditionalJumps++;
    }
    
    public int getUnconditionalJumpCount() {
      return unconditionalJumps;
    }
    
    public void incrementLevel() {
      maxNesting = Math.max(++nesting,maxNesting);
    }
    
    public void decrementLevel() {
      nesting--;
    }
    
    public int getMaximumNesting() {
      return maxNesting;
    }
  }
  
  private class EnclosingConstructor extends EnclosingMethod {
    private boolean superInvoked;
    
    private EnclosingConstructor(String fqn) {
      super(fqn);
    }
    
    public void reportSuperInvocation() {
      superInvoked = true;
    }
    
    public boolean wasSuperInvoked() {
      return superInvoked;
    }
  }
  
  private void accept(ASTNode child) {
    if (child != null) {
      child.accept(this);
    }
  }
  
  private ITypeBinding safeResolve(Type type) {
    try {
      return type.resolveBinding();
    } catch (ClassCastException e) {
      logger.log(Level.WARNING, "Eclipse resolve binding bug", e);
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  private void accept(List<?> children) {
    for (ASTNode child : (List<? extends ASTNode>)children) {
      child.accept(this);
    }
  }
}
