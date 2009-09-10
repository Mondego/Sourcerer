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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EvalPanel extends VerticalPanel  {
  private EvalServiceAsync evalService;
  private Vote[] voteOptions;
  private String email;
  private ResultPanel resultPanel;
  
  public EvalPanel() {
    if (evalService == null) {
      evalService = GWT.create(EvalService.class);
    }
    evalService.getVoteOptions(new AsyncCallback<Vote[]>() {
      public void onSuccess(Vote[] result) {
        voteOptions = result;
      }
      
      public void onFailure(Throwable caught) {
        displayFailure(caught.getMessage());
      }
    });
  }
  
  public void begin() {
    evalService.getIntroductionText(new AsyncCallback<String>() {
      public void onSuccess(String result) {
        setup(result);
      }
      
      public void onFailure(Throwable caught) {
        displayFailure(caught.getMessage());
      }
      
      private void setup(String text) {
        add(new HTML(text));
        setHorizontalAlignment(ALIGN_CENTER);
        
        final Button beginButton = new Button("Begin Evaluation");
        beginButton.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            email = Window.prompt("Please enter your email address", "");
            if (email != null) {
              beginButton.setEnabled(false);
              showEvaluationProgress();
            }
          }
        });
        add(beginButton);
      }
    });
  }
  
  private void showEvaluationProgress() {
    evalService.getEvaluationProgress(email, new AsyncCallback<EvaluationProgress>() {
      public void onSuccess(EvaluationProgress result) {
        showEvaluationProgress(result);
      }
      
      public void onFailure(Throwable caught) {
        displayFailure(caught.getMessage());
      }
    });
  }
  
  private void showEvaluationProgress(EvaluationProgress progress) {
    clear();
    setHorizontalAlignment(ALIGN_LEFT);
    
    HTML progressHeader = new HTML(email + "'s Evaluation Progress");
    progressHeader.setStyleName("progress-header");
    add(progressHeader);
    
    String text = progress.getPartialQueries().size() + " of "  + progress.getTotalQueries() + " queries in progress:";
    text += "<ul>";
    for (String query : progress.getPartialQueries()){
      text += "<li>" + query + "</li>";
    }
    text += "</ul>";
    HTML partiallyCompleted = new HTML(text);
    partiallyCompleted.setStyleName("progress-partial");
    add(partiallyCompleted);
    
    text = progress.getCompletedQueries().size() + " of " + progress.getTotalQueries() + " queries completed:";
    text += "<ul>";
    for (String query : progress.getCompletedQueries()) {
      text += "<li>" + query + "</li>";
    }
    text += "</ul>";
    HTML completed = new HTML(text);
    completed.setStyleName("progress-completed");
    add(completed);
    
    final ListBox list = new ListBox();
    for (String query : progress.getPartialQueries()) {
      list.addItem(query, query);
    }
    for (String query : progress.getNewQueries()) {
      list.addItem(query, query);
    }
    add(list);
    
    Button selected = new Button("Evaluate Selected Query", new ClickHandler() {
      public void onClick(ClickEvent event) {
        showNextQuery(list.getValue(list.getSelectedIndex()));
      }
    });
    add(selected);
    Button random = new Button("Evaluate Random Query", new ClickHandler() {
      public void onClick(ClickEvent event) {
        showNextQuery(null);
      }
    });
    add(random);
  }
  
  private void showNextQuery(String query) {
    displayLoadingScreen();
    
    evalService.getNextQuery(email, query, new AsyncCallback<Query>() {
      public void onSuccess(final Query result) {
        clear();
        setHorizontalAlignment(ALIGN_LEFT);
        
        HTML queryTitle = new HTML("Query <span class='query-id'>" + result.getQueryID() + "</span>: <span class='query-terms'>" + result.getQueryText() + "</span>");
        queryTitle.setStyleName("query-title");
        add(queryTitle);
        
        HTML queryDescription = new HTML(result.getQueryDescription());
        queryDescription.setStyleName("query-description");
        add(queryDescription);
        
        resultPanel = new ResultPanel(result.getTotalResults());
        add(resultPanel);
        
        HorizontalPanel voting = new HorizontalPanel();
        for (final Vote vote : voteOptions) {
          vote.clearSubVotesVotes();
          VerticalPanel votePanel = new VerticalPanel();
          Button b = new Button(vote.getText());
          votePanel.add(b);
          votePanel.setCellHorizontalAlignment(b, ALIGN_CENTER);
          final CheckBox[] boxes = new CheckBox[vote.getSubvotes().length];
          int index = 0;
          for (String subvote : vote.getSubvotes()) {
            boxes[index] = new CheckBox(subvote);
            votePanel.add(boxes[index]);
            index++;
          }
          b.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              int index = 0;
              for (CheckBox box : boxes) {
                if (box.getValue()) {
                  vote.setSubVoteVote(index);
                  box.setValue(false);
                }
                index++;
              }
              evalService.reportVote(email, resultPanel.getResultID(), vote, new AsyncCallback<Void>() {
                public void onSuccess(Void result) {
                  showNextResult();
                }

                public void onFailure(Throwable caught) {
                  displayFailure(caught.getMessage());
                }
              });
            }
          });
          voting.add(votePanel);
        }
        add(voting);
        showNextResult();
      }
      
      public void onFailure(Throwable caught) {
        displayFailure(caught.getMessage());
      }
    });
  }
  
  private void showNextResult() {
    evalService.getNextResult(email, new AsyncCallback<Result>() {
      
      public void onSuccess(Result result) {
        if (result == null) {
          showEvaluationProgress();
        } else {
          resultPanel.updateResult(result);
        }
      }
      
      public void onFailure(Throwable caught) {
        displayFailure(caught.getMessage());
      }
    });
  }

  private void displayLoadingScreen() {
    clear();
    setHorizontalAlignment(ALIGN_CENTER);
    Label loading = new Label("Loading...");
    loading.setStyleName("query-loading");
    add(loading);
  }
  
  private void displayFailure(String msg) {
    clear();
    add(new Label("Evaluation failed, please contact the administrator"));
    add(new Label(msg));
  }
}
