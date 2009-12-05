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

import static edu.uci.ics.sourcerer.scs.client.ResultProcessor.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.ToolbarItem;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseOverEvent;
import com.smartgwt.client.widgets.events.MouseOverHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.CloseClickHandler;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;

import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import edu.uci.ics.sourcerer.db.adapter.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 */
public class Sourcerer_cs implements EntryPoint, 
	ScsSearcher,
	IERProvider,
	ICodeViewer{
	
	ERTables erTables = null;
	
	public ERTables getErTables(){
		return this.erTables;
	}

	private final HashMap<String, CachedHitResult> hitsDetailCache = new HashMap<String, CachedHitResult>();
	
	private ScsClientMode currentMode = ScsClientMode.SEARCH;

	Set<String> queryTermsSet = new HashSet<String>();
	List<String> hitEids = new LinkedList<String>();
	String hitsXml;

	VLayout vlRoot = new VLayout();

	final DynamicForm form = new DynamicForm();
	final HitsPager hsHitsNav = new HitsPager(this);
	final VLayout vlHits = new VLayout();
	
	final SelectItem cbHeuristics = new SelectItem();

	final TreeGrid tgElementsJdk = new TreeGrid();
	final TreeGrid tgElementsLib = new TreeGrid();
	final TreeGrid tgElementsLocal = new TreeGrid();
	final VLSimilar similar = new VLSimilar();
	final Tab tabSimilar = new Tab("Similar");
	final Tab tabCode = new Tab("Code");
	final HTMLFlow hfCodeHolder = new HTMLFlow();
	final VLTopApis vlTopApis = new VLTopApis();
	final TabSet tsCode = new TabSet();

	final Button btnAdvanced = new Button();
	final TextItem queryField = new TextItem("fQuery");
	final IButton butSearch = new IButton("Search");
	
	final TabSet tabAdvanced = new TabSet();
	final Tab tabHeuristics = new Tab("Scheme");
	
	SearchHeuristic currentSearchHeuristic = SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME;

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server.<br/> Please check your network "
			+ "connection and try again. <br/> If the problem persists contact the" +
					"Sourcerer group.";

	/**
	 * Create a remote service proxy to talk to the server-side search service.
	 */
	private final SourcererSearchServiceAsync scsService = GWT
			.create(SourcererSearchService.class);

	private final SourcererDBServiceAsync sdbService = GWT
			.create(SourcererDBService.class);
	

	public void onModuleLoad() {
		
		vlRoot.setLayoutMargin(5);
		vlRoot.setDefaultLayoutAlign(VerticalAlignment.TOP);
		vlRoot.setWidth100();
		vlRoot.setHeight100();

		VLayout vlTop = new VLayout();
		vlTop.setWidth100();
		vlTop.setHeight(36);

		// search controls
		Layout formContainer = new Layout();
		formContainer.setHeight(36);
		formContainer.setLayoutAlign(VerticalAlignment.CENTER);
		formContainer.setOverflow(Overflow.HIDDEN);
		formContainer.setBackgroundColor("#0f0e0e");
		formContainer.setShowShadow(true);

		form.setAutoFocus(true);
		form.setWidth("*");

		queryField
				.setTitle("&nbsp;&nbsp;<span class='scs'><b>SOURCERER</b> Code Search</span>");
		queryField.setSelectOnFocus(true);
		queryField.setWrapTitle(false);
		queryField.setDefaultValue("");
		queryField.setWidth(500);
		queryField.setHeight(24);
		form.setFields(queryField);

		butSearch.setWidth(80);
		butSearch.setHeight(24);

		form.setMargin(4);
		butSearch.setTop(2);
		butSearch.setLeft(675);

		form.addChild(butSearch);

		formContainer.addMember(form);

		HStack rightAligner = new HStack();
		rightAligner.setAlign(Alignment.RIGHT);
		rightAligner.setLayoutRightMargin(8);
		rightAligner.setLayoutTopMargin(1);
		rightAligner.setWidth100();

		final HLayout hlAdvControls = new HLayout();
		hlAdvControls.setLayoutAlign(Alignment.RIGHT);
		hlAdvControls.setLayoutTopMargin(6);
		hlAdvControls.setLayoutRightMargin(20);
		hlAdvControls.setLayoutBottomMargin(6);
		hlAdvControls.setLayoutLeftMargin(2);
		hlAdvControls.setPaddingAsLayoutMargin(true);
		hlAdvControls.setPadding(12);
		hlAdvControls.setWidth100();
		
		HStack rightAligner2 = new HStack();
		rightAligner2.setAlign(Alignment.RIGHT);
		rightAligner2.setWidth100();
		
		hlAdvControls.addMember(rightAligner2);
		
		DynamicForm dfHeuristicsForm = new DynamicForm();
		dfHeuristicsForm.setWidth("430");
		dfHeuristicsForm.setColWidths(80, "*");
		
		dfHeuristicsForm.setExtraSpace(4);
		dfHeuristicsForm.setPadding(4);
		
		tabAdvanced.setHeight(75);
		tabAdvanced.setWidth("450");
		tabAdvanced.setTabBarAlign(Side.RIGHT);
		
		tabAdvanced.addTab(tabHeuristics);
		tabHeuristics.setPane(dfHeuristicsForm);
		
		rightAligner2.addMember(tabAdvanced);
		
		hlAdvControls.setHeight("*");
		hlAdvControls.setBackgroundColor("#F4FAFF");
		hlAdvControls.setShowShadow(true);

		cbHeuristics.setTitle("Scheme");
		cbHeuristics.setWidth(350);
		LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();

		m.put(SearchHeuristic.TEXT_USEDFQN_FQN_SimSNAME_SNAME.name(), 
				SearchHeuristic.TEXT_USEDFQN_FQN_SimSNAME_SNAME.toString());
		
		m.put(SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME.name(), 
				SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME.toString());
		
		m.put(SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME.name(), 
				SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME.toString());
		
		m.put(SearchHeuristic.TEXT_USEDFQN_FQN_SNAME.name(), 
				SearchHeuristic.TEXT_USEDFQN_FQN_SNAME.toString());
		
		m.put(SearchHeuristic.TEXT_FQN_SNAME.name(), 
				SearchHeuristic.TEXT_FQN_SNAME.toString());
		
		m.put(SearchHeuristic.TEXT_SNAME.name(), 
				SearchHeuristic.TEXT_SNAME.toString());
		
		m.put(SearchHeuristic.TEXT.name(), 
				SearchHeuristic.TEXT.toString());
		
		m.put(SearchHeuristic.FQN_USEDFQN_SimSNAME_SNAME.name(), 
				SearchHeuristic.FQN_USEDFQN_SimSNAME_SNAME.toString());
		
		m.put(SearchHeuristic.FQN_USEDFQN_JdkLibSimSNAME_SNAME.name(), 
				SearchHeuristic.FQN_USEDFQN_JdkLibSimSNAME_SNAME.toString());
		
		m.put(SearchHeuristic.FQN_USEDFQN_JdkLibTcSimSNAME_SNAME.name(), 
				SearchHeuristic.FQN_USEDFQN_JdkLibTcSimSNAME_SNAME.toString());
		
		m.put(SearchHeuristic.FQN_USEDFQN_SNAME.name(), 
				SearchHeuristic.FQN_USEDFQN_SNAME.toString());
		
		m.put(SearchHeuristic.FQN_SNAME.name(), 
				SearchHeuristic.FQN_SNAME.toString());
		
		m.put(SearchHeuristic.NONE.name(), SearchHeuristic.NONE.toString());
		
		cbHeuristics.setValueMap(m);
		cbHeuristics.setDefaultToFirstOption(true);
				
		dfHeuristicsForm.setFields(cbHeuristics) ;

		cbHeuristics.addChangedHandler(new SearchHeuristicsChangedHandler(this));

		btnAdvanced.setTitle("Show Advanced");
		btnAdvanced.setWidth(120);
		btnAdvanced.setLayoutAlign(VerticalAlignment.CENTER);

		rightAligner.addMember(btnAdvanced);
		hlAdvControls.setVisible(false);

		HStack advContainer = new HStack();
		advContainer.setLayoutAlign(Alignment.RIGHT);
		advContainer.addMember(rightAligner);
		formContainer.addMember(advContainer);

		btnAdvanced.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {

				btnAdvanced.setTitle((btnAdvanced.getTitle().equals(
						"Show Advanced") ? "Hide Advanced" : "Show Advanced"));
				hlAdvControls.setVisible(!hlAdvControls.isVisible());
			}
		});

		// result containers
		HLayout hlResultsRootContainer = new HLayout();
		hlResultsRootContainer.setOverflow(Overflow.HIDDEN);

		final SectionStack ssApiElements = new SectionStack();
		final SectionStackSection sssJdk = new SectionStackSection("JDK");
		final SectionStackSection sssLib = new SectionStackSection("Libraries");
		final SectionStackSection sssLocal = new SectionStackSection("Local");

		ssApiElements.addSection(sssJdk);
		ssApiElements.addSection(sssLib);
		ssApiElements.addSection(sssLocal);
		sssJdk.addItem(tgElementsJdk);
		sssLib.addItem(tgElementsLib);
		sssLocal.addItem(tgElementsLocal);

		ssApiElements.setVisibilityMode(VisibilityMode.MULTIPLE);
		sssJdk.setExpanded(true);
		sssJdk.setResizeable(true);
		sssLib.setExpanded(true);
		sssLib.setResizeable(true);
		sssLocal.setExpanded(true);
		sssLocal.setResizeable(true);

		// apis
		final TabSet apiTabs = new TabSet();
		apiTabs.setOverflow(Overflow.HIDDEN);
		apiTabs.setWidth("280px");
		apiTabs.setShowResizeBar(true);

		final Tab tabApiTree = new Tab("Popular APIs");
		apiTabs.addTab(tabApiTree);
		tabApiTree.setPane(vlTopApis);

		final VLayout vlHitDetailsView = new VLayout(); 
		
		
		tsCode.setStyleName("ts-code");
		tsCode.setShowResizeBar(false);
		tsCode.setOverflow(Overflow.HIDDEN);
		
		tsCode.addCloseClickHandler(new CloseClickHandler() {  
		             public void onCloseClick(TabCloseClickEvent event) {  
		                 Tab tab = event.getTab();  
		                 tsCode.removeTab(tab); 
		             }  
		});
		
		tsCode.addTab(tabCode);
		tabCode.setPane(hfCodeHolder);
		
		final Tab tabApiElements = new Tab("Dependencies");
		tsCode.addTab(tabApiElements);
		tabApiElements.setPane(ssApiElements);
		
		similar.setCodeViewer(this);
		tabSimilar.setPane(similar);
		tsCode.addTab(tabSimilar);

		final VLayout vlHitsContainer = new VLayout();
		vlHitsContainer.setDefaultLayoutAlign(VerticalAlignment.TOP);
		vlHitsContainer.setOverflow(Overflow.HIDDEN);
		vlHitsContainer.setShowResizeBar(true);
		vlHitsContainer.setResizeBarSize(4);
		vlHitsContainer.setMinWidth(550);
		vlHitsContainer.setResizeBarTarget("next");
		//vlHitsContainer.setStyleName("vlHitsContainer");

		LayoutSpacer spacer1 = new LayoutSpacer();
		spacer1.setHeight(4);

		vlHits.setOverflow(Overflow.AUTO);
		vlHitsContainer.addMember(hsHitsNav);
		vlHitsContainer.addMember(spacer1);
		vlHitsContainer.addMember(vlHits);

		HTMLFlow hfDet = new HTMLFlow();
		hfDet.setStyleName("hfDet");
		hfDet.setHeight(12);
		hfDet.setContents("Details of the selected result <br/>");
		
		vlHitDetailsView.addMember(hfDet);
		vlHitDetailsView.addMember(tsCode);
		vlHitDetailsView.setWidth("*");
		vlHitDetailsView.setStyleName("hits-detail");
		
		hlResultsRootContainer.addMember(apiTabs);
		hlResultsRootContainer.addMember(vlHitsContainer);
		hlResultsRootContainer.addMember(vlHitDetailsView);

		LayoutSpacer spacer = new LayoutSpacer();
		spacer.setHeight(8);

		vlTop.addMember(formContainer);
		vlTop.addMember(hlAdvControls);

		vlRoot.addMember(vlTop);
		vlRoot.addMember(spacer);
		vlRoot.addMember(hlResultsRootContainer);

		vlRoot.draw();

		// Create a handler for the sendButton and nameField
		class SearchHandler implements ClickHandler, KeyUpHandler {

			public void onClick(ClickEvent event) {
				hsHitsNav.setFrom(0);
				clearResults();
				sendQueryToServer(true);
				
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				
				if (event.getKeyName().equals("Enter")) {
					hsHitsNav.setFrom(0);
					clearResults();
					sendQueryToServer(true);
				}
			}
		}
		
		// Add a handler to send the name to the server
		SearchHandler handler = new SearchHandler();
		butSearch.addClickHandler(handler);
		queryField.addKeyUpHandler(handler);
	}
	
	public void sendQueryToServer(final boolean isNewQuery) {

		final String query = form.getValueAsString("fQuery").trim();
		if (query.length() <= 0)
			return;

		queryTermsSet.clear();
		for (String s : query.split("\\s")) {
			queryTermsSet.add(s);
		}

		hitEids.clear();
		hitsXml = "";
		
		vlHits.removeMembers(vlHits.getMembers());
		vlHits.addMember(new HTMLFlow("Searching.."));
		
		clearHitDetails();

		scsService.searchSCSServer(query, hsHitsNav.getFrom(), 10,
				currentSearchHeuristic, new AsyncCallback<String>() {

					public void onFailure(Throwable caught) {
					}

					public void onSuccess(String result) {
						hitsXml = result;
						buildHitIds(hitsXml);
						updateHitsPager(hsHitsNav, result);
						
						erTables = null;
						sdbService.getERTables(hitEids,
								new AsyncCallback<ERTables>() {
									public void onFailure(Throwable caught) {
										
									}

									public void onSuccess(ERTables result) {
										erTables = result;
										erTables.buildIndices();
										Sourcerer_cs.this.rebuildResults(vlHits, Sourcerer_cs.this.hitsXml);
										if(isNewQuery){
											updateTopApis();
										} 
									}
								});
		
					}
				});
		
	}
	
