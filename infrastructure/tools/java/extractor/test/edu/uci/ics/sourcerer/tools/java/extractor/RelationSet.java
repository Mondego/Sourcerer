///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.tools.java.extractor;
//
//import java.util.Collection;
//import java.util.EnumMap;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
//import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class RelationSet {
//  private Map<Relation, Collection<RelationEX>> relations;
//  private Map<String, Collection<RelationEX>> initializers;
//  private Collection<String> constructors;
//  private Collection<String> enums;
//  private Collection<String> fields;
//  
//  private RelationSet(EntitySet eSet) {
//    relations = new EnumMap<>(Relation.class);
//    initializers = new HashMap<>();
//    this.constructors = eSet.getConstructors();
//    this.enums = eSet.getEnums();
//    this.fields = eSet.getFields();
//  }
//  
//  private static Pattern initializer = Pattern.compile("(.*)\\.initializer-(\\d+)(.*)");
//  static RelationSet make(Collection<RelationEX> relations, EntitySet eSet) {
//    Collection<String> insideStaticInitializers = new HashSet<>();
//    RelationSet set = new RelationSet(eSet);
//    Collection<String> staticInitializers = eSet.getStaticInitializers();
//    for (RelationEX relation : relations) {
//      Collection<RelationEX> list = set.relations.get(relation.getType());
//      if (list == null) {
//        list = new LinkedList<>();
//        set.relations.put(relation.getType(), list);
//      }
//      Matcher matcher = initializer.matcher(relation.getLhs());
//      if (matcher.matches()) {
//        if (staticInitializers.contains(relation.getLhs())) {
//          String fqn = matcher.group(1) + ".<clinit>()";
//          if (relation.getType() == Relation.INSIDE) {
//            if (!insideStaticInitializers.contains(fqn)) {
//              list.add(new RelationEX(relation.getType(), fqn, relation.getRhs(), relation.getLocation()));
//              insideStaticInitializers.add(fqn);
//            }
//          } else {
////            list.add(relation);
//            list.add(new RelationEX(relation.getType(), fqn, relation.getRhs(), relation.getLocation()));
//          }
//        } else {
//          String key = matcher.group(1);
//          list = set.initializers.get(key);
//          if (list == null) {
//            list = new LinkedList<>();
//            set.initializers.put(key, list);
//          }
//          list.add(relation);
//        }
//      } else if (relation.getType() == Relation.INSIDE) {
//        int dot = relation.getRhs().lastIndexOf('.');
//        if (relation.getRhs().indexOf('(', dot) == -1) {
//          list.add(relation);
//        } else {
//          list.add(new RelationEX(relation.getType(), relation.getLhs(), relation.getRhs().substring(0, dot), relation.getLocation()));
//        }
//      } else {
//        list.add(relation);
//      }
//    }
//    return set;
//  }
//  
//  static void destructiveCompare(ComparisonMismatchReporter reporter, RelationSet setA, RelationSet setB) {
//    for (Relation type : Relation.values()) {
//      if (type == Relation.USES || type == Relation.OVERRIDES) {
//        continue;
//      }
//      Collection<RelationEX> listA = setA.relations.get(type);
//      Collection<RelationEX> listB = setB.relations.get(type);
//      if (listA == null && listB != null) {
//        for (RelationEX relationB : listB) {
//          if (checkMissing(relationB, setB, setA)) {
//            reporter.missingFromA(relationB);
//          }
//        }
//      } else if (listA != null && listB == null) {
//        for (RelationEX relationA : listA) {
//          if (checkMissing(relationA, setA, setB)) {
//            reporter.missingFromB(relationA);
//          }
//        }
//      } else if (listA != null && listB != null) {
//        for (RelationEX relationA : listA) {
//          Iterator<RelationEX> iterB = listB.iterator();
//          boolean missing = true;
//          while (iterB.hasNext()) {
//            RelationEX relationB = iterB.next();
//            if (relationA.getLhs().equals(relationB.getLhs()) && relationA.getRhs().equals(relationB.getRhs())) {
//              iterB.remove();
//              missing = false;
//              break;
//            }
//          }
//          if (missing && checkMissing(relationA, setA, setB)) {
//            reporter.missingFromB(relationA);
//          }
//        }
//        for (RelationEX relationB : listB) {
//          if (checkMissing(relationB, setB, setA)) {
//            reporter.missingFromA(relationB);
//          }
//        }
//      }
//    }
//  }
//  
//  private static boolean checkMissing(RelationEX relation, RelationSet mySet, RelationSet otherSet) {
//    boolean missing = true;
//    String fqn = relation.getLhs();
//    String klass = null;
//    int dot = fqn.lastIndexOf('.', fqn.lastIndexOf('('));
//    if (dot != -1) {
//      klass = fqn.substring(0, dot);
//    }
//    // Compensate for instance initializers being relocated to each constructor
//    if (mySet.constructors.contains(relation.getLhs())) {
//      Collection<RelationEX> relations = otherSet.initializers.get(klass);
//      if (relations != null) {
//        for (RelationEX relationA : relations) {
//          if (relationA.getType() == relation.getType() && relationA.getRhs().equals(relation.getRhs())) {
//            missing = false;
//            break;
//          }
//        }
//      }
//    } 
//    else {
//      // Compensate for enums generating extra relations from bytecode
//      if (mySet.enums.contains(klass)) {
//        if (fqn.endsWith("<clinit>()") || fqn.endsWith("values()") || fqn.endsWith("valueOf(java.lang.String)")) {
//          missing = false;
//        }
//      }
//      // Compensate for fields owning their initialization
//      else if (mySet.fields.contains(fqn)) {
//        String clinit = fqn.substring(0, fqn.lastIndexOf('.')) + ".<clinit>()";
//        Collection<RelationEX> relations = otherSet.relations.get(relation.getType());
//        if (relations != null) {
//          for (RelationEX other : relations) {
//            if (other.getLhs().equals(clinit) && other.getRhs().equals(relation.getRhs())) {
//              missing = false;
//              break;
//            }
//          }
//        }
//      }
//    }
//    return missing;
//  }
//}
