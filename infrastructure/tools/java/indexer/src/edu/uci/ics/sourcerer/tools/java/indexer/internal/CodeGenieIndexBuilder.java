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
package edu.uci.ics.sourcerer.tools.java.indexer.internal;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.type.TypeUtils;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedColumn;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CodeGenieIndexBuilder extends AbstractIndexBuilder {
  private CodeGenieIndexBuilder() {}
  
  public static void buildIndex() {
    new CodeGenieIndexBuilder().run();
  }
  
  @Override
  protected void index() throws SolrServerException, IOException {
    task.start("Indexing methods", "methods processed", 100_000);
    QualifiedTable e1 = EntitiesTable.TABLE.qualify("a");
    QualifiedTable e2 = EntitiesTable.TABLE.qualify("b");
    try (SelectQuery selectMethods = exec.createSelectQuery(ProjectsTable.PROJECT_ID.compareEquals(EntitiesTable.PROJECT_ID.qualify(e1)), EntitiesTable.ENTITY_ID.qualify(e1).compareEquals(RelationsTable.LHS_EID), RelationsTable.RHS_EID.compareEquals(EntitiesTable.ENTITY_ID.qualify(e2)))) {
      QualifiedColumn<Integer> methodIDsel = EntitiesTable.ENTITY_ID.qualify(e1);
      QualifiedColumn<String> methodFQNsel = EntitiesTable.FQN.qualify(e1);
      QualifiedColumn<String> methodParamsSel = EntitiesTable.PARAMS.qualify(e1);
      QualifiedColumn<String> methodReturnFQNsel = EntitiesTable.FQN.qualify(e2);
      selectMethods.addSelect(methodIDsel, methodFQNsel, methodParamsSel, methodReturnFQNsel);
      selectMethods.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.CRAWLED), EntitiesTable.ENTITY_TYPE.qualify(e1).compareEquals(Entity.METHOD), RelationsTable.RELATION_TYPE.compareEquals(Relation.RETURNS));
//      selectMethods.setLimit(100);
      
      TypedQueryResult result = selectMethods.selectStreamed();
      while (result.next()) {
        Integer entityID = result.getResult(methodIDsel);
        String fqn = result.getResult(methodFQNsel);
        String params = result.getResult(methodParamsSel);
        String returnType = result.getResult(methodReturnFQNsel);

        if (entityID == null || fqn == null || params == null || returnType == null) {
          task.report(Level.SEVERE, "Missing information for: " + entityID + " (" + returnType + " "+ fqn + params + ")");
        } else {
          SolrInputDocument doc = new SolrInputDocument();
          doc.addField("entity_id", entityID);
          doc.addField("fqn", fqn);
          doc.addField("params", params);
          doc.addField("param_count", TypeUtils.countParams(params));
          doc.addField("return_fqn", returnType);
          add(doc);
          task.progress();
        }
      }
    }
    task.finish();
  }
}
