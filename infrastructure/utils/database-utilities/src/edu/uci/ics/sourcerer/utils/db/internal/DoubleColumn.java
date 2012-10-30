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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;

import edu.uci.ics.sourcerer.util.Strings;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class DoubleColumn extends ColumnImpl<Double> {
  private DecimalFormat format;
  DoubleColumn(DatabaseTableImpl table, String name, boolean nullable) {
    super(table, name, "FLOAT" + (nullable ? "" : " NOT NULL"), nullable, Types.FLOAT);
  }
  
  DoubleColumn(DatabaseTableImpl table, String name, int totalDigits, int decimalDigits, boolean nullable) {
    super(table, name, "FLOAT(" + totalDigits + "," + decimalDigits+")" + (nullable ? "" : " NOT NULL"), nullable, Types.DOUBLE);
    format = new DecimalFormat(Strings.create('#', totalDigits - decimalDigits) + "." + Strings.create('#', decimalDigits));
  }

  @Override
  protected void bindHelper(Double value, PreparedStatement statement, int index) throws SQLException {
    statement.setDouble(index, value.doubleValue());
  }

  @Override
  protected Double fromHelper(String value) {
    return Double.valueOf(value);
  }

  @Override
  protected String toHelper(Double value) {
    if (value.isInfinite() || value.isNaN()) {
      if (isNullable()) {
        return "NULL";
      } else {
        return "0";
      }
    } else if (format != null) {
      return format.format(value.doubleValue());
    } else {
      return value.toString();
    }
  }
}
