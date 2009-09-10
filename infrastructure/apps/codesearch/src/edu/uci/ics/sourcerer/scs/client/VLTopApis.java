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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;

import edu.uci.ics.sourcerer.db.adapter.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent;
import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEventHandler;
import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent.Operation;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 11, 2009
 */
public class VLTopApis extends VLayout implements ISelectedFqnsProvider {
	
	public interface ITakesSelectionUpdateEvent extends EventHandler{
	      void process(ApiSelectionUpdateEvent event);
	   }

	
	HashSet<String> selectedUsedJdkFqns = new HashSet<String>();
	HashSet<String> selectedUsedLibFqns = new HashSet<String>();
	
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
	
	public void register(final VLTopApis.ITakesSelectionUpdateEvent component){
		
		
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
	
					// highlightHit(((VLTopApis)
					// event.getSource()).getSelectedUsedJdkFqns(),
					// ((VLTopApis) event.getSource()).getSelectedUsedLibFqns(),
					// hit);
					component.process(event);
	
				}
			}, ApiSelectionUpdateEvent.getType()

		);
		
	}
	
	

	

	
	private IFqnSelectionObserver fqnSelectionObserver;
	private ApiSelectedEventHandler apiSelectHandler; 
	
	
	public void setSelectionObserver(IFqnSelectionObserver fh){
		this.fqnSelectionObserver = fh;
	}
	
	SectionStack ss = new SectionStack();
	SectionStackSection ssJdk = new SectionStackSection("from JDK");
	SectionStackSection ssLib = new SectionStackSection("from Libraries");
	VLayout vlJdkApis = new VLayout();
	VLayout vlLibApis = new VLayout();
	
	public VLTopApis(){
		
		ss.setVisibilityMode(VisibilityMode.MULTIPLE);
		
		vlJdkApis.setOverflow(Overflow.AUTO);
		vlLibApis.setOverflow(Overflow.AUTO);
		
		ssJdk.setExpanded(true);
		ssLib.setExpanded(true);
		ssJdk.setResizeable(true);
		ssLib.setResizeable(true);
		
		ss.setHeight100();
		ss.setOverflow(Overflow.AUTO);
		
		ss.addSection(ssJdk);
		ss.addSection(ssLib);
		ssJdk.addItem(vlJdkApis);
		ssLib.addItem(vlLibApis);
		
		this.addMember(ss);
		this.setLayoutAlign(VerticalAlignment.TOP);
		
//		this.addApiSelectionUpdateHandler(new ApiSelectionUpdateEventHandler(){
//
//			public void onApiSelectionUpdate(ApiSelectionUpdateEvent event) {
//				for(String s: ((VLTopApis) event.getSource()).getSelectedUsedJdkFqns()){
//					System.out.println(s);
//				}
//				
//			}
//			
//		});
		
		
	}
	
	public void clearContents(){
		vlJdkApis.removeMembers(vlJdkApis.getMembers());
		vlLibApis.removeMembers(vlLibApis.getMembers());
		
		selectedUsedJdkFqns.clear();
		selectedUsedLibFqns.clear();
		
		
	}
	
	public void setTopJdkApis(List<HitFqnEntityId> jdkApis){
		vlJdkApis.removeMembers(vlJdkApis.getMembers());
		
		for(HitFqnEntityId _h: jdkApis){
			this.vlJdkApis.addMember(makeApiFqnLink(_h, EntityCategory.JDK));
		}
		
	}
	
	public void setTopLibApis(List<HitFqnEntityId> libApis){
		vlLibApis.removeMembers(vlLibApis.getMembers());
		
		for(HitFqnEntityId _h: libApis){
			this.vlLibApis.addMember(makeApiFqnLink(_h, EntityCategory.LIB));
		}
	}
	

	
	private ApiFqnLink makeApiFqnLink(HitFqnEntityId hitInfo, EntityCategory cat){
		ApiFqnLink _f = new ApiFqnLink(hitInfo.fqn, cat, this);
		
		String sname = extractShortName(hitInfo.fqn);
		if(sname.trim().equals("<init>")) sname = "&lt;init&gt";
		
		_f.setContents("<b>" + sname + "</b>  (" + hitInfo.useCount + ") <small>" + hitInfo.fqn + "</small>");
		return _f;
	}

	public void showWaiting() {
		clearContents();
		vlJdkApis.addMember(new HTMLFlow("Calculating.."));
		vlLibApis.addMember(new HTMLFlow("Calculating.."));
	}

	public void update(String fqn, EntityCategory cat, Operation op) {
		
		
		fireEvent(new ApiSelectionUpdateEvent(fqn, cat, op));
	}
	
}



