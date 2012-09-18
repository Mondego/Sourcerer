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
package edu.uci.ics.sourcerer.util.io.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class IOUtilFactory {
  public static SimpleSerializer makeSimpleSerializer(Argument<File> file) throws IOException {
    return SimpleSerializerImpl.make(file.getValue());
  }
  
  public static SimpleSerializer makeSimpleSerializer(File file) throws IOException {
    return SimpleSerializerImpl.make(file);
  }
  
  public static SimpleSerializer resumeSimpleSerializer(Argument<File> file) throws IOException {
    return SimpleSerializerImpl.resume(file.getValue());
  }
  
  public static SimpleDeserializer makeSimpleDeserializer(Argument<File> file) throws IOException {
    return SimpleDeserializerImpl.create(file.getValue());
  }
  
  public static SimpleDeserializer makeSimpleDeserializer(File file) throws IOException {
    return SimpleDeserializerImpl.create(file);
  }
  
  public static SimpleDeserializer createSimpleDeserializer(InputStream is) throws IOException {
    return SimpleDeserializerImpl.create(is);
  }
}
