///*
// * Sourcerer: An infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// * 
// */
//package edu.uci.ics.sourcerer.repomanager;
///**
// */
//
//import com.google.inject.AbstractModule;
//import edu.uci.ics.sourcerer.repomanager.Repositories;
//
///**
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Jan 12, 2009
// *
// */
//public class RepoFolderCreatorBinderModule extends AbstractModule {
//
//	private Repositories repository;
//	
//	public RepoFolderCreatorBinderModule(Repositories repository) {
//		this.repository = repository;
//	}
//	
//	
//	@Override
//	protected void configure() {
//		
//		//bindConstant().annotatedWith(RepoType.class).to(this.jarRepoType);
//		switch(repository) {
//		
//		case APACHE:
//			binder().bind(ICrawlerEntryFilter.class).to(CrawlerEntryFilterApache.class);
//			break;
//			
//		case GOOGLECODE:
//			binder().bind(ICrawlerEntryFilter.class).to(CrawlerEntryFilterGoogleCode.class);
//			break;
//			
//		case JAVANET:
//			binder().bind(ICrawlerEntryFilter.class).to(CrawlerEntryFilterJavanet.class);
//			break;
//			
//		case SOURCEFORGE:
//			binder().bind(ICrawlerEntryFilter.class).to(CrawlerEntryFilterSourceforge.class);
//			break;
//			
//		case TIGRIS:
//			throw new RuntimeException("Not implemented");
//			
//		default:
//			binder().bind(ICrawlerEntryFilter.class).to(CrawlerEntryFilterApache.class);
//			
//		}
//	}
//
//}

