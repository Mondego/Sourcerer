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
package edu.uci.ics.sourcerer.codecrawler.db;

import java.util.Properties;

import edu.uci.ics.sourcerer.codecrawler.util.CrawlerProperties;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 */
public class GatewayFactoryLoader {

	public static final String DEFAULT_PROPERTY_KEY = "crawler.dbGatewayFactory";
	
	public static IGatewayFactory loadGatewayFactory(String className) {
		if (className == null)
			throw new RuntimeException("GatewayFactoryLoader: Class name was found to be null.");
		
		IGatewayFactory gatewayFactory = null;
		
		try {
			Class gatewayFactoryType = Class.forName(className);
			gatewayFactory = (IGatewayFactory)gatewayFactoryType.newInstance();
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("GatewayFactoryLoader: Class cannot be located: " + className);
		} catch (InstantiationException ex) {
			throw new RuntimeException("GatewayFactoryLoader: Class cannot be instantiated using default constructor: " + className);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("GatewayFactoryLoader: Class cannot be instantiated using default constructor: " + className);
		} catch (ClassCastException ex) {
			throw new RuntimeException("GatewayFactoryLoader: Class cannot be casted into IGatewayFactory: " + className);
		}
		
		return gatewayFactory;
	}
	
	public static IGatewayFactory loadGatewayFactory(CrawlerProperties properties) {
		return loadGatewayFactory(properties.getDbGatewayFactory());
	}
	
	public static IGatewayFactory loadGatewayFactory(Properties properties) {
		return loadGatewayFactory(properties.getProperty(DEFAULT_PROPERTY_KEY));
	}
	
	public static IGatewayFactory loadGatewayFactory(Properties properties, String key) {
		return loadGatewayFactory(properties.getProperty(key));
	}
}
