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

import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;


import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent;
import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEventHandler;
import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent.Operation;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 11, 2009
 */
public class VLTopApis extends VLayout implements ISelectedFqnsProvider, IApiFqnLinkContainer {
	
	// TODO make this a parameter
	int maxToShow = 10;
	
	HashSet<String> selectedUsedJdkFqns = new HashSet<String>();
	HashSet<String> selectedUsedLibFqns = new HashSet<String>();
	
	public void setTopUsedLibApis(List<UsedFqn> libFqns){
		
		vlLibApisC.removeMembers(vlLibApisC.getMembers());
		vlLibApisCo.removeMembers(vlLibApisCo.getMembers());
		vlLibApisM.removeMembers(vlLibApisM.getMembers());
		vlLibApisI.removeMembers(vlLibApisI.getMembers());
		vlLibApisO.removeMembers(vlLibApisO.getMembers());
		
		for(UsedFqn libFqn: libFqns){
			switch(libFqn.getType()){
			case CLASS:
				if(vlLibApisC.getMembers().length<maxToShow) this.vlLibApisC.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			case INTERFACE:
				if(vlLibApisI.getMembers().length<maxToShow) this.vlLibApisI.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			case CONSTRUCTOR:
				if(vlLibApisCo.getMembers().length<maxToShow) this.vlLibApisCo.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			case METHOD:
				if(vlLibApisM.getMembers().length<maxToShow) this.vlLibApisM.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			
			default:
				if(vlLibApisO.getMembers().length<maxToShow) this.vlLibApisO.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			
			}
			
		}
	}
	
	public HashSet<String> getSelectedUsedJdkFqns(){
		return this.selectedUsedJdkFqns;
	}
	
	public HashSet<String> getSelectedUsedLibFqns(){
		return this.selectedUsedLibFqns;
	}
	
	public void addFqnAsSelected(String fqn, EntityCategory cat) {
		if(cat == EntityCategory.JDK){
			selectedUsedJdkFqns.add(fqn);
		} else if (cat == EntityCategory.LIB){
			selectedUsedLibFqns.add(fqn);
		}
	}

	public void removeFqnAsSelected(String fqn, EntityCategory cat) {
		if(cat == EntityCategory.JDK){
			selectedUsedJdkFqns.remove(fqn);
		} else if (cat == EntityCategory.LIB){
			selectedUsedLibFqns.remove(fqn);
		}
		
	}
	
	public void register(final ITakesSelectionUpdateEvent component){
		
		HandlerRegistration reg = doAddHandler(
			new ApiSelectionUpdateEventHandler() {
	
				public void onApiSelectionUpdate(ApiSelectionUpdateEvent event) {
	
					if (event.op == ApiSelectedEvent.Operation.SELECT) {
						((VLTopApis) event.getSource()).addFqnAsSelected(event.fqn,
								event.cat);
						
					} else if (event.op == ApiSelectedEvent.Operation.DESELECT) {
						((VLTopApis) event.getSource()).removeFqnAsSelected(
								event.fqn, event.cat);
					}
	
					component.process(event);
	
				}
			}, ApiSelectionUpdateEvent.getType()

		);
	}

	
//	private IFqnSelectionObserver fqnSelectionObserver;
//	private ApiSelectedEventHandler apiSelectHandler; 
//	
//	public void setSelectionObserver(IFqnSelectionObserver fh){
//		this.fqnSelectionObserver = fh;
//	}
	
	SectionStack ss = new SectionStack();
	
	SectionStackSection ssI = new SectionStackSection("Interfaces");
	SectionStackSection ssC = new SectionStackSection("Classes");
	SectionStackSection ssM = new SectionStackSection("Methods");
	SectionStackSection ssCo = new SectionStackSection("Constructors");
	SectionStackSection ssO = new SectionStackSection("Others");
	
	VLayout vlLibApisM = new VLayout();
	VLayout vlLibApisC = new VLayout();
	VLayout vlLibApisI = new VLayout();
	VLayout vlLibApisCo = new VLayout();
	VLayout vlLibApisO = new VLayout();
	
	
	public VLTopApis(){
		
		vlLibApisM.setOverflow(Overflow.AUTO);
		vlLibApisI.setOverflow(Overflow.AUTO);
		vlLibApisC.setOverflow(Overflow.AUTO);
		vlLibApisCo.setOverflow(Overflow.AUTO);
		vlLibApisO.setOverflow(Overflow.AUTO);
		
		ss.setVisibilityMode(VisibilityMode.MULTIPLE);
		
		ssI.setExpanded(true);
		ssC.setExpanded(true);
		ssM.setExpanded(true);
		ssCo.setExpanded(true);
		ssO.setExpanded(true);
		
		ssI.setResizeable(true);
		ssC.setResizeable(true);
		ssM.setResizeable(true);
		ssCo.setResizeable(true);
		ssO.setResizeable(true);
		
		
		ss.setHeight100();
		ss.setOverflow(Overflow.HIDDEN);
		
		ss.addSection(ssI);
		ss.addSection(ssC);
		ss.addSection(ssM);
		ss.addSection(ssCo);
		ss.addSection(ssO);
		
		ssC.addItem(vlLibApisC);
		ssI.addItem(vlLibApisI);
		ssM.addItem(vlLibApisM);
		ssCo.addItem(vlLibApisCo);
		ssO.addItem(vlLibApisO);
		
		
		
		vlLibApisM.setScrollbarSize(10);
		vlLibApisI.setScrollbarSize(10);
		vlLibApisC.setScrollbarSize(10);
		vlLibApisCo.setScrollbarSize(10);
		vlLibApisO.setScrollbarSize(10);
		
		this.addMember(ss);
		this.setLayoutAlign(VerticalAlignment.TOP);
		
	}
	
	public void clearContents(){
		vlLibApisC.removeMembers(vlLibApisC.getMembers());
		vlLibApisCo.removeMembers(vlLibApisCo.getMembers());
		vlLibApisM.removeMembers(vlLibApisM.getMembers());
		vlLibApisI.removeMembers(vlLibApisI.getMembers());
		vlLibApisO.removeMembers(vlLibApisO.getMembers());
		
		
		selectedUsedJdkFqns.clear();
		selectedUsedLibFqns.clear();
		
		
	}
	
	private ApiFqnLink makeApiFqnLink(UsedFqn uFqn, EntityCategory cat){
		return makeApiFqnLink(uFqn.getFqn(), uFqn.getUseCount() + "", cat);
	}

	private ApiFqnLink makeApiFqnLink(String fqn, String useCount, EntityCategory cat){
		ApiFqnLink _f = new ApiFqnLink(fqn, cat, this);
		
		String sname = extractShortName(fqn);
		if(sname.trim().equals("<init>")) sname = "&lt;init&gt";
		
		_f.setContents("<b>" + sname + "</b>"
				+ "&nbsp;&nbsp;"
				//+ "(" + useCount + ") " 
				+ "<small>" 
				+ fqn + "</small>");
		return _f;
	}

	public void showWaiting() {
		clearContents();
		vlLibApisC.addMember(new HTMLFlow("Calculating.."));
		vlLibApisI.addMember(new HTMLFlow("Calculating.."));
		vlLibApisM.addMember(new HTMLFlow("Calculating.."));
		vlLibApisCo.addMember(new HTMLFlow("Calculating.."));
		vlLibApisO.addMember(new HTMLFlow("Calculating.."));
		
	}

	public void update(String fqn, EntityCategory cat, Operation op) {
		
		fireEvent(new ApiSelectionUpdateEvent(fqn, cat, op));
	}
	
}



