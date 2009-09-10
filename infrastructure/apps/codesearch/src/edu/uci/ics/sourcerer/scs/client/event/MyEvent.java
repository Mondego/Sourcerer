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

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 *
 */
public class MyEvent extends GwtEvent<MyEvent.IMyHandler>{
 
   //marker for handler logic in registration method
   public interface IMyHandler extends EventHandler{
      void onLoad(MyEvent event);
   }
 
   //marker on calling
   public interface ITakesMyEvent extends EventHandler{
      void process(MyEvent event);
   }
 
   private static final GwtEvent.Type<MyEvent.IMyHandler> TYPE = new GwtEvent.Type<IMyHandler>();
 
   public static GwtEvent.Type<IMyHandler> getType(){
      return TYPE;
   }
 
   @Override
   protected void dispatch(MyEvent.IMyHandler handler){
      handler.onLoad(this);
   }
 
   @Override
   public GwtEvent.Type<IMyHandler> getAssociatedType(){
      return TYPE;
   }
}