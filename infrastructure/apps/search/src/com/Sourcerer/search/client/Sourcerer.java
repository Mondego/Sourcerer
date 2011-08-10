package com.Sourcerer.search.client;




import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
//import java.util.Date;
//import com.google.gwt.http.client.Request;
//import com.google.gwt.http.client.RequestBuilder;
//import com.google.gwt.http.client.RequestCallback;
//import com.google.gwt.http.client.RequestException;
//import com.google.gwt.http.client.Response;
//import com.google.gwt.http.client.URL;
//import com.google.gwt.i18n.client.DateTimeFormat;
//import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
//import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * @author Ji Ho Han (james.han92@gmail.com)
 */
public class Sourcerer implements EntryPoint {
	
	private TextBox newSymbolTextBox = new TextBox();
	private Button addSearchButton = new Button("Search");
	private HorizontalPanel addPanel = new HorizontalPanel();
//	private static final int REFRESH_INTERVAL = 5000; // ms
	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel hyperlinkPanel = new HorizontalPanel();
	private Label titleLabel = new Label();	
	private Label searchedLabel = new Label();
	private HTML resultHTML = new HTML();
	private HorizontalPanel newPanel = new HorizontalPanel();
	private TextBox searchedTextBox = new TextBox();						//a textbox for after search
	private Button searchedButton = new Button("Search");					//a search button for after search
	private SearchServiceAsync searchSvc = GWT.create(SearchService.class);
	private Hyperlink next = new Hyperlink("Next", false, "");
	private Hyperlink previous = new Hyperlink("Previous", false, "");
	

	@SuppressWarnings("deprecation")
	public void onModuleLoad() {
		System.out.println(History.getToken());
		//add Panel 
		addPanel.add(newSymbolTextBox);
		addPanel.add(addSearchButton);
		hyperlinkPanel.add(previous);
		hyperlinkPanel.add(next);
		mainPanel.add(titleLabel);
		mainPanel.add(addPanel);
		RootPanel.get().add(mainPanel);
		newSymbolTextBox.setFocus(true);
		searchedTextBox.setFocus(true);
		titleLabel.setText("Sourcerer");
		searchedLabel.setText("Sourcerer");
		
		addPanel.addStyleName("addPanel");
		resultHTML.addStyleName("resultLabel");
		addSearchButton.addStyleName("Button");
		addSearchButton.addStyleDependentName("search");
		newPanel.addStyleName("newPanel");
		searchedTextBox.addStyleDependentName("search");
		searchedButton.addStyleName("Button");
		titleLabel.addStyleName("lastUpdatedLabel");
		searchedLabel.addStyleName("searchedLabel");
		searchedTextBox.addStyleName("searchedTextBox");
		searchedButton.addStyleName("Button");
		searchedButton.addStyleDependentName("searched");
		previous.addStyleName("previouslink");
		resultHTML.addStyleName("HTMLstyle");
		next.addStyleName("nextlink");
		
		//Listen for mouse events in the input box
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
		
		searchedButton.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        result();
		      }
		    });
		searchedTextBox.addKeyPressHandler(new KeyPressHandler() {
		      public void onKeyPress(KeyPressEvent event) {
		        if (event.getCharCode() == KeyCodes.KEY_ENTER) {
		          result();
		        }
		      }
		  	});
		final AsyncCallback<String> callback = new AsyncCallback<String>(){
			public void onFailure(Throwable caught){
				//throw exception
			}
			
			public void onSuccess(String result){
				resultHTML.setHTML(result);

			}
		};
		
		//keep track of which keywords were searched
		//when something is searched, the main panel removes all the beginning page widgets and adds
		//the new widgets and the search result
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				if(History.getToken().compareTo("") != 0){
					String token = History.getToken();
					String str = "";

					int index = token.indexOf("q=");
					int j = token.indexOf("&", index);
					if(index >= 0){
						if(j == -1){
							str = token.substring(index+2);
						}
						else{
							str = token.substring(index+2, j);
						}
					}
					else{
						str = "";
					}
					int pageNumber = getPageNumber();
					
					
					mainPanel.remove(addPanel);
					mainPanel.remove(titleLabel);
					newPanel.add(searchedLabel);
					newPanel.add(searchedTextBox);
					newPanel.add(searchedButton);
					mainPanel.add(newPanel);
					searchSvc.update(str, pageNumber, callback);
					mainPanel.add(resultHTML);
					mainPanel.add(hyperlinkPanel);
					searchedTextBox.setText(str);
				}
				else{
					mainPanel.remove(newPanel);
					mainPanel.remove(resultHTML);
					mainPanel.remove(hyperlinkPanel);
					mainPanel.add(titleLabel);
					mainPanel.add(addPanel);
					searchSvc.update(newSymbolTextBox.getText(), 1, callback);
				}
			}
		});
		//add click handler for the hyperlink
		next.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event){
				String token = History.getToken();
				int i = token.indexOf("p=");
				int j = token.indexOf("&", i);
				int pageNumber = getPageNumber() + 1;
				if(j != -1){
					token = token.substring(0,i+2) + pageNumber + token.substring(j);
				}
				next.setTargetHistoryToken(token);
			}
		});
		
		previous.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event){
				String token = History.getToken();
				int i = token.indexOf("p=");
				int j = token.indexOf("&", i);
				int pageNumber = getPageNumber() - 1;
				if(j != -1 && pageNumber != 0){
					token = token.substring(0, i+2) + pageNumber + token.substring(j);
				}
				previous.setTargetHistoryToken(token);
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
		
		if(History.getToken().compareTo("") != 0){
			History.newItem("?q=" + searchedTextBox.getText() + "&p=1&");
		}
		else{
			History.newItem("?q=" + newSymbolTextBox.getText() + "&p=1&");
			
		}

		
	}
	
	//gets the page number of the token
	private int getPageNumber(){
		String temp = History.getToken();
		int pageNumber = temp.indexOf("p=");
		int j = temp.indexOf("&", pageNumber);
		if(pageNumber >= 0){
			if(j == -1){
				temp = temp.substring(pageNumber+2);
			}
			else{
				temp = temp.substring(pageNumber+2, j);
			}
			pageNumber = Integer.parseInt(temp);
		}
		else { 
			temp = "";
		}
		return pageNumber;
	}
	
/*	private void refresh(){
		lastUpdatedLabel.setText("Last update : " + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
	}*/
	
	
}
