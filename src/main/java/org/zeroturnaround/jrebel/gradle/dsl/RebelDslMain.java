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

import groovy.lang.Closure;

import java.io.File;


import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.gradle.util.ConfigureUtil;

/**
 * The parent class of the Rebel plugin's DSL extension. 
 * 
 * @author Sander Sõnajalg
 */
public class RebelDslMain {

  private String packaging;

  // TODO obsolete?
  private File classesDirectory;

  // TODO obsolete?
  private File resourcesDirectory;

  private String warSourceDirectory;

  private String webappDirectory;

  private RebelDslClasspath classpath;

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  private RebelClasspath resourcesClasspath;
  
  private String rootPath;

  // TODO obsolete?
  private File relativePath;

  private String rebelXmlDirectory;

  private Boolean showGenerated;

  private Boolean addResourcesDirToRebelXml;

  private Boolean alwaysGenerate;

  // TODO rename
  private RebelDslWeb web;

  private RebelDslWar war;
  
  public RebelDslMain() {
  }
  
  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public File getClassesDirectory() {
    return classesDirectory;
  }

  public void setClassesDirectory(File classesDirectory) {
    this.classesDirectory = classesDirectory;
  }

  public File getResourcesDirectory() {
    return resourcesDirectory;
  }

  public void setResourcesDirectory(File resourcesDirectory) {
    this.resourcesDirectory = resourcesDirectory;
  }

  public String getWarSourceDirectory() {
    return warSourceDirectory;
  }

  public void setWarSourceDirectory(String warSourceDirectory) {
    this.warSourceDirectory = warSourceDirectory;
  }

  public String getWebappDirectory() {
    return webappDirectory;
  }

  public void setWebappDirectory(String webappDirectory) {
    this.webappDirectory = webappDirectory;
  }

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  public RebelClasspath getResourcesClasspath() {
    return resourcesClasspath;
  }

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  public void setResourcesClasspath(RebelClasspath resourcesClasspath) {
    this.resourcesClasspath = resourcesClasspath;
  }

  public RebelDslWeb getWeb() {
    return web;
  }
  
  public void setWeb(RebelDslWeb _web) {
    this.web = _web;
  }

  public RebelDslClasspath getClasspath() {
    return classpath;
  }
  
  public void setClasspath(RebelDslClasspath _classpath) {
    this.classpath = _classpath;
  }
  
  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public File getRelativePath() {
    return relativePath;
  }

  public void setRelativePath(File relativePath) {
    this.relativePath = relativePath;
  }

  public String getRebelXmlDirectory() {
    return rebelXmlDirectory;
  }

  public void setRebelXmlDirectory(String rebelXmlDirectory) {
    this.rebelXmlDirectory = rebelXmlDirectory;
  }

  public Boolean getShowGenerated() {
    return showGenerated;
  }

  public void setShowGenerated(Boolean showGenerated) {
    this.showGenerated = showGenerated;
  }

  public Boolean getAddResourcesDirToRebelXml() {
    return addResourcesDirToRebelXml;
  }

  public void setAddResourcesDirToRebelXml(Boolean addResourcesDirToRebelXml) {
    this.addResourcesDirToRebelXml = addResourcesDirToRebelXml;
  }

  public Boolean getAlwaysGenerate() {
    return alwaysGenerate;
  }

  public void setAlwaysGenerate(Boolean alwaysGenerate) {
    this.alwaysGenerate = alwaysGenerate;
  }

  public RebelDslWar getWar() {
    return war;
  }

  public void setWar(RebelDslWar war) {
    this.war = war;
  }

  /**
   * Evaluate the 'classpath {..}' block
   */
  public void classpath(Closure closure) {
    classpath = new RebelDslClasspath();
    ConfigureUtil.configure(closure, classpath);
  }
  
  /**
   * Evaluate the 'web {..}' block
   */
  public void web(Closure closure) {
    web = new RebelDslWeb();
    ConfigureUtil.configure(closure, web);
  }
  
  /**
   * Evaluate the 'war {..}' block
   */
  public void war(Closure closure) {
    war = new RebelDslWar();
    ConfigureUtil.configure(closure, war);
  }

  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("packaging", packaging);
    builder.append("classesDirectory", classesDirectory);
    builder.append("resourcesDirectory", resourcesDirectory);
    builder.append("warSourceDirectory", warSourceDirectory);
    builder.append("webappDirectory", webappDirectory);
    builder.append("classpath", classpath);
    builder.append("resourcesClasspath", resourcesClasspath);
    builder.append("web", web);
    builder.append("rootPath", rootPath);
    builder.append("relativePath", relativePath);
    builder.append("rebelXmlDirectory", rebelXmlDirectory);
    builder.append("showGenerated", showGenerated);
    builder.append("addResourcesDirToRebelXml", addResourcesDirToRebelXml);
    builder.append("alwaysGenerate", alwaysGenerate);
    builder.append("war", war);
    return builder.toString();
  }
  
}
