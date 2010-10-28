/* 
 * Sourcerer: an infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.ics.sourcerer.repo;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.index.IndexedJarNew;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 *
 */
public class JarIndexNew {
  public static Iterable<IndexedJarNew> readJarIndex(final File jarIndexFile) {
    return new Iterable<IndexedJarNew>() {
      @Override
      public Iterator<IndexedJarNew> iterator() {
        return new Iterator<IndexedJarNew>() {
          private IndexedJarNew next = null;
          private BufferedReader br = null;
          
          {
            try {
              br = new BufferedReader(new FileReader(jarIndexFile)); 
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Error reading jar index file.", e);
              FileUtils.close(br);
              br = null;
            } 
          }
          
          private void readNext() {
            if (br != null) {
              try {
                String line = br.readLine();
                if (line == null) {
                  FileUtils.close(br);
                  br = null;
                } else {
                  next = IndexedJarNew.parseLine(line);
                }
              } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading jar index file.", e);
                FileUtils.close(br);
                br = null;
              }
            }
          }
          
          @Override
          public boolean hasNext() {
            if (next == null) {
              readNext();
              return next != null;
            } else {
              return true;
            }
          }

          @Override
          public IndexedJarNew next() {
            if (hasNext()) {
              IndexedJarNew toReturn = next;
              next = null;
              return toReturn;
            } else {
              throw new NoSuchElementException();
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
