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
//package edu.uci.ics.sourcerer.extractor;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.Collection;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import edu.uci.ics.sourcerer.model.Entity;
//import edu.uci.ics.sourcerer.model.LocalVariable;
//import edu.uci.ics.sourcerer.model.Modifier;
//import edu.uci.ics.sourcerer.model.Relation;
//import edu.uci.ics.sourcerer.model.extracted.EntityEX;
//import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
//import edu.uci.ics.sourcerer.model.extracted.RelationEX;
//import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
//import edu.uci.ics.sourcerer.repo.base.IFileSet;
//import edu.uci.ics.sourcerer.repo.base.IJavaFile;
//import edu.uci.ics.sourcerer.repo.base.RepoProject;
//import edu.uci.ics.sourcerer.repo.base.Repository;
//import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
//import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
//import edu.uci.ics.sourcerer.util.Helper;
//import edu.uci.ics.sourcerer.util.io.FileUtils;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ExtractorOutputTest {
//  private Repository repo;
//  private ExtractedRepository extractedRepo;
//  
//  private String verifyLine;
//  
//  private int errors;
//  private int warnings;
//  private int duplicates;
//  
//  private ExtractorOutputTest(Repository repo, ExtractedRepository extractedRepo) {
//    this.repo = repo;
//    this.extractedRepo = extractedRepo;
//  }
//
//  private void printVerifyLine() {
//    if (verifyLine != null) {
//      logger.info(verifyLine);
//      verifyLine = null;
//    }
//  }
//  
//  private void reportInvalidLine(String line) {
//    printVerifyLine();
//    logger.info("  ERROR - Invalid line: " + line);
//  }
//  
//  private void reportError(String line) {
//    printVerifyLine();
//    logger.info("  ERROR:   " + line);
//    errors++;
//  }
//  
//  private void reportWarning(String line) {
//    printVerifyLine();
//    logger.info("  WARNING: " + line);
//    warnings++;
//  }
//  
//  private void reportDuplicate(String line) {
//    printVerifyLine();
//    logger.info("  DUPLICATE: " + line);
//    duplicates++;
//  }
//  
//  private String addPkg(String fqn, String pkg) {
//    return fqn.replace(pkg, "*pkg*");
//  }
//  
//  private String removePkg(String fqn, String pkg) {
//    return fqn.replace("*pkg*", pkg);
//  }
//  
//  private void verifyOutput() {
//    errors = 0;
//    warnings = 0;
//    duplicates = 0;
//    logger.info("--- Extractor Output Verification ---");
//    for (ExtractedProject extractedProject : extractedRepo.getProjects()) {
//      if (extractedProject.shouldVerify()) {
//        logger.info("Verifying Project " + extractedProject.getRelativePath());
//        ExtractorOutputMap map = ExtractorOutputMap.getExtractorOutputMap(extractedProject);
//        
//        RepoProject project = repo.getProject(extractedProject.getRelativePath());
//        IFileSet files = project.getFileSet();
//        for (IJavaFile file : files.getUniqueJavaFiles()) {
//          String relativePath = files.convertToRelativePath(file.getPath());
//          verifyLine = "Verifying " + relativePath;
//          verifyOutput(file, map.getExtractorOutput(relativePath));
//        }
//      }
//    }
//    logger.info("--- Summary ---");
//    logger.info(errors + " errors");
//    logger.info(warnings + " warnings");
//    logger.info(duplicates + " duplicates");
//  }
//  
//  private void verifyOutput(IJavaFile file, ExtractorOutput output) {
//    try {
//      Set<EntityEX> verifiedEntities = Helper.newHashSet();
//      Set<RelationEX> verifiedRelations = Helper.newHashSet();
//      Set<LocalVariableEX> verifiedLocalVariables = Helper.newHashSet();
//      
//      String fullText = FileUtils.getFileAsString(file.getPath());
//      
//      BufferedReader br = new BufferedReader(new StringReader(fullText));
//      boolean testBegun = false;
//      for (String line = br.readLine(); line != null; line = br.readLine()) {
//        if (!testBegun) {
//          if (line.startsWith("// BEGIN TEST")) {
//            testBegun = true;
//          }
//        } else if (line.startsWith("//")) {
//          line = line.substring(3);
//          String[] parts = line.split(" ");
//          if (parts.length == 0) {
//            reportInvalidLine(line);
//            continue;
//          }
//          // Is it an entity?
//          Entity entity = Entity.parse(parts[0]);
//          if (entity != null) {
//            if(parts.length < 3) {
//              reportInvalidLine(line);
//              continue;
//            }
//            Collection<Modifier> mods = null;
//            try {
//              mods = convertModifiers(parts[1]);
//            } catch (IllegalArgumentException e) {
//              reportInvalidLine(line);
//              continue;
//            }
//            String fqn = removePkg(parts[2], file.getPackage());
//            // Attempt to locate the matching entity
//            EntityEX found = null;
//            for (EntityEX extracted : output.getEntities()) {
//              if (extracted.getType().equals(entity) && extracted.getFqn().equals(fqn) && verifyMods(extracted, mods)) {
//                // If present, verify the first and last word
//                if (parts.length == 5) {
//                  String text = getText(extracted, fullText);
//                  if (text.startsWith(parts[3]) && text.endsWith(parts[4])) {
//                    found = extracted;
//                  }
//                } else if (parts.length == 4) {
//                  if (parts[3].equals("-") && extracted.getStartPosition().equals("-1") && extracted.getLength().equals("0")) {
//                    found = extracted;
//                  }
//                } else {
//                  found = extracted;
//                }
//                break;
//              }
//            }
//            if (found == null) {
//              reportError(line);
//            } else {
//              verifiedEntities.add(found);
//            }
//            continue;
//          } 
//          // Is it a relation?
//          Relation relation = Relation.parse(parts[0]);
//          if (relation != null) {
//            if (parts.length < 4 && relation != Relation.INSIDE) {
//              reportInvalidLine(line);
//              continue;
//            } else if(parts.length < 3) {
//              reportInvalidLine(line);
//              continue;
//            }
//            String lhs = removePkg(parts[1], file.getPackage());
//            String rhs = removePkg(parts[2], file.getPackage());
//            // Attempt to locate the matching relation
//            RelationEX found = null;
//            for (RelationEX extracted : output.getRelations()) {
//              if (verifiedRelations.contains(extracted)) {
//                continue;
//              }
//              if (extracted.getType().equals(relation) && extracted.getLhs().equals(lhs) && extracted.getRhs().equals(rhs)) {
//                if (relation != Relation.INSIDE) {
//                  String text = getText(extracted, fullText);
//                  if (text.equals(parts[3]) || parts[3].equals("?")) {
//                    found = extracted;
//                    break;
//                  }
//                } else {
//                  found = extracted;
//                  break;
//                }
//              }
//            }
//            if (found == null) {
//              reportError(line);
//            } else {
//              verifiedRelations.add(found);
//            }
//            continue;
//          }
//          // Is it a local variable?
//          LocalVariable localVariable = LocalVariable.parse(parts[0]);
//          if (localVariable != null) {
//            if (parts.length < 7 && localVariable == LocalVariable.PARAM) {
//              reportInvalidLine(line);
//              continue;
//            } else if (parts.length < 6) {
//              reportInvalidLine(line);
//              continue;
//            }
//            String type = removePkg(parts[2], file.getPackage());
//            String parent = removePkg(parts[4], file.getPackage());
//            // Attempt to locate matching local variable
//            LocalVariableEX found = null;
//            for (LocalVariableEX extracted : output.getLocalVariables()) {
//              if (verifiedLocalVariables.contains(extracted)) {
//                continue;
//              }
//              if (extracted.getType().equals(localVariable) && extracted.getName().equals(parts[1]) && extracted.getTypeFqn().equals(type) && extracted.getParent().equals(parent)) {
//                String typeText = getTypeText(extracted, fullText);
//                String text = getText(extracted, fullText);
//                if ((typeText.equals(parts[3]) || parts[3].equals("?")) && (text.equals(parts[5]) || parts[5].equals("?"))) {
//                  if ((localVariable == LocalVariable.PARAM && extracted.getPosition().equals(parts[6])) || localVariable == LocalVariable.LOCAL) {
//                    found = extracted;
//                    break;
//                  }
//                }
//              }
//            }
//            if (found == null) {
//              reportError(line);
//            } else {
//              verifiedLocalVariables.add(found);
//            }
//            continue;
//          }
//          reportInvalidLine(line);
//        } else if (!line.matches("\\w*")) {
//          break;
//        }
//      }
//      br.close();
//      
//      // Output the warnings
//      for (EntityEX extracted : output.getEntities()) {
//        if (!verifiedEntities.contains(extracted)) {
//          String text = getText(extracted, fullText);
//          String start = getStart(text);
//          String end = getEnd(text);
//          reportWarning(extracted.getType().name() + " " + modsToString(extracted) + " " + addPkg(extracted.getFqn(), file.getPackage()) + " " + start + " " + end);
//        }
//      }
//      {
//        Set<RelationEX> duplicateChecker = Helper.newHashSet();
//        for (RelationEX extracted : output.getRelations()) {
//          if (duplicateChecker.contains(extracted)) {
//            if (extracted.getType() == Relation.INSIDE) {
//              reportDuplicate(extracted.getType().name() + " " + addPkg(extracted.getLhs(), file.getPackage()) + " " + addPkg(extracted.getRhs(), file.getPackage()));
//            } else {
//              reportDuplicate(extracted.getType().name() + " " + addPkg(extracted.getLhs(), file.getPackage()) + " " + addPkg(extracted.getRhs(), file.getPackage()) + " " + getText(extracted, fullText));
//            }
//            duplicates++;
//          } else {
//            duplicateChecker.add(extracted);
//          }
//          if (!verifiedRelations.contains(extracted)) {
//            if (extracted.getType() == Relation.INSIDE) {
//              reportWarning(extracted.getType().name() + " " + addPkg(extracted.getLhs(), file.getPackage()) + " " + addPkg(extracted.getRhs(), file.getPackage()));
//            } else {
//              reportWarning(extracted.getType().name() + " " + addPkg(extracted.getLhs(), file.getPackage()) + " " + addPkg(extracted.getRhs(), file.getPackage()) + " " + getText(extracted, fullText));
//            }
//          }
//        }
//      }
//      {
//        Set<LocalVariableEX> duplicateChecker = Helper.newHashSet();
//        for (LocalVariableEX extracted : output.getLocalVariables()) {
//          if (duplicateChecker.contains(extracted)) {
//            if (extracted.getType() == LocalVariable.PARAM) {
//              reportDuplicate(extracted.getType().name() + " " + extracted.getName() + " " + addPkg(extracted.getTypeFqn(), file.getPackage()) + " " + getTypeText(extracted, fullText) + " " + addPkg(extracted.getParent(), file.getPackage()) + " " + getText(extracted, fullText) + " " + extracted.getPosition());
//            } else {
//              reportDuplicate(extracted.getType().name() + " " + extracted.getName() + " " + addPkg(extracted.getTypeFqn(), file.getPackage()) + " " + getTypeText(extracted, fullText) + " " + addPkg(extracted.getParent(), file.getPackage()) + " " + getText(extracted, fullText));
//            } 
//            duplicates++;
//          } else {
//            duplicateChecker.add(extracted);
//          }
//          if (!verifiedLocalVariables.contains(extracted)) {
//            if (extracted.getType() == LocalVariable.PARAM) {
//              reportWarning(extracted.getType().name() + " " + extracted.getName() + " " + addPkg(extracted.getTypeFqn(), file.getPackage()) + " " + getTypeText(extracted, fullText) + " " + addPkg(extracted.getParent(), file.getPackage()) + " " + getText(extracted, fullText) + " " + extracted.getPosition());
//            } else {
//              reportWarning(extracted.getType().name() + " " + extracted.getName() + " " + addPkg(extracted.getTypeFqn(), file.getPackage()) + " " + getTypeText(extracted, fullText) + " " + addPkg(extracted.getParent(), file.getPackage()) + " " + getText(extracted, fullText));
//            } 
//          }
//        }
//      }
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Unable to verify output for: " + file.getPath(), e);
//    }
//  }
//  
//  private Pattern whitespace = Pattern.compile("\\s*(.*)", Pattern.DOTALL);
//  private String getText(EntityEX entity, String fullText) {
//    int startPos = Integer.parseInt(entity.getStartPosition());
//    int length = Integer.parseInt(entity.getLength());
//    if (startPos == -1) {
//      return "-";
//    } else {
//      Matcher matcher = whitespace.matcher(fullText.substring(startPos, startPos + length));
//      if (matcher.matches()) {
//        return matcher.group(1);
//      } else {
//        return null;
//      }
//    }
//  }
//  
//  private String getStart(String text) {
//    int spaceIndex = text.indexOf(' ');
//    int nIndex = text.indexOf('\n');
//    int rIndex = text.indexOf('\r');
//    if (spaceIndex == -1 && nIndex == -1 && rIndex == -1) {
//      return text;
//    } else {
//      int min = Integer.MAX_VALUE;
//      if (spaceIndex != -1) {
//        min = spaceIndex;
//      }
//      if (nIndex != -1) {
//        min = Math.min(min, nIndex);
//      }
//      if (rIndex != -1) {
//        min = Math.min(min, rIndex);
//      }
//      return text.substring(0, min);
//    }
//  }
//  
//  private Collection<Modifier> convertModifiers(String mods) {
//    Collection<Modifier> retval = Helper.newLinkedList();
//    for (String mod :  mods.split("-")) {
//      retval.add(Modifier.valueOf(mod.toUpperCase()));
//    }
//    return retval;
//  }
//  
//  private boolean verifyMods(EntityEX extracted, Collection<Modifier> mods) {
//    Set<Modifier> modifiers = Modifier.convertFromString(extracted.getMods());
//    int count = 0;
//    for (Modifier mod : mods) {
//      if (modifiers.contains(mod)) {
//        count++;
//      } else {
//        return false;
//      }
//    }
//    return count == modifiers.size();
//  }
//  
//  private String modsToString(EntityEX extracted) {
//    StringBuffer mods = new StringBuffer();
//    boolean first = true;
//    for (Modifier mod : Modifier.convertFromString(extracted.getMods())) {
//      if (first) {
//        first = false;
//      } else {
//        mods.append('-');
//      }
//      mods.append(mod.name().toLowerCase());
//    }
//    
//    if (mods.length() == 0) {
//      return "-";
//    } else {
//      return mods.toString();
//    }
//  }
//  
//  private String getEnd(String text) {
//    int max = Math.max(text.lastIndexOf(' '), text.lastIndexOf('\n'));
//    max = Math.max(max, text.lastIndexOf('\r'));
//    return text.substring(max + 1);
//  }
//  
//  private String getText(RelationEX relation, String fullText) {
//    int startPos = Integer.parseInt(relation.getStartPosition());
//    int length = Integer.parseInt(relation.getLength());
//    if (startPos == -1) {
//      return "-";
//    } else {
//      return fullText.substring(startPos, startPos + length);
//    }
//  }
//  
//  private String getText(LocalVariableEX localVariable, String fullText) {
//    int startPos = Integer.parseInt(localVariable.getStartPos());
//    int length = Integer.parseInt(localVariable.getLength());
//    if (startPos == -1) {
//      return "-";
//    } else {
//      return fullText.substring(startPos, startPos + length);
//    }
//  }
//  
//  private String getTypeText(LocalVariableEX localVariable, String fullText) {
//    int startPos = Integer.parseInt(localVariable.getTypeStartPos());
//    int length = Integer.parseInt(localVariable.getTypeLength());
//    if (startPos == -1) {
//      return "-";
//    } else {
//      return fullText.substring(startPos, startPos + length);
//    }
//  }
//  
//  public static void runTest() {
//    File repoRoot = AbstractRepository.INPUT_REPO.getValue();
//    File tempDir = FileUtils.getTempDir();
//    Repository repo = Repository.getRepository(repoRoot, tempDir);
//    
//    File extractorOutput = AbstractRepository.OUTPUT_REPO.getValue();
//    ExtractedRepository extractedRepo = ExtractedRepository.getRepository(extractorOutput);
//    
//    ExtractorOutputTest test = new ExtractorOutputTest(repo, extractedRepo);
//    test.verifyOutput();
//  }
//}
