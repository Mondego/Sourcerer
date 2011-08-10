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
package edu.uci.ics.sourcerer.search.adapter;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TestSearchAdapter {
  @Test
  public void testSearchAdapter() {
    SearchResult result = SearchAdapter.search("sort");
    List<SingleResult> results = result.getResults(0, 10);
    Assert.assertNotNull(results);
    Assert.assertEquals(10, results.size());
    for (SingleResult res : results) {
      Assert.assertNotNull(res.getEntityID());
    }
    List<SingleResult> results2 = result.getResults(5, 15);
    Assert.assertNotNull(results2);
    Assert.assertEquals(15, results2.size());
    Assert.assertEquals(results.get(5), results2.get(0));
    Assert.assertEquals(results.get(6), results2.get(1));
    Assert.assertEquals(results.get(7), results2.get(2));
  }
}