//	private void updateErTables(){
//		final String query = form.getValueAsString("fQuery").trim();
//		if (query.length() <= 0)
//			return;
//		
//		
//		erTables = null;
//		sdbService.getERTables(hitEids,
//				new AsyncCallback<ERTables>() {
//					public void onFailure(Throwable caught) {
//						
//					}
//
//					public void onSuccess(ERTables result) {
//						erTables = result;
//						erTables.buildIndices();
//					}
//				});
//	}
	
	private void updateTopApis(){
		vlTopApis.clearContents();
		final String query = form.getValueAsString("fQuery").trim();
		if (query.length() <= 0)
			return;
		
		
		vlTopApis.showWaiting();
		scsService.getUsageAsFacets(query, currentSearchHeuristic,
				new AsyncCallback<String>(){

					public void onFailure(Throwable caught) {
						
					}

					public void onSuccess(String result) {
						
						vlTopApis.setTopJdkApis(ResultProcessor.getUsedApisFromFacetedHits(result,EntityCategory.JDK));
						vlTopApis.setTopLibApis(ResultProcessor.getUsedApisFromFacetedHits(result,EntityCategory.LIB));
						
					}
			
		});
		
	}
	
	private void updateERBoundControls(ERTables er) {

		tgElementsJdk.setData(EntityTreeDataSource.getTree(er,
				EntityCategory.JDK));
		tgElementsJdk.setFields(new TreeGridField("Short Name"));
		tgElementsJdk.sort("Uses", SortDirection.DESCENDING);

		tgElementsLib.setData(EntityTreeDataSource.getTree(er,
				EntityCategory.LIB));
		tgElementsLib.setFields(new TreeGridField("Short Name"));
		tgElementsLib.sort("Uses", SortDirection.DESCENDING);

		tgElementsLocal.setData(EntityTreeDataSource.getTree(er,
				EntityCategory.LOCAL));
		tgElementsLocal.setFields(new TreeGridField("Short Name"));
		tgElementsLocal.sort("Uses", SortDirection.DESCENDING);
	}

	private void updateHitsPager(HitsPager hitsPager, String xmlResultInString) {
		
		HitsStat hs = ResultProcessor.getStatsFromHits(xmlResultInString);
		
		hitsPager.setFrom(hs.start);
		hitsPager.setTotalResults(hs.numOfResults);
		hitsPager.updateResultsText();

	}

	private void buildHitIds(String xmlResultInString) {
		hitEids.clear();
		hitEids = getHitEidsFromHits(xmlResultInString);
	}
	
	private void rebuildResults(VLayout vlHits, String xmlResultInString) {
		vlHits.removeMembers(vlHits.getMembers());
		
		for(HitFqnEntityId hFE : ResultProcessor.getFqnEntityIdFromHits(xmlResultInString)){
			addHitToVLHits(vlHits, hFE.fqn, hFE.entityId);
		}
	}

	private void addHitToVLHits(final VLayout vlHits, String fqn, String entity_id) {
		
		final VLHit hit = new VLHit(vlTopApis, entity_id, this);
		
		vlTopApis.register(hit);
		
		hit.setHeight(40);
		final VLHitDetails vlHitDetails = new VLHitDetails();
		
		vlHitDetails.setVisible(false);
		vlHitDetails.setHeight(0);
		vlHitDetails.indicateWaiting();

		final String _eid = entity_id;

		HTMLFlow hFqn = new HTMLFlow();
		
		hit.addMouseOutHandler(new MouseOutHandler(){

			public void onMouseOut(MouseOutEvent event) {
				if(hit.selected == false){
					hit.setStyleName("deselected-hit");
				}
			}
			
		});
		
		hit.addMouseOverHandler(new MouseOverHandler(){

			public void onMouseOver(MouseOverEvent event) {
				
				for(Canvas h: vlHits.getMembers()){
					
					if (((VLHit) h).selected == false){ 
						h.setStyleName("deselected-hit");
					}
					
				}
				
				if(hit.selected == false)
					hit.setStyleName("hover-hit");		
			}
		});
		
		hFqn.setHeight(20);
		hFqn.setStyleName("entity");
		hFqn.setContents(HtmlPartsBuilder.makeFqnParts(fqn));
		
		hit.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					public void onClick(
							com.smartgwt.client.widgets.events.ClickEvent event) {
						
						for(Canvas h: vlHits.getMembers()){
							((VLHit) h).selected = false;
							h.setStyleName("deselected-hit");
						}
						
						hit.selected = true;
						hit.setStyleName("selected-hit");
						
						vlHitDetails.setVisible(true);
						vlHitDetails.setHeight100();

						similar.clearContents();

						CachedHitResult cachedResult = hitsDetailCache.get(_eid);
						
						if (cachedResult != null) {
							vlHitDetails.setEntityId(_eid);
							updateERBoundControls(cachedResult.erTables);
							hfCodeHolder.setContents(cachedResult.code);
							similar.setSimilarLocalEntities(ResultProcessor
									.getFqnEntityIdFromHits(cachedResult.mltViaLocal));
							similar.setSimilarLibEntities(ResultProcessor
									.getFqnEntityIdFromHits(cachedResult.mltViaLib));
							similar.setSimilarJdkEntities(ResultProcessor
									.getFqnEntityIdFromHits(cachedResult.mltViaJdk));

						} else {

							final CachedHitResult _cachedResult = new CachedHitResult();

							scsService.getEntityCode(_eid, queryTermsSet, null,
									new AsyncCallback<String>() {
										public void onFailure(Throwable caught) {
										}

										public void onSuccess(String result) {
											vlHitDetails.setEntityId(_eid);

											hfCodeHolder.setContents(result);
											_cachedResult.code = result;

											vlHitDetails.setIsWaiting(false);
										}
									});

							scsService.searchMltViaJdkUsage(_eid,
									new AsyncCallback<String>() {
										public void onFailure(Throwable caught) {
										}

										public void onSuccess(String result) {
											similar
													.setSimilarJdkEntities(ResultProcessor
															.getFqnEntityIdFromHits(result));
											_cachedResult.mltViaJdk = result;
										}

									});

							scsService.searchMltViaLibUsage(_eid,
									new AsyncCallback<String>() {
										public void onFailure(Throwable caught) {
										}

										public void onSuccess(String result) {
											similar
													.setSimilarLibEntities(ResultProcessor
															.getFqnEntityIdFromHits(result));
											_cachedResult.mltViaLib = result;
										}

									});

							scsService.searchMltViaLocalUsage(_eid,
									new AsyncCallback<String>() {
										public void onFailure(Throwable caught) {
										}

										public void onSuccess(String result) {
											similar
													.setSimilarLocalEntities(ResultProcessor
															.getFqnEntityIdFromHits(result));
											_cachedResult.mltViaLocal = result;
										}

									});

							
							LinkedList<String> eids = new LinkedList<String>();
							eids.add(_eid);
							sdbService.getERTables(eids,
									new AsyncCallback<ERTables>() {
										public void onFailure(Throwable caught) {
										}

										public void onSuccess(ERTables result) {
											_cachedResult.erTables = result;
											updateERBoundControls(result);
										}
									});

						hitsDetailCache.put(_eid, _cachedResult);
						
						}
					}
				});
		
		hit.addMember(hFqn);
		hit.addMember(vlHitDetails);
		vlHits.addMember(hit);
	}

	
	public void setSearchHeuristic(SearchHeuristic sh) {
		this.currentSearchHeuristic = sh;
	}

	public ScsClientMode getMode() {
		return this.currentMode;
	}

	public void setMode(ScsClientMode mode) {
		this.currentMode = mode;
	}

	public void clearResults(){
		
		// internal query session data
		hitsDetailCache.clear();
		hitEids.clear();
		queryTermsSet.clear();
		
		// main hits
		vlHits.removeMembers(vlHits.getMembers());
		// navigation control
		hsHitsNav.setFrom(0);
		hsHitsNav.clearResultsText();
		
		// top apis
		vlTopApis.clearContents();
		
		clearHitDetails();
		
	}
	
	private void clearHitDetails(){
		// hit details
		tgElementsJdk.setData(getEmptyInitTree());
		tgElementsLib.setData(getEmptyInitTree());
		tgElementsLocal.setData(getEmptyInitTree());
		similar.clearContents();
		hfCodeHolder.setContents("");
	}
		
	private Tree getEmptyInitTree(){
		Tree t = new Tree();
		return t;
	}

	public void showCode(final String entityId) {
		scsService.getEntityCode(entityId, queryTermsSet, null,
				new AsyncCallback<String>(){

			public void onFailure(Throwable caught) {
				
			}

			public void onSuccess(String result) {
				Tab t = new Tab(entityId);
				t.setCanClose(true);
				
				
				HTMLFlow hf = new HTMLFlow();
				hf.setContents(result);
				t.setPane(hf);
				
				tsCode.addTab(t);
			}
		});
	}

}
