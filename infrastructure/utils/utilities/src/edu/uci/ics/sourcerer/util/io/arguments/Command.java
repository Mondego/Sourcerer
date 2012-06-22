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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import edu.uci.ics.sourcerer.util.io.logging.Logging;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Command {
  private String name;
  private String description;
  private Argument<?>[] properties;
  private Collection<Argument<?>[]> conditionalProperties;
  
  public Command(String name, String description) {
    this.name = name;
    this.description = description;
  }
  
  public Command setProperties(Argument<?> ... properties) {
    this.properties = properties;
    return this;
  }
  
  public Command setConditionalProperties(Argument<?> ... properties) {
    if (conditionalProperties == null) {
      conditionalProperties = new LinkedList<>();
    }
    conditionalProperties.add(properties);
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  protected String getDescription() {
    return description;
  }
  
  protected Argument<?>[] getProperties() {
    if (properties == null) {
      return new Argument<?>[0];
    } else {
      return properties;
    }
  }
  
  protected Collection<Argument<?>[]> getConditionalProperties() {
    if (conditionalProperties == null) {
      return Collections.emptyList();
    } else {
      return conditionalProperties;
    }
  }

  protected void execute() {
    Logging.initializeLogger(this);
    action();
  }
  
  protected abstract void action();
  
  public @interface Disable {}
  
  public static void execute(String[] args, Class<?> klass) {
    ArgumentManager.executeCommand(args, klass);
  }
}
