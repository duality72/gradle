/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.nativecode.toolchain.internal.msvcpp;

import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.internal.Factory;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativecode.base.internal.*;
import org.gradle.process.internal.ExecAction;

import java.io.File;

public class VisualCppToolChain extends AbstractToolChain {

    public static final String NAME = "visualCpp";
    static final String COMPILER_EXE = "cl.exe";
    static final String LINKER_EXE = "link.exe";
    static final String STATIC_LINKER_EXE = "lib.exe";

    private final File compilerExe;
    private final File linkerExe;
    private final File staticLinkerExe;
    private final Factory<ExecAction> execActionFactory;

    public VisualCppToolChain(OperatingSystem operatingSystem, Factory<ExecAction> execActionFactory) {
        super(operatingSystem);
        this.compilerExe = operatingSystem.findInPath(COMPILER_EXE);
        this.linkerExe = operatingSystem.findInPath(LINKER_EXE);
        this.staticLinkerExe = operatingSystem.findInPath(STATIC_LINKER_EXE);
        this.execActionFactory = execActionFactory;
    }

    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return "Visual C++";
    }

    @Override
    protected void checkAvailable(ToolChainAvailability availability) {
        availability.mustExist(COMPILER_EXE, compilerExe);
        availability.mustExist(LINKER_EXE, linkerExe);
        availability.mustExist(STATIC_LINKER_EXE, staticLinkerExe);
    }

    @Override
    public String getSharedLibraryLinkFileName(String libraryName) {
        return getSharedLibraryName(libraryName).replaceFirst("\\.dll$", ".lib");
    }

    public <T extends BinaryToolSpec> Compiler<T> createCppCompiler() {
        checkAvailable();
        return (Compiler<T>) new CppCompiler(compilerExe, execActionFactory);
    }

    public <T extends BinaryToolSpec> Compiler<T> createCCompiler() {
        checkAvailable();
        return (Compiler<T>) new CCompiler(compilerExe, execActionFactory);
    }

    public <T extends LinkerSpec> Compiler<T> createLinker() {
        return (Compiler<T>) new LinkExeLinker(linkerExe, execActionFactory);
    }

    public <T extends StaticLibraryArchiverSpec> Compiler<T> createStaticLibraryArchiver() {
        return (Compiler<T>) new LibExeStaticLibraryArchiver(staticLinkerExe, execActionFactory);
    }
}
