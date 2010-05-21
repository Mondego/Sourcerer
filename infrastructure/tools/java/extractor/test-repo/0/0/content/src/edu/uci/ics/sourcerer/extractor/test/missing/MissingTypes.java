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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
package edu.uci.ics.sourcerer.extractor.test.missing;

import java.util.LinkedList;
import java.util.List;

import missing.pkg.Foo;
import missing.pkg.Bar;
import missing.pkg.Baz;
import missing.ondemand.*;
import other.option.*;

public class MissingTypes {
  private Foo foo;
  private Bar bar;
  private Baz baz;
  
  public static Foo foo(Foo foo, Bar bar, Baz baz) {
    List<Baz> bazList = new LinkedList<Bar>();
    bazList.add(new FooBar());
    bazList.add(new BazFoo());
    BazFoo bazFoo = BazFoo.bar();
    return new Foo(bar, baz);
 }
}
