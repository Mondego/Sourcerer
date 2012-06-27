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

import java.util.Iterator;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@Deprecated
public class TestClass {
  @Deprecated
  public void method(@TestAnnotation(blahs = { "foo", "bar" }, value = "snug") String foo) {
    foo = new String("flor");
  }
  
  public Iterator<String> method2() {
    return new Iterator<String>() {
      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public String next() {
        new Object() {
          @Deprecated
          String blah;
        };
        return null;
      }

      @Override
      public void remove() {
      }};
  }
}
