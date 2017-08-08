/**
 *  Copyright (C) 2012 ZeroTurnaround <support@zeroturnaround.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.zeroturnaround.jrebel.gradle.dsl;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;

/**
 * Gradle DSL level model for war configuration (corresponds to RebelWar in backend model).
 * 
 * @author Sander Sonajalg
 */
public class RebelDslWar {

  private String dir;
  
  public String getDir() {
    return dir;
  }

  public void setDir(String value) {
    this.dir = value;
  }
  
  /**
   * Convert from DSL-level model objects to the backend model objects.
   */
  public RebelWar toRebelWar() {
    RebelWar war = new RebelWar();
    war.setDir(this.dir);
    return war;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("dir", this.dir);
    return builder.toString();
  }
  
}
