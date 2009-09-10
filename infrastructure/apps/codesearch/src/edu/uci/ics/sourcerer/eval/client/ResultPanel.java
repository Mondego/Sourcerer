/*
 * Sourcerer: An infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package edu.uci.ics.sourcerer.eval.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ResultPanel extends VerticalPanel {
  private int totalResults;
  private String id;
  public ResultPanel(int totalResults) {
    this.totalResults = totalResults;
  }
  
  public void updateResult(Result result) {
    clear();
    
    id = result.getEntityID();

    HTML count = new HTML("Result " + result.getNumber() + " of " + totalResults);
    count.setStyleName("result-count");
    add(count);
    
    HTML fqn = new HTML(result.getFormattedFqn());
    fqn.setStyleName("query-fqn");
    add(fqn);
    
    HTML code = new HTML(result.getFormattedCode());
    code.setStyleName("query-code");
    add(code);
    
    Window.scrollTo(0, 0);
  }
  
  public String getResultID() {
    return id;
  }
}
