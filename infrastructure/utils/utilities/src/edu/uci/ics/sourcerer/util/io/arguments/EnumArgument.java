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
package edu.uci.ics.sourcerer.util.io.arguments;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EnumArgument <E extends Enum<E>> extends Argument<E> {
  private Class<E> enumType;
  public EnumArgument(String name, Class<E> enumType, String description) {
    super(name, description);
    this.enumType = enumType;
  }
  
  public EnumArgument(String name, Class<E> enumType, E defaultValue, String description) {
    super(name, defaultValue, description);
  }
  
  @Override
  public String getType() {
    return "enum";
  }
  
  @Override
  protected E parseString(String value) {
    return Enum.valueOf(enumType, value);
  }
}
