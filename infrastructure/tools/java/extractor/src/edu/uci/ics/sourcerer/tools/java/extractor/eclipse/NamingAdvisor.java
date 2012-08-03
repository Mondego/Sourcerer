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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IMethodInfo;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class NamingAdvisor {
  private Map<String, String> classMap;
  private Map<String, String> methodMap;
  private NamingAdvisor() {
    classMap = new HashMap<>();
    methodMap = new HashMap<>();
  }
  
  public String advise(String name) {
    return classMap.get(name);
  }
  
  private void addClassSig(char[] sig) {
    String s = new String(sig).replace('/', '.');
    addClass(Signature.toString(s));
  }
  
  private void addClass(String fqn) {
    String simpleName = fqn.substring(fqn.lastIndexOf('.') + 1);
    String other = classMap.get(simpleName);
    if (other == null) {
      classMap.put(simpleName, fqn);
    } else if (!other.equals(fqn)) {
      classMap.put(simpleName, "2_UNKNOWN." + simpleName);
    }
  }
  
  
  public void addImport(String fqn) {
    addClass(fqn);
  }
  
  public String adviseMethod(MethodInvocation node) {
    String lookup = node.getName().getFullyQualifiedName() + node.arguments().size();
    return methodMap.get(lookup);
  }
  
  private void addMethod(String klass, String name, char[] desc) {
    String lookup = name + Signature.getParameterCount(desc);
    String fqn = klass.replace('/', '.') + "." + name + convertDescriptor(desc);
    String other = methodMap.get(lookup);
    if (other == null) {
      methodMap.put(lookup, fqn);
    } else if (!other.equals(fqn)) {
      methodMap.put(lookup, null);
    }
  }
  
  private static String convertDescriptor(char[] desc) {
    StringBuilder params = new StringBuilder();
    params.append('(');
    for (char[] p : Signature.getParameterTypes(desc)) {
      params.append(Signature.toString(new String(p).replace('/', '.'))).append(',');
    }
    if (params.length() > 1) {
      params.setCharAt(params.length() - 1, ')');
    } else {
      params.append(')');
    }
    return params.toString();
  }
  
  static NamingAdvisor create() {
    return new NamingAdvisor();
  }
  
  private void addClassFile(IClassFile classFile) {
    IClassFileReader reader = ToolFactory.createDefaultClassFileReader(classFile, IClassFileReader.CONSTANT_POOL | IClassFileReader.METHOD_INFOS);
    IConstantPool constants = reader.getConstantPool();
    for (int i = 0, max = constants.getConstantPoolCount(); i < max; i++) {
      switch (constants.getEntryKind(i)) {
        case IConstantPoolConstant.CONSTANT_Class:
          {
            IConstantPoolEntry constant = constants.decodeEntry(i);
            addClass(new String(constant.getClassInfoName()).replace('/','.'));
            break;
          }
        case IConstantPoolConstant.CONSTANT_Fieldref:
          {
            IConstantPoolEntry constant = constants.decodeEntry(i);
            addClassSig(constant.getFieldDescriptor());
            break;
          }
        case IConstantPoolConstant.CONSTANT_Methodref:
          {
            IConstantPoolEntry constant = constants.decodeEntry(i);
            addMethod(new String(constant.getClassName()), new String(constant.getMethodName()), constant.getMethodDescriptor());
            break;
          }
        case IConstantPoolConstant.CONSTANT_InterfaceMethodref:
          {
            IConstantPoolEntry constant = constants.decodeEntry(i);
            addMethod(new String(constant.getClassName()), new String(constant.getMethodName()), constant.getMethodDescriptor());
            break;
          }
        default:
      }
    }
    
    // Add the method parameter / return types
    for (IMethodInfo method : reader.getMethodInfos()) {
      for (char[] p : Signature.getParameterTypes(method.getDescriptor())) {
        addClassSig(p);
      }
      addClassSig(Signature.getReturnType(method.getDescriptor()));
    }
  }
  
  static NamingAdvisor create(IClassFile classFile, Collection<IClassFile> members) {
    NamingAdvisor advisor = new NamingAdvisor();
    advisor.addClassFile(classFile);
    if (members != null) {
      for (IClassFile member : members) {
        advisor.addClassFile(member);
      }
    }
    return advisor;
  }
}
