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
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent;
import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEventHandler;
import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent.Operation;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

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
	
	public void setTopUsedJdkApis(List<UsedFqn> jdkFqns){
		vlJdkApisCI.removeMembers(vlJdkApisCI.getMembers());
		vlJdkApisMC.removeMembers(vlJdkApisMC.getMembers());
		vlJdkApisFi.removeMembers(vlJdkApisFi.getMembers());
		vlJdkApisOt.removeMembers(vlJdkApisOt.getMembers());
		
		for(UsedFqn jdkFqn: jdkFqns){
			switch(jdkFqn.getType()){
			case CLASS_INF:
				this.vlJdkApisCI.addMember(makeApiFqnLink(jdkFqn, EntityCategory.JDK));
				break;
			case METHOD_CONST:
				this.vlJdkApisMC.addMember(makeApiFqnLink(jdkFqn, EntityCategory.JDK));
				break;
			case FIELD:
				this.vlJdkApisFi.addMember(makeApiFqnLink(jdkFqn, EntityCategory.JDK));
				break;
			case OTHER:
				this.vlJdkApisOt.addMember(makeApiFqnLink(jdkFqn, EntityCategory.JDK));
				break;
			default: break;
			}
			
		}
	}
	
	public void setTopUsedLibApis(List<UsedFqn> libFqns){
		
		vlLibApisCI.removeMembers(vlLibApisCI.getMembers());
		vlLibApisMC.removeMembers(vlLibApisMC.getMembers());
		vlLibApisFi.removeMembers(vlLibApisFi.getMembers());
		vlLibApisOt.removeMembers(vlLibApisOt.getMembers());
		
		for(UsedFqn libFqn: libFqns){
			switch(libFqn.getType()){
			case CLASS_INF:
				this.vlLibApisCI.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			case METHOD_CONST:
				this.vlLibApisMC.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			case FIELD:
				this.vlLibApisFi.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			case OTHER:
				this.vlLibApisOt.addMember(makeApiFqnLink(libFqn, EntityCategory.LIB));
				break;
			default: break;
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
	
//	VLayout vlJdkApis = new VLayout();
//	VLayout vlLibApis = new VLayout();
	
	VLayout vlLibApisMC = new VLayout();
	VLayout vlLibApisCI = new VLayout();
	VLayout vlLibApisFi = new VLayout();
	VLayout vlLibApisOt = new VLayout();
	
	VLayout vlJdkApisMC = new VLayout();
	VLayout vlJdkApisCI = new VLayout();
	VLayout vlJdkApisFi = new VLayout();
	VLayout vlJdkApisOt = new VLayout();
	
	TabSet tsJdk = new TabSet();
	TabSet tsLib = new TabSet();
	
	Tab tabLibApisMC = new Tab("Meth/Cons");
	Tab tabLibApisCI = new Tab("Clas/Inf");
	Tab tabLibApisFi = new Tab("Fields");
	Tab tabLibApisOt = new Tab("Other");
	
	Tab tabJdkApisMC = new Tab("Meth/Cons");
	Tab tabJdkApisCI = new Tab("Clas/Inf");
	Tab tabJdkApisFi = new Tab("Fields");
	Tab tabJdkApisOt = new Tab("Other");
	
	
	
	
	
	public VLTopApis(){
		
		ss.setVisibilityMode(VisibilityMode.MULTIPLE);
		
//		vlJdkApis.setOverflow(Overflow.AUTO);
//		vlLibApis.setOverflow(Overflow.AUTO);
		
		ssJdk.setExpanded(true);
		ssLib.setExpanded(true);
		ssJdk.setResizeable(true);
		ssLib.setResizeable(true);
		
		ss.setHeight100();
		ss.setOverflow(Overflow.AUTO);
		
		ss.addSection(ssJdk);
		ss.addSection(ssLib);
		
//		ssJdk.addItem(vlJdkApis);
//		ssLib.addItem(vlLibApis);
		
		ssJdk.addItem(tsJdk);
		ssLib.addItem(tsLib);
		
		tsJdk.addTab(tabJdkApisMC);
		tsJdk.addTab(tabJdkApisCI);
		tsJdk.addTab(tabJdkApisFi);
		tsJdk.addTab(tabJdkApisOt);
		
		tsLib.addTab(tabLibApisMC);
		tsLib.addTab(tabLibApisCI);
		tsLib.addTab(tabLibApisFi);
		tsLib.addTab(tabLibApisOt);
		
		tabJdkApisMC.setPane(vlJdkApisMC);
		tabJdkApisCI.setPane(vlJdkApisCI);
		tabJdkApisFi.setPane(vlJdkApisFi);
		tabJdkApisOt.setPane(vlJdkApisOt);
		
		tabLibApisMC.setPane(vlLibApisMC);
		tabLibApisCI.setPane(vlLibApisCI);
		tabLibApisFi.setPane(vlLibApisFi);
		tabLibApisOt.setPane(vlLibApisOt);
		
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
		vlJdkApisCI.removeMembers(vlJdkApisCI.getMembers());
		vlJdkApisMC.removeMembers(vlJdkApisMC.getMembers());
		vlJdkApisFi.removeMembers(vlJdkApisFi.getMembers());
		vlJdkApisOt.removeMembers(vlJdkApisOt.getMembers());
		
		vlLibApisCI.removeMembers(vlLibApisCI.getMembers());
		vlLibApisMC.removeMembers(vlLibApisMC.getMembers());
		vlLibApisFi.removeMembers(vlLibApisFi.getMembers());
		vlLibApisOt.removeMembers(vlLibApisOt.getMembers());
		
		selectedUsedJdkFqns.clear();
		selectedUsedLibFqns.clear();
		
		
	}
	
//	public void setTopJdkApis(List<HitFqnEntityId> jdkApis){
//		vlJdkApis.removeMembers(vlJdkApis.getMembers());
//		
//		for(HitFqnEntityId _h: jdkApis){
//			this.vlJdkApis.addMember(makeApiFqnLink(_h, EntityCategory.JDK));
//		}
//		
//	}
//	
//	public void setTopLibApis(List<HitFqnEntityId> libApis){
//		vlLibApis.removeMembers(vlLibApis.getMembers());
//		
//		for(HitFqnEntityId _h: libApis){
//			this.vlLibApis.addMember(makeApiFqnLink(_h, EntityCategory.LIB));
//		}
//	}
	
	private ApiFqnLink makeApiFqnLink(UsedFqn uFqn, EntityCategory cat){
		return makeApiFqnLink(uFqn.getFqn(), uFqn.getUseCount() + "", cat);
	}

	
//	private ApiFqnLink makeApiFqnLink(HitFqnEntityId hitInfo, EntityCategory cat){
//		return makeApiFqnLink(hitInfo.fqn, hitInfo.useCount, cat);
//	}
	
	private ApiFqnLink makeApiFqnLink(String fqn, String useCount, EntityCategory cat){
		ApiFqnLink _f = new ApiFqnLink(fqn, cat, this);
		
		String sname = extractShortName(fqn);
		if(sname.trim().equals("<init>")) sname = "&lt;init&gt";
		
		_f.setContents("<b>" + sname + "</b>  (" 
				+ useCount + ") <small>" 
				+ fqn + "</small>");
		return _f;
	}

	public void showWaiting() {
		clearContents();
		vlJdkApisMC.addMember(new HTMLFlow("Calculating.."));
		vlLibApisMC.addMember(new HTMLFlow("Calculating.."));
	}

	public void update(String fqn, EntityCategory cat, Operation op) {
		
		
		fireEvent(new ApiSelectionUpdateEvent(fqn, cat, op));
	}
	
}



