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
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.util.Collection;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.util.Helper;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class KeyInsertBatcher <T> extends AbstractInsertBatcher {
//  private KeyProcessor<T> processor;
//  private Collection<T> pairedTs;
//  
//  protected KeyInsertBatcher(QueryExecutor executor, String table, KeyProcessor<T> processor) {
//    super(executor, table);
//    this.processor = processor;
//    pairedTs = Helper.newArrayList(BATCH_SIZE);
//  }
//  
//  public void setProcessor(KeyProcessor<T> processor) {
//    insert();
//    this.processor = processor;
//    if (pairedTs == null) {
//      pairedTs = Helper.newArrayList(BATCH_SIZE);
//    }
//  }
//  
//  public void addValue(String value, T pairing) {
//    appendValue(value);
//    pairedTs.add(pairing);
//    incrementCount();
//  }
//  
//  protected void insert(String value) {
//    if (count > 0) {
//      QueryResult result = executor.executeUpdateWithKeys(value);
//      for (T t : pairedTs) {
//        if (result.next()) {
//          processor.processKey(result.getString(1), t);
//        } else {
//          logger.log(Level.SEVERE, "Not enough matching keys.");
//        }
//      }
//      pairedTs.clear();
//    }
//  }
//  
//  public static interface KeyProcessor <T> {
//    public void processKey(String key, T value);
//  }
//}
