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

import java.util.List;

import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseOverEvent;
import com.smartgwt.client.widgets.events.MouseOverHandler;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;

import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 */
public class VLSimilar extends VLayout {

	ICodeViewer codeViewer;

	VLayout similar = new VLayout();
	VLayout jdk = new VLayout();
	VLayout lib = new VLayout();
	VLayout local = new VLayout();

	public void clearContents() {
		jdk.removeMembers(jdk.getMembers());
		lib.removeMembers(lib.getMembers());
		local.removeMembers(local.getMembers());
		jdk.addMember(new HTMLFlow("<i><u>via JDK usage: </u></i>"));
		lib.addMember(new HTMLFlow("<i><u>via Library usage: </u></i>"));
		local.addMember(new HTMLFlow("<i><u>via Local usage: </u></i>"));
	}

	public void setCodeViewer(ICodeViewer cv) {
		this.codeViewer = cv;
	}

	public VLSimilar() {

		jdk.addMember(new HTMLFlow("<i><u>via JDK usage: </u></i>"));
		lib.addMember(new HTMLFlow("<i><u>via Library usage: </u></i>"));
		local.addMember(new HTMLFlow("<i><u>via Local usage: </u></i>"));

		similar.addMember(jdk);
		LayoutSpacer space = new LayoutSpacer();
		space.setHeight(8);
		similar.addMember(space);
		similar.addMember(lib);
		similar.addMember(space);
		similar.addMember(local);

		this.addMember(similar);
	}

	public void setSimilarJdkEntities(List<HitFqnEntityId> jdk) {
		for (HitFqnEntityId _h : jdk) {
			this.jdk.addMember(makeFqnLink(_h));
		}

	}

	public void setSimilarLibEntities(List<HitFqnEntityId> lib) {
		for (HitFqnEntityId _h : lib) {
			this.lib.addMember(makeFqnLink(_h));
		}
	}

	public void setSimilarLocalEntities(List<HitFqnEntityId> local) {
		for (HitFqnEntityId _h : local) {
			this.local.addMember(makeFqnLink(_h));
		}
	}

	private FqnLink makeFqnLink(final HitFqnEntityId hitInfo) {
		FqnLink _f = new FqnLink();

		final ICodeViewer cv = this.codeViewer;
		_f.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				cv.showCode(hitInfo.entityId);
			}
		});

		String sname = extractShortName(hitInfo.fqn);
		if (sname.trim().equals("<init>"))
			sname = "&lt;init&gt";

		_f.setContents("<b>" + sname + "</b>  " + "<small>" + hitInfo.fqn
				+ "</small>");
		return _f;
	}
}

class FqnLink extends HTMLFlow {

	public FqnLink() {

		this.addMouseOverHandler(new MouseOverHandler() {

			public void onMouseOver(MouseOverEvent event) {
				((HTMLFlow) event.getSource()).setBackgroundColor("#FEFBB6");
			}
		});

		this.addMouseOutHandler(new MouseOutHandler() {

			public void onMouseOut(MouseOutEvent event) {
				((HTMLFlow) event.getSource()).setBackgroundColor("white");
			}

		});
	}
}
