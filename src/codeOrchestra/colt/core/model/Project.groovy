package codeOrchestra.colt.core.model

import codeOrchestra.groovyfx.FXBindable
import groovy.xml.MarkupBuilder

/**
 * @author Dima Kruk
 */
abstract class Project {

    @FXBindable String name = ""
    String path

    boolean disposed

    @FXBindable boolean newProject

    // Default aspects
    abstract ProjectPaths getProjectPaths()
    abstract ProjectLiveSettings getProjectLiveSettings()
    abstract ProjectBuildSettings getProjectBuildSettings()

    abstract String getProjectType()

    abstract Closure buildXml()

    void buildModel(Object node) {
        name = node.@projectName
    }

    String toXmlString() {
        StringWriter writer = new StringWriter()
        new MarkupBuilder(writer).xml(projectName:name, projectType:getProjectType(), buildXml())
        writer.toString()
    }

    void fromXmlString(String source) {
        buildModel(new XmlSlurper().parseText(source))
    }

    public File getBaseDir() {
        return new File(path).getParentFile();
    }

    abstract File getOutputDir()

    abstract String getWebOutputPath()

}
