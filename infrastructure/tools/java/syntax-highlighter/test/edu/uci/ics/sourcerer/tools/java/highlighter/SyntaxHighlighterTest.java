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
package edu.uci.ics.sourcerer.tools.java.highlighter;


import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.sourcerer.util.io.arguments.Command;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SyntaxHighlighterTest {
  public static final Command TEST = new Command("test", "Does nothing") {
    @Override
    protected void action() {
    }
  };
  
  @BeforeClass
  public static void setUpBeforeClass() {
    Command.execute(new String[] { "--test" }, SyntaxHighlighterTest.class);
  }

  @Test
  public void testHighlighter() {
//    try {
//      String code = FileUtils.getFileAsString(new File("test/edu/uci/ics/sourcerer/tools/java/highlighter/SyntaxHighlighterTest.java"));
//      TagInfo links = TagInfo.make();
//      links.addLinkLocation(1242, 7, "command");
//      links.addLinkLocation(1261, 7, "command2");
//      FileUtils.writeStringToFile(SyntaxHighlighter.highlightSyntax(code, links), new File("test/edu/uci/ics/sourcerer/tools/java/highlighter/test.html"));
//    } catch (IOException e) {}
  }
}
