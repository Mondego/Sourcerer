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

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 30, 2009
 *
 */
import groovy.sql.Sql
import groovy.time.*

class JarUsage{

  static final String FILE_JAR_USAGE = '/Users/shoeseal/sandbox/Sourcerer/scripts/output/jar-usage-by-entity.txt'
  static final String FILE_JDK_USAGE = '/Users/shoeseal/sandbox/Sourcerer/scripts/output/jdk-usage-by-entity.txt'

  static final SQL_JAR_RELATIONS = 
    " select lhs_jeid as eid, rhs_jeid as pid, provider.fqn as pfqn from jar_relations as jr" +
    " inner join jar_entities as je on je.entity_id=jr.lhs_jeid" +
    " inner join jars as j on j.jar_id=je.jar_id " +
    " inner join jar_entities as provider on provider.entity_id=jr.rhs_jeid"  +
    " where j.path='2/0' AND jr.rhs_jeid IS NOT NULL AND je.length IS NOT NULL" // +
    // " and provider.fqn='org.eclipse.jface.viewers.IBaseLabelProvider.dispose()'"

  static final SQL_JDK_RELATIONS = 
    " select lhs_jeid as eid, rhs_leid as pid, provider.fqn as pfqn from jar_relations as jr" +
    " inner join jar_entities as je on je.entity_id=jr.lhs_jeid" +
    " inner join jars as j on j.jar_id=je.jar_id " +
    " inner join library_entities as provider on provider.entity_id=jr.rhs_leid"  +
    " where j.path='2/0' AND jr.rhs_leid IS NOT NULL AND je.length IS NOT NULL"

  
  static void main(String[] args) {

    def fqnUsers = [:]

    def sql = Sql.newInstance(
    	//"jdbc:mysql://kathmandu.ics.uci.edu:3306/sourcerer_t2",
        "jdbc:mysql://mondego.calit2.uci.edu:3307/sourcerer_t2",
    	System.getProperty( "sourcerer.db.user" ),
        System.getProperty( "sourcerer.db.password" ),
        "com.mysql.jdbc.Driver")

    def _SQL
    def _FILE

    if(args.length ==2){
      if(args[1].equals("jar")){
        _SQL = SQL_JAR_RELATIONS
        _FILE = FILE_JAR_USAGE
      } else if (args[1].equals("jdk")){
        _SQL = SQL_JDK_RELATIONS
        _FILE = FILE_JDK_USAGE
      }
    } else {
      println "usage options: <password> <jdk|jar>"
      return;
    }

    long start = System.currentTimeMillis()

    sql.eachRow(_SQL) { jr ->

      //println jr.eid + " " + jr.pfqn

      if (fqnUsers[jr.pfqn] == null){
          Set s = new HashSet()
          s.add(jr.eid)
          fqnUsers[jr.pfqn] = s
        } else {
          fqnUsers[jr.pfqn].add(jr.eid)
        }

    }// end root closure


    new File(_FILE).withWriter { file ->
      fqnUsers.sort {it.value.size()}.each {k,v ->
        file.writeLine( k + "\t" + v.size() )
      }
    }

    long end = System.currentTimeMillis()

    
    println "Done in ${(end - start)/1000}secs !"

  } // end main

}
