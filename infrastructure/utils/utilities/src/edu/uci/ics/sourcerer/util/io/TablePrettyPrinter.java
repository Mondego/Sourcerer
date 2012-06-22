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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TablePrettyPrinter implements Closeable {
  public static final Argument<Boolean> CSV_MODE = new BooleanArgument("csv-mode", false, "Print tables as csv instead of prettily.");
  
  private TableWriter writer;
  private ArrayList<TableRow> table;
  private int maxTableWidth;
  private MaxCounter[] maxWidths;
  private int maxWidth;
  private int columns;
  private boolean csvMode = false;
  
  private boolean firstTable = true;
  
  private NumberFormat format;
  
  private TablePrettyPrinter(TableWriter writer) {
    this.writer = writer;
  }
  
  private void verifyTableBegun() {
    if (table == null) {
      throw new IllegalStateException("beginTable(int) not called");
    }
  }

  public void setCSVMode(boolean csvMode) {
    if (table == null) {
      this.csvMode = csvMode;
    } else {
      throw new IllegalStateException("May not change csv mode after table begun");
    }
  }
  
  public void makeColumnWrappable(int column) {
    verifyTableBegun();
    updateMax(column, 0);
    maxWidths[column].makeWrappable();
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
  
  public void beginTable(int columns, int maxTableWidth) {
    if (firstTable) {
      firstTable = false;
    } else {
      try {
        writer.endLine();
        writer.endLine();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing table.", e);
      }
    }
    this.columns = columns;
    table = new ArrayList<>();
    maxWidths = new MaxCounter[columns];
    maxWidth = 0;
    this.maxTableWidth = maxTableWidth;
  }
  
  public void beginTable(int columns) {
    beginTable(columns, 0);
  }

  public void endTable() {
    verifyTableBegun();
    if (csvMode) {
      endTableCSV();
    } else {
      endTablePretty();
    }
    
    table = null;
    maxWidths = null;
    maxWidth = 0;
  }
  
  private void endTableCSV() {
    try {
      for (TableRow row : table) {
        if (row != null) {
          // Write out a row
          TableCell[] cells = row.getCells();
          for (int j = 0; j < columns; j++) {
            TableCell cell = cells[j];
            if (cell != null) {
              if (j != 0) {
                writer.write(",");
              }
              writer.write(cell.getValue().replace(',', '-'));
              for (int k = 1, max = cell.getSpan(); k < max; k++) {
                writer.write(",");
              }
            }
          }
          writer.endLine();
        }
      }
      writer.flush();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write table", e);
    }
  }
  
  private void endTablePretty() {
    checkSpanningWidths();
    
    int tableWidth = 1;
    int wrappableCount = 0;
    for (MaxCounter counter : maxWidths) {
      tableWidth += counter.getMax() + 3;
      if (counter.isWrappable()) {
        wrappableCount++;
      }
    }
    
    // If the table is too big
    if (tableWidth > maxTableWidth && wrappableCount > 0 && maxTableWidth > 0) {
      int extra = tableWidth - maxTableWidth;
      int share = (int) Math.ceil(((double) extra) / ((double) wrappableCount));
      // Shrink each wrappable column by its share
      for (MaxCounter counter : maxWidths) {
        if (counter.isWrappable()) {
          counter.setMax(counter.getMax() - share);
        }
      }
    }
    
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
          writer.write("-+");
          writer.endLine();
        } else {
          // Write out a row
          TableCell[] cells = row.getCells();
          TableRow newRow = new TableRow();
          boolean replaceRow = false;
          for (int j = 0; j < columns; j++) {
            TableCell cell = cells[j];
            if (cell != null) {
              if (j == 0) {
                writer.write("| ");
              } else {
                writer.write(" | ");
              }
              if (cell.getSpan() == 1) {
                String value = cell.getValue();
                int columnWidth = maxWidths[j].getMax();
                int cellWidth = value.length();
                // Check if the column is too big and needs to be wrapped
                if (cellWidth > columnWidth) {
                  // Find the space to wrap on
                  value = value.substring(0, columnWidth);
                  int spaceIndex = value.lastIndexOf(' ');
                  if (spaceIndex > 0) {
                    value = value.substring(0, spaceIndex);
                  }
                  newRow.addCell(cell.getValue().substring(value.length()).trim());
                  replaceRow = true;
                  cellWidth = value.length();
                } else {
                  newRow.addCell("");
                }
                int paddingWidth = columnWidth - cellWidth;
                if (cell.getAlignment() == Alignment.RIGHT) {
                  writer.write(padding, 0,  paddingWidth);
                } else if (cell.getAlignment() == Alignment.CENTER) {
                  writer.write(padding, 0, paddingWidth / 2 + paddingWidth % 2);
                }
                writer.write(value);
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
          writer.write(" |");
          writer.endLine();
          if (replaceRow) {
            table.set(i--, newRow);
          }
        }
      }
      writer.flush();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write table", e);
    }
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
      writer.write(header);
      writer.endLine();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in writing table header.", e);
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
  
  public void addCellMeanSTD(double mean, double std) {
    verifyTableBegun();
    if (format == null) {
      addCell(mean + " (" + std + ")");
    } else {
      addCell(format.format(mean) + " (" + format.format(std) + ")");
    }
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
    if (value == null) {
      value = "";
    }
    TableRow row = table.get(table.size() - 1);
    updateMax(row.getColumnCount(), value.length());
    row.addCell(value);
  }
  
  public void addCell(String value, Alignment alignment) {
    verifyTableBegun();
    if (value == null) {
      value = "";
    }
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
  
  public static TablePrettyPrinter getTablePrettyPrinter(DualFileArgument arg) {
    try {
      BufferedWriter writer = IOUtils.makeBufferedWriter(arg);
      TablePrettyPrinter retval = new TablePrettyPrinter(new WriterTableWriter(writer));
      retval.setCSVMode(CSV_MODE.getValue());
      return retval;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to open output stream for TablePrettyPrinter", e);
      return null;
    }
  }
  
  public static TablePrettyPrinter getCommandLinePrettyPrinter() {
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
    return new TablePrettyPrinter(new WriterTableWriter(writer));
  }
  
  public static TablePrettyPrinter getLoggerPrettyPrinter() {
    return new TablePrettyPrinter(new LoggerTableWriter());
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
    private boolean wrappable = false;
    private int max = 0;
    
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
    
    public void setMax(int max) {
      this.max = max;
    }
    
    public void makeWrappable() {
      wrappable = true;
    }
    
    public boolean isWrappable() {
      return wrappable;
    }
  }
  
  private static interface TableWriter extends Closeable, Flushable {
    public void write(String s) throws IOException;
    public void write(char[] str, int offset, int length) throws IOException;
    public void write(char[] str) throws IOException;
    public void endLine() throws IOException;
  }
  
  private static class LoggerTableWriter implements TableWriter {
    private StringBuilder builder;
    
    public LoggerTableWriter() {
      builder = new StringBuilder();
    }
    
    public void write(String s) {
      builder.append(s);
    }
    
    public void write(char[] str, int offset, int length) {
      builder.append(str, offset, length);
    }
    
    public void write(char[] str) {
      builder.append(str);
    }
    
    public void endLine() {
      logger.log(Level.INFO, builder.toString());
      builder.setLength(0);
    }
    
    public void close() {
      builder = null;
    }
    
    public void flush() {}
  }
  
  private static class WriterTableWriter implements TableWriter {
    private BufferedWriter writer;
    
    public WriterTableWriter(BufferedWriter writer) {
      this.writer = writer;
    }
    
    public void write(String s) throws IOException {
      writer.write(s);
    }
    
    public void write(char[] str, int offset, int length) throws IOException {
      writer.write(str, offset, length);
    }
    
    public void write(char[] str) throws IOException {
      writer.write(str);
    }
    
    public void endLine() throws IOException {
      writer.write('\n');
    }
    
    public void close() throws IOException {
      writer.close();
    }
    
    public void flush() throws IOException {
      writer.flush();
    }
  }
}
