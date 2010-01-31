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
import gdurelle.tagcloud.client.tags.WordTag;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 21, 2010
 *
 */
public class SearchWordSuggestUI implements EntryPoint {

	/* (non-Javadoc)
	 * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
	 */
	public void onModuleLoad() {
		// TODO Auto-generated method stub
	Window.setTitle("Sourcerer Word Suggestion Demonstration");
		TagCloud tc = new TagCloud();
		WordTag wt = new WordTag("TEST", "http://google.com");
		wt.setNumberOfOccurences(20);
		WordTag wt2 = new WordTag("TEST2", "http://google.com");
		wt.setNumberOfOccurences(2);
		tc.addWord(wt);
		tc.addWord(wt2);
		VerticalPanel vp = new VerticalPanel();
		vp.add(new HTML("test"));
		vp.add(tc);
	
		RootPanel.get().add(vp);
		
	System.out.println("working");
		
	}

}
