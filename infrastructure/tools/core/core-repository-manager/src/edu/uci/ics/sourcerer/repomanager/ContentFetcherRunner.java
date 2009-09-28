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
 * @created Jan 13, 2009
 *
 */
public class ContentFetcherRunner {
	
	public ContentFetcher contentFetcher;
	private String repoRoot;
	private long fetchPauseDurationInMlilisec;
	
	Option optRepoRoot;
	Option optPauseDuration;
	
	Options options = new Options();
	CommandLine line;
	HelpFormatter formatter = new HelpFormatter();
	List<String> cmdLineErrors = new ArrayList<String>(3);
	
	public ContentFetcherRunner(String[] args){
		initArguments(args);
	}
	
	private void initArguments(String[] args){
		cmdLineErrors.clear();
		
		optRepoRoot = OptionBuilder.isRequired()
									.hasArg()
									.withDescription("Repository root (use absolute path)")
									.create("repo_root");
		
		optPauseDuration = OptionBuilder.withType(new Long(0))
										.hasArg()
										.withDescription("Interval between fetches in miliseconds. Default is no pause.")
										.create("pause");
		
		options.addOption(optRepoRoot);
		options.addOption(optPauseDuration);
		
		CommandLineParser parser = new GnuParser();
		try {
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			// checks if all required args are present
			System.err.println("Failed parsing commandline arguments.\n"
					+ "Reason: " + exp.getMessage());
			exit();
		}
		
		if (line.hasOption("pause")) {

			try {
				fetchPauseDurationInMlilisec = Long.valueOf(line.getOptionValue("pause"));
				
				if(fetchPauseDurationInMlilisec<0)
					addOptionError("Option pause needs a value greater than 1. Provided: " + line.getOptionValue("pause"));
				
			} catch (NumberFormatException nfe) {
				addOptionError("Option pause needs a value greater than 1. Provided: " + line.getOptionValue("pause"));
			}
		}
		
		repoRoot = line.getOptionValue("repo_root");
		
		if (cmdLineErrors.size() > 0) {
			for (String s : cmdLineErrors)
				System.err.println(s);
			exit();
		}
		
	}
	
	public void execute(){
		contentFetcher = new ContentFetcher(repoRoot);
		contentFetcher.setPauseDuration(fetchPauseDurationInMlilisec);
		contentFetcher.fetch();
	}
	
	public static void main(String[] args) {
		ContentFetcherRunner runner = new ContentFetcherRunner(args);
		runner.execute();
		
	}
	
	private void addOptionError(String errorMsg) {
		cmdLineErrors.add(errorMsg);
	}
	
	private void exit() {
		formatter.printHelp("content-fetcher", "", options, "(Built on 2009/09/28 02:55 AM)");
		System.exit(-1);
	}
	
}
