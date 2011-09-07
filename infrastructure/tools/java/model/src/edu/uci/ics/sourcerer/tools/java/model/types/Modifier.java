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
package edu.uci.ics.sourcerer.tools.java.model.types;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum Modifier {
  PUBLIC(0x0001),
  PRIVATE(0x0002),
  PROTECTED(0x0004),
  STATIC(0x0008),
  FINAL(0x0010),
  SYNCHRONIZED(0x0020),
  VOLATILE(0x0040),
  TRANSIENT(0x0080),
  NATIVE(0x0100),
  INTERFACE(0x0200),
  ABSTRACT(0x0400),
  STRICTFP(0x0800),
  SYNTHETIC(0x1000),
  ;
  
  private final int value;
  
  private Modifier(int value) {
    this.value = value;
    if (value != (1 << ordinal())) {
      throw new IllegalStateException("The bit value should match the ordinal value");
    }
  }

  public int getValue() {
    return value;
  }
  
  public String toString() {
    return name().toLowerCase();
  }
  
  public boolean is(int modifier) {
    return (modifier & value) != 0;
  }
}
