/**
   Copyright (C) 2012 ZeroTurnaround <support@zeroturnaround.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.zeroturnaround.jrebel.gradle;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;
import org.gradle.util.GradleVersion;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;
import org.zeroturnaround.jrebel.gradle.util.FileUtil;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

public class RebelGenerateTask extends DefaultTask {

  public static final String PACKAGING_TYPE_JAR = "jar";

  public static final String PACKAGING_TYPE_WAR = "war";

  public static final String GRADLE_PLUGIN_VERSION = extractVersionOfPluginFromManifest();

  public static final String GRADLE_VERSION = GradleVersion.current().getVersion();

  private LoggerWrapper log = new LoggerWrapper(getProject().getLogger());

  private String packaging;

  private RebelClasspath classpath;

  private RebelWeb web;

  private RebelWar war;

  private RebelMainModel rebelModel;

  private boolean skipWritingRebelXml;

  private boolean alwaysGenerate;

  private List<File> defaultClassesDirectories;

  private File defaultResourcesDirectory;

  private File defaultWebappDirectory;

  private boolean showGenerated;

  private File rebelXmlDirectory;

  private boolean isPluginConfigured;

  private String configuredRootPath;

  /**
   * XXX -- i'm not sure about this property at all. this is used in fixPath, so i don't dare to delete it as well.. ask
   *        Rein who probably originally introduced it to Maven plugin where it was copy-pasted from.
   */
  private File configuredRelativePath;

  public String getConfiguredRootPath() {
    return configuredRootPath;
  }

  public void setConfiguredRootPath(String path) {
    this.configuredRootPath = path;
  }

  public File getConfiguredRelativePath() {
    return configuredRelativePath;
  }

  public void setConfiguredRelativePath(File path) {
    this.configuredRelativePath = path;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public boolean getShowGenerated() {
    return showGenerated;
  }

  public void setShowGenerated(boolean showGenerated) {
    this.showGenerated = showGenerated;
  }

  public RebelClasspath getClasspath() {
    return classpath;
  }

  public void setClasspath(RebelClasspath path) {
    this.classpath = path;
  }

  public RebelWeb getWeb() {
    return web;
  }

  public void setWeb(RebelWeb web) {
    this.web = web;
  }

  public RebelWar getWar() {
    return war;
  }

  public void setWar(RebelWar _war) {
    this.war = _war;
  }

  public boolean getAlwaysGenerate() {
    return alwaysGenerate;
  }

  public void setAlwaysGenerate(boolean alwaysGenerate) {
    this.alwaysGenerate = alwaysGenerate;
  }

  public List<File> getDefaultClassesDirectory() {
    return defaultClassesDirectories;
  }

  public File getDefaultResourcesDirectory() {
    return defaultResourcesDirectory;
  }

  public File getDefaultWebappDirectory() {
    return defaultWebappDirectory;
  }

  public File getRebelXmlDirectory() {
    return rebelXmlDirectory;
  }

  /**
   * Getter for the functional tests to examine the model
   */
  public RebelMainModel getRebelModel() {
    return rebelModel;
  }

  /**
   * Only for automated tests! Tests should not try to write the actual file.
   */
  public void skipWritingRebelXml() {
    this.skipWritingRebelXml = true;
  }

  /**
   * The RebelPlugin#configure block has been executed
   */
  public void setPluginConfigured() {
    this.isPluginConfigured = true;
  }

  private File getRebelXml() {
    if (rebelXmlDirectory == null) {
      return null;
    }
    return new File(rebelXmlDirectory, "rebel.xml");
  }

  /**
   * The actual invocation of our plugin task. Will construct the in-memory model (RebelXmlBuilder),
   * generate the XML output based on it and write the XML into a file-system file (rebel.xml).
   */
  @TaskAction
  public void generate() {
    // Only able to run if the 'RebelPlugin#configure' block has been executed, i.e. if the Java Plugin has been added.
    if (!isPluginConfigured) {
      throw new IllegalStateException(
        "generateRebel is only valid when JavaPlugin is applied directly or indirectly " +
        "(via other plugins that apply it implicitly, like Groovy or War); please update your build"
      );
    }

    propagateConventionMappingSettings();

    log.info("rebel.alwaysGenerate = " + alwaysGenerate);
    log.info("rebel.showGenerated = " + showGenerated);
    log.info("rebel.rebelXmlDirectory = " + rebelXmlDirectory);
    log.info("rebel.packaging = " + packaging);
    log.info("rebel.war = " + war);
    log.info("rebel.web = " + web);
    log.info("rebel.classpath = " + classpath);
    log.info("rebel.defaultClassesDirectories = " + defaultClassesDirectories);
    log.info("rebel.defaultResourcesDirectory = " + defaultResourcesDirectory);
    log.info("rebel.defaultWebappDirectory = " + defaultWebappDirectory);
    log.info("rebel.configuredRootPath = " + configuredRootPath);
    log.info("rebel.configuredRelativePath = " + configuredRelativePath);

    File rebelXmlFile = getRebelXml();
    File buildXmlFile = getProject().getBuildFile();

    if (!alwaysGenerate && (rebelXmlFile != null) && rebelXmlFile.exists() && (buildXmlFile != null) && buildXmlFile.exists() && rebelXmlFile.lastModified() > buildXmlFile.lastModified()) {
      return;
    }

    // find the type of the project
    if (getPackaging().equals(PACKAGING_TYPE_JAR)) {
      rebelModel = buildModelForJar();
    }
    else if (getPackaging().equals(PACKAGING_TYPE_WAR)) {
      rebelModel = buildModelForWar();
    }

    if (rebelModel != null && !skipWritingRebelXml) {
      generateRebelXml(rebelXmlFile);
    }
  }

  private static String extractVersionOfPluginFromManifest() {
    String result = RebelGenerateTask.class.getPackage().getImplementationVersion();
    return result == null ? "Unknown" : result;
  }

  /**
   * Construct a builder for jar projects
   */
  private RebelMainModel buildModelForJar() {
    log.info("Building rebel backend model for jar ..");
    RebelMainModel model = new RebelMainModel();

    buildClasspath(model);

    log.info("Backend model eventually built: " + model);
    return model;
  }

  /**
   * Construct a builder for war projects
   */
  private RebelMainModel buildModelForWar() {
    log.info("Building rebel backend model for war ..");
    RebelMainModel model = new RebelMainModel();

    buildWeb(model);
    buildClasspath(model);
    buildWar(model);

    log.info("Backend model eventually built: " + model);
    return model;
  }

  /**
   * Compile the model that corresponds to the <classpath> node in rebel.xml.
   */
  private void buildClasspath(RebelMainModel model) {

    // User has defined no 'classpath {}' block in the DSL configuration. Just add the default and return.
    if (classpath == null) {
      log.info("No custom classpath configuration found .. using the defaults");
      buildDefaultClasspath(model, null);
    }

    // User has provided custom 'classpath {}' configuration
    else {
      // Search for the default element. If we find it, we have to place it exactly into the same place where we
      // found it (preserving the order). If we *don't* find it, we'll add the default classpath as first element.
      boolean addDefaultAsFirst = true;
      RebelClasspathResource defaultClasspath = null;

      // Just search for the default element. Don't add anything anywhere yet.
      for (RebelClasspathResource resource : classpath.getResources()) {
        // we found the default.
        if (resource.isDefaultClasspathElement()) {
          addDefaultAsFirst = false;
          defaultClasspath = resource;
          break;
        }
      }

      // Default classpath element not found. Put the default as first.
      if (addDefaultAsFirst) {
        // check if configuration allows adding the default
        buildDefaultClasspath(model, defaultClasspath);
      }

      // Iterate through all classpath elements and add them.
      for (RebelClasspathResource resource : classpath.getResources()) {

        // Special treatment for the default.
        if (resource.isDefaultClasspathElement()) {
          buildDefaultClasspath(model, resource);
        }
        // An ordinary element. Add it.
        else {
          // TODO TODO TODO add the fixpath stuff!! --- better implementation!!
          // TODO fix paths for other elements as well!
          resource.setDirectory(fixFilePath(resource.getDirectory()));
          model.addClasspathDir(resource);
        }
      }
    }
  }

  /**
   * Add the default classes directory to classpath
   */
  private void buildDefaultClasspath(RebelMainModel model, RebelClasspathResource defaultClasspath) throws BuildException {
    // Add default resources dir to rebel.xml unless user's configuration disallows it
    if (classpath == null || !classpath.isOmitDefaultResourcesDir()) {
      addDefaultResourcesDirToClasspath(model);
    }

    // Add default classes dir to rebel.xml unless user's configuration disallows it
    if (classpath == null || !classpath.isOmitDefaultClassesDir()) {
      addDefaultClassesDirToClasspath(model, defaultClasspath);
    }
  }

  /**
   * Add the default classes directory to classpath (create the dirs if dont exist yet)
   */
  private void addDefaultClassesDirToClasspath(RebelMainModel model, RebelClasspathResource defaultClasspath) {
    for (File classesDir : defaultClassesDirectories) {
      // project output directory
      RebelClasspathResource classpathResource = new RebelClasspathResource();

      String fixedDefaultClassesDirectory = fixFilePath(classesDir);
      log.info("fixed default classes directory : " + fixedDefaultClassesDirectory);

      classpathResource.setDirectory(fixedDefaultClassesDirectory);

      if (defaultClasspath != null) {
        classpathResource.setIncludes(defaultClasspath.getIncludes());
        classpathResource.setExcludes(defaultClasspath.getExcludes());
      }

      model.addClasspathDir(classpathResource);
      createIfDoesNotExist(classesDir);
    }
  }

  /**
   * Add the default resources directory to classpath (create the dir if dont exist yet)
   */
  private void addDefaultResourcesDirToClasspath(RebelMainModel model) throws BuildException {
    log.info("Adding default resources directory to classpath ..");

    RebelClasspathResource resourcesClasspathResource = new RebelClasspathResource();
    String fixedDefaultResourcesDir = fixFilePath(defaultResourcesDirectory);
    log.info("Default resources directory after normalizing: " + fixedDefaultResourcesDir);

    resourcesClasspathResource.setDirectory(fixedDefaultResourcesDir);
    model.addClasspathDir(resourcesClasspathResource);
    createIfDoesNotExist(defaultResourcesDirectory);
  }

  /**
   * Build the model for the <web> element in rebel.xml
   */
  private void buildWeb(RebelMainModel model) {

    // User has not devfined a 'web {}' block
    if (web == null) {
      buildDefaultWeb(model, null);
    }

    // A 'web {}' block was defined in configuration DSL
    else {

      // Go through all elements, look up the default one
      boolean addDefaultAsFirst = true;
      RebelWebResource defaultWeb = null;

      for (RebelWebResource resource : web.getResources()) {
        if (resource.isDefaultElement()) {
          defaultWeb = resource;
          addDefaultAsFirst = false;
          break;
        }
      }

      // Add the default one as first, if a specific location was not specified by the empty element
      if (addDefaultAsFirst) {
        if (web != null && !web.getOmitDefault()) {
          buildDefaultWeb(model, defaultWeb);
        }
      }

      // Add all the other elements from the user's configuration
      List<RebelWebResource> resources = web.getResources();
      if (resources != null && resources.size() > 0) {
        for (int i = 0; i < resources.size(); i++) {
          RebelWebResource resource = resources.get(i);

          // Add the default element
          if (resource.isDefaultElement()) {
            if (!web.getOmitDefault()) {
              buildDefaultWeb(model, resource);
            }
          }
          // Add a normal, non-default element
          else {
            resource.setDirectory(fixFilePath(resource.getDirectory()));
            model.addWebResource(resource);
          }
        }
      }
    }
  }

  /**
   * The default for the <web> element in rebel.xml
   */
  private void buildDefaultWeb(RebelMainModel model, RebelWebResource defaultWeb) {
    RebelWebResource r = new RebelWebResource();
    r.setTarget("/");
    r.setDirectory(fixFilePath(defaultWebappDirectory));

    if (defaultWeb != null) {
      r.setIncludes(defaultWeb.getIncludes());
      r.setExcludes(defaultWeb.getExcludes());
    }

    model.addWebResource(r);
  }

  /**
   * Build model for thw <war> element in rebel.xml
   */
  private void buildWar(RebelMainModel model) {
    // fix the path on the RebelWar object (whoooh...not nicest and not the nicest placing)
    if (war != null) {
      if (war.getDir()!= null) {
        war.setOriginalDir(war.getDir());
        war.setDir(fixFilePath(war.getDir()));
      }
      if (war.getFile()!= null) {
        war.setOriginalFile(war.getFile());
        war.setFile(fixFilePath(war.getFile()));
      }
      model.setWar(war);
    }
  }

  private void generateRebelXml(File rebelXmlFile) {
    // TODO replacement of those placeholders does not work and probably has never worked (probably copy-pasted from maven plugin). REPLACE!
    log.info("Processing ${project.group}:${project.name} with packaging " + getPackaging());
    log.info("Generating \"${rebelXmlFile}\"...");

    // Do generate the rebel.xml
    try {
      String xmlFileContents = getRebelModel().toXmlString();

      // Print generated rebel.xml out to console if user wants to see it
      if (getShowGenerated()) {
        System.out.println(xmlFileContents);
      }

      // Write out the rebel.xml file
      rebelXmlFile.getParentFile().mkdirs();
      FileUtil.writeToFile(rebelXmlFile, xmlFileContents);
    }
    catch (IOException e) {
      throw new BuildException("Failed writing \"${rebelXmlFile}\"", e);
    }
  }

  /**
   * Get the absolute, normalized path.
   * XXX maybe should be moved to an external utility class
   */
  private String fixFilePath(File file) {
    File baseDir = getProject().getProjectDir();

    if (file.isAbsolute() && !FileUtil.isRelativeToPath(new File(baseDir, getRelativePath()), file)) {
      return StringUtils.replace(FileUtil.getCanonicalPath(file), "\\", "/");
    }

    if (!file.isAbsolute()) {
      file = new File(baseDir, file.getPath());
    }

    String relative = FileUtil.getRelativePath(new File(baseDir, getRelativePath()), file);

    if (!(new File(relative)).isAbsolute()) {
      return StringUtils.replace(getRootPath(), "\\", "/") + "/" + relative;
    }

    // relative path was outside baseDir

    // if root path is absolute then try to get a path relative to root
    if ((new File(getRootPath())).isAbsolute()) {
      String s = FileUtil.getRelativePath(new File(getRootPath()), file);

      if (!(new File(s)).isAbsolute()) {
        return StringUtils.replace(getRootPath(), "\\", "/") + "/" + s;
      }
      else {
        // root path and the calculated path are absolute, so
        // just return calculated path
        return s;
      }
    }

    // return absolute path to file
    return StringUtils.replace(file.getAbsolutePath(), "\\", "/");
  }

  private String fixFilePath(String path) {
    return fixFilePath(new File(path));
  }

  private String getRelativePath() {
    if (getConfiguredRelativePath() != null) {
      return getConfiguredRelativePath().getAbsolutePath();
    }
    else {
      return ".";
    }
  }

  private String getRootPath() {
    if (getConfiguredRootPath() != null) {
      return getConfiguredRootPath();
    }
    else {
      return getProject().getProjectDir().getAbsolutePath();
    }
  }

  private void createIfDoesNotExist(File dir) {
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  /* ====================================================================================================
   *   Properties intercepted by Gradle's convention-mapping byte code magic. These methods will actually
   *   be intercepted and return values set by the callback set up in RebelPlugin#configure.
   *
   *   These properties are cached into local variables to lessen the magic. See propagateConventionMappingSettings().
   */

  public static final String NAME_DEFAULT_CLASSES_DIRECTORIES = "defaultClassesDirectories$MAGIC";

  public static final String NAME_DEFAULT_RESOURCES_DIRECTORY = "defaultResourcesDirectory$MAGIC";

  public static final String NAME_DEFAULT_WEBAPP_DIRECTORY = "defaultWebappDirectory$MAGIC";

  public static final String NAME_REBEL_XML_DIRECTORY = "rebelXmlDirectory$MAGIC";

  public List<File> getDefaultClassesDirectories$MAGIC() {
    return null;
  }

  public File getDefaultResourcesDirectory$MAGIC() {
    return null;
  }

  public File getDefaultWebappDirectory$MAGIC() {
    return null;
  }

  public File getRebelXmlDirectory$MAGIC() {
    return null;
  }

  /**
   * Let the convention-mappings propagate its settings to me through the magic getters,
   * save copies of them locally into normal instance variables.
   *
   * (public only for unit tests)
   */
  public void propagateConventionMappingSettings() {
    defaultClassesDirectories = getDefaultClassesDirectories$MAGIC();
    defaultResourcesDirectory = getDefaultResourcesDirectory$MAGIC();
    defaultWebappDirectory = getDefaultWebappDirectory$MAGIC();
    rebelXmlDirectory = getRebelXmlDirectory$MAGIC();
  }

  // ========== END OF convention-mapping's intercepted magic methods

}