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

// aspartame 
  /* blah */package test   
  
  ;

import java.io.BufferedReader;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TestErasure <K extends Closeable & Iterable<K>> {
  public <T> T method(T blah) {
    return blah;
  }
 
  public <T extends Closeable> T method2(T blah) {
    return blah;
  }
  
  public void method3() {
    Map<String, String> map = new HashMap<>();
    map.put("Hi", method("ho"));
    method2(new BufferedReader(null));
  }

  public void method4(K something) {
    method4(something);
    something.iterator();
  }
}
