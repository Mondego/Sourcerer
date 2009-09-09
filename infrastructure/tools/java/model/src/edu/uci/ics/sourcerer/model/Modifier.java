// Sourcerer: an infrastructure for large-scale source code analysis.
// Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
package edu.uci.ics.sourcerer.model;

import java.util.Set;

import edu.uci.ics.sourcerer.util.Helper;

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
  ABSTRACT(0x0400),
  STRICTFP(0x0800);
  
  private final int value;
  
  private Modifier(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
  
  public String toString() {
    return name().toLowerCase();
  }
  
  public static boolean is(Modifier type, String modifier) {
    return is(type, Integer.parseInt(modifier));
  }
  
  public static boolean is(Modifier type, int modifier) {
    return (modifier & type.value) == type.value;
  }
  
  public static int convertToInt(Set<Modifier> modifiers) {
    int value = 0;
    for (Modifier mod : modifiers) {
      value |= mod.value;
    }
    return value;
  }
  
  public static String convertToString(Set<Modifier> modifiers) {
    return Integer.toString(convertToInt(modifiers));
  }
  
  public static Set<Modifier> convertFromInt(int modifiers) {
    Set<Modifier> mods = Helper.newHashSet();
    for (Modifier mod : values()) {
      if ((mod.value & modifiers) == mod.value) {
        mods.add(mod);
      }
    }
    return mods;
  }
  
  public static Set<Modifier> convertFromString(String modifiers) {
    if (modifiers == null) {
      return null;
    } else {
      return convertFromInt(Integer.parseInt(modifiers));
    }
  }
}
