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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SearchResult {
  private final String query;
  private final String url;
  private ArrayList<SingleResult> results;

  private int lastQueryTime;
  private int numFound = -1;
  
  private final SAXParser parser;
  private final SaxHandler handler;
  
  protected SearchResult(String url, String query) {
    try {
      this.query = URLEncoder.encode(query, "UTF-8");
      this.url = url + "/solr/select/?q=%s&start=%d&rows=%d&fl=score";
      SAXParserFactory fact = SAXParserFactory.newInstance();
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
    if (results == null) {
      results = new ArrayList<>(lastExclusive);
    }
    if (results.size() < lastExclusive || numFound == -1) {
      int numToFetch = lastExclusive - results.size(); 
      try (InputStream ins = new URL(String.format(url, query, results.size(), numToFetch)).openStream()) {
        handler.reset();
        parser.parse(ins, handler);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Unable to perform search", e);
      }
    } else {
      lastQueryTime = 0;
    }
    return Collections.unmodifiableList(new ArrayList<>(results.subList(firstResult, lastExclusive > results.size() ? results.size() : lastExclusive)));
  }
  
  public long getLastQueryTime() {
    return lastQueryTime;
  }
  
  public int getNumFound() {
    if (numFound == -1) {
      getResults(0, 0);
    }
    return numFound;
  }
  
  private static enum State {
    START,
    QUERY_TIME,
    RESULT,
    DOC,
    DOC_SCORE,
    DOC_ENTITY_ID,
    DOC_FQN,
    DOC_PARAMS,
    DOC_PARAM_COUNT,
    DOC_RETURN_FQN,
    DONE,
    ;
  }
  
  private class SaxHandler extends DefaultHandler {
    private State state = State.START;
    private SingleResult result = null;

    public void reset() {
      state = State.START;
      result = null;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      switch (state) {
        case START:
        {
          String name = attributes.getValue("name");
          if ("int".equals(qName) && "QTime".equals(name)) {
            state = State.QUERY_TIME;
          } else if ("result".equals(qName) && "response".equals(name)) {
            state = State.RESULT;
            numFound = Integer.valueOf(attributes.getValue("numFound"));
          }
        }
        break;
        case RESULT:
        {
          if ("doc".equals(qName)) {
            state = State.DOC;
            result = new SingleResult();
          }
        }
        break;
        case DOC:
        {
          String name = attributes.getValue("name");
          if ("long".equals(qName) && "entity_id".equals(name)) {
            state = State.DOC_ENTITY_ID;
          } else if ("int".equals(qName)) {
            state = State.DOC_PARAM_COUNT;
          } else if ("float".equals(qName)){
            state = State.DOC_SCORE;
          } else if ("str".equals(qName)) {
            if ("fqn".equals(name)) {
              state = State.DOC_FQN;
            } else if ("params".equals(name)) {
              state = State.DOC_PARAMS;
            } else if ("return_fqn".equals(name)) {
              state = State.DOC_RETURN_FQN;
            }
          }
        }
      }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      switch (state) {
        case QUERY_TIME:
          if ("int".equals(qName)) {
            state = State.START;
          }
          break;
        case RESULT:
          if ("result".equals(qName)) {
            state = State.DONE;
          }
          break;
        case DOC:
          if ("doc".equals(qName)) {
            state = State.RESULT;
            result.setRank(results.size());
            results.add(result);
            result = null;
          }
          break;
        case DOC_SCORE:
          if ("float".equals(qName)) {
            state = State.DOC;
          }
          break;
        case DOC_ENTITY_ID:
          if ("long".equals(qName)) {
            state = State.DOC;
          }
          break;
        case DOC_PARAM_COUNT:
          if ("int".equals(qName)) {
            state = State.DOC;
          }
          break;
        case DOC_FQN:
        case DOC_PARAMS:
        case DOC_RETURN_FQN:
          if ("str".equals(qName)) {
            state = State.DOC;
          }
          break;
      }
    }



    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      switch (state) {
        case QUERY_TIME: lastQueryTime = Integer.parseInt(new String(ch, start, length)); break;
        case DOC_SCORE: result.setScore(Float.parseFloat(new String(ch, start, length))); break;
        case DOC_ENTITY_ID: result.setEntityID(Long.parseLong(new String(ch, start, length))); break;
        case DOC_FQN: result.setFqn(new String(ch, start, length)); break; 
        case DOC_PARAM_COUNT: result.setParamCount(Integer.parseInt(new String(ch, start, length))); break;
        case DOC_PARAMS: result.setParams(new String(ch, start, length)); break;
        case DOC_RETURN_FQN: result.setReturnFqn(new String(ch, start, length)); break;
      }
    }
  };
}
