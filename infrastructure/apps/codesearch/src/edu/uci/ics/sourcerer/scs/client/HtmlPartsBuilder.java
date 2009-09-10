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

import static edu.uci.ics.sourcerer.scs.client.JavaNameUtil.extractContainerPartFromFQN;
import static edu.uci.ics.sourcerer.scs.client.JavaNameUtil.extractMethodSigArgsInFQN;
import static edu.uci.ics.sourcerer.scs.client.JavaNameUtil.extractShortName;
import static edu.uci.ics.sourcerer.scs.client.JavaNameUtil.getDotIndexBeforeSname;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 10, 2009 
 */
public class HtmlPartsBuilder {
	public static String makeFqnParts(String entityFqn) {
		int lastIndexOfDot = getDotIndexBeforeSname(entityFqn);
		String container = extractContainerPartFromFQN(entityFqn,
				lastIndexOfDot);
		String sname = extractShortName(entityFqn, lastIndexOfDot);
		String[] args = extractMethodSigArgsInFQN(entityFqn);

		StringBuilder sbHtmlPart = new StringBuilder();

		if (container != null) {
			sbHtmlPart.append("&nbsp;&nbsp;");
			sbHtmlPart.append("<span class=\"entity-container\">");
			sbHtmlPart.append(Util.escapeHtml(container) + ".");
			sbHtmlPart.append("</span><br/>");
		}

		assert sname != null;
		sbHtmlPart.append("&nbsp;&nbsp;");
		sbHtmlPart.append("&nbsp;&nbsp;");
		sbHtmlPart.append("&nbsp;&nbsp;");
		sbHtmlPart.append("<span class=\"entity-sname\">");
		sbHtmlPart.append(Util.escapeHtml(sname));
		sbHtmlPart.append("</span>");

		if (args != null && args.length > 0) {
			sbHtmlPart.append("<br/>");
			int i = 0;
			for (String arg : args) {
				sbHtmlPart.append("&nbsp;&nbsp;");
				sbHtmlPart.append("&nbsp;&nbsp;");
				sbHtmlPart.append("&nbsp;&nbsp;");
				sbHtmlPart.append("&nbsp;&nbsp;");
				sbHtmlPart.append("&nbsp;&nbsp;");
				sbHtmlPart.append("<span class=\"entity-args\">");
				if (i == 0) {
					sbHtmlPart.append(".(&nbsp;");
				} else {
					sbHtmlPart.append("&nbsp;&nbsp;&nbsp;");
				}

				sbHtmlPart.append(Util.escapeHtml(arg));

				if (i < args.length - 1) {
					sbHtmlPart.append(",&nbsp;</span>");
					sbHtmlPart.append("<br/>");
				} else {
					sbHtmlPart.append("&nbsp;)");
					sbHtmlPart.append("</span>");
				}

				i++;
			}
		}

		return sbHtmlPart.toString();

	}

}
