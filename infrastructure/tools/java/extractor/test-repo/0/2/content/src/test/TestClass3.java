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
public class TestClass3 {
  private int bar = Math.min(3, 54);
  public class InnerClass {
    private String foo = "three" + "five".substring(0, 3);
    
    public String getFoo() {
      bar++;
      return foo;
    }
    
    public class InnerInnerClass {
    }
  }
  
  public static class StaticInnerClass {
    
  }
  
  public static void main(String[] args) {
    class MethodClass {}
    new MethodClass();
  }
}
