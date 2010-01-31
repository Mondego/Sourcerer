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
package edu.uci.ics.sourcerer.searchwordsuggest.client;

import gdurelle.tagcloud.client.tags.TagCloud;

import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 21, 2010
 *
 */
public class UIWindow extends VerticalPanel {
	TextBox tb = new TextBox();
	TagCloud tc = new TagCloud();
	
	public UIWindow(){
		 tb.addKeyboardListener(new KeyboardListenerAdapter() {
		      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
		        if(keyCode == KEY_ENTER){
		        	search(); 
		        }
		      }
		    });
		 
		 this.add(tb);
		 this.add(tc);
		 

	}
	
	private void search(){
		
	}
	
	
}
