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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.core.client.GWT;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
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
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.CloseClickHandler;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;

import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;

import edu.uci.ics.sourcerer.scs.client.event.ApiSelectedEvent;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;
import edu.uci.ics.sourcerer.scs.common.client.HitsStat;
import edu.uci.ics.sourcerer.scs.common.client.ResultProcessor;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;
import edu.uci.ics.sourcerer.scs.common.client.SearchResultsWithSnippets;
import gdurelle.tagcloud.client.tags.TagCloud;
import gdurelle.tagcloud.client.tags.WordTag;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 */
public class Sourcerer_cs implements EntryPoint, EventPreview, ScsSearcher,
		ITakesSelectionUpdateEvent, IERProvider, ICodeViewer {

	ERTables erTables = null;
	HashSet<String> filterFqns = new HashSet<String>();
	HashSet<String> queryTermsSet = new HashSet<String>();
	LinkedList<String> hitEids = new LinkedList<String>();
	SearchResultsWithSnippets hits;

	public void process(ApiSelectionUpdateEvent event) {
		this.addFqnFilter(event.fqn);

		// if (event.op == ApiSelectedEvent.Operation.SELECT){
		// this.addFqnFilter(event.fqn);
		// } else {
		// this.removeFqnFilter(event.fqn);
		// }
	}

	public ERTables getErTables() {
		return this.erTables;
	}

	private final HashMap<String, CachedHitResult> hitsDetailCache = new HashMap<String, CachedHitResult>();

	private ScsClientMode currentMode = ScsClientMode.SEARCH;

	VLayout vlRoot = new VLayout();

	final DynamicForm form = new DynamicForm();
	final HitsPager hsHitsNav = new HitsPager(this);
	final VLayout vlHits = new VLayout();
	final HLayout hlAdvControls = new HLayout();

	final SelectItem cbHeuristics = new SelectItem();

	final VLayout vlHitDetailsView = new VLayout();
	final VLSimilar similar = new VLSimilar();
	final Tab tabSimilar = new Tab("Similar");
	final Tab tabCode = new Tab("Code");
	final HTMLFlow hfCodeHolder = new HTMLFlow();
	final VLTopApis vlTopApis = new VLTopApis();
	final TabSet tsCode = new TabSet();

	final Button btnAdvanced = new Button();
	final Button btnOptions = new Button();
	final Window wOptions = new Window();

	final TextItem queryField = new TextItem("fQuery");
	final IButton butSearch = new IButton("Search");

	final TabSet tsOptions = new TabSet();
	final Tab tabHeuristics = new Tab("Scheme");

	VLayout vlFilter = new VLayout();
	VLFilterApis vlFilterApis = new VLFilterApis();
	TagCloud tagCloud = new TagCloud();

	SearchHeuristic currentSearchHeuristic = SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME;

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server.<br/> Please check your network "
			+ "connection and try again. <br/> If the problem persists contact the"
			+ "Sourcerer group.";

	/**
	 * Create a remote service proxy to talk to the server-side search service.
	 */
	private final SourcererSearchServiceAsync scsService = GWT
			.create(SourcererSearchService.class);

	private final SourcererDBServiceAsync sdbService = GWT
			.create(SourcererDBService.class);

	public void onModuleLoad() {

		DOM.addEventPreview(this);

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

		hlAdvControls.setHeight(150);
		hlAdvControls.setMembersMargin(5);
		hlAdvControls.setLayoutMargin(10);

		vlFilter.setWidth("35%");
		vlFilter.setMargin(2);
		vlFilter.setOverflow(Overflow.CLIP_H);
		vlFilter.setScrollbarSize(10);
		vlFilter.setHeight("100px");
		vlFilter.setShowResizeBar(true);
		Label lblFilter = new Label();
		lblFilter.setWidth100();
		lblFilter.setHeight(10);
		lblFilter.setContents("<i>API Filters Applied (click to remove)</i>");
		vlFilter.addMember(lblFilter);

		vlFilterApis.setMargin(4);
		vlFilterApis.searcher = this;
		vlFilter.addMember(vlFilterApis);

		hlAdvControls.addMember(vlFilter);

		tagCloud.setWidth("100%");
		tagCloud.setHeight("100%");
		// tagCloud.setStylePrimaryName("tagcloud");
		tagCloud.setMaxNumberOfWords(30);
		//tagCloud.setTitle("Popular Words - Click to add to query");

		VLayout vlTagCloud = new VLayout();

		vlTagCloud.setWidth100();
		vlTagCloud.setHeight(120);
		vlTagCloud.setOverflow(Overflow.AUTO);
		vlTagCloud.setScrollbarSize(12);
		// vlTagCloud.setPadding(8);

		Label lblTag = new Label();
		lblTag.setWidth100();
		lblTag.setHeight(10);
		lblTag.setContents("<i>Popular Words (click to add to query)</i>");

		VLayout _tagMargin = new VLayout();
		_tagMargin.setMargin(8);
		_tagMargin.setHeight100();
		_tagMargin.setWidth100();
		// _tagMargin.setBackgroundColor("#E4E4F7");
		_tagMargin.addMember(tagCloud);
		vlTagCloud.addMember(lblTag);

		vlTagCloud.addMember(_tagMargin);

		hlAdvControls.addMember(vlTagCloud);

		wOptions.setAutoSize(true);
		wOptions.setTitle("Sourcerer Search Options");
		wOptions.setWidth(570);
		wOptions.setHeight(140);
		wOptions.setLeft(400);
		wOptions.setCanDragReposition(true);
		wOptions.setCanDragResize(true);
		wOptions.addItem(rightAligner2);

		tsOptions.setHeight100();
		tsOptions.setWidth100();
		tsOptions.setTabBarAlign(Side.RIGHT);
		tsOptions.addTab(tabHeuristics);

		rightAligner2.addMember(tsOptions);

		hlAdvControls.setHeight("*");
		hlAdvControls.setBackgroundColor("#F4FAFF");
		hlAdvControls.setShowShadow(true);

		setupHeuristics();

		btnAdvanced.setTitle("Show Advanced");
		btnAdvanced.setWidth(120);
		btnAdvanced.setLayoutAlign(VerticalAlignment.CENTER);

		btnOptions.setTitle("Options");
		btnOptions.setWidth(80);
		btnOptions.setLayoutAlign(VerticalAlignment.CENTER);

		rightAligner.addMember(btnAdvanced);
		rightAligner.addMember(btnOptions);
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

		btnOptions.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				wOptions.show();

			}
		});

		// result containers
		HLayout hlResultsRootContainer = new HLayout();
		hlResultsRootContainer.setOverflow(Overflow.HIDDEN);

		// apis
		final TabSet apiTabs = new TabSet();
		apiTabs.setOverflow(Overflow.HIDDEN);
		apiTabs.setWidth("280px");
		apiTabs.setShowResizeBar(true);

		final Tab tabApiTree = new Tab("Top APIs (Click to filter results)");
		apiTabs.addTab(tabApiTree);
		tabApiTree.setPane(vlTopApis);

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
		vlHitDetailsView.setWidth(0);
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
				filterFqns.clear();
				vlFilterApis.clearAllFqns();
				sendQueryToServer(true);

			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {

				if (event.getKeyName().equals("Enter")) {
					hsHitsNav.setFrom(0);
					clearResults();
					filterFqns.clear();
					vlFilterApis.clearAllFqns();
					sendQueryToServer(true);
				}
			}
		}

		// Add a handler to send the name to the server
		SearchHandler handler = new SearchHandler();
		butSearch.addClickHandler(handler);
		queryField.addKeyUpHandler(handler);
	}

	private void setupHeuristics() {
		DynamicForm dfHeuristicsForm = new DynamicForm();
		dfHeuristicsForm.setWidth("480");
		dfHeuristicsForm.setColWidths(80, "*");

		dfHeuristicsForm.setExtraSpace(4);
		dfHeuristicsForm.setPadding(4);
		tabHeuristics.setPane(dfHeuristicsForm);

		cbHeuristics.setTitle("Scheme");
		cbHeuristics.setWidth(470);
		LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();

		// text, used javadoc, sim and used names

		m.put(SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME
				.name(),
				SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME
						.toString());

		m.put(SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibSimSNAME_SNAME
				.name(),
				SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibSimSNAME_SNAME
						.toString());

		m.put(SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME
				.name(),
				SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME
						.toString());

		m.put(SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_SNAME.name(),
				SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_SNAME.toString());

		// text, sim and used names

		m.put(SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME.name(),
				SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME
						.toString());

		m.put(SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME.name(),
				SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME
						.toString());

		m.put(SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME.name(),
				SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME
						.toString());

		m.put(SearchHeuristic.TEXT_USEDFQN_FQN_SNAME.name(),
				SearchHeuristic.TEXT_USEDFQN_FQN_SNAME.toString());

		// text, names

		m.put(SearchHeuristic.TEXT_FQN_SNAME.name(),
				SearchHeuristic.TEXT_FQN_SNAME.toString());

		m.put(SearchHeuristic.TEXT_SNAME.name(), SearchHeuristic.TEXT_SNAME
				.toString());

		m.put(SearchHeuristic.TEXT.name(), SearchHeuristic.TEXT.toString());

		// w/o text, names only

		m.put(SearchHeuristic.FQN_USEDFQN_JdkLibSimSNAME_SNAME.name(),
				SearchHeuristic.FQN_USEDFQN_JdkLibSimSNAME_SNAME.toString());

		m.put(SearchHeuristic.FQN_USEDFQN_JdkLibTcSimSNAME_SNAME.name(),
				SearchHeuristic.FQN_USEDFQN_JdkLibTcSimSNAME_SNAME.toString());

		m.put(SearchHeuristic.FQN_USEDFQN_JdkLibHdSimSNAME_SNAME.name(),
				SearchHeuristic.FQN_USEDFQN_JdkLibHdSimSNAME_SNAME.toString());

		m.put(SearchHeuristic.FQN_USEDFQN_SNAME.name(),
				SearchHeuristic.FQN_USEDFQN_SNAME.toString());

		m.put(SearchHeuristic.FQN_SNAME.name(), SearchHeuristic.FQN_SNAME
				.toString());

		// raw

		m.put(SearchHeuristic.NONE.name(), SearchHeuristic.NONE.toString());

		cbHeuristics.setValueMap(m);
		cbHeuristics.setDefaultToFirstOption(true);

		dfHeuristicsForm.setFields(cbHeuristics);

		cbHeuristics
				.addChangedHandler(new SearchHeuristicsChangedHandler(this));
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
		hits = null;
		vlHits.removeMembers(vlHits.getMembers());
		vlHits.addMember(new HTMLFlow("Searching.."));

		clearHitDetails();
		hsHitsNav.clearResultsText();
		
		if (isNewQuery) {
			vlTopApis.clearContents();
			vlTopApis.showWaiting();
			tagCloud.getTags().clear();
			tagCloud.refresh();
		}

		sdbService.getSearchResultsWithSnippets(query, hsHitsNav.getFrom(), 10,
				currentSearchHeuristic, filterFqns,
				new AsyncCallback<SearchResultsWithSnippets>() {

					public void onFailure(Throwable arg0) {
						// TODO Auto-generated method stub
						hsHitsNav.clearResultsText();
						vlHits.removeMembers(vlHits.getMembers());
						vlHits.addMember(new HTMLFlow("No Results Found"));
						vlTopApis.clearContents();

					}

					public void onSuccess(SearchResultsWithSnippets result) {
						// TODO Auto-generated method stub

						hits = result;

						if (hits.stat.numOfResults == 0) {
							hsHitsNav.clear();
							vlHits.addMember(new HTMLFlow("No Results Found."));
							vlTopApis.clearContents();
							return;
						}

						buildHitIds(hits);
						updateHitsPager(hsHitsNav, result);

						Sourcerer_cs.this.rebuildResults(vlHits,
								Sourcerer_cs.this.hits);
						if (isNewQuery) {
							updateTopApis(result);
							buildTagCloud(result);
						}

					}
				});

	}

	private void buildTagCloud(SearchResultsWithSnippets result) {

		// int discount = Math.min((int)result.stat.numOfResults, 30);
		// if(discount==0) return;
		//		
		tagCloud.getTags().clear();

		Map<String, Integer> sortedTags = SorterByValue.sort(result.wordCounts);

		int i = 0;
		for (String term : sortedTags.keySet()) {
			if (i > 30)
				break;
			if (queryTermsSet.contains(term))
				continue;
			if (stopWords.contains(term))
				continue;
			int count = result.wordCounts.get(term); // /(discount/2);
			if (count < 3)
				continue;
			
			WordTag wt = new WordTag();
			wt.setLink("#tagword#" + term);
			wt.setWord(term);
			wt.setNumberOfOccurences(count);
			tagCloud.addWord(wt);

			i++;
		}

		// for(String term: new TreeSet<String>(result.wordCounts.keySet())){
		//			
		// }
	}

	private void updateTopApis(SearchResultsWithSnippets result) {
		vlTopApis.clearContents();
		final String query = form.getValueAsString("fQuery").trim();
		if (query.length() <= 0)
			return;

		vlTopApis.showWaiting();
		vlTopApis.setTopUsedLibApis(result.usedFqns);
	}

	private void updateHitsPager(HitsPager hitsPager,
			SearchResultsWithSnippets results) {

		HitsStat hs = results.stat;

		hitsPager.setFrom(hs.start);
		hitsPager.setTotalResults(hs.numOfResults);
		hitsPager.updateResultsText();

	}

	private void buildHitIds(SearchResultsWithSnippets results) {
		hitEids.clear();
		hitEids = results.getHitEids();
	}

	private void rebuildResults(VLayout vlHits,
			SearchResultsWithSnippets results) {
		vlHits.removeMembers(vlHits.getMembers());

		for (HitFqnEntityId hFE : results.results) {
			addHitToVLHits(vlHits, hFE.fqn, hFE.entityId, hFE.snippetParts);
		}
	}

	private void addHitToVLHits(final VLayout vlHits, String fqn,
			String entity_id, List<String> snippet) {

		final VLHit hit = new VLHit(vlTopApis, entity_id, this);

		// vlTopApis.register(hit);
		vlTopApis.register(this);

		hit.setHeight(40);
		final VLHitDetails vlHitDetails = new VLHitDetails();

		vlHitDetails.setVisible(false);
		vlHitDetails.setHeight(0);
		vlHitDetails.indicateWaiting();

		final String _eid = entity_id;

		HTMLFlow hFqn = new HTMLFlow();

		hit.addMouseOutHandler(new MouseOutHandler() {

			public void onMouseOut(MouseOutEvent event) {
				if (hit.selected == false) {
					hit.setStyleName("deselected-hit");
				}
			}

		});

		hit.addMouseOverHandler(new MouseOverHandler() {

			public void onMouseOver(MouseOverEvent event) {

				for (Canvas h : vlHits.getMembers()) {

					if (((VLHit) h).selected == false) {
						h.setStyleName("deselected-hit");
					}
				}

				if (hit.selected == false)
					hit.setStyleName("hover-hit");
			}
		});

		hFqn.setHeight(20);
		hFqn.setStyleName("entity");
		hFqn.setContents(HtmlPartsBuilder.makeFqnParts(fqn));

		hit.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					public void onClick(
							com.smartgwt.client.widgets.events.ClickEvent event) {

						vlHitDetailsView.setVisible(true);
						vlHitDetailsView.setWidth("50%");
						
						for (Canvas h : vlHits.getMembers()) {
							((VLHit) h).selected = false;
							h.setStyleName("deselected-hit");
						}

						hit.selected = true;
						hit.setStyleName("selected-hit");

						vlHitDetails.setVisible(true);
						vlHitDetails.setHeight100();

						similar.clearContents();

						CachedHitResult cachedResult = hitsDetailCache
								.get(_eid);

						if (cachedResult != null) {
							vlHitDetails.setEntityId(_eid);
							hfCodeHolder.setContents(cachedResult.code);
							similar
									.setSimilarLocalEntities(ResultProcessor
											.getFqnEntityIdFromHits(cachedResult.mltViaLocal));
							similar
									.setSimilarLibEntities(ResultProcessor
											.getFqnEntityIdFromHits(cachedResult.mltViaLib));
							similar
									.setSimilarJdkEntities(ResultProcessor
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

							hitsDetailCache.put(_eid, _cachedResult);
						}
					}
				});

		hit.addMember(hFqn);
		hit.addMember(vlHitDetails);

		for(String snippetPart: snippet){
			if (snippetPart.length() > 0
					&& !snippetPart.equals("<div class=\"result_code\"></div>")) {
				HTMLFlow htmlSnippet = new HTMLFlow(snippetPart);
				htmlSnippet.setBackgroundColor("white");
				htmlSnippet.setMargin(4);
				htmlSnippet.setBorder("1px solid silver");
				hit.addMember(htmlSnippet);
			}
		}

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

	public void clearResults() {

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

	private void clearHitDetails() {
		// hit details
		similar.clearContents();
		hfCodeHolder.setContents("");
	}

	public void showCode(final String entityId) {
		scsService.getEntityCode(entityId, queryTermsSet, null,
				new AsyncCallback<String>() {

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

	public void addFqnFilter(String fqn) {
		
		// show advanced
		btnAdvanced.setTitle("Hide Advanced");
		hlAdvControls.setVisible(true);
		
		if (filterFqns.contains(fqn))
			return;

		filterFqns.add(fqn);
		vlFilterApis.clearAndAddFqns(filterFqns);
		sendQueryToServer(true);
	}

	public void clearAllFilters() {
		// TODO Auto-generated method stub

	}

	public void disableFilter() {
		// TODO Auto-generated method stub

	}

	public void enableFilter() {
		// TODO Auto-generated method stub

	}

	public boolean isFilterEnabled() {
		return false;
	}

	public void removeFqnFilter(String fqn) {
		filterFqns.remove(fqn);
		vlFilterApis.clearAndAddFqns(filterFqns);
		sendQueryToServer(true);
	}

	static HashSet<String> stopWords = new HashSet<String>();
	static {
		stopWords.add("get");
		stopWords.add("set");
		stopWords.add("create");
		stopWords.add("is");
		stopWords.add("anonymous");
		stopWords.add("add");
		stopWords.add("type");
		stopWords.add("to");
		stopWords.add("run");
		stopWords.add("name");
		
		// stopWords.add("page");
		// stopWords.add("visit");

		// stopWords.add("action");
		// stopWords.add("changed");
		// stopWords.add("update");
		// stopWords.add("selection");
		// stopWords.add("remove");
		// stopWords.add("text");
		// stopWords.add("string");
		// stopWords.add("handle");
		// stopWords.add("listener");
		// stopWords.add("dialog");
		// stopWords.add("change");
		// stopWords.add("file");
		// stopWords.add("value");
		// stopWords.add("id");
		// stopWords.add("element");
		// stopWords.add("selected");
	}

	public boolean onEventPreview(Event event) {
		if (DOM.eventGetType(event) == Event.ONCLICK) {
			Element target = DOM.eventGetTarget(event);
			if ("a".equalsIgnoreCase(getTagName(target))) {
				String href = DOM.getElementAttribute(target, "href");
				// now test if href is:
				// - #anchor link
				// - "doThat.html" - relative in-page link
				// - external link (path is different the e.g.
				// GWT.getModuleBaseURL()
				// System.out.println("href: "+href);

				if (href.startsWith("#tagword#")) {
					
					this.queryTermsSet.add(href.replaceFirst("#tagword#", ""));
					String oldQuery = form.getValueAsString("fQuery").trim();
					String newQuery = oldQuery + " " + href.replaceFirst("#tagword#", "");
					form.setValue("fQuery", newQuery);
					sendQueryToServer(true);
					return false;
				}
			}
		}
		return true;
	}

	native String getTagName(Element element)
	/*-{
		return element.tagName;
	}-*/;

}
