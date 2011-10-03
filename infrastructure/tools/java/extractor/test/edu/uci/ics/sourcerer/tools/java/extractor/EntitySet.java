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
package edu.uci.ics.sourcerer.tools.java.extractor;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifier;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class EntitySet {
  private Map<Entity, Collection<EntityEX>> entities;
  private Collection<String> staticInitializers;
  private Collection<String> constructors;
  private Collection<String> enums;
  private Collection<String> fields;
  
  private EntitySet() {
    entities = new EnumMap<>(Entity.class);
    staticInitializers = new HashSet<>();
    constructors = new HashSet<>();
    enums = new HashSet<>();
    fields = new HashSet<>();
  }
  
  private static Pattern initializer = Pattern.compile("(.*)\\.initializer-(\\d+)(.*)");
  static EntitySet make(Collection<EntityEX> entities) {
    Collection<String> initializers = new HashSet<>();
    EntitySet set = new EntitySet();
    for (EntityEX entity : entities) {
      Collection<EntityEX> list = set.entities.get(entity.getType());
      if (list == null) {
        list = new LinkedList<>();
        set.entities.put(entity.getType(), list);
      }
      Matcher matcher = initializer.matcher(entity.getFqn());
      if (matcher.matches()) {
        if (Modifier.STATIC.is(entity.getModifiers().getValue())) {
          set.staticInitializers.add(entity.getFqn());
          String fqn = matcher.group(1) + ".<clinit>()";
          if (!initializers.contains(fqn)) {
            initializers.add(fqn);
            list.add(new EntityEX(Entity.INITIALIZER, fqn, null, null, entity.getModifiers(), null, entity.getLocation()));
          }
        }
      } else {
        if (entity.getType() == Entity.CONSTRUCTOR) {
          set.constructors.add(entity.getFqn());
        } else if (entity.getType() == Entity.ENUM) {
          set.enums.add(entity.getFqn());
        } else if (entity.getType() == Entity.FIELD || entity.getType() == Entity.ENUM_CONSTANT) {
          set.fields.add(entity.getFqn());
        }
        list.add(entity);
      }
    }
    return set;
  }
  
  Collection<String> getStaticInitializers() {
    return staticInitializers;
  }
  
  Collection<String> getConstructors() {
    return constructors;
  }
  
  Collection<String> getEnums() {
    return enums;
  }
  
  Collection<String> getFields() {
    return fields;
  }
  
  static void destructiveCompare(ComparisonMismatchReporter reporter, EntitySet setA, EntitySet setB) {
    for (Entity type : Entity.values()) {
      Collection<EntityEX> listA = setA.entities.get(type);
      Collection<EntityEX> listB = setB.entities.get(type);
      if (listA == null && listB != null) {
        for (EntityEX entity : listB) {
          reporter.missingFromA(entity);
        }
      } else if (listA != null && listB == null) {
        for (EntityEX entity : listA) {
          reporter.missingFromB(entity);
        }
      } else if (listA != null && listB != null) {
        for (EntityEX entityA : listA) {
          Iterator<EntityEX> iterB = listB.iterator();
          boolean missing = true;
          while (iterB.hasNext()) {
            EntityEX entityB = iterB.next();
            if (entityA.getFqn().equals(entityB.getFqn())) {
              iterB.remove();
              missing = false;
              break;
            }
          }
          if (missing) {
            reporter.missingFromB(entityA);
          }
        }
        for (EntityEX entityB : listB) {
          reporter.missingFromA(entityB);
        }
      }
    }
  }
}
