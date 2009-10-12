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
package uci.ics.sourcerer.extractor;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.LocalVariable;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TestExtractorOutput {
  private Repository repo;
  private ExtractedRepository extractedRepo;
  
  private String verifyLine;
  
  private int errors;
  private int warnings;
  private int duplicates;
  
  private TestExtractorOutput(Repository repo, ExtractedRepository extractedRepo) {
    this.repo = repo;
    this.extractedRepo = extractedRepo;
  }

  private void printVerifyLine() {
    if (verifyLine != null) {
      logger.info(verifyLine);
      verifyLine = null;
    }
  }
  
  private void reportInvalidLine(String line) {
    printVerifyLine();
    logger.info("  ERROR - Invalid line: " + line);
  }
  
  private void reportError(String line) {
    printVerifyLine();
    logger.info("  ERROR:   " + line);
    errors++;
  }
  
  private void reportWarning(String line) {
    printVerifyLine();
    logger.info("  WARNING: " + line);
    warnings++;
  }
  
  private void reportDuplicate(String line) {
    printVerifyLine();
    logger.info("  DUPLICATE: " + line);
    duplicates++;
  }
  
  private String addPkg(String fqn, String pkg) {
    return fqn.replace(pkg, "*pkg*");
  }
  
  private String removePkg(String fqn, String pkg) {
    return fqn.replace("*pkg*", pkg);
  }
  
  private void verifyOutput() {
    errors = 0;
    warnings = 0;
    duplicates = 0;
    logger.info("--- Extractor Output Verification ---");
    for (ExtractedProject extractedProject : extractedRepo.getProjects()) {
      logger.info("Verifying Project " + extractedProject.getRelativePath());
      ExtractorOutputMap map = ExtractorOutputMap.getExtractorOutputMap(extractedProject);
      
      RepoProject project = repo.getProject(extractedProject.getRelativePath());
      IFileSet files = project.getFileSet();
      for (IJavaFile file : files.getUniqueJavaFiles()) {
        String relativePath = extractedRepo.convertToRelativePath(file.getPath());
        verifyLine = "Verifying " + relativePath;
        verifyOutput(file, map.getExtractorOutput(relativePath));
      }
    }
    logger.info("--- Summary ---");
    logger.info(errors + " errors");
    logger.info(warnings + " warnings");
    logger.info(duplicates + " duplicates");
  }
  
  private void verifyOutput(IJavaFile file, ExtractorOutput output) {
    try {
      Set<EntityEX> verifiedEntities = Helper.newHashSet();
      Set<RelationEX> verifiedRelations = Helper.newHashSet();
      Set<LocalVariableEX> verifiedLocalVariables = Helper.newHashSet();
      
      String fullText = FileUtils.getFileAsString(file.getPath());
      
      BufferedReader br = new BufferedReader(new StringReader(fullText));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.startsWith("//")) {
          line = line.substring(3);
          String[] parts = line.split(" ");
          if (parts.length == 0) {
            reportInvalidLine(line);
            continue;
          }
          // Is it an entity?
          Entity entity = Entity.parse(parts[0]);
          if (entity != null) {
            if(parts.length < 2) {
              reportInvalidLine(line);
              continue;
            }
            String fqn = removePkg(parts[1], file.getPackage());
            // Attempt to locate the matching entity
            EntityEX found = null;
            for (EntityEX extracted : output.getEntities()) {
              if (extracted.getType().equals(entity) && extracted.getFqn().equals(fqn)) {
                found = extracted;
                break;
              }
            }
            if (found == null) {
              reportError(line);
            } else {
              verifiedEntities.add(found);
            }
            continue;
          } 
          // Is it a relation?
          Relation relation = Relation.parse(parts[0]);
          if (relation != null) {
            if (parts.length < 4 && relation != Relation.INSIDE) {
              reportInvalidLine(line);
              continue;
            } else if(parts.length < 3) {
              reportInvalidLine(line);
              continue;
            }
            String lhs = removePkg(parts[1], file.getPackage());
            String rhs = removePkg(parts[2], file.getPackage());
            // Attempt to locate the matching relation
            RelationEX found = null;
            for (RelationEX extracted : output.getRelations()) {
              if (verifiedRelations.contains(extracted)) {
                continue;
              }
              if (extracted.getType().equals(relation) && extracted.getLhs().equals(lhs) && extracted.getRhs().equals(rhs)) {
                if (relation != Relation.INSIDE) {
                  String text = getText(extracted, fullText);
                  if (text.equals(parts[3]) || parts[3].equals("?")) {
                    found = extracted;
                    break;
                  }
                } else {
                  found = extracted;
                  break;
                }
              }
            }
            if (found == null) {
              reportError(line);
            } else {
              verifiedRelations.add(found);
            }
            continue;
          }
          // Is it a local variable?
          LocalVariable localVariable = LocalVariable.parse(parts[0]);
          if (localVariable != null) {
            if (parts.length < 7 && localVariable == LocalVariable.PARAM) {
              reportInvalidLine(line);
              continue;
            } else if (parts.length < 6) {
              reportInvalidLine(line);
              continue;
            }
            String type = removePkg(parts[2], file.getPackage());
            String parent = removePkg(parts[4], file.getPackage());
            // Attempt to locate matching local variable
            LocalVariableEX found = null;
            for (LocalVariableEX extracted : output.getLocalVariables()) {
              if (verifiedLocalVariables.contains(extracted)) {
                continue;
              }
              if (extracted.getType().equals(localVariable) && extracted.getName().equals(parts[1]) && extracted.getTypeFqn().equals(type) && extracted.getParent().equals(parent)) {
                String typeText = getTypeText(extracted, fullText);
                String text = getText(extracted, fullText);
                if ((typeText.equals(parts[3]) || parts[3].equals("?")) && (text.equals(parts[5]) || parts[5].equals("?"))) {
                  if ((localVariable == LocalVariable.PARAM && extracted.getPosition().equals(parts[6])) || localVariable == LocalVariable.LOCAL) {
                    found = extracted;
                    break;
                  }
                }
              }
            }
            if (found == null) {
              reportError(line);
            } else {
              verifiedLocalVariables.add(found);
            }
            continue;
          }
          reportInvalidLine(line);
        } else if (!line.matches("\\w*")) {
          break;
        }
      }
      br.close();
      
      // Output the warnings
      for (EntityEX extracted : output.getEntities()) {
        if (!verifiedEntities.contains(extracted)) {
          reportWarning(extracted.getType().name() + " " + addPkg(extracted.getFqn(), file.getPackage()));
        }
      }
      {
        Set<RelationEX> duplicateChecker = Helper.newHashSet();
        for (RelationEX extracted : output.getRelations()) {
          if (duplicateChecker.contains(extracted)) {
            if (extracted.getType() == Relation.INSIDE) {
              reportDuplicate(extracted.getType().name() + " " + addPkg(extracted.getLhs(), file.getPackage()) + " " + addPkg(extracted.getRhs(), file.getPackage()));
            } else {
              reportDuplicate(extracted.getType().name() + " " + addPkg(extracted.getLhs(), file.getPackage()) + " " + addPkg(extracted.getRhs(), file.getPackage()) + " " + getText(extracted, fullText));
            }
            duplicates++;
          } else {
            duplicateChecker.add(extracted);
          }
          if (!verifiedRelations.contains(extracted)) {
            if (extracted.getType() == Relation.INSIDE) {
              reportWarning(extracted.getType().name() + " " + addPkg(extracted.getLhs(), file.getPackage()) + " " + addPkg(extracted.getRhs(), file.getPackage()));
            } else {
              reportWarning(extracted.getType().name() + " " + addPkg(extracted.getLhs(), file.getPackage()) + " " + addPkg(extracted.getRhs(), file.getPackage()) + " " + getText(extracted, fullText));
            }
          }
        }
      }
      {
        Set<LocalVariableEX> duplicateChecker = Helper.newHashSet();
        for (LocalVariableEX extracted : output.getLocalVariables()) {
          if (duplicateChecker.contains(extracted)) {
            if (extracted.getType() == LocalVariable.PARAM) {
              reportDuplicate(extracted.getType().name() + " " + extracted.getName() + " " + addPkg(extracted.getTypeFqn(), file.getPackage()) + " " + getTypeText(extracted, fullText) + " " + addPkg(extracted.getParent(), file.getPackage()) + " " + getText(extracted, fullText) + " " + extracted.getPosition());
            } else {
              reportDuplicate(extracted.getType().name() + " " + extracted.getName() + " " + addPkg(extracted.getTypeFqn(), file.getPackage()) + " " + getTypeText(extracted, fullText) + " " + addPkg(extracted.getParent(), file.getPackage()) + " " + getText(extracted, fullText));
            } 
            duplicates++;
          } else {
            duplicateChecker.add(extracted);
          }
          if (!verifiedLocalVariables.contains(extracted)) {
            if (extracted.getType() == LocalVariable.PARAM) {
              reportWarning(extracted.getType().name() + " " + extracted.getName() + " " + addPkg(extracted.getTypeFqn(), file.getPackage()) + " " + getTypeText(extracted, fullText) + " " + addPkg(extracted.getParent(), file.getPackage()) + " " + getText(extracted, fullText) + " " + extracted.getPosition());
            } else {
              reportWarning(extracted.getType().name() + " " + extracted.getName() + " " + addPkg(extracted.getTypeFqn(), file.getPackage()) + " " + getTypeText(extracted, fullText) + " " + addPkg(extracted.getParent(), file.getPackage()) + " " + getText(extracted, fullText));
            } 
          }
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to verify output for: " + file.getPath(), e);
    }
  }
  
  private String getText(RelationEX relation, String fullText) {
    int startPos = Integer.parseInt(relation.getStartPosition());
    int length = Integer.parseInt(relation.getLength());
    if (startPos == -1) {
      return "-";
    } else {
      return fullText.substring(startPos, startPos + length);
    }
  }
  
  private String getText(LocalVariableEX localVariable, String fullText) {
    int startPos = Integer.parseInt(localVariable.getStartPos());
    int length = Integer.parseInt(localVariable.getLength());
    if (startPos == -1) {
      return "-";
    } else {
      return fullText.substring(startPos, startPos + length);
    }
  }
  
  private String getTypeText(LocalVariableEX localVariable, String fullText) {
    int startPos = Integer.parseInt(localVariable.getTypeStartPos());
    int length = Integer.parseInt(localVariable.getTypeLength());
    if (startPos == -1) {
      return "-";
    } else {
      return fullText.substring(startPos, startPos + length);
    }
  }
  
  public static void runTest() {
    PropertyManager properties = PropertyManager.getProperties();
    
    File repoRoot = new File(properties.getValue(Property.REPO_ROOT));
    File tempDir = FileUtils.getTempDir();
    Repository repo = Repository.getRepository(repoRoot, tempDir);
    
    File extractorOutput = new File(properties.getValue(Property.INPUT));
    ExtractedRepository extractedRepo = ExtractedRepository.getRepository(extractorOutput);
    
    TestExtractorOutput test = new TestExtractorOutput(repo, extractedRepo);
    test.verifyOutput();
  }
  
  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    runTest();
  }
}
