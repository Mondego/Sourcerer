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

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 4, 2009
 */
public class VLHitDetails extends VLayout {
	
	boolean isWaiting = true;
	
	Label lblEntityId = new Label("");
	
	HSEvalResult evalBar = new HSEvalResult();
	
	
	public VLHitDetails(){	
		lblEntityId.setLayoutAlign(VerticalAlignment.CENTER);
		evalBar.setLayoutAlign(Alignment.RIGHT);
		evalBar.setWidth100();
		
		HLayout _top = new HLayout();
		_top.addMember(lblEntityId);
		lblEntityId.setWidth(120);
		
		_top.addMember(evalBar);
		
		evalBar.setWidth100();
		evalBar.setHeight(12);
		
		_top.setHeight(8);
		
		this.addMember(_top);
		this.setStyleName("hit-details");
	}
	
	public void setEntityId(String eid){
		lblEntityId.setContents("&nbsp;Entity ID: " + eid);
	}
	
	public void indicateWaiting(){
		isWaiting = true;
		this.lblEntityId.setContents("Retrieving Code..");
	}
	
	public void setIsWaiting(boolean w){
		this.isWaiting = w;
	}
}
