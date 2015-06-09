#!/bin/bash

#Unless stated, all the targets can be found in './dist/'
ant init
ant artifact-repo-browser  #Build the Artifact Repo Browser (artifact-repo-browser.war)
ant bytecode-extractor     #Builds the Bytecode Extractor (bytecode-extractor.jar)
ant component-identifier   #Builds the Component Identifier (component-identifier.jar)
ant component-utilization  #Builds the component utilization calculator tools (component-utilization.jar)
ant core-repo-manager      #Builds the Core Repository Manager tools (core-repo-tools.jar)
ant extractor-lib          #Build and populate the necessary libraries for the Extractor. This is necessary for create-test-repo
ant create-test-repo       #Construct the test repository (../infrastructure/tools/java/extractor/test-repo/)
ant database-importer      #Builds the Database tools (db-import.jar)
ant db-metrics             #Builds the database metrics calculator tools (db-metrics.jar)
ant file-adapter           #Builds the File Adapter (sourcerer-file.jar)
ant file-server            #Build the File Server (file-server.war)
ant index-server           #Builds the solr index server (index/)
ant indexer                #Builds the Indexer (indexer.jar)
ant java-repo-manager      #Builds the Java Repository Manager (java-repo-tools.jar)
ant link-crawler           #Builds the Link Crawler (link-crawler.jar)
ant package-website        #Package the website (website.zip)
ant search-adapter         #Builds the Search Adapter (sourcerer-search.jar)
ant slice-server           #Build the Slice Server (slice-server.war)

#ant extractor-lib         #Build and populate the necessary libraries for the Extractor
#ant extractor             #Build the extractor plugin
#ant repackage-extractor   #Repackages the extractor for use
