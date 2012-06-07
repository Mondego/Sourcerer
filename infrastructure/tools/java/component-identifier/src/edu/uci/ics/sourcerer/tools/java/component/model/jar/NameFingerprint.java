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
package edu.uci.ics.sourcerer.tools.java.component.model.jar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.uci.ics.sourcerer.util.io.LineBuilder;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class NameFingerprint extends Fingerprint {
  private String superName;
  private String[] interfaces;
  private String[] fields;
  private String[] methods;
  private String[] innerClasses;
  private int hash = 0;
  
  private NameFingerprint() {
    super();
  }
  
  private static FingerprintClassVisitor CLASS_VISITOR = new FingerprintClassVisitor();
  private static class FingerprintClassVisitor extends ClassVisitor {
    private NameFingerprint fingerprint;
    private Collection<String> fields;
    private Collection<String> methods;
    private Collection<String> innerClasses;

    
    private FingerprintClassVisitor() {
      super(Opcodes.V1_7);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      fingerprint = new NameFingerprint();
      
      fingerprint.superName = superName;
      fingerprint.interfaces = interfaces;
      Arrays.sort(fingerprint.interfaces);
      
      fields = new ArrayList<>();
      methods = new ArrayList<>();
      innerClasses = new ArrayList<>();
    }
   
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
        fields.add(desc);
      }
      return null;
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      methods.add(desc);
      return null;
    }
    
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
      innerClasses.add(name);
    }
    
    
    public NameFingerprint createFingerprint() {
      fingerprint.fields = fields.toArray(new String[fields.size()]);
      Arrays.sort(fingerprint.fields);
      fingerprint.methods = methods.toArray(new String[methods.size()]);
      Arrays.sort(fingerprint.methods);
      fingerprint.innerClasses = innerClasses.toArray(new String[innerClasses.size()]);
      Arrays.sort(fingerprint.innerClasses);
      return fingerprint;
    }
  }
  
  static NameFingerprint create(InputStream is) throws IOException {
    ClassReader reader = new ClassReader(is);
    reader.accept(CLASS_VISITOR, 0);
    return CLASS_VISITOR.createFingerprint();
  }
  
  @Override
  public int hashCode() {
    if (hash == 0) {
      hash = Arrays.hashCode(methods);
    }
    return hash;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof NameFingerprint) {
      NameFingerprint other = (NameFingerprint) o;
      return superName.equals(other.superName) &&
          Arrays.equals(interfaces, other.interfaces) &&
          Arrays.equals(fields, other.fields) &&
          Arrays.equals(methods, other.methods) &&
          Arrays.equals(innerClasses, other.innerClasses);
    } else {
      return false;
    }
  }
  
  @Override
  public String serialize() {
    LineBuilder builder = new LineBuilder();
    builder.append(superName);
    builder.append(interfaces);
    builder.append(fields);
    builder.append(methods);
    builder.append(innerClasses);
    return builder.toString();
  }
  
  public static ObjectDeserializer<Fingerprint> makeDeserializer() {
    return new ObjectDeserializer<Fingerprint>() {
      private String[] deserializeArray(Scanner scanner) {
        int length = scanner.nextInt();
        String[] result = new String[length];
        for (int i = 0; i < length; i++) {
          result[i] = scanner.next();
        }
        return result;
      }
      
      @Override
      public Fingerprint deserialize(Scanner scanner) {
        NameFingerprint fingerprint = new NameFingerprint();
        
        fingerprint.superName = scanner.next();
        if ("null".equals(fingerprint.superName)) {
          fingerprint.superName = null;
        }
        fingerprint.interfaces = deserializeArray(scanner);
        fingerprint.fields = deserializeArray(scanner);
        fingerprint.methods = deserializeArray(scanner);
        fingerprint.innerClasses = deserializeArray(scanner);
        
        return fingerprint;
      }
    };
  }
}
