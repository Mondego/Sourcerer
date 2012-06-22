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
package edu.uci.ics.sourcerer.services.search.adapter;

import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SearchAdapter {
  public static final Argument<String> SEARCH_URL = new StringArgument("search-url", "URL for Sourcerer Solr Search Server");
  private final String url;
  
  private SearchAdapter(String url) {
    this.url = url;
  }
  
  public static SearchAdapter create() {
    return new SearchAdapter(SEARCH_URL.getValue());
  }
  
  public static SearchAdapter create(String url) {
    return new SearchAdapter(url);
  }
  
  public SearchResult search(String query) {
    return new SearchResult(url, query);
  }
}
