package com.Sourcerer.search.client;

import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
//import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;



public class Sourcerer implements EntryPoint {
	
	private TextBox newSymbolTextBox = new TextBox();
	private Button addSearchButton = new Button("Search");
	private HorizontalPanel addPanel = new HorizontalPanel();
//	private static final int REFRESH_INTERVAL = 5000; // ms
	private VerticalPanel mainPanel = new VerticalPanel();
//	private Label lastUpdatedLabel = new Label();	
	private Label resultLabel = new Label();
	private SearchServiceAsync searchSvc = GWT.create(SearchService.class);
	
	public void onModuleLoad() {
		
		//add Panel 
		addPanel.add(newSymbolTextBox);
		addPanel.add(addSearchButton);
		mainPanel.add(addPanel);
		mainPanel.add(resultLabel);
//		mainPanel.add(lastUpdatedLabel);
		RootPanel.get().add(mainPanel);
		newSymbolTextBox.setFocus(true);
		
		addPanel.addStyleName("addPanel");
		resultLabel.addStyleName("resultLabel");
		addSearchButton.addStyleName("Button");
		addSearchButton.addStyleDependentName("search");
		
		addSearchButton.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        result();
		      }
		    });
		    
		 // Listen for keyboard events in the input box.
		newSymbolTextBox.addKeyPressHandler(new KeyPressHandler() {
		      public void onKeyPress(KeyPressEvent event) {
		        if (event.getCharCode() == KeyCodes.KEY_ENTER) {
		          result();
		        }
		      }
		  	});
		
		/*
	    Timer refreshTimer = new Timer() {
		      @Override
		      public void run() {
		        refresh();
		      }
		    };
		    refreshTimer.scheduleRepeating(REFRESH_INTERVAL); */
		
	}

	private void result(){
		if(searchSvc == null) {
			searchSvc = GWT.create(SearchService.class);
		}
		
		AsyncCallback<String> callback = new AsyncCallback<String>(){
			public void onFailure(Throwable caught){
				//throw exception
			}
			
			public void onSuccess(String result){
				resultLabel.setText(result);
			}
		};
	searchSvc.update(newSymbolTextBox.getText(), callback);
	}
	
/*	private void refresh(){
		lastUpdatedLabel.setText("Last update : " + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
	}*/
	
	
}
