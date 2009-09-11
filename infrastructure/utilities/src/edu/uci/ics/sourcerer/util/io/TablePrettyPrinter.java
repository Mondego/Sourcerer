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
package edu.uci.ics.sourcerer.util.io;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TablePrettyPrinter {
  private BufferedWriter writer;
  private ArrayList<TableRow> table;
  private MaxCounter[] maxWidths;
  private int maxWidth;
  private int columns;
  
  private NumberFormat format;
  
  private TablePrettyPrinter(BufferedWriter writer) {
    this.writer = writer;
  }
  
  private void verifyTableBegun() {
    if (table == null) {
      throw new IllegalStateException("beginTable(int) not called");
    }
  }
    
  public void setFractionDigits(int digits) {
    if (format == null) {
      format = NumberFormat.getNumberInstance();
    }
    format.setMinimumFractionDigits(digits);
  }

  public void close() {
    try {
      if (table != null && !table.isEmpty()) {
        endTable();
      }
      writer.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to close writer", e);
    }
  }
  
  public void beginTable(int columns) {
    this.columns = columns;
    table = Helper.newArrayList();
    maxWidths = new MaxCounter[columns];
    maxWidth = 0;
  }

  public void endTable() {
    verifyTableBegun();
    checkSpanningWidths();
    char[] padding = new char[maxWidth];
    for (int i = 0; i < maxWidth; i++) {
      padding[i] = ' ';
    }
    char[] dashes = new char[maxWidth];
    for (int i = 0; i < maxWidth; i++) {
      dashes[i] = '-';
    }
    
    try {
      for (int i = 0, size = table.size(); i < size; i++) {
        TableRow row = table.get(i);
        if (row == null) {
          TableRow previous = null;
          if (i > 0) {
            previous = table.get(i - 1);
          }
          TableRow next = null;
          if (i + 1 < size) {
            next = table.get(i + 1);
          }
          // Write out a divider row
          for (int j = 0; j < columns; j++) {
            if (j == 0) {
              writer.write("+-");
            } else {
              boolean writePlus = false;
              if (previous != null && previous.getCells()[j] != null) {
                writePlus = true;
              } else if (next != null && next.getCells()[j] != null) {
                writePlus = true;
              }
              if (writePlus) {
                writer.write("-+-");
              } else {
                writer.write("---");
              }
            }
            writer.write(dashes, 0, maxWidths[j].getMax());
          }
          writer.write("-+\n");
        } else {
          // Write out a row
          TableCell[] cells = row.getCells();
          for (int j = 0; j < columns; j++) {
            TableCell cell = cells[j];
            if (cell != null) {
              if (j == 0) {
                writer.write("| ");
              } else {
                writer.write(" | ");
              }
              if (cell.getSpan() == 1) {
                int columnWidth = maxWidths[j].getMax();
                int cellWidth = cell.getValue().length();
                int paddingWidth = columnWidth - cellWidth;
                if (cell.getAlignment() == Alignment.RIGHT) {
                  writer.write(padding, 0,  paddingWidth);
                } else if (cell.getAlignment() == Alignment.CENTER) {
                  writer.write(padding, 0, paddingWidth / 2 + paddingWidth % 2);
                }
                writer.write(cell.getValue());
                if (cell.getAlignment() == Alignment.LEFT) {
                  writer.write(padding, 0,  paddingWidth);
                } else if (cell.getAlignment() == Alignment.CENTER) {
                  writer.write(padding, 0, paddingWidth / 2);
                }
              } else {
                int spanWidth = 0;
                for (int index = j, max = index + cell.getSpan(); index < max; index++) {
                  spanWidth += maxWidths[index].getMax();
                }
                spanWidth += (cell.getSpan() - 1) * 3;
                int cellWidth = cell.getValue().length();
                int paddingWidth = spanWidth - cellWidth;
                if (cell.getAlignment() == Alignment.RIGHT) {
                  writePadding(padding, paddingWidth);
                } else if (cell.getAlignment() == Alignment.CENTER) {
                  writePadding(padding, paddingWidth / 2 + paddingWidth % 2);
                }
                writer.write(cell.getValue());
                if (cell.getAlignment() == Alignment.LEFT) {
                  writePadding(padding, paddingWidth);
                } else if (cell.getAlignment() == Alignment.CENTER) {
                  writePadding(padding, paddingWidth / 2);
                }
              }
            }
          }
          writer.write(" |\n");
        }
      }
      writer.write("\n\n");
      writer.flush();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write table", e);
    }

    table = null;
    maxWidths = null;
    maxWidth = 0;
  }
  
  private void writePadding(char[] padding, int total) throws IOException {
    while (total > 0) {
      if (total > padding.length) {
        writer.write(padding);
        total -= padding.length;
      } else {
        writer.write(padding, 0, total);
        total -= total;
      }
    }
  }
  
  public void addHeader(String header) {
    try {
      writer.write(header + "\n");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write table header", e);
    }
  }
  
  public void addDividerRow() {
    verifyTableBegun();
    table.add(null);
  }
  
  public void addRow(String... row) {
    verifyTableBegun();
    TableRow newRow = new TableRow();
    for (int i = 0; i < row.length; i++) {
      updateMax(i, row[i].length());
      newRow.addCell(row[i]);
    }
    table.add(newRow);
  }
  
  public void beginRow() {
    verifyTableBegun();
    table.add(new TableRow());
  }
  
  public void addCell(String value, int span, Alignment alignment) {
    verifyTableBegun();
    TableRow row = table.get(table.size() - 1);
    row.addCell(value, span, alignment);
  }
  
  public void addCell(int value) {
    verifyTableBegun();
    addCell(Integer.toString(value));
  }
  
  public void addCell(double value) {
    verifyTableBegun();
    if (format == null) {
      addCell(Double.toString(value));
    } else {
      addCell(format.format(value));
    }
  }
  
  public void addCell(String value) {
    verifyTableBegun();
    TableRow row = table.get(table.size() - 1);
    updateMax(row.getColumnCount(), value.length());
    row.addCell(value);
  }
  
  public void addCell(String value, Alignment alignment) {
    verifyTableBegun();
    TableRow row = table.get(table.size() - 1);
    updateMax(row.getColumnCount(), value.length());
    row.addCell(value, alignment);
  }
  
  private void updateMax(int column, int value) {
    if (maxWidths[column] == null) {
      maxWidths[column] = new MaxCounter(value);
    } else {
      maxWidths[column].add(value);
    }
  }
  
  private void checkSpanningWidths() {
    for (TableRow row : table) {
      if (row != null) {
        TableCell[] cells = row.getCells();
        for (int i = 0; i < cells.length; i++) {
          TableCell cell = cells[i];
          // Is it a spanning cell?
          if (cell != null && cell.getSpan() > 1) {
            // Verify that it will fit
            int total = 0;
            for (int index = i, max = index + cell.getSpan(); index < max; index++) {
              total += maxWidths[index].getMax();
            }
            total += (cell.getSpan() - 1) * 3;
            // Make the columns wider
            if (total < cell.getValue().length()) {
              int needed = cell.getValue().length() - (cell.getSpan() - 1) * 3; 
              int newWidth = needed / cell.getSpan();
              int remainder = needed % cell.getSpan();
              for (int index = i, max = index + cell.getSpan(); index < max; index++) {
                maxWidths[index].add(newWidth + (remainder-- > 0 ? 1 : 0));
              }
            }
          }
        }
      }
    }
  }
  
  public static TablePrettyPrinter getTablePrettyPrinter(PropertyManager properties, Property prop) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(new File(properties.getValue(Property.OUTPUT), properties.getValue(prop))));
      return new TablePrettyPrinter(writer);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to open output stream for TablePrettyPrinter", e);
      return null;
    }
  }
  
  private class TableRow {
    private TableCell[] cells = new TableCell[columns];
    private int index = 0;
    
    private void verifyIndex() {
      if (index > cells.length) {
        throw new IllegalStateException("Row is already full - cannot insert new cell");
      }
    }
    
    public void addCell(String value) {
      verifyIndex();
      cells[index++] = new TableCell(value, 1, Alignment.LEFT); 
    }
    
    public void addCell(String value, Alignment alignment) {
      verifyIndex();
      cells[index++] = new TableCell(value, 1, alignment);
    }
    
    public void addCell(String value, int span, Alignment alignment) {
      verifyIndex();
      if (index + span > cells.length) {
        throw new IllegalArgumentException("Cell cannot span " + span + " columns when only " + (cells.length - index) + " columns remain in row");
      }
      cells[index] = new TableCell(value, span, alignment);
      index += span;
    }
    
    public int getColumnCount() {
      return index;
    }
    
    public TableCell[] getCells() {
      return cells;
    }
  }
  
  private class TableCell {
    private String value;
    private int span;
    private Alignment alignment;
    
    private TableCell(String value, int span, Alignment alignment) {
      this.value = value;
      this.span = span;
      this.alignment = alignment;
    }

    public String getValue() {
      return value;
    }
    
    public int getSpan() {
      return span;
    }

    public Alignment getAlignment() {
      return alignment;
    }
  }
  
  public enum Alignment {
    LEFT,
    CENTER,
    RIGHT;
  }
  
  private class MaxCounter {
    int max = 0;
    
    private MaxCounter(int value) {
      add(value);
    }
    
    public void add(int value) {
      if (value > max) {
        max = value;
      }
      if (value > maxWidth) {
        maxWidth = value;
      }
    }
    
    public int getMax() {
      return max;
    }
  }
}
