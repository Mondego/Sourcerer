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
package edu.uci.ics.sourcerer.scs.client;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripSeparator;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 31, 2009
 */
public class HitsPager extends ToolStrip{
	
	ScsSearcher searcher;
	
	long from;
	long to;
	int resultsPerPage = 10;
	long totalResults;
	
	Button btnFirst = new Button("1st");
	Button btnPrevious = new Button("<");
	Button btnNext = new Button(">");
	
	Label lblShowing = new Label("Navigate");
	HTMLPane lblResults = new HTMLPane();
	ToolStripSeparator stripSeparator = new ToolStripSeparator();  
	
	public ScsSearcher getSearcher(){
		return this.searcher;
	}
	
	public HitsPager(final ScsSearcher searcher){
		super();
		this.searcher = searcher;
		this.setDefaultLayoutAlign(VerticalAlignment.CENTER);
		this.setOverflow(Overflow.HIDDEN);
		this.setStyleName("sc-navbar");
		this.setHeight(28);
		
		btnFirst.setWidth(26);
		btnPrevious.setWidth(26);
		btnNext.setWidth(26);
		lblShowing.setWidth(60);
		lblShowing.setStyleName("sc-label");
		lblResults.setStyleName("sc-label");
		
		lblResults.setLayoutAlign(VerticalAlignment.CENTER);
		lblResults.setWidth("*");
		lblResults.setHeight(28);
		lblResults.setTop(4);
		stripSeparator.setHeight(8);  
		
		this.addMember(lblShowing);
		this.addMember(btnFirst);
		this.addMember(btnPrevious);
		this.addMember(btnNext);
		this.addMember(stripSeparator);
		this.addMember(lblResults);
		
		btnFirst.addClickHandler(new FirstHandler(this));
		btnNext.addClickHandler(new NextHandler(this));
		btnPrevious.addClickHandler(new PrevHandler(this));
		
		
	}
	
	class FirstHandler implements ClickHandler{
		HitsPager pager;
		public FirstHandler(HitsPager pager){
			this.pager = pager;
		}
		
		public void onClick(ClickEvent event) {
			if(pager.getFrom()>0){
				pager.setFrom(0);
				pager.getSearcher().sendQueryToServer(false);
			}
		}
	}
	
	class NextHandler implements ClickHandler{
		HitsPager pager;
		public NextHandler(HitsPager pager){
			this.pager = pager;
		}
		public void onClick(ClickEvent event) {
			if(pager.getFrom() + pager.getResultsPerPage() < pager.getTotalResults()){
				pager.setFrom(pager.getFrom() + pager.getResultsPerPage());
				pager.getSearcher().sendQueryToServer(false);
			}
		}
		
	}
	
	class PrevHandler implements ClickHandler{
		HitsPager pager;
		public PrevHandler(HitsPager pager){
			this.pager = pager;
		}
		
		public void onClick(ClickEvent event) {
			if (pager.getFrom() > 0){
				pager.setFrom(pager.getFrom()-pager.getResultsPerPage());
				pager.getSearcher().sendQueryToServer(false);
			}
		}
		
	}
	
	public void clearResultsText(){
		lblResults.setContents("");
	}
	
	public void updateResultsText() {
		
		if (totalResults == 0){
			lblResults.setContents("No results found");
			return;
		}
		
		if (from + resultsPerPage > totalResults){
			to = totalResults - 1;
		} else {
			to = from + resultsPerPage - 1;
		}
		
//		lblResults.setTitle("Showing <b>" + from  + "</b> to <b>" 
//								+ to + "</b> of <b>" 
//								+ totalResults +"</b> results");
		
		lblResults.setHeight(28);
		lblResults.setTop(4);
		lblResults.setContents("Showing <b>" + ((int) from + 1)  + "</b> to <b>" 
				+ ((int) to + 1) + "</b> of <b>" 
				+ totalResults +"</b> results");
		//lblResults.redraw();
		
		
	}

	public long getResultsPerPage() {
		return this.resultsPerPage;
	}

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public long getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(long totalResults) {
//		if(totalResults>0) 
//			this.totalResults = totalResults - 1;
//		else 
			this.totalResults = totalResults;
	}
	
}


