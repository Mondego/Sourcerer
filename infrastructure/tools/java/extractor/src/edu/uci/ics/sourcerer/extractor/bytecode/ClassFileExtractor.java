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
package edu.uci.ics.sourcerer.extractor.bytecode;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.logging.Level;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import edu.uci.ics.sourcerer.extractor.io.WriterBundle;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClassFileExtractor implements ClassVisitor {
  private
  public ClassFileExtractor(WriterBundle writers) {
    writers.get
  }
  
  public void extractClassFile(IClassFile file) {
    try {
      ClassReader reader = new ClassReader(file.getBytes());
      reader.accept(this, 0);
    } catch (JavaModelException e) {
      logger.log(Level.SEVERE, "Unable to get contents of jar file.", e);
    }
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    logger.info(name + ": " + signature);
  }

  
  @Override
  public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
    return null;
  }

  @Override
  public void visitAttribute(Attribute arg0) {
  }

  @Override
  public void visitEnd() {
  }

  @Override
  public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4) {
    return null;
  }

  @Override
  public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    logger.info("  " + name + ": " + signature);
    return null;
  }

  @Override
  public void visitOuterClass(String arg0, String arg1, String arg2) {
  }

  @Override
  public void visitSource(String arg0, String arg1) {
  }
}
