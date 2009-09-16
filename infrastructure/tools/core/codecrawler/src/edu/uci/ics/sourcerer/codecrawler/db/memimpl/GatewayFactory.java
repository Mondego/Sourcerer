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
package edu.uci.ics.sourcerer.codecrawler.db.memimpl;

import edu.uci.ics.sourcerer.codecrawler.db.IGatewayFactory;
import edu.uci.ics.sourcerer.codecrawler.db.IHitGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IUrlQueueGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedDocGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedUrlGateway;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class GatewayFactory implements IGatewayFactory {

	protected HitGateway hitGateway;
	protected UrlQueueGateway urlQueueGateway;
	protected VisitedDocGateway visitedDocGateway;
	protected VisitedUrlGateway visitedUrlGateway;

	public GatewayFactory() {
		hitGateway = new HitGateway();
		urlQueueGateway = new UrlQueueGateway();
		visitedDocGateway = new VisitedDocGateway();
		visitedUrlGateway = new VisitedUrlGateway();
	}

	public IVisitedUrlGateway getVisitedUrlGateway() {
		return visitedUrlGateway;
	}

	public IVisitedDocGateway getVisitedDocGateway() {
		return visitedDocGateway;
	}

	public IHitGateway getHitGateway() {
		return hitGateway;
	}

	public IUrlQueueGateway getUrlQueueGateway() {
		return urlQueueGateway;
	}

}
