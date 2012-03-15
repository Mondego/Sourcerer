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
package edu.uci.ics.sourcerer.util;

import java.lang.ref.SoftReference;

import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class CachedReference<T> {
  public static final Argument<Boolean> DISABLE_REF_CACHING = new BooleanArgument("disable-ref-caching", false, "Forces cached references to never cache.").permit();
  private SoftReference<T> ref;
  
  protected abstract T create();
  
  public T get() {
    if (DISABLE_REF_CACHING.getValue()) {
      return create();
    } else {
      T result = null;
      if (ref != null) {
        result = ref.get();
      }
      if (result == null) {
        result = create();
        ref = new SoftReference<>(result);
      }
      return result;
    }
  }
  
  public T getIfCached() {
    if (ref == null) {
      return null;
    } else {
      return ref.get();
    }
  }
  
  public void clear() {
    if (ref != null) {
      ref.clear();
    }
  }
}
