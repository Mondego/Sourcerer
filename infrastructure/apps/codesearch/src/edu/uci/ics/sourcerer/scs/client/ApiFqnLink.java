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

import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseOverEvent;
import com.smartgwt.client.widgets.events.MouseOverHandler;

import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 */
public class ApiFqnLink extends HTMLFlow{
	boolean selected = false;
	IApiFqnLinkContainer container;
	
	public void onFqnSelect(String fqn, EntityCategory cat, 
			ApiSelectedEvent.Operation op){
		
		container.update(fqn, cat, op);
		
	}
	
		
	public ApiFqnLink(final String fqn, 
			final EntityCategory cat,
			IApiFqnLinkContainer container){
		
		this.container = container;
		
		
		this.addClickHandler(new ClickHandler(){

			public void onClick(ClickEvent event) {
				ApiFqnLink.this.selected = !ApiFqnLink.this.selected;
				if(ApiFqnLink.this.selected == true){
					// ((HTMLFlow) event.getSource()).setBackgroundColor("#C8FCB7");
					ApiFqnLink.this.onFqnSelect(fqn, cat, ApiSelectedEvent.Operation.SELECT);
					
				} else {
					// ((HTMLFlow) event.getSource()).setBackgroundColor("#FEFBB6");
					ApiFqnLink.this.onFqnSelect(fqn, cat, ApiSelectedEvent.Operation.DESELECT);
				}
			
				
			}
			
		});
		
		this.addMouseOverHandler(new MouseOverHandler(){

			public void onMouseOver(MouseOverEvent event) { 
				((HTMLFlow) event.getSource()).setBackgroundColor("#FEFBB6");
				
			}
			
		});
		
		this.addMouseOutHandler(new MouseOutHandler(){

			public void onMouseOut(MouseOutEvent event) {
				if(!ApiFqnLink.this.selected == true){
					((HTMLFlow) event.getSource()).setBackgroundColor("white");
				} else {
					((HTMLFlow) event.getSource()).setBackgroundColor("#C8FCB7");
				}
				
			}
			
		});
	}
}