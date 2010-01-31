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

import static edu.uci.ics.sourcerer.scs.client.JavaNameUtil.extractShortName;

import java.util.Set;

import com.smartgwt.client.widgets.layout.VLayout;

import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent.Operation;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 27, 2010
 *
 */
public class VLFilterApis extends VLayout implements IApiFqnLinkContainer {
	
	ScsSearcher searcher;
	
	public VLFilterApis(){
		
	}
	
	// TODO enable both jdk and lib entities to be added
	// controlled by searcher
	public void clearAndAddFqns(Set<String> fqns){
		
		clearAllFqns();
		
		for(String fqn: fqns){
			this.addMember(makeApiFqnLink(fqn, EntityCategory.LIB));
		}
	}
	
	public void clearAllFqns(){
		this.removeMembers(this.getMembers());
	}
	
	
	public void update(String fqn, EntityCategory cat, Operation op) {
		searcher.removeFqnFilter(fqn);
	}
	
	
	private ApiFqnLink makeApiFqnLink(String fqn, EntityCategory cat){
		ApiFqnLink _f = new ApiFqnLink(fqn, cat, this);
		
		String sname = extractShortName(fqn);
		if(sname.trim().equals("<init>")) sname = "&lt;init&gt";
		
		_f.setContents("<b>" + sname + "</b>&nbsp;&nbsp;<small>" 
				+ fqn + "</small>");
		return _f;
	}
}
