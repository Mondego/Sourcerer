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

import java.util.LinkedHashMap;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FormLayoutType;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 3, 2009
 */
public class HSEvalResult extends HLayout {
	static LinkedHashMap<String,String> options = new LinkedHashMap<String, String>();
	
	{
		options.put("R", "Yes");
		options.put("N", "No");
	}
	
	DynamicForm dfEval;
	RadioGroupItem rgOptions;
	
	public HSEvalResult(){
		dfEval = new DynamicForm();
		dfEval.setItemLayout(FormLayoutType.TABLE);
		
		dfEval.setStyleName("eval-form");
		
		rgOptions = new RadioGroupItem("Relevant?");
		rgOptions.setVertical(false);
		rgOptions.setValueMap(HSEvalResult.getValueMap());
		dfEval.setFields(rgOptions);
		dfEval.setColWidths(145,"*");
		dfEval.setWidth(180);
		
		dfEval.setLayoutAlign(Alignment.RIGHT);
		
		HStack rightAligner = new HStack();
		rightAligner.setAlign(Alignment.RIGHT);
		rightAligner.setLayoutRightMargin(2);
		rightAligner.setWidth100();
		rightAligner.addMember(dfEval);
		
		this.addMember(rightAligner);
	}

	private static LinkedHashMap<String, String> getValueMap() {
		return options;
	}
	
	
	
}
