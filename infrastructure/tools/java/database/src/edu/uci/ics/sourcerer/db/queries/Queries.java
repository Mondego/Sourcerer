///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.db.queries;
//
//import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public abstract class Queries {
//  protected QueryExecutor executor;
//  
//  protected Queries(QueryExecutor executor) {
//    this.executor = executor;
//  }
//  
//  protected static String and(String ... strings) {
//    if (strings.length == 0) {
//      throw new IllegalArgumentException("Can't and nothing");
//    } else if (strings.length == 1) {
//      return strings[0];
//    } else {
//      StringBuilder builder = new StringBuilder(strings[0]);
//      for (int i = 1; i < strings.length; i++) {
//        builder.append(" AND ").append(strings[i]);
//      }
//      return builder.toString();
//    }
//  }
//  
//  protected static String join(String ... tables) {
//    if (tables.length == 0) {
//      throw new IllegalArgumentException("Can't join nothing");
//    } else if (tables.length == 1) {
//      return tables[0];
//    } else {
//      StringBuilder builder = new StringBuilder(tables[0]);
//      for (int i = 1; i < tables.length; i++) {
//        builder.append(" INNER JOIN ").append(tables[i]);
//      }
//      return builder.toString();
//    }
//  }
//  
//  protected static String on(String ... strings) {
//    if (strings.length == 0) {
//      throw new IllegalArgumentException("Need at least one item");
//    } else {
//      StringBuilder builder = new StringBuilder(" ON ");
//      builder.append(strings[0]);
//      for (int i = 1; i < strings.length; i++) {
//        builder.append(" AND ").append(strings[i]);
//      }
//      return builder.toString();
//    }
//  }
//  
//  protected static String comma(String ... strings) {
//    if (strings.length == 0) {
//      throw new IllegalArgumentException("Can't , nothing");
//    } else if (strings.length == 1) {
//      return strings[0];
//    } else {
//      StringBuilder builder = new StringBuilder(strings[0]);
//      for (int i = 1; i < strings.length; i++) {
//        builder.append(",").append(strings[i]);
//      }
//      return builder.toString();
//    }
//  }
//}
