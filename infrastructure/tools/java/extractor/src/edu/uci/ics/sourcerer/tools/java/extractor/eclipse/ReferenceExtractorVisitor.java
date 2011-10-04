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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Deque;
import java.util.List;
import java.util.Map;
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

import edu.uci.ics.sourcerer.extractor.io.CommentWriter;
import edu.uci.ics.sourcerer.extractor.io.EntityWriter;
import edu.uci.ics.sourcerer.extractor.io.FileWriter;
import edu.uci.ics.sourcerer.extractor.io.ImportWriter;
import edu.uci.ics.sourcerer.extractor.io.LocalVariableWriter;
import edu.uci.ics.sourcerer.extractor.io.ProblemWriter;
import edu.uci.ics.sourcerer.extractor.io.RelationWriter;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Comment;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;
import edu.uci.ics.sourcerer.tools.java.model.types.Problem;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ReferenceExtractorVisitor extends ASTVisitor {
  private static final String UNKNOWN = "(1UNKNOWN)";
  
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

  private boolean inLhsAssignment = false;
  
  private boolean bindingFree = false;
  
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
    if (node.getJavaElement().getResource() == null) {
      if (node.getPackage() == null) {
        compilationUnitPath = compilationUnitName;
      } else {
        compilationUnitPath = node.getPackage().getName() + "." + compilationUnitName;
      }
    } else {
      compilationUnitPath = node.getJavaElement().getResource().getRawLocation().toString();
    } 
    
    // Get the package fqn
    if (node.getPackage() == null) {
      entityWriter.writeEntity(Entity.PACKAGE, "default", null, 0, null, null);
      fqnStack.push("default", Entity.PACKAGE);
    } else {
      entityWriter.writeEntity(Entity.PACKAGE, node.getPackage().getName().getFullyQualifiedName(), null, 0, null, null);
      fqnStack.push(node.getPackage().getName().getFullyQualifiedName(), Entity.PACKAGE);
    }
    
    return true;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void endVisit(CompilationUnit node) {
    // Write the file path
    fileWriter.writeFile(File.SOURCE, compilationUnitName, MetricsCalculator.computeLinesOfCode(getSource(node)), compilationUnitPath);
    
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
    IBinding binding = node.resolveBinding();
    if (binding == null) {
      if (bindingFree) {
        importWriter.writeImport(node.getName().getFullyQualifiedName(), node.isStatic(), node.isOnDemand(), getLocation(node));
      } else {
//        throw new IllegalStateException("Binding resolution appears to have failed!");
        logger.info("Binding resolution appears to have failed!");
        importWriter.writeImport(node.getName().getFullyQualifiedName(), node.isStatic(), node.isOnDemand(), getLocation(node));
      }
    } else {
      try {
        if (binding instanceof ITypeBinding) {
          importWriter.writeImport(getTypeFqn((ITypeBinding)binding), node.isStatic(), node.isOnDemand(), getLocation(node));
        } else {
          importWriter.writeImport(node.getName().getFullyQualifiedName(), node.isStatic(), node.isOnDemand(), getLocation(node));
        }
      } catch (NullPointerException e) {
        logger.log(Level.WARNING, "Eclipse NPE bug in import");
        importWriter.writeImport(node.getName().getFullyQualifiedName(), node.isStatic(), node.isOnDemand(), getLocation(node));
      }
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
    String fqn = null;
    Entity type = null;
    ITypeBinding binding = node.resolveBinding();
    if (binding == null && !bindingFree) {
      throw new IllegalStateException("Binding resolution appears to have failed!");
    }
    if (node.isPackageMemberTypeDeclaration()) {
      fqn = fqnStack.getTypeFqn(node.getName().getIdentifier());
    } else if (node.isMemberTypeDeclaration()) {
      if (node.getName().getIdentifier().length() > 0) {
        fqn = fqnStack.getMemberFqn(node.getName().getIdentifier());
      } else {
        logger.severe("A type declaration should not declare an annonymous type!");
        fqn = fqnStack.getAnonymousClassFqn();
      }
    } else if (node.isLocalTypeDeclaration()) {
      if (binding == null) {
        fqn = getUnknownFqn(node.getName().getIdentifier());
      } else {
        fqn = fqnStack.getLocalFqn(node.getName().getIdentifier(), binding);
      }
    } else {
      logger.severe("Unsure what type the declaration is!");
      fqn = "(ERROR)" + node.getName().getIdentifier();
    }
    
    // Write the entity
    Metrics metrics = MetricsCalculator.computeLinesOfCode(getSource(node));
    if (node.isInterface()) {
      type = Entity.INTERFACE;
    } else {
      type = Entity.CLASS;
    }
    
    String parent = fqnStack.getEnclosingClass();
    
    fqnStack.push(fqn, type);
    
    // Write the inside relation
    relationWriter.writeRelation(Relation.INSIDE, fqn, parent, getUnknownLocation());
    
    entityWriter.writeEntity(type, fqn, node.getName().getIdentifier(), node.getModifiers(), metrics, getLocation(node));
    
    
    // Write the extends relation
    Type superType = node.getSuperclassType();
    String superFqn = null;
    if (superType == null) {
      superFqn = "java.lang.Object";
      relationWriter.writeRelation(Relation.EXTENDS, fqn, superFqn, getUnknownLocation());
    } else {
      superFqn = getTypeFqn(superType);
      relationWriter.writeRelation(Relation.EXTENDS, fqn, superFqn, getLocation(superType));
    }
    
    // Write the implements relation
    List<Type> superInterfaceTypes = node.superInterfaceTypes();
    for (Type superInterfaceType : superInterfaceTypes) {
      String superInterfaceFqn = getTypeFqn(superInterfaceType);
      relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, superInterfaceFqn, getLocation(superInterfaceType));
    }
    
    if (binding != null) {
      if (binding.isAnonymous()) {
        logger.log(Level.SEVERE, "A type declaration should not declare an annonymous type!");
      } else {
        // Verify the fqn
//        String fqn2 = getTypeFqn(binding);
//        if (!fqn.equals(fqn2)) {
//          logger.log(Level.SEVERE, "Mismatch between " + fqn + " and " + fqn2);
//        }

        // Write out the synthesized constructors
        for (IMethodBinding method : binding.getDeclaredMethods()) {
          if (method.isDefaultConstructor()) {
            // Write the entity
            String constructorFqn = getMethodName(method, true);
            entityWriter.writeEntity(Entity.CONSTRUCTOR, constructorFqn, "<init>", "()", null, method.getModifiers(), MetricsCalculator.computeLinesOfCode(null), getUnknownLocation());
            constructorFqn += "()";
            // Write the inside relation
            relationWriter.writeRelation(Relation.INSIDE, constructorFqn, fqn, getUnknownLocation());
            
            // Write the calls relation (implicit superconstructor call)
            if (superFqn == null) {
              relationWriter.writeRelation(Relation.CALLS, constructorFqn, "java.lang.Object.<init>()", getUnknownLocation());
            } else {
              relationWriter.writeRelation(Relation.CALLS, constructorFqn, superFqn + ".<init>()", getUnknownLocation());
            }
          }
        }
      }
    }

    accept(node.getJavadoc());
    accept(node.typeParameters());
    accept(node.getSuperclassType());
    accept(node.superInterfaceTypes());
    accept(node.bodyDeclarations());

    return false;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    fqnStack.pop();
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
    if (node.getParent() instanceof ClassInstanceCreation) {
      ClassInstanceCreation parent = (ClassInstanceCreation) node.getParent();
      
      // Get the fqn
      String fqn = fqnStack.getAnonymousClassFqn();
      String parentFqn = fqnStack.getFqn();
      Location parentLocation = getLocation(parent);
      fqnStack.push(fqn, Entity.CLASS);
      
      // Write the entity
      entityWriter.writeEntity(Entity.CLASS, fqn, getNameFromTypeFqn(fqn), 0, MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));
    
      // Write the inside relation
      relationWriter.writeRelation(Relation.INSIDE, fqn, parentFqn, getUnknownLocation());
    
      
      Type superType = parent.getType();
      ITypeBinding superBinding = superType.resolveBinding();
      
      if (superBinding == null) {
        // Write the uses relation
        // Can't do extends/implements because unsure if it's an interface or class type
//        relationWriter.writeUses(fqn, getTypeFqn(superType), getLocation(superType));
      } else {
        if (superBinding.isInterface()) {
          // Write the implements relation
          relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, getTypeFqn(superBinding), getLocation(superType));
          
          relationWriter.writeRelation(Relation.EXTENDS, fqn, "java.lang.Object", getUnknownLocation());
        } else {
          // Write the extends relation
          relationWriter.writeRelation(Relation.EXTENDS, fqn, getTypeFqn(superBinding), getLocation(superType));
        }
      }
      
      ITypeBinding binding = node.resolveBinding();
      
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
              entityWriter.writeEntity(Entity.CONSTRUCTOR, basicFqn, "<init>", args, null, method.getModifiers(), MetricsCalculator.computeLinesOfCode(null), getUnknownLocation());
            } else {
              entityWriter.writeEntity(Entity.CONSTRUCTOR, basicFqn, "<init>", args, rawArgs, method.getModifiers(), MetricsCalculator.computeLinesOfCode(null), getUnknownLocation());
            }
            

            // Write the inside relation
            relationWriter.writeRelation(Relation.INSIDE, constructorFqn, fqn, getUnknownLocation());

            // Write the instantiates relation
            relationWriter.writeRelation(Relation.INSTANTIATES, parentFqn, fqn, parentLocation);
            
            // Write the calls relation            
            relationWriter.writeRelation(Relation.CALLS, parentFqn, constructorFqn, parentLocation);

            // Write the parameters
            int count = 0;
            for (ITypeBinding param : method.getParameterTypes()) {
              localVariableWriter.writeLocalVariable(LocalVariable.PARAM, "(ANONYMOUS)", 0, getTypeFqn(param), getUnknownLocation(), constructorFqn, count++, getUnknownLocation());
            }
            
            // Reference the superconstructor
            ITypeBinding superClassBinding = binding.getSuperclass();
            if (superClassBinding != null) {
              superClassBinding = superClassBinding.getErasure();
            }
            if (superClassBinding != null) {
              String superFqn = getTypeFqn(superClassBinding);
              String superConstructorFqn = superFqn + ".<init>" + args;
              relationWriter.writeRelation(Relation.CALLS, constructorFqn, superConstructorFqn, getUnknownLocation());
            }
          }
        }
      }
    }
    
    return true;
  }

  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    if (node.getParent() instanceof ClassInstanceCreation) {
      fqnStack.pop();
    }
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
      fqn = fqnStack.getTypeFqn(node.getName().getIdentifier());
    } else if (node.isMemberTypeDeclaration()) {
      fqn = fqnStack.getFqn() + "$" + node.getName().getIdentifier();
    } else if (node.isLocalTypeDeclaration()) {
      logger.log(Level.WARNING, "Can't have local enums! eclipse error");
      fqnStack.push(null, null);
      return false;
    } else {
      logger.log(Level.SEVERE, "Unsure what type the declaration is!");
      fqn = "(ERROR)";
    }

    String parent = fqnStack.getFqn();
    fqnStack.push(fqn, Entity.ENUM);
    Location unknown = getUnknownLocation();
    
    // Write the entity
    entityWriter.writeEntity(Entity.ENUM, fqn, getNameFromTypeFqn(fqn), node.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));
    
    // Write the inside relation
    relationWriter.writeRelation(Relation.INSIDE, fqn, parent, unknown);
    
    // Write the implements relation
    for (Type superInterfaceType : (List<Type>) node.superInterfaceTypes()) {
      relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, getTypeFqn(superInterfaceType), getLocation(superInterfaceType));
    }
    
    // Write the extends relation
    relationWriter.writeRelation(Relation.EXTENDS, fqn, "java.lang.Enum<" + fqn + ">", unknown);
    
    ITypeBinding binding = node.resolveBinding();
    if (binding != null) {
      // Write out the synthesized constructors
      for (IMethodBinding method : binding.getDeclaredMethods()) {
        if (method.isDefaultConstructor()) {
          // Write the entity
          String constructorFqn = getMethodName(method, true) + "()";
          entityWriter.writeEntity(Entity.CONSTRUCTOR, constructorFqn, "()", null, method.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), unknown);

          // Write the inside relation
          relationWriter.writeRelation(Relation.INSIDE, constructorFqn, fqn, unknown);
          
          // Write the calls relation
          relationWriter.writeRelation(Relation.CALLS, constructorFqn, "java.lang.Enum.<init>(java.lang.String,int)", unknown);
        }
      }
    }
    
    // Write the values method
    {
      String methodFqn = fqn + ".values()";
      entityWriter.writeEntity(Entity.METHOD, methodFqn, 9, null, unknown);
      relationWriter.writeRelation(Relation.INSIDE, methodFqn, fqn, unknown);
      relationWriter.writeRelation(Relation.RETURNS, methodFqn, fqn + "[]", unknown);
    }

    // Write the valueOf method
    {
      String methodFqn = fqn + ".valueOf(java.lang.String)";
      entityWriter.writeEntity(Entity.METHOD, methodFqn, 9, null, unknown);
      relationWriter.writeRelation(Relation.INSIDE, methodFqn, fqn, unknown);
      relationWriter.writeRelation(Relation.RETURNS, methodFqn, fqn, unknown);
      localVariableWriter.writeLocalVariable(LocalVariable.PARAM, "name", 0, "java.lang.String", unknown, methodFqn, 0, unknown);
    }
    
    accept(node.getJavadoc());
    accept(node.enumConstants());
    accept(node.bodyDeclarations());
    
    return false;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    fqnStack.pop();
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
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(EnumConstantDeclaration node) {
    // Get the fqn
    String fqn = fqnStack.getTypeFqn(node.getName().getIdentifier());
    
    // Write the entity
    entityWriter.writeEntity(Entity.ENUM_CONSTANT, fqn, 0, MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));
    
    // Write the inside relation
    relationWriter.writeRelation(Relation.INSIDE, fqn, fqnStack.getFqn(), getUnknownLocation());
    
    // Write the holds relation
    relationWriter.writeRelation(Relation.HOLDS, fqn, fqnStack.getFqn(), getUnknownLocation());
    
    // Write the calls relation
    IMethodBinding methodBinding = node.resolveConstructorBinding();
    if (methodBinding == null) {
      String methodFqn = fqnStack.getFqn() + ".<init>" + getFuzzyMethodArgs(node.arguments());
      relationWriter.writeRelation(Relation.CALLS, fqn, methodFqn, getLocation(node.getName()));
    } else {
      relationWriter.writeRelation(Relation.CALLS, fqn, getMethodName(methodBinding, false) + getMethodArgs(methodBinding), getLocation(node.getName()));
    }
    
    // Write the instantiates relation
    relationWriter.writeRelation(Relation.INSTANTIATES, fqn, fqnStack.getFqn(), getLocation(node.getName()));
    
    // Push the enum constant onto the stack
    fqnStack.push(fqn, Entity.ENUM_CONSTANT);
    accept(node.arguments());
    accept(node.getAnonymousClassDeclaration());
    return false;
  }
  
  public void endVisit(EnumConstantDeclaration node) {
    fqnStack.pop();
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
    String fqn = fqnStack.getInitializerFqn();

    // Write the entity
    entityWriter.writeEntity(Entity.INITIALIZER, fqn, node.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));

    // Write the inside relation
    relationWriter.writeRelation(Relation.INSIDE, fqn, fqnStack.getFqn(), getUnknownLocation());

    fqnStack.push(fqn, Entity.INITIALIZER);

    return true;
  }

  @Override
  public void endVisit(Initializer node) {
    fqnStack.pop();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(FieldDeclaration node) {
    for (VariableDeclarationFragment fragment : (List<VariableDeclarationFragment>)node.fragments()) {
      fragment.accept(this);
    }
    return false;
  }

  /**
   * This method writes:
   * <ul>
   *   <li>Parametrized by relation to <code>IRelationWriter</code>.</li>
   * </ul>
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(TypeParameter node) {
    // Write the parametrized by relation
    relationWriter.writeRelation(Relation.PARAMETRIZED_BY, fqnStack.getFqn(), getTypeParam(node), getLocation(node));
    
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
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(SingleVariableDeclaration node) {
    IVariableBinding binding = node.resolveBinding();
    Type type = node.getType();

    if (binding != null) {
      String typeFqn = getTypeFqn(binding.getType());
      if (binding.isParameter()) {
        // Write the parameter
        int param = fqnStack.getNextParameterPos();
        localVariableWriter.writeLocalVariable(LocalVariable.PARAM, binding.getName(), binding.getModifiers(), typeFqn, getLocation(type), fqnStack.getFqn(), param, getLocation(node.getName()));
        fqnStack.push(fqnStack.getFqn() + "#" + param, Entity.PARAMETER);
        accept(node.modifiers());
        fqnStack.pop();
        accept(node.getType());
        accept(node.getName());
        accept(node.getInitializer());
        return false;
      } else {
        // Write the local variable
        localVariableWriter.writeLocalVariable(LocalVariable.LOCAL, binding.getName(), binding.getModifiers(), typeFqn, getLocation(type), fqnStack.getFqn(), null, getLocation(node.getName()));
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
    if (node.getParent() instanceof FieldDeclaration) {
      FieldDeclaration parent = (FieldDeclaration)node.getParent();
      
      // Get the fqn
      String fqn = fqnStack.getTypeFqn(node.getName().getIdentifier());
      
      // Write the entity
      entityWriter.writeEntity(Entity.FIELD, fqn, parent.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));
      
      // Write the inside relation
      relationWriter.writeRelation(Relation.INSIDE, fqn, fqnStack.getFqn(), getUnknownLocation());
      
      Type type = parent.getType();
      String typeFqn = getTypeFqn(type);
      
      // Write the holds relation
      relationWriter.writeRelation(Relation.HOLDS, fqn, typeFqn, getLocation(type));
      
      // Add the field to the fqnstack
      fqnStack.push(fqn, Entity.FIELD);
      
      // Write the uses relation
      accept(parent.getType());
      
      // Write the javadoc comment
      accept(parent.getJavadoc());
    } else if (node.getParent() instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement parent = (VariableDeclarationStatement)node.getParent();
      
      Type type = parent.getType();
      String typeFqn = getTypeFqn(type);
      
      // Write the local variable
      localVariableWriter.writeLocalVariable(LocalVariable.LOCAL, node.getName().getIdentifier(), parent.getModifiers(), typeFqn, getLocation(type), fqnStack.getFqn(), null, getLocation(node));
    } else if (node.getParent() instanceof VariableDeclarationExpression) {
      VariableDeclarationExpression parent = (VariableDeclarationExpression)node.getParent();
      
      Type type = parent.getType();
      String typeFqn = getTypeFqn(type);
      
      // Write the local variable
      localVariableWriter.writeLocalVariable(LocalVariable.LOCAL, node.getName().getIdentifier(), parent.getModifiers(), typeFqn, getLocation(type), fqnStack.getFqn(), null, getLocation(node));
    } else {
      logger.log(Level.SEVERE, "Unknown parent for variable declaration fragment.");
    }
    
    IVariableBinding binding = node.resolveBinding();
    if (binding != null && binding.isField() && !(node.getParent() instanceof FieldDeclaration)) {
      logger.log(Level.SEVERE, "It's a field but it shouldn't be!");
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
    Entity type = null;
    
    String signature = getMethodParams(node.parameters());
    String rawSignature = getErasedMethodParams(node.parameters());
    if (node.isConstructor()) {
      type = Entity.CONSTRUCTOR;
      fqn = fqnStack.getFqn() + ".<init>" + signature;
//      fqn = getFuzzyConstructorFqn(node);

      // Write the entity
      if (signature.equals(rawSignature)) {
        entityWriter.writeEntity(Entity.CONSTRUCTOR, fqn, signature, null, node.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));
      } else {
        entityWriter.writeEntity(Entity.CONSTRUCTOR, fqn, signature, rawSignature, node.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));
      }
    } else {
      type = Entity.METHOD;
      fqn = fqnStack.getFqn() + '.' + node.getName().getIdentifier();
      
      // Write the entity
      if (signature.equals(rawSignature)) {
        entityWriter.writeEntity(Entity.METHOD, fqn, signature, null, node.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));
      } else {
        entityWriter.writeEntity(Entity.METHOD, fqn, signature, rawSignature, node.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));
      }
      
      // Write the returns relation
      Type returnType = node.getReturnType2();
      if (returnType == null) {
        logger.severe("Null return type for " + fqn);
      } else {
        relationWriter.writeRelation(Relation.RETURNS, fqn, getTypeFqn(returnType), getLocation(returnType));
      }
    }
    
    // Write the inside relation
    relationWriter.writeRelation(Relation.INSIDE, fqn, fqnStack.getFqn(), getUnknownLocation());

    // Write the throws relation
    for (Name name : (List<Name>)node.thrownExceptions()) {
      ITypeBinding exceptionBinding = name.resolveTypeBinding();
      if (exceptionBinding == null) {
        relationWriter.writeRelation(Relation.THROWS, fqn, getUnknownFqn(name.getFullyQualifiedName()), getLocation(node));
      } else {
        relationWriter.writeRelation(Relation.THROWS, fqn, getTypeFqn(exceptionBinding), getLocation(name));
      }
    }
    
    // Attempt to determine overrides relations
    Deque<ITypeBinding> bindingStack = Helper.newStack();
    IMethodBinding method = node.resolveBinding();
    if (method != null) {
      ITypeBinding declaringClass = method.getDeclaringClass();
      if (declaringClass != null && declaringClass.getSuperclass() != null) {
        bindingStack.add(declaringClass.getSuperclass());
      }
      for (ITypeBinding interfaceBinding : declaringClass.getInterfaces()) {
        bindingStack.add(interfaceBinding);
      }
      while (!bindingStack.isEmpty()) {
        ITypeBinding top = bindingStack.pop();
        for (IMethodBinding methodBinding : top.getDeclaredMethods()) {
          if (method.overrides(methodBinding)) {
            relationWriter.writeRelation(Relation.OVERRIDES, fqn, getMethodName(methodBinding, false) + getMethodArgs(methodBinding), getLocation(node));
          }
        }
        ITypeBinding superType = top.getSuperclass();
        if (superType != null) {
          bindingStack.add(superType);
        }
        for (ITypeBinding interfaceBinding : top.getInterfaces()) {
          bindingStack.add(interfaceBinding);
        }
      }
    }
      
    fqnStack.push(fqn, type);

    return true;
  }

  /**
   * This method writes:
   * <ul>
   *   <li>Default constructor call relation to <code>IRelationWriter</code>.</li>
   * </ul>
   */
  @Override
  public void endVisit(MethodDeclaration node) {
    // Write the default constructor call if needed
    if (node.isConstructor() && !fqnStack.wasSuperInvoked()) {
      IMethodBinding method = node.resolveBinding();
      if (method != null) {
        ITypeBinding declaring = method.getDeclaringClass();
        if (declaring != null) {
          if (declaring.isEnum()) {
            relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), "java.lang.Enum.<init>(java.lang.String,int)", getUnknownLocation());
          } else {
            ITypeBinding parent = declaring.getSuperclass();
            if (parent == null) {
              relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), "java.lang.Object.<init>()", getUnknownLocation());
            } else {
              relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), getTypeFqn(getBaseType(parent)) + ".<init>()", getUnknownLocation());
            }
          }
        }
      }
    }
    fqnStack.pop();
  }

  /**
   * This method writes:
   *<ul>
   *  <li>Calls relation to <code>IRelationWriter</code>.</li>
   *</ul>
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(MethodInvocation node) {
    // Get the fqn
    String fqn = null;
    IMethodBinding binding = node.resolveMethodBinding();
    if (binding == null) {
      fqn = UNKNOWN + "." + node.getName().getFullyQualifiedName() + getFuzzyMethodArgs(node.arguments());
    } else {
      fqn = getMethodName(binding, false) + getMethodArgs(binding);
    }

    // Write the calls relation
    relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, new Location(fqnStack.getEnclosingClass(), compilationUnitPath, node.getName().getStartPosition(), node.getLength() - (node.getName().getStartPosition() - node.getStartPosition())));
    
    return true;
  }

  /**
   * This method writes:
   *<ul>
   *  <li>Calls relation to <code>IRelationWriter</code>.</li>
   *</ul>
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(SuperMethodInvocation node) {
    // Get the fqn
    String fqn = null;
    IMethodBinding binding = node.resolveMethodBinding();
    if (binding == null) {
      fqn = getUnknownSuperFqn(node.getName().getIdentifier()) + getFuzzyMethodArgs(node.arguments());
    } else {
      fqn = getMethodName(binding, false) + getMethodArgs(binding);
      
    } 
    
    // Write the calls relation
    relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, getLocation(node));

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
  @SuppressWarnings("unchecked")
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
        relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, getLocation(node));
        // Write the instantiates relation
        relationWriter.writeRelation(Relation.INSTANTIATES, fqnStack.getFqn(), getTypeFqn(node.getType()), getLocation(node));
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
  @SuppressWarnings("unchecked")
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
    relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, getLocation(node));
    fqnStack.reportSuperInvocation();
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
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(SuperConstructorInvocation node) {
    // Get the fqn
    String fqn = null;
    IMethodBinding binding = node.resolveConstructorBinding();
    if (binding == null) {
      fqn = getUnknownSuperFqn("<init>") + getFuzzyMethodArgs(node.arguments());
    } else {
      fqn = getMethodName(binding, false) + getMethodArgs(binding);
    }

    // Write the call relation
    relationWriter.writeRelation(Relation.CALLS, fqnStack.getFqn(), fqn, getLocation(node));

    fqnStack.reportSuperInvocation();
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
    relationWriter.writeRelation(Relation.CASTS, fqnStack.getFqn(), getTypeFqn(node.getType()), getLocation(node));

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
      fqn = getUnknownFqn(node.getName().getIdentifier());
    } else {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      if (declaringClass != null) {
        declaringClass = declaringClass.getErasure();
      }
      if (declaringClass == null) {
        if (binding.isRecovered()) {
          fqn = getUnknownFqn(binding.getName());
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
      relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), fqn, getLocation(node.getName()));
    } else {
      relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), fqn, getLocation(node.getName()));
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
      fqn = getUnknownSuperFqn(node.getName().getIdentifier());
    } else {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      if (declaringClass != null) {
        declaringClass = declaringClass.getErasure();
      }
      if (declaringClass == null) {
        fqn = getUnknownSuperFqn(binding.getName());
      } else {
        fqn = getTypeFqn(declaringClass) + "." + binding.getName();
      }
    }
    
    // Write the relation
    if (inLhsAssignment) {
      relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), fqn, getLocation(node));
    } else {
      relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), fqn, getLocation(node));
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
        relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(typeBinding), getLocation(node));
      } else {
        // Write the uses relation
        relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(typeBinding), getLocation(node.getName()));
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
            fqn = getUnknownFqn(binding.getName());
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
          relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), fqn, getLocation(node));
        } else {
          relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), fqn, getLocation(node));
        }
//        if (!inFieldDeclaration) {
//          relationWriter.writeUses(fqnStack.getFqn(), fqn, getLocation(node));
//        }
      } else {
        // Write the uses relation
//        relationWriter.writeUses(fqnStack.getFqn(), getTypeFqn(getBaseType(varBinding.getType())), getLocation(node));
      }
    } else if (binding instanceof ITypeBinding) {
      ITypeBinding typeBinding = (ITypeBinding) binding;
      // Write the uses relation
      relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(getBaseType(typeBinding)), getLocation(node));
    }
    return true;
  }

  @Override
  public boolean visit(ParameterizedType node) {
    String typeFqn = getTypeFqn(node);
      
    // Write uses
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), typeFqn, getLocation(node));

    return true;
  }
  
  @Override
  public boolean visit(QualifiedType node) {
    ITypeBinding typeBinding = getBaseType(node.resolveBinding());
    IBinding qualifierBinding = node.getQualifier().resolveBinding();
    if (qualifierBinding instanceof IPackageBinding) {
      // Write the uses relation
      relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(typeBinding), getLocation(node));
    } else {
      // Write the uses relation
      relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(typeBinding), getLocation(node.getName()));
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
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), node.toString(), getLocation(node));
    return false;
  }
  
  @Override
  public boolean visit(ArrayType node) {
    // Write the uses relation
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), getTypeFqn(node), getLocation(node));
    
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
    relationWriter.writeRelation(Relation.CHECKS, fqnStack.getFqn(), getTypeFqn(node.getRightOperand()), getLocation(node));

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
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), "boolean", getLocation(node));
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
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), "char", getLocation(node));
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
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), node.resolveTypeBinding().getQualifiedName(), getLocation(node));
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
    commentWriter.writeComment(Comment.BLOCK, getLocation(node));
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
    commentWriter.writeComment(Comment.LINE, getLocation(node));
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
      commentWriter.writeComment(Comment.UJAVADOC, getLocation(node));
    } else {
      commentWriter.writeComment(Comment.JAVADOC, fqnStack.getFqn(), getLocation(node));
    }
    return true;
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
  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    // Get the fqn
    String fqn = null;
    if (node.isPackageMemberTypeDeclaration()) {
      fqn = fqnStack.getTypeFqn(node.getName().getIdentifier());
    } else if (node.isMemberTypeDeclaration()) {
      if (node.getName().getIdentifier().length() > 0) {
        fqn = fqnStack.getMemberFqn(node.getName().getIdentifier());
      } else {
        logger.severe("A annotation declaration should not declare an annonymous type!");
        fqn = fqnStack.getAnonymousClassFqn();
      }
    } else if (node.isLocalTypeDeclaration()) {
      ITypeBinding binding = node.resolveBinding();
      fqn = fqnStack.getLocalFqn(node.getName().getIdentifier(), binding);
    } else {
      logger.severe("Unsure what type the declaration is!");
      fqn = "(ERROR)" + node.getName().getIdentifier();
    }
    
    String parent = fqnStack.getFqn();
    
    fqnStack.push(fqn, Entity.ANNOTATION);
    
    // Write the entity
    entityWriter.writeEntity(Entity.ANNOTATION, fqn, node.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));

    // Write the inside relation
    relationWriter.writeRelation(Relation.INSIDE, fqn, parent, getUnknownLocation());

    // Write the extends relation
    relationWriter.writeRelation(Relation.EXTENDS, fqn, "java.lang.Object", getUnknownLocation());
    
    // Write the implements relation
    relationWriter.writeRelation(Relation.IMPLEMENTS, fqn, "java.lang.annotation.Annotation", getUnknownLocation());
    
    accept(node.getJavadoc());
    accept(node.modifiers());
    accept(node.bodyDeclarations());
    
    return false;
  }
  
  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    fqnStack.pop();
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
    String fqn = fqnStack.getFqn() + "." + node.getName().getIdentifier() + "()";

    // Write the entity
    entityWriter.writeEntity(Entity.ANNOTATION_ELEMENT, fqn, node.getModifiers(), MetricsCalculator.computeLinesOfCode(getSource(node)), getLocation(node));

    // Write the inside relation
    relationWriter.writeRelation(Relation.INSIDE, fqn, fqnStack.getFqn(), getUnknownLocation());

    // Write the returns relation
    Type returnType = node.getType();
    relationWriter.writeRelation(Relation.RETURNS, fqn, getTypeFqn(returnType), getLocation(returnType));

    fqnStack.push(fqn, Entity.ANNOTATION_ELEMENT);
    
    return true;
  }
  
  @Override
  public void endVisit(AnnotationTypeMemberDeclaration node) {
    fqnStack.pop();
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
      fqn = getUnknownFqn(node.getTypeName().getFullyQualifiedName());
    } else {
      ITypeBinding typeBinding = binding.getAnnotationType();
      if (typeBinding == null) {
        fqn = getUnknownFqn(binding.getName());
      } else {
        fqn = getTypeFqn(typeBinding);
      }
    }
    
    // Write the annotates relation
    relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn(), fqn, getLocation(node));
    
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
      fqn = getUnknownFqn(node.getTypeName().getFullyQualifiedName());
    } else {
      ITypeBinding typeBinding = binding.getAnnotationType();
      if (typeBinding == null) {
        fqn = getUnknownFqn(binding.getName());
      } else {
        fqn = getTypeFqn(typeBinding);
      }
    }    
    
    // Write the annotates relation
    relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn(), fqn, getLocation(node));
    
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
      fqn = getUnknownFqn(node.getTypeName().getFullyQualifiedName());
    } else {
      ITypeBinding typeBinding = binding.getAnnotationType();
      if (typeBinding == null) {
        fqn = getUnknownFqn(binding.getName());
      } else {
        fqn = getTypeFqn(typeBinding);
      }
    }    
    
    // Write the annotates relation
    relationWriter.writeRelation(Relation.ANNOTATED_BY, fqnStack.getFqn(), fqn, getLocation(node));
    
    return true;
  }
  
//  @SuppressWarnings("unchecked")
  
  @Override
  public boolean visit(ArrayAccess node) {
    // TODO Auto-generated method stub    
    return super.visit(node);
  }
  


  @Override
  public boolean visit(ArrayInitializer node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(AssertStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(Block node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(BreakStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ConditionalExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ContinueStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(DoStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(EmptyStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    relationWriter.writeRelation(Relation.USES, fqnStack.getFqn(), "java.lang.Iterable", getUnknownLocation());
    return super.visit(node);
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ForStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(IfStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(InfixExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(LabeledStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MemberRef node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MemberValuePair node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MethodRef node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MethodRefParameter node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(Modifier node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ParenthesizedExpression node) {
    // TODO Auto-generated method stub
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
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SwitchCase node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SwitchStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(SynchronizedStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TagElement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TextElement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(ThisExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TryStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TypeDeclarationStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(TypeLiteral node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(WhileStatement node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(WildcardType node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }
  
  private Location getLocation(ASTNode node) {
    return new Location(fqnStack.getEnclosingClass(), compilationUnitPath, node.getStartPosition(), node.getLength());
  }
  
  private Location getUnknownLocation() {
    return new Location(fqnStack.getEnclosingClass(), compilationUnitPath, null, null);
  }
  
  private String getSource(ASTNode node) {
    if (compilationUnitSource == null || node.getStartPosition() == -1) {
      return null;
    } else {
      return compilationUnitSource.substring(node.getStartPosition(), node.getStartPosition() + node.getLength());
    }
  }
  
//  @SuppressWarnings("unchecked")
//  private String getFuzzyConstructorFqn(ClassInstanceCreation creation) {
//    StringBuilder fqnBuilder = new StringBuilder();
//    fqnBuilder.append(getTypeFqn(creation.getType())).append(".<init>");
//    getFuzzyMethodArgs(fqnBuilder, creation.arguments());
//    return fqnBuilder.toString();
//  }
  
//  @SuppressWarnings("unchecked")
//  private String getFuzzyConstructorFqn(MethodDeclaration declaration) {
//    StringBuilder fqnBuilder = new StringBuilder();
//    fqnBuilder.append(fqnStack.getFqn()).append(".<init>");
//    getMethodParams(fqnBuilder, declaration.parameters());
//    return fqnBuilder.toString();
//  }

//  @SuppressWarnings("unchecked")
//  private String getFuzzyMethodFqn(MethodInvocation invocation) {
//    StringBuilder fqnBuilder = new StringBuilder();
//    fqnBuilder.append(UNKNOWN).append(".").append(invocation.getName().getFullyQualifiedName());
//    getFuzzyMethodArgs(fqnBuilder, invocation.arguments());
//    return fqnBuilder.toString();
//  }
  
//  @SuppressWarnings("unchecked")
//  private String getFuzzyMethodFqn(MethodDeclaration declaration) {
//    StringBuilder fqnBuilder = new StringBuilder();
//    fqnBuilder.append(fqnStack.getFqn()).append('.').append(declaration.getName().getIdentifier());
//    getMethodParams(fqnBuilder, declaration.parameters());
//    return fqnBuilder.toString();
//  }

//  private String getFuzzyMethodArgs(Iterable<Expression> arguments) {
//    StringBuilder argBuilder = new StringBuilder();
//    getFuzzyMethodArgs(argBuilder, arguments);
//    return argBuilder.toString();
//  }

  private String getMethodArgs(IMethodBinding binding) {
    StringBuilder builder = new StringBuilder();
    getMethodArgs(builder, binding);
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
    getErasedMethodArgs(builder, binding);
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
  
  private String getFuzzyMethodArgs(Iterable<Expression> arguments) {
    StringBuilder builder = new StringBuilder();
    getFuzzyMethodArgs(builder, arguments);
    return builder.toString();
  }
  
  private void getFuzzyMethodArgs(StringBuilder argBuilder, Iterable<Expression> arguments) {
    boolean first = true;
    argBuilder.append('(');
    for (Expression exp : arguments) {
      ITypeBinding binding = exp.resolveTypeBinding();
      if (first) {
        first = false;
      } else {
        argBuilder.append(',');
      }
      if (binding == null) {
        argBuilder.append(UNKNOWN);
      } else {
        argBuilder.append(getTypeFqn(binding));
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

//  private String getAnonymousConstructorFqn(String classFqn, IMethodBinding binding) {
//    StringBuilder fqnBuilder = new StringBuilder(classFqn);
//    fqnBuilder.append(".<init>");
//    getMethodArgs(fqnBuilder, binding);
//    return fqnBuilder.toString();
//  }
  
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

  private static String getNameFromTypeFqn(String fqn) {
    int dot = fqn.lastIndexOf('.');
    if (dot == -1) {
      return fqn;
    } else {
      return fqn.substring(dot + 1);
    }
  }
  
  private static final String BRACKETS = "[][][][][][][][][][][][][][][][][][][][]";
 
  private String getUnknownFqn(String name) {
    return UNKNOWN + name;
  }
  
  private String getUnknownSuperFqn(String name) {
    return "(1SUPER)" + name;
  }
  
  private String getErasedTypeFqn(Type type) {
    if (type == null) {
      logger.log(Level.SEVERE, "Attempt to get type fqn of null type!");
      throw new NullPointerException("Attempt to get type fqn of null type!");
    }
    ITypeBinding binding = type.resolveBinding();
    if (binding == null) {
      if (type.isPrimitiveType()) {
        return ((PrimitiveType)type).getPrimitiveTypeCode().toString();
      } else if (type.isSimpleType()) {
        return getUnknownFqn(((SimpleType)type).getName().getFullyQualifiedName());
      } else if (type.isArrayType()) {
        ArrayType arrayType = (ArrayType) type;
        Type elementType = arrayType.getElementType();
        if (elementType == null) {
          return getUnknownFqn(BRACKETS.substring(0, 2 * arrayType.getDimensions()));
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
    ITypeBinding binding = type.resolveBinding();
    if (binding == null) {
      if (type.isPrimitiveType()) {
        return ((PrimitiveType)type).getPrimitiveTypeCode().toString();
      } else if (type.isSimpleType()) {
        return getUnknownFqn(((SimpleType)type).getName().getFullyQualifiedName());
      } else if (type.isArrayType()) {
        ArrayType arrayType = (ArrayType) type;
        Type elementType = arrayType.getElementType();
        if (elementType == null) {
          return getUnknownFqn(BRACKETS.substring(0, 2 * arrayType.getDimensions()));
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
      String fqn = binding.getBinaryName();
      if (fqn == null) {
        return UNKNOWN;
      } else {
//        fqn = fqn.replaceAll("\\$(\\d+)", "\\$anonymous-$1");
        return fqn;
      }
    } else if (binding.isLocal()) {
//      String fqn = binding.getBinaryName();
//      if (fqn == null) {
      return fqnStack.getLocalFqn(binding.getName(), binding);
//        if (fqnStack.isMethodTop()) {
//          // TODO check local type binary names
//          return fqnStack.getAnonymousClassFqn() + binding.getName();
//        } else {
//          return getTypeFqn(binding.getDeclaringClass()) + "$" + binding.getName();
//        }
//      } else {
//        return fqn;
//      }
    } else if (binding.isParameterizedType()) {
      StringBuilder fqn = new StringBuilder();
      if (binding.getErasure().isParameterizedType()) {
        logger.log(Level.SEVERE, "Parametrized type erasure is a parametrized type: " + binding.getQualifiedName());
        fqn.append(getUnknownFqn(binding.getQualifiedName()));
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
              return getUnknownFqn(binding.getName());
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
      stack = Helper.newStack();
    }
    
    public void clear() {
      stack.clear();
    }

    public void push(String fqn, Entity type) {
      stack.push(new Enclosing(fqn, type));
    }

    public void pop() {
      stack.pop();
    }

    public String getTypeFqn(String identifier) {
      if (stack.isEmpty()) {
        return identifier;
      } else {
        return stack.peek().getFqn() + "." + identifier;
      }
    }
    
    public String getMemberFqn(String identifier) {
      if (stack.isEmpty()) {
        logger.log(Level.SEVERE, "Cannot have member declaration with empty stack.");
        return identifier;
      } else {
        return stack.peek().getFqn() + "$" + identifier;
      }
    }
    
    public String getLocalFqn(String identifier, ITypeBinding binding) {
      if (binding == null) {
        logger.log(Level.SEVERE, "Cannot have local declaration with no binding.");
        return getUnknownFqn(identifier);
      }
      if (stack.isEmpty()) {
        logger.log(Level.SEVERE, "Cannot have local declaration with empty stack.");
        return getUnknownFqn(identifier);
      } else {
        String uniqueID = binding.getBinaryName();
        if (uniqueID == null) {
          uniqueID = binding.getDeclaringClass().getBinaryName();
          if (binding.getDeclaringMethod() != null) {
            uniqueID += binding.getDeclaringMethod().getName();
          }
          uniqueID += identifier;
        }
        for (Enclosing fqn : stack) {
          if (fqn.isDeclaredType()) {
            String localFqn = fqn.getLocalClassFqn(identifier, uniqueID);
            if (localFqn != null) {
              return localFqn;
            }
          }
        }
        for (Enclosing fqn : stack) {
          if (fqn.isDeclaredType()) {
            return fqn.createLocalClassFqn(identifier, uniqueID);
          }
        }
        logger.log(Level.SEVERE, "Cannot have local declaration with no declared types on stack.");
        return getUnknownFqn(identifier);
      }
    }
    
    public String getFqn() {
      return stack.peek().getFqn();
    }

    public String getInitializerFqn() {
      return stack.peek().getInitializerFqn();
    }

    public String getAnonymousClassFqn() {
      for (Enclosing fqn : stack) {
        if (fqn.isDeclaredType()) {
          return fqn.getAnonymousClassFqn();
        }
      }
      return null;
    }

    public void reportSuperInvocation() {
      stack.peek().reportSuperInvocation();
    }
    
    public boolean wasSuperInvoked() {
      return stack.peek().wasSuperInvoked();
    }
   
    public int getNextParameterPos() {
      return stack.peek().getNextParameterPos();
    }
    
    public int getNextTypeParameterPos() {
      return stack.peek().getNextTypeParameterPos();
    }
    
    public String getEnclosingClass() {
      for (Enclosing item : stack) {
        if (item.isDeclaredType()) {
          return item.getFqn();
        }
      }
      throw new IllegalStateException("There must always be an enclosing class.");
    }
  }

  private class Enclosing {
    private String fqn;
    private Entity type;
    private int initializerCount;
    private int anonymousClassCount;
    private int parameterCount;
    private int typeParameterCount;
    private boolean superInvoked;
    
    private Map<String, String> localClassMap;

    private Enclosing(String fqn, Entity type) {
      this.fqn = fqn;
      this.type = type;
      this.initializerCount = 0;
      this.anonymousClassCount = 0;
      this.parameterCount = 0;
      this.superInvoked = false;
      this.localClassMap = null;
    }

    public void reportSuperInvocation() {
      if (type.isConstructor()) {
        superInvoked = true;
      } else {
        logger.log(Level.SEVERE, "Super invocation reported for non-constructor in" + compilationUnitPath);
      }
    }
    
    public boolean wasSuperInvoked() {
      if (type.isConstructor()) {
        return superInvoked;
      } else {
        throw new IllegalStateException("Only a constructor can have a super invoked: " + type);
      }
    }
    
    public String getFqn() {
      return fqn;
    }

    public String getInitializerFqn() {
      return fqn + ".initializer-" + ++initializerCount;
    }

    public String getAnonymousClassFqn() {
//      return fqn + "$anonymous-" + ++anonymousClassCount;
      return fqn + "$" + ++anonymousClassCount;
    }
    
    public String createLocalClassFqn(String name, String uniqueID) {
      if (localClassMap == null) {
        localClassMap = Helper.newHashMap();
      }
      String retval = fqn + "$" + (localClassMap.size() + 1) + name;
      localClassMap.put(uniqueID, retval);
      return retval;
    }
    
    public String getLocalClassFqn(String name, String binaryName) {
      if (localClassMap == null) {
        localClassMap = Helper.newHashMap();
      }
      return localClassMap.get(binaryName);
    }
    
    public boolean isDeclaredType() {
      return type.isDeclaredType() || type.isPackage();
    }

    public int getNextParameterPos() {
      return parameterCount++;
    }
    
    public int getNextTypeParameterPos() {
      return typeParameterCount++;
    }
  }
  
  private void accept(ASTNode child) {
    if (child != null) {
      child.accept(this);
    }
  }
  
  private void accept(List<? extends ASTNode> children) {
    for (ASTNode child : children) {
      child.accept(this);
    }
  }
}
