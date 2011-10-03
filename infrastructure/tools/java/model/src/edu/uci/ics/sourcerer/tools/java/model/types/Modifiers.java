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

import java.util.AbstractSet;
import java.util.EnumSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import edu.uci.ics.sourcerer.util.BitEnumSet;
import edu.uci.ics.sourcerer.util.BitEnumSetFactory;
import edu.uci.ics.sourcerer.util.io.CustomSerializable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Modifiers extends AbstractSet<Modifier> implements BitEnumSet<Modifier>, CustomSerializable {
  private int value;
  private Set<Modifier> modifiers;
  
  private Modifiers(int value) {
    this.value = value;
  }
  
  private Modifiers(Modifier[] modifiers) {
    for (Modifier mod : modifiers) {
      add(mod);
    }
  }
  
  public static Modifiers make(int value) {
    return new Modifiers(value);
  }
  
  public static Modifiers make(Modifier ... modifiers) {
    return new Modifiers(modifiers);
  }
  
  private void initialize() {
    modifiers = EnumSet.noneOf(Modifier.class);
    if (value != 0) {
      for (Modifier mod : Modifier.values()) {
        if (mod.is(value)) {
          modifiers.add(mod);
        }
      }
    }
  }
  
  @Override
  public boolean add(Modifier modifier) {
    if (modifiers == null) {
      initialize();
    }
    value |= modifier.getValue();
    return modifiers.add(modifier);
  }
  
  @Override
  public String toString() {
    return modifiers.toString();
  }
  
  protected static Modifiers deserialize(Scanner scanner) {
    if (scanner.hasNextInt()) {
      return make(scanner.nextInt());
    } else if ("null".equals(scanner.next())){
      return null;
    } else {
      throw new InputMismatchException();
    }
  }
  
  public String serialize() {
    return Integer.toString(value);
  }

  @Override
  public Iterator<Modifier> iterator() {
    if (modifiers == null) {
      initialize();
    }
    return modifiers.iterator();
  }

  @Override
  public int size() {
    if (modifiers == null) {
      initialize();
    }
    return modifiers.size();
  }

  @Override
  public int getValue() {
    return value;
  }
  
  private static BitEnumSetFactory<Modifier, Modifiers> factory = new BitEnumSetFactory<Modifier, Modifiers>() {
    @Override
    public Modifiers make(int value) {
      return Modifiers.make(value);
    }
  };
  
  public static BitEnumSetFactory<Modifier, Modifiers> getFactory() {
    return factory;
  }
}
