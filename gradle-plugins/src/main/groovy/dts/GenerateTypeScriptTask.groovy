package dts

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.file.FileCollection

/**
 * Gradle task that generates TypeScript definition (.d.ts) files from Java API sources.
 * 
 * Usage in build.gradle:
 * 
 * tasks.withType(GenerateTypeScriptTask).configureEach {
 *     sourceDirectories = ['src/api/java']  // Strings are converted to Files
 *     outputDirectory = 'src/main/resources/${modid}/api'  // String converted to File
 *     apiPackages = ['noppes.npcs.api', 'kamkeel.npcdbc.api']
 * }
 */
abstract class GenerateTypeScriptTask extends DefaultTask {
    
    // Accept Object types (String or File) with @Internal annotation
    @Internal
    List<Object> sourceDirectories = []
    
    @Internal
    Object outputDirectory
    
    @Input
    Set<String> apiPackages = []
    
    @Input
    Boolean cleanOutputFirst = false
    
    @Input
    List<String> excludePatterns = []
    
    // Provide task input/output properties that Gradle can validate
    @InputFiles
    protected List<File> getResolvedSourceDirectories() {
        return sourceDirectories.collect { obj ->
            if (obj instanceof File) return obj
            String pathStr = obj.toString()
            if (pathStr.contains('${modid}')) {
                String modid = project.archivesBaseName ?: 'mod'
                pathStr = pathStr.replace('${modid}', modid)
            }
            File f = new File(pathStr)
            if (!f.isAbsolute()) {
                f = new File(project.projectDir, pathStr)
            }
            return f
        }
    }
    
    @OutputDirectory
    protected File getResolvedOutputDirectory() {
        def obj = outputDirectory
        if (obj instanceof File) return obj
        String pathStr = obj.toString()
        if (pathStr.contains('${modid}')) {
            String modid = project.archivesBaseName ?: 'mod'
            pathStr = pathStr.replace('${modid}', modid)
        }
        File f = new File(pathStr)
        if (!f.isAbsolute()) {
            f = new File(project.projectDir, pathStr)
        }
        return f
    }
    
    /**
     * Converts a value (String or File) to a File object.
     * Supports special tokens like ${modid}.
     */
    // conversion logic inlined into getters to avoid calling private helper at configuration time
    
    GenerateTypeScriptTask() {
        group = 'api'
        description = 'Generates TypeScript definition files from Java API sources'
    }
    
    @TaskAction
    void generate() {
        List<File> srcDirs = getResolvedSourceDirectories()
        File outDir = getResolvedOutputDirectory()
        
        logger.lifecycle("=".multiply(60))
        logger.lifecycle("Generating TypeScript definitions...")
        logger.lifecycle("=".multiply(60))
        
        if (srcDirs.isEmpty()) {
            logger.warn("No source directories specified!")
            return
        }
        
        // Validate directories
        srcDirs.each { dir ->
            if (!dir.exists()) {
                logger.warn("Source directory does not exist: ${dir}")
            } else {
                logger.lifecycle("Source: ${dir}")
            }
        }
        
        logger.lifecycle("Output: ${outDir}")
        logger.lifecycle("API Packages: ${apiPackages}")
        
        // Clean output if requested
        if (cleanOutputFirst && outDir.exists()) {
            logger.lifecycle("Cleaning output directory...")
            outDir.eachFileRecurse { file ->
                if (file.name.endsWith('.d.ts') && !file.name.equals('minecraft-raw.d.ts') && !file.name.equals('forge-events-raw.d.ts')) {
                    file.delete()
                }
            }
        }
        
        // Create converter and process
        JavaToTypeScriptConverter converter = new JavaToTypeScriptConverter(outDir, apiPackages)
        
        List<File> validDirs = srcDirs.findAll { it.exists() }
        if (validDirs.isEmpty()) {
            logger.error("No valid source directories found!")
            return
        }
        
        converter.processDirectories(validDirs, logger)
        
        logger.lifecycle("=".multiply(60))
        logger.lifecycle("TypeScript definition generation complete!")
        logger.lifecycle("=".multiply(60))
    }
}
