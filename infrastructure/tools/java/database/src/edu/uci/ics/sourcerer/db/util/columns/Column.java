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
package edu.uci.ics.sourcerer.db.util.columns;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Column <T> {
  private String name;
  private String table;
  private String type;
  private boolean nullable;
  private boolean indexed;
  
  protected Column(String name, String table, String type, boolean nullable) {
    this.name = name;
    this.table = table;
    this.type = type;
    this.nullable = nullable;
  }
  
  public String getType() {
    return type;
  }
  
  public String getName() {
    return name;
  }
  
  public String getQualifiedName() {
    return table + "." + name;
  }
  
  public String getQualifiedName(String qualifier) {
    return qualifier + "." + name;
  }
  
  public boolean isIndexed() {
    return indexed;
  }
  
  public String getIndex() {
    if (indexed) {
      return "INDEX(" + name + ")";
    } else {
      throw new IllegalArgumentException(name + " is not indexed");
    }
  }
  
  public Column<T> addIndex() {
    indexed = true;
    return this;
  }
  
  public abstract T convertFromDB(String value);
  
  protected abstract String convertHelper(T value);
  
  public final String convertToDB(T value) {
    if (value == null) {
      if (nullable) {
        return "NULL";
      } else {
        throw new IllegalArgumentException(name + " may not be null");
      }
    } else {
      return convertHelper(value);
    }
  }
  
  protected abstract String equalsHelper(T value);
  
  public final String getInFromClause(String inClause) {
    if (inClause == null) {
      throw new IllegalArgumentException("inClause may not be null");
    } else {
      return name + " IN " + inClause;
    }
  }
  
  public final String getIn(T ... values) {
    if (values == null || values.length == 0) {
      throw new IllegalArgumentException("inClause must have at least one entry");
    } else {
      StringBuilder builder = new StringBuilder();
      builder.append(name);
      builder.append(" IN (");
      for (T value : values) {
        builder.append(convertToDB(value)).append(',');
      }
      builder.setCharAt(builder.length() - 1, ')');
      return builder.toString();
    }
  }
  
  public final String getNin(T ... values) {
    if (values == null || values.length == 0) {
      throw new IllegalArgumentException("inClause must have at least one entry");
    } else {
      StringBuilder builder = new StringBuilder();
      builder.append(name);
      builder.append(" NOT IN (");
      for (T value : values) {
        builder.append(convertToDB(value)).append(',');
      }
      builder.setCharAt(builder.length() - 1, ')');
      return builder.toString();
    }
  }
  
  public final <R> String getEquals(Column<R> column) {
    if (column == null) {
      throw new IllegalArgumentException("Column must not be null");
    } else {
      return getQualifiedName() + "=" + column.getQualifiedName();
    }
  }
  
  public final <R> String getEquals(String qualifier, Column<R> column) {
    if (column == null) {
      throw new IllegalArgumentException("Column must not be null");
    } else {
      return qualifier + "." + name + "=" + column.getQualifiedName();
    }
  }
  
  public final <R> String getEquals(Column<R> column, String qualifier) {
    if (column == null) {
      throw new IllegalArgumentException("Column must not be null");
    } else {
      return getQualifiedName() + "=" + qualifier + "." + column.getName();
    }
  }
  
  public final <R> String getEquals(String qualifierL, Column<R> column, String qualifierR) {
    if (column == null) {
      throw new IllegalArgumentException("Column must not be null");
    } else {
      return qualifierL = "." + getName() + "=" + qualifierR + "." + column.getName();
    }
  }
  
  public final String getEquals(T value) {
    if (value == null) {
      return name + " IS NULL";
    } else {
      return name + "=" + equalsHelper(value);
    }
  }
  
  public final String getQualifiedEquals(T value) {
    if (value == null) {
      return getQualifiedName() + " IS NULL";
    } else {
      return getQualifiedName() + "=" + equalsHelper(value);
    }
  }
  
  public final String getQualifierEquals(T value, String qualifier) {
    if (value == null) {
      return getQualifiedName(qualifier) + " IS NULL";
    } else {
      return getQualifiedName(qualifier) + "=" + equalsHelper(value);
    }
  }
  
  public String getLike(T value) {
    throw new UnsupportedOperationException("Only valid for string columns");
  }
  
  public final String getNequals(T value) {
    if (value == null) {
      return name + " IS NOT NULL";
    } else {
      return name + "<>" + equalsHelper(value);
    }
  }
  
  public final String setNull() {
    if (nullable) {
      return name + "=NULL";
    } else {
      throw new IllegalArgumentException("May not set a non-nullable column to be null: " + name);
    }
  }
}
