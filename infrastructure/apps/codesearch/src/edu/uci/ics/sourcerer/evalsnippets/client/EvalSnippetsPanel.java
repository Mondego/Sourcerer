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
package edu.uci.ics.sourcerer.evalsnippets.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.uci.ics.sourcerer.eval.client.EvalService;
import edu.uci.ics.sourcerer.scs.common.JavaToHtml;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 9, 2010
 * 
 */
public class EvalSnippetsPanel extends VerticalPanel {

	String FILE_SERVER_URL_PART = "http://kathmandu.ics.uci.edu:8080/file-server?jarEntityID=";

	EvalSnippetServiceAsync evalSnippetsService;

	Set<SearchHeuristic> schemes;
	HTML htQuery = new HTML();
	String query = "";

	Map<String, Map<String, String>> result;
	int counter = 1;
	
	Map<String, Map<String, String>> relevancy = new HashMap<String, Map<String, String>>();

	VerticalPanel vpSnippets = new VerticalPanel();
	Button btnNext = new Button();

	public EvalSnippetsPanel() {

		// taSnippet.setCharacterWidth(80);
		// taSnippet.setVisibleLines(50);

		if (evalSnippetsService == null) {
			evalSnippetsService = GWT.create(EvalSnippetService.class);
		}

		btnNext.setText("Next");
		btnNext.setEnabled(false);
		
		btnNext.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent arg0) {
				nextEval();
			}
		});
		
		add(htQuery);
		add(vpSnippets);
		add(btnNext);

	}

	public void begin() {

		evalSnippetsService
				.getSchemes(new AsyncCallback<Set<SearchHeuristic>>() {

					public void onFailure(Throwable caught) {
						displayFailure(caught.getMessage());
					}

					public void onSuccess(Set<SearchHeuristic> result) {
						schemes = result;
						startEval();

					}

				});

	}

	private void startEval() {
		query = Window.prompt("Please enter a query to run evaluation for", "");
		htQuery.setHTML("QUERY: <b>" + query + "</b>");

		evalSnippetsService.getSnippetsInBulk(query,
				new AsyncCallback<Map<String, Map<String, String>>>() {

					public void onFailure(Throwable caught) {
						displayFailure(caught.getMessage());
					}

					public void onSuccess(
							Map<String, Map<String, String>> _result) {
						result = _result;
						nextEval();
					}
				});
	}

	private void nextEval() {
		vpSnippets.clear();
		
		String recordPos = counter + " of " + result.size() + " entities";
		vpSnippets.add(new HTML(recordPos));

		Iterator<String> i = getResultIterator();
		if (i.hasNext()) {
			// still has a entity to evaluate

			// get entity id
			String eid = i.next();
			String linkEid = "Entity id: <a href=\"" + FILE_SERVER_URL_PART
					+ eid + "\"  target=\"_blank\">" + eid + "</a>";
			vpSnippets.add(new HTML(linkEid));
			vpSnippets.add(new HTML("<hr/>"));

			for (SearchHeuristic sh : schemes) {
				
				vpSnippets.add(new HTML(sh.toString() + "<br/>"));
				
				boolean foundSnippetForScheme = false;
				String snippet = null;
				// get snippets for all heuristics
				if (result.get(eid).containsKey(sh.toString())) {
					snippet = result.get(eid).get(sh.toString());
					if (snippet != null 
							&& snippet.length()>0
							&& !snippet.equals("<div class=\"result_code\"></div>")){
						foundSnippetForScheme = true;
					}
				} 
				
			    final ListBox lbRelev = new ListBox();
			    final String _eid = eid;
			    final String _scheme = sh.toString();
			    lbRelev.addItem("Relevant");
			    lbRelev.addItem("NotRelevant");
			    lbRelev.addItem("Empty");
			    lbRelev.setVisibleItemCount(3);
			    lbRelev.addChangeHandler(new ChangeHandler() {
					
					public void onChange(ChangeEvent arg0) {
						 int selectedIndex = lbRelev.getSelectedIndex();
						 if (selectedIndex >= 0){
							 updateRelevancy(_eid, _scheme, lbRelev.getValue(selectedIndex));
							 // Window.alert("Something got selected " + lbRelev.getValue(selectedIndex));
						 }
						 
					}
				});
			    
			    // vpSnippets.add(new Label(sh.toString()));
			    
			    if(foundSnippetForScheme){
			    	vpSnippets.add(new HTML(snippet));
			    	lbRelev.setItemSelected(1, true);
			    	updateRelevancy(eid, sh.toString(), "NotRelevant");
				} else {
					lbRelev.setItemSelected(2, true);
					updateRelevancy(eid, sh.toString(), "Empty");
				}

			    vpSnippets.add(lbRelev);
				vpSnippets.add(new HTML("<hr/>"));
			}

			// show next button
			btnNext.setEnabled(true);	
			counter++;

		} else {
			// done
			showEvaluationTable();
		}

	}
	
	private Iterator<String> itr;
	private Iterator<String> getResultIterator() {
		if(itr==null){
			itr = result.keySet().iterator(); 
		}
		return itr; 
	}

	private void updateRelevancy(String eid, String scheme, String relValue){
		if(!relevancy.containsKey(eid)){
			relevancy.put(eid, new HashMap<String, String>());
		}
		relevancy.get(eid).put(scheme, relValue);
		System.out.println(eid + ", " + scheme + ", " + relValue);
	}
	
	
	private void showEvaluationTable() {
		String table = "";
		
		Set<String> relEidPool = new HashSet<String>();
		Map<String, Integer> relevant_by_scheme = new HashMap<String, Integer>();
		Map<String, Integer> retrieved_by_scheme = new HashMap<String, Integer>();
		
		StringBuffer _sbufTable = new StringBuffer();
		String[] schemeCols = new String[schemes.size()];
		
		int i =0;
		for(SearchHeuristic _h: schemes){
			schemeCols[i++] = _h.toString();
		}
		
		_sbufTable.append("Query:<b> "+ query + "</b>");
		
		// header
		_sbufTable.append("<hr/>eid");
		for(String sch: schemeCols){
			_sbufTable.append(",");
			_sbufTable.append(sch);
		}
		_sbufTable.append(",Relevancy");
		_sbufTable.append("<br/>");
		
		// data
		for(String eid: relevancy.keySet()){
			boolean r = false;
			_sbufTable.append(eid);
			for(String sch: schemeCols){
				String _schRelVal = relevancy.get(eid).get(sch);
				_sbufTable.append(",");
				_sbufTable.append(_schRelVal);
				r = r || (_schRelVal.equals("Relevant"));
				
				if (_schRelVal.equals("Relevant")) {
					if (relevant_by_scheme.containsKey(sch)) {
						relevant_by_scheme.put(sch, relevant_by_scheme.get(sch)
								.intValue() + 1);
					} else {
						relevant_by_scheme.put(sch, 1);
					}
				}
				
				if (_schRelVal.equals("Relevant")
						|| _schRelVal.equals("NotRelevant")) {
					if (retrieved_by_scheme.containsKey(sch)) {
						retrieved_by_scheme.put(sch, retrieved_by_scheme.get(sch)
								.intValue() + 1);
					} else {
						retrieved_by_scheme.put(sch, 1);
					}
				}
				
				
			}
			if (r){
				relEidPool.add(eid);
			}
			_sbufTable.append("," + r + "<br/>");
		}
		
		table = _sbufTable.toString();
		
		
		StringBuffer sbufPR = new StringBuffer();
		sbufPR.append("<br/>Precison-Recall<hr/>");
		for(String sch: schemeCols){
			
			sbufPR.append("<i>" + sch + "</i><br/>");
			
			int _relSch = relevant_by_scheme.get(sch)==null?0:relevant_by_scheme.get(sch);
			int _retSch = retrieved_by_scheme.get(sch)==null?0:retrieved_by_scheme.get(sch);
			int _relPool = relEidPool.size();
			
			if(_relSch == 0){
				sbufPR.append("PRECISION: NA");
				sbufPR.append("<br/>");
			}else{
				double precision = (double) _relSch/(double) _retSch;
				sbufPR.append("PRECISION: " + precision);
				sbufPR.append("<br/>");
			}
			
			if(_relPool == 0){
				sbufPR.append("RECALL: NA");
				sbufPR.append("<br/>");
			} else {
				double recall = (double) _relSch/(double) _relPool;
				sbufPR.append("RECALL: " + recall);
				sbufPR.append("<br/>");
			}
			
			sbufPR.append("<br/>");
		}
		
		
		clear();
		add(new Label("Evaluation done."));
		sbufPR.append("<hr/>");
		add(new HTML(table));
		sbufPR.append("<hr/>");
		add(new HTML(sbufPR.toString()));
		
		
		
	}
	
	

	private String getPrecisionRecall() {

		
		
		return "";
	}

	private void displayFailure(String msg) {
		clear();
		add(new HTML("Evaluation failed." +
				"<br/>Possibly no results for Query:  " + query +
				"<br/>Please contact bajracharya@gmail.com for more information" +
				"<br/>Refresh browser to run another query."));
		add(new Label(msg));
	}
}
