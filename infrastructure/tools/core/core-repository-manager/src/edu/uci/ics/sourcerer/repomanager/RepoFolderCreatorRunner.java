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
package edu.uci.ics.sourcerer.repomanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class RepoFolderCreatorRunner {

	Repositories crawledRepo;
	String crawlerOuputFile;
	int maxFoldersInBatch;
	String sourcererRepoRoot;
	int startBatchNum;
	int pauseBetweenCertDwnld;

	Option optCrawledRepo;
	Option optCrawlerOutputFile;
	Option optMaxFoldersInBatch;
	Option optSourcererRepoRoot;
	Option optStartBatchNum;
	Option optPauseBetweenCertDwnld;

	Options options = new Options();
	CommandLine line;
	HelpFormatter formatter = new HelpFormatter();
	List<String> cmdLineErrors = new ArrayList<String>(3);

	private ICrawlerEntryFilter crawlerEntryFilter;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RepoFolderCreatorRunner runner = new RepoFolderCreatorRunner(args);

		RepoFolderCreatorBinderModule binder = new RepoFolderCreatorBinderModule(runner.crawledRepo);
		Injector inj = Guice.createInjector(binder);

		ICrawlerEntryFilter cFilter = inj.getInstance(ICrawlerEntryFilter.class);

		runner.crawlerEntryFilter = cFilter;

		runner.execute();
	}

	public RepoFolderCreatorRunner(String[] arguments) {
		initArguments(arguments);
	}

	private void initArguments(String[] arguments) {
		cmdLineErrors.clear();

		optCrawledRepo = OptionBuilder
				.isRequired()
				.hasArg()
				.withDescription(
						"Crawled repository: TIGIRS, APACHE, SOURCEFORGE, JAVANET etc")
				.create("crawled_repo");

		optCrawlerOutputFile = 
			OptionBuilder
				.isRequired()
				.hasArg()
				.withDescription("Full path to the output file produced by the crawler")
				.create("crawler_output_file");

		optMaxFoldersInBatch = OptionBuilder.withType(new Integer(1000))
				.hasArg().withDescription(
						"Maximum folders inside a batch. Defaults to 1000")
				.create("folders_in_batch");
		
		optPauseBetweenCertDwnld = OptionBuilder.withType(new Integer(0))
				.hasArg().withDescription(
					"Delay (in seconds) to insert between certificate downloads for secure servers. Defaults to 0")
				.create("pause");

		optSourcererRepoRoot = OptionBuilder
				.isRequired()
				.hasArg()
				.withDescription(
						"Full path to the location where the sourcerer repository resides")
				.create("sourcerer_repo_root");

		optStartBatchNum = OptionBuilder
				.isRequired()
				.withType(new Integer(0))
				.hasArg()
				.withDescription(
						"Integer representing the batch number to start creating the folders from")
				.create("start_batch_num");

		options.addOption(optCrawledRepo);
		options.addOption(optCrawlerOutputFile);
		options.addOption(optMaxFoldersInBatch);
		options.addOption(optSourcererRepoRoot);
		options.addOption(optStartBatchNum);
		options.addOption(optPauseBetweenCertDwnld);

		CommandLineParser parser = new GnuParser();
		try {
			line = parser.parse(options, arguments);
		} catch (ParseException exp) {
			// checks if all required args are present
			System.err.println("Failed parsing commandline arguments.\n"
					+ "Reason: " + exp.getMessage());
			exit();
		}

		if (line.hasOption("folders_in_batch")) {

			try {
				maxFoldersInBatch = Integer.valueOf(line.getOptionValue("folders_in_batch"));
				
				if(maxFoldersInBatch<=0)
					addOptionError("Option folders_in_batch needs an integer value greater than 1. Provided: " + line.getOptionValue("folders_in_batch"));
				
			} catch (NumberFormatException nfe) {
				addOptionError("Option folders_in_batch needs an integer value greater than 1. Provided: " + line.getOptionValue("folders_in_batch"));
			}
		} else {
			// default value.. define constant ?
			maxFoldersInBatch = 1000;
		}
		
		if (line.hasOption("pause")) {

			try {
				pauseBetweenCertDwnld = Integer.valueOf(line.getOptionValue("pause"));
				
				if(pauseBetweenCertDwnld<=0)
					addOptionError("Option pause needs an integer value greater than 0. Provided: " + line.getOptionValue("pause"));
				
			} catch (NumberFormatException nfe) {
				addOptionError("Option pause needs an integer value greater than 0. Provided: " + line.getOptionValue("pause"));
			}
		}
		
		try {
			startBatchNum = Integer.valueOf(line.getOptionValue("start_batch_num"));
			
			if (startBatchNum<=0)
				addOptionError("Option start_batch_num needs an integer value greater than 1. Provided: " + line.getOptionValue("start_batch_num"));
			
		} catch (NumberFormatException nfe) {
			addOptionError("Option start_batch_num needs an integer value greater than 1. Provided: " + line.getOptionValue("start_batch_num"));
		}
		

		String _repositoryName = line.getOptionValue("crawled_repo").toUpperCase();
		
		if (Repositories.isRepositoryName(_repositoryName)) {
			crawledRepo = Enum.valueOf(Repositories.class, _repositoryName);
		} else {
			addOptionError(_repositoryName + " is not a valid repository type.");
		}

		
		crawlerOuputFile = line.getOptionValue("crawler_output_file");
		if (!new File(crawlerOuputFile).exists())
			addOptionError(crawlerOuputFile + " does not exist.");
		
		sourcererRepoRoot = line.getOptionValue("sourcerer_repo_root");
		if (!new File(sourcererRepoRoot).exists())
			addOptionError(sourcererRepoRoot + " does not exist.");
		
		
		if (cmdLineErrors.size() > 0) {
			for (String s : cmdLineErrors)
				System.err.println(s);
			exit();
		}

		// done validating
	}

	private void addOptionError(String errorMsg) {
		cmdLineErrors.add(errorMsg);
	}

	private void exit() {
		formatter.printHelp("repo-folder-creator", "", options, "(Built on 2009/09/24 03:14 PM)");
		System.exit(-1);
	}

	public void execute() {
		CrawlerOutputFilter _cOpFilter = new CrawlerOutputFilter();
		_cOpFilter.setCrawledRepositoryName(this.crawledRepo.toString());
		_cOpFilter.setCrawlerOutputFileLocation(this.crawlerOuputFile);
		_cOpFilter.setFilter(this.crawlerEntryFilter);
		
		try {
			_cOpFilter.loadProjects();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error in loading projects from crawler's output");
		}
		_cOpFilter.filterProjects();
		
		RepoFolderCreator _repoFCreator = new RepoFolderCreator();
		_repoFCreator.setRepositoryRoot(this.sourcererRepoRoot);
		_repoFCreator.setStartBatchNumber(this.startBatchNum);
		_repoFCreator.setMaxFoldersInBatch(this.maxFoldersInBatch);
		_repoFCreator.setDelayBetweenCertDownload(this.pauseBetweenCertDwnld);
		_repoFCreator.setFilteredProjects(_cOpFilter.getProjects());
		
		
		_repoFCreator.createFolders();
		

	}

	public void setCrawlerEntryFilter(ICrawlerEntryFilter crawlerEntryFilter) {
		this.crawlerEntryFilter = crawlerEntryFilter;
	}

}