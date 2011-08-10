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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
//import java.util.Collection;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SearchResult {
  private final String query;
  private final ArrayList<SingleResult> results;
  private int nextToFetch = 0;

  private final SAXParser parser;
  private final SaxHandler handler;
  
  protected SearchResult(String query) {
    this.query = query;
    this.results = Helper.newArrayList();
    SAXParserFactory fact = SAXParserFactory.newInstance();
    try {
      parser = fact.newSAXParser();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    handler = new SaxHandler();
  }
  
  /**
   * Returns up to the specified number of <tt>SingleResult</tt>s, starting
   * with the result specified.
   *
   * @param firstResult the index of the first result to return
   * @param numResults the number of results to return
   */
  public List<SingleResult> getResults(int firstResult, int numResults) {
    int lastExclusive = firstResult + numResults;
    if (nextToFetch < lastExclusive) {
      int epp = 2 * (lastExclusive - nextToFetch);
      int pid = nextToFetch / epp;
      InputStream ins = null;
      try {
        ins = new URL("http://sourcerer.ics.uci.edu/sourcerer/ws-search/search?qry=" + query + "&epp=" + epp + "&pid=" + pid + "&client=adapter").openStream();
        handler.index = pid * epp;
        parser.parse(ins, handler);
      } catch (Exception e) {
      } finally {
        FileUtils.close(ins);
      }
    }
    return results.subList(firstResult, lastExclusive > results.size() ? results.size() : lastExclusive);
  }
  
  private class SaxHandler extends DefaultHandler {
    private int index;
    private SingleResult result;
    
    private boolean entityID = false;
    private boolean entityName = false;
    private boolean filePath = false;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (qName.equals("entries")) {
        if (index >= nextToFetch) {
          result = new SingleResult();
        }
      } else if (qName.equals("entityId")) {
        entityID = true;
      } else if (qName.equals("entityName")) {
        entityName = true;
      } else if (qName.equals("filePath")) {
        filePath = true;
      }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (qName.equals("entries")) {
        if (result != null) {
          results.add(result);
          nextToFetch++;
          result = null;
        }
        index++;
      }
    }



    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (result != null) {
        if (entityID) {
          result.setEntityID(new String(ch, start, length));
          entityID = false;
        } else if (entityName) {
          result.setEntityName(new String(ch, start, length));
          entityName = false;
        } else if (filePath) {
          result.setFilePath(new String(ch, start, length));
          filePath = false;
        }
      }
    }
  };
}
