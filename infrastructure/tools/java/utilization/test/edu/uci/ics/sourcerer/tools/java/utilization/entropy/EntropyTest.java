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
package edu.uci.ics.sourcerer.tools.java.utilization.entropy;

import junit.framework.Assert;

import org.junit.Test;

import edu.uci.ics.sourcerer.tools.java.utilization.identifier.Library;
import edu.uci.ics.sourcerer.tools.java.utilization.identifier.LibraryMockFactory;
import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EntropyTest {

  @Test
  public void test() {
    LibraryEntopyCalculator calc = LibraryEntropyCalculatorFactory.createCalculator();

    Library library = null;
    double entropy = 0;
    
    // Even division maximizes entropy
    library = LibraryMockFactory.create(
        "foo.A",
        "bar.A");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    library = LibraryMockFactory.create(
        "foo.A",
        "bar.A",
        "baz.A",
        "boo.A");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    // Start skewing it, and the entropy goes down
    library = LibraryMockFactory.create(
        "foo.A",
        "foo.B",
        "foo.C",
        "bar.A",
        "baz.A");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    // Even division to 2 levels
    library = LibraryMockFactory.create(
        "foo.a.A",
        "foo.b.A",
        "bar.a.A",
        "bar.b.A");
    entropy = calc.compute(library);
    printEntropy(library, entropy);

    // Even distribution with multiple FQNs per package
    library = LibraryMockFactory.create(
        "foo.A",
        "foo.B",
        "foo.C",
        "foo.D",
        "bar.A",
        "bar.B",
        "bar.C",
        "bar.D");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    // Slightly skewed distribution with multiple FQNs per package
    library = LibraryMockFactory.create(
        "foo.A",
        "foo.B",
        "bar.A",
        "bar.B",
        "bar.C",
        "bar.D",
        "bar.E",
        "bar.F");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    // Can get the entropy arbitrarily low by adding more
    // Even distribution with multiple FQNs per package
    library = LibraryMockFactory.create(
        "foo.A",
        "foo.B",
        "foo.C",
        "foo.D",
        "foo.E",
        "foo.F",
        "foo.G",
        "foo.H",
        "foo.I",
        "foo.J",
        "bar.A",
        "bar.B",
        "bar.C",
        "bar.D",
        "bar.E",
        "bar.F",
        "bar.G",
        "bar.H",
        "bar.I",
        "bar.J");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    library = LibraryMockFactory.create(
        "foo.A",
        "foo.B",
        "foo.C",
        "foo.D");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    library = LibraryMockFactory.create(
        "foo.A",
        "foo.B",
        "foo.C",
        "foo.D",
        "bar.A");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    library = LibraryMockFactory.create(
        "foo.A",
        "foo.B",
        "foo.C",
        "foo.D",
        "bar.A",
        "bar.B");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
    
    library = LibraryMockFactory.create(
        "javax.servlet.http.Cookie",
        "javax.servlet.http.HttpServletResponse",
        "javax.servlet.ServletConfig",
        "javax.servlet.ServletContext",
        "javax.servlet.ServletException",
        "javax.servlet.ServletInputStream",
        "javax.servlet.ServletOutputStream",
        "javax.servlet.ServletResponse",
        "javax.servlet.ServletRequest",
        "javax.servlet.Servlet");
    entropy = calc.compute(library);
    printEntropy(library, entropy);
  }
  
  private void printEntropy(Library library, double entropy) {
    System.out.println("Entropy: " + entropy);
    for (FqnFragment fqn : library.getFqns()) {
      System.out.println("  " + fqn.getFqn());
    }
    System.out.println();
  }
}
