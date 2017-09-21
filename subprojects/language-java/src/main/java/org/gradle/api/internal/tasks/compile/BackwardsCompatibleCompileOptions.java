/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.internal.tasks.compile;

import org.apache.commons.lang.StringUtils;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.util.DeprecationLogger;

import java.io.File;

public class BackwardsCompatibleCompileOptions extends CompileOptions {
    private final FileResolver fileResolver;

    public BackwardsCompatibleCompileOptions(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    /**
     * Returns the bootstrap classpath to be used for the compiler process. Defaults to {@code null}.
     *
     * @deprecated Use {@link #getBootstrapClasspath()} instead.
     */
    @Deprecated
    @Internal
    public String getBootClasspath() {
        DeprecationLogger.nagUserOfReplacedProperty("CompileOptions.bootClasspath", "CompileOptions.bootstrapClasspath");
        return getBootstrapClasspath() == null ? null : getBootstrapClasspath().getAsPath();
    }

    /**
     * Sets the bootstrap classpath to be used for the compiler process. Defaults to {@code null}.
     *
     * @deprecated Use {@link #setBootstrapClasspath(FileCollection)} instead.
     */
    @Deprecated
    public void setBootClasspath(String bootClasspath) {
        DeprecationLogger.nagUserOfReplacedProperty("CompileOptions.bootClasspath", "CompileOptions.bootstrapClasspath");
        if (bootClasspath == null) {
            setBootstrapClasspath(null);
        } else {
            Object[] paths = StringUtils.split(bootClasspath, File.pathSeparatorChar);
            setBootstrapClasspath(fileResolver.resolveFiles(paths));
        }
    }
}
