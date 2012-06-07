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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TypeFingerprint extends Fingerprint {
  private final String fingerprint;
  
  private TypeFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }
  
  private static FingerprintClassVisitor CLASS_VISITOR = new FingerprintClassVisitor();
  private static class FingerprintClassVisitor extends ClassVisitor {
    private StringBuilder result;
    private Collection<String> fields;
    private Collection<String> methods;
    private Collection<String> innerClasses;
    private Set<String> referencedTypes;
    private Set<String> calledMethods;
    private MessageDigest md5;
    private MethodVisitor methodVisitor = new MethodVisitor(Opcodes.V1_7) {
      @Override
      public void visitTypeInsn(int opcode, String desc) {
        referencedTypes.add(desc);
      }
      
      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        calledMethods.add(desc);
      }
    };
    
    private FingerprintClassVisitor() {
      super(Opcodes.V1_7);
      try {
        md5 = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        logger.log(Level.SEVERE, "Unable to load message digest", e);
        throw new RuntimeException(e);
      }
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      result = new StringBuilder();
      // Record the superclass name
      result.append(superName);
      
      // Record the interfaces
      Arrays.sort(interfaces);
      for (String iface : interfaces) {
        result.append("iface").append(iface);
      }
      
      fields = new ArrayList<>();
      methods = new ArrayList<>();
      innerClasses = new ArrayList<>();
      referencedTypes = new TreeSet<>();
      calledMethods = new TreeSet<>();
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
      return methodVisitor;
    }
    
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
      innerClasses.add(name);
    }
    
    
    public String createFingerprint() {
      // Add the fields
      String[] arr = fields.toArray(new String[fields.size()]);
      Arrays.sort(arr);
      for (String field : arr) {
        result.append("f").append(field);
      }
      // Add the methods
      arr = methods.toArray(new String[methods.size()]);
      Arrays.sort(arr);
      for (String field : arr) {
        result.append("m").append(field);
      }
      // Add the inner classes
      arr = innerClasses.toArray(new String[innerClasses.size()]);
      Arrays.sort(arr);
      for (String field : arr) {
        result.append("in").append(field);
      }
      // Add the referenced types
      for (String type : referencedTypes) {
        result.append("t").append(type);
      }
      // Add the called methods
      for (String method : calledMethods) {
        result.append("cm").append(method);        
      }
      
      md5.update(result.toString().getBytes());
      String hash = new BigInteger(1, md5.digest()).toString(16);
      md5.reset();
      return hash;
    }
  }
    
  static TypeFingerprint create(InputStream is) throws IOException {
    try {
      ClassReader reader = new ClassReader(is);
      reader.accept(CLASS_VISITOR, 0);
      return new TypeFingerprint(CLASS_VISITOR.createFingerprint());
    } catch (Exception e) {
      return new TypeFingerprint("ERROR");
    }
  }
    
  @Override
  public int hashCode() {
    return fingerprint.hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof TypeFingerprint) {
      return fingerprint.equals(((TypeFingerprint)o).fingerprint);
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return fingerprint;
  }
  
  @Override
  public String serialize() {
    return fingerprint;
  }
  
  public static ObjectDeserializer<Fingerprint> makeDeserializer() {
    return new ObjectDeserializer<Fingerprint>() {
      @Override
      public Fingerprint deserialize(Scanner scanner) {
        return new TypeFingerprint(scanner.next());
      }
    };
  }
}
