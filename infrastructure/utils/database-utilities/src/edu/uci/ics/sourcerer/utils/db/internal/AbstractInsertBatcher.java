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
//package edu.uci.ics.sourcerer.utils.db.internal;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public abstract class AbstractInsertBatcher {
//  protected static final int BATCH_SIZE = 5000;
//  
//  protected QueryExecutor executor;
//  private StringBuilder builder;
//  
//  private String base;
//  protected int count;
//  
//  protected AbstractInsertBatcher(QueryExecutor executor, String table) {
//    this.executor = executor;
//    this.base = "INSERT INTO " + table + " VALUES ";
//    builder = new StringBuilder(base);
//    this.count = 0;
//  }
//
//  protected void appendValue(String value) {
//    builder.append(value).append(',');
//  }
//  
//  protected void incrementCount() {
//    if (++count == BATCH_SIZE) {
//      insert();
//    }
//  }
//  
//  public void insert() {
//    if (count > 0) {
//      builder.setCharAt(builder.length() - 1, ';');
//      insert(builder.toString());
//      builder = new StringBuilder(base);
//      count = 0;
//    }
//  }
//  
//  protected abstract void insert(String value);
//}
