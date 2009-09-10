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
package edu.uci.ics.sourcerer.scs.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 *  
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 *
 */
public class MyComponent {
 
   private HandlerManager handlerManager;
 
   /**
    * Adds this handler to the widget.
    *
    * @param  the type of handler to add
    * @param type the event type
    * @param handler the handler
    * @return {@link HandlerRegistration} used to remove the handler
    */
   //copied from GWT 1.6 Widget
   protected final <H extends EventHandler> HandlerRegistration addHandler(GwtEvent.Type<H> type, final H handler){
      return ensureHandlers().addHandler(type, handler);
   }
 
   /**
    * Ensures the existence of the handler manager.
    *
    * @return the handler manager
    * */
   //copied from GWT 1.6 Widget
   private HandlerManager ensureHandlers(){
      return handlerManager == null ? handlerManager = new HandlerManager(this) : handlerManager;
   }
 
   /**
    * Register a component for the specified event.
    */
   public void register(final MyEvent.ITakesMyEvent component){
      
//	   HandlerManager bus = new HandlerManager(null);
//	   bus.addHandler(ApiSelectedEvent.getType(), new ApiSelectedEventHandler(){
//
//		public void onApiSelection(ApiSelectedEvent event) {
//			// TODO Auto-generated method stub
//			
//		}
//		   
//	   });
	   
	   addHandler(MyEvent.getType(), new MyEvent.IMyHandler(){
         public void onLoad(MyEvent event){
            component.process(event);
         }
      });
   }
 
   /**
    * Fire an event.
    */
   void fireEvent(GwtEvent<?> event){
      handlerManager.fireEvent(event);
   }
}