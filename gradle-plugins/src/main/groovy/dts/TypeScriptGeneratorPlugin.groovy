package dts

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin for generating TypeScript definition files from Java API sources.
 */
class TypeScriptGeneratorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.tasks.findByName('generateTypeScriptDefinitions') == null) {
            project.tasks.register('generateTypeScriptDefinitions', GenerateTypeScriptTask)
        }
    }
}