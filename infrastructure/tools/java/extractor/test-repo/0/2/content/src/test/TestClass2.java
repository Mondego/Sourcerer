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
package test;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TestClass2 {
  private Integer field;
  
  static {
    System.err.println("debug");
  }
  
  {
    System.out.println("boo");
  }
  
  static {
    System.err.println("debug 3");
  }
  
  public TestClass2() {
  }
  
  public TestClass2(Integer foo) {
    this.field = foo;
  }
  
  public TestClass2(Integer foo, String bar) {
    this(foo);
    System.out.println(field++);
  }
  
  public void method() {
    System.out.println("bar");
  }
  
  public static void method2() {
    System.out.println("baz");
  }
  
  public Integer getField() {
    return field;
  }
}