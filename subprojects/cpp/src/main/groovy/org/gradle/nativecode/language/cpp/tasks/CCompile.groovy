/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.nativecode.language.cpp.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.*
import org.gradle.language.jvm.internal.SimpleStaleClassCleaner
import org.gradle.nativecode.base.ToolChain
import org.gradle.nativecode.language.cpp.internal.DefaultCCompileSpec

import javax.inject.Inject

// TODO:DAZ Deal with duplication with CppCompile
/**
 * Compiles C++ source files into object files.
 */
@Incubating
class CCompile extends DefaultTask {
    private FileCollection source

    ToolChain toolChain

    @Input
    boolean positionIndependentCode

    @OutputDirectory
    File objectFileDir

    @InputFiles
    FileCollection includes

    @InputFiles @SkipWhenEmpty // Workaround for GRADLE-2026
    FileCollection getSource() {
        source
    }

    // Invalidate output when the tool chain output changes
    @Input
    def getOutputType() {
        return toolChain.outputType
    }

    @Input
    List<String> macros

    @Input
    List<String> compilerArgs

    @Inject
    CCompile() {
        includes = project.files()
        source = project.files()
    }

    @TaskAction
    void compile() {
        def cleaner = new SimpleStaleClassCleaner(getOutputs())
        cleaner.setDestinationDir(getObjectFileDir())
        cleaner.execute()

        def spec = new DefaultCCompileSpec()
        spec.tempDir = getTemporaryDir()

        spec.objectFileDir = getObjectFileDir()
        spec.includeRoots = getIncludes()
        spec.source = getSource()
        spec.macros = getMacros()
        spec.args = getCompilerArgs()
        if (isPositionIndependentCode()) {
            spec.positionIndependentCode = true
        }

        def result = toolChain.createCCompiler().execute(spec)
        didWork = result.didWork
    }

    void includes(SourceDirectorySet dirs) {
        includes.from({dirs.srcDirs})
    }

    void includes(FileCollection includeRoots) {
        includes.from(includeRoots)
    }

    void source(Object sourceFiles) {
        source.from sourceFiles
    }
}
