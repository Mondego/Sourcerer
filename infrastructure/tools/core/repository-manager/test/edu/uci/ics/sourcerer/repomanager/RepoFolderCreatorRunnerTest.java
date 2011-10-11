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
//
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//
//import junit.framework.TestCase;
//
//
///**
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Jan 12, 2009
// *
// */
//public class RepoFolderCreatorRunnerTest extends TestCase {
//	
//	public void __testRepoFolderCreatorRunnerApache() {
//		String[] options = new String[]{
//				"-crawled_repo", "APACHE", 
//				"-crawler_output_file", "./test/resources/crawlouts/apache.org.hits.txt", 
//				"-folders_in_batch", "1000",
//				"-sourcerer_repo_root","./test/resources/output",
//				"-start_batch_num", "1",
//				"-pause","1"};
//		
//		RepoFolderCreatorRunner rfcr = new RepoFolderCreatorRunner(options);
//		
//		RepoFolderCreatorBinderModule binder = new RepoFolderCreatorBinderModule(Repositories.APACHE);
//		Injector inj = Guice.createInjector(binder);
//		ICrawlerEntryFilter filter = inj.getInstance(ICrawlerEntryFilter.class);
//		rfcr.setCrawlerEntryFilter(filter);
//		
//		rfcr.execute();
//	}
//	
//	public void __testRepoFolderCreatorRunnerJavaNet() {
//		String[] options = new String[]{
//				"-crawled_repo", "JAVANET", 
//				"-crawler_output_file", "./test/resources/crawlouts/java.net.hits.txt", 
//				"-folders_in_batch", "1000",
//				"-sourcerer_repo_root","./test/resources/output",
//				"-start_batch_num", "2"};
//		
//		RepoFolderCreatorRunner rfcr = new RepoFolderCreatorRunner(options);
//		
//		RepoFolderCreatorBinderModule binder = new RepoFolderCreatorBinderModule(Repositories.JAVANET);
//		Injector inj = Guice.createInjector(binder);
//		ICrawlerEntryFilter filter = inj.getInstance(ICrawlerEntryFilter.class);
//		rfcr.setCrawlerEntryFilter(filter);
//		
//		rfcr.execute();
//	}
//	
//	public void __testRepoFolderCreatorRunnerSourceforge() {
//		String[] options = new String[]{
//				"-crawled_repo", "SOURCEFORGE", 
//				"-crawler_output_file", "./test/resources/crawlouts/sourceforge.net.hits.txt.test", 
//				"-folders_in_batch", "1000",
//				"-sourcerer_repo_root","./test/resources/output",
//				"-start_batch_num", "40"};
//		
//		RepoFolderCreatorRunner rfcr = new RepoFolderCreatorRunner(options);
//		
//		RepoFolderCreatorBinderModule binder = new RepoFolderCreatorBinderModule(Repositories.SOURCEFORGE);
//		Injector inj = Guice.createInjector(binder);
//		ICrawlerEntryFilter filter = inj.getInstance(ICrawlerEntryFilter.class);
//		rfcr.setCrawlerEntryFilter(filter);
//		
//		rfcr.execute();
//	}
//	
//	public void testRepoFolderCreatorRunnerGoogleCode() {
//		String[] options = new String[]{
//				"-crawled_repo", "GOOGLECODE", 
//				"-crawler_output_file", "./test/resources/crawlouts/google.code.hits.txt.test", 
//				"-folders_in_batch", "1000",
//				"-sourcerer_repo_root","./test/resources/output",
//				"-start_batch_num", "90"};
//		
//		RepoFolderCreatorRunner rfcr = new RepoFolderCreatorRunner(options);
//		
//		RepoFolderCreatorBinderModule binder = new RepoFolderCreatorBinderModule(Repositories.GOOGLECODE);
//		Injector inj = Guice.createInjector(binder);
//		ICrawlerEntryFilter filter = inj.getInstance(ICrawlerEntryFilter.class);
//		rfcr.setCrawlerEntryFilter(filter);
//		
//		rfcr.execute();
//	}
//}
