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
package edu.uci.ics.sourcerer.utils.db.internal;

import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class QualifiedTableImpl implements QualifiedTable {
  private Table table;
  private String id;
  
  QualifiedTableImpl(Table table, String id) {
    this.table = table;
    this.id = id;
  }
  
  @Override
  public String getQualifier() {
    return id;
  }

  @Override
  public Table getQualifiedTable() {
    return table;
  }
  
  @Override
  public String toSql() {
    return table.toSql() + " as " + id;
  }
  
  @Override
  public QualifiedTable qualify(String qualifier) {
    return new QualifiedTableImpl(table, qualifier);
  }
  
  @Override
  public String toString() {
    return toSql();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof QualifiedTableImpl) {
      QualifiedTableImpl other = (QualifiedTableImpl) o;
      return table.equals(other.table) && id.equals(other.id);
    } else {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return 37 * table.hashCode() + id.hashCode();
  }
}
