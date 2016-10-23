/*
 * Copyright (C) 2016 Robert Eggar.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.reggar.includehashequals;

import com.google.auto.value.processor.AutoValueProcessor;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public final class AutoValueIncludeHashEqualsExtensionTest {
    private JavaFileObject nullable;

    @Before
    public void setUp() {
        nullable = JavaFileObjects.forSourceString("test.Nullable", ""
                + "package test;\n"
                + "import java.lang.annotation.Retention;\n"
                + "import java.lang.annotation.Target;\n"
                + "import static java.lang.annotation.ElementType.FIELD;\n"
                + "import static java.lang.annotation.ElementType.METHOD;\n"
                + "import static java.lang.annotation.ElementType.PARAMETER;\n"
                + "import static java.lang.annotation.RetentionPolicy.CLASS;\n"
                + "@Retention(CLASS)\n"
                + "@Target({METHOD, PARAMETER, FIELD})\n"
                + "public @interface Nullable {\n"
                + "}");
    }

    @Test
    public void simple() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.github.reggar.includehashequals.IncludeHashEquals;\n"
                + "@AutoValue public abstract class Test {\n"
                + "@IncludeHashEquals @Nullable public abstract String a();\n"
                + "@IncludeHashEquals public abstract int b();\n"
                + "public abstract long c();\n"
                + "public abstract float d();\n"
                + "public abstract double e();\n"
                + "public abstract boolean f();\n"
                + "public abstract int[] g();\n"
                + "public abstract String h();\n"
                + "@Nullable public abstract String i();\n"
                + "}\n"
        );

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Object;\n"
                + "import java.lang.Override;\n"
                + "import java.lang.String;\n"
                + "\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "  AutoValue_Test(String a, int b, long c, float d, double e, boolean f, int[] g, String h, String i) {\n"
                + "    super(a, b, c, d, e, f, g, h, i);\n"
                + "  }\n"
                + "\n"
                + "  @Override\n"
                + "  public final boolean equals(Object o) {\n"
                + "    if (o == this) {\n"
                + "      return true;\n"
                + "    }\n"
                + "    if (o instanceof Test) {\n"
                + "      Test that = (Test) o;\n"
                + "      return ((this.a() == null) ? (that.a() == null) : this.a().equals(that.a()))\n"
                + "          && (this.b() == that.b());\n"
                + "    }\n"
                + "    return false;\n"
                + "  }\n"
                + "\n"
                + "  @Override\n"
                + "  public final int hashCode() {\n"
                + "    int h = 1;\n"
                + "    h *= 1000003;\n"
                + "    h ^= (a() == null) ? 0 : this.a().hashCode();\n"
                + "    h *= 1000003;\n"
                + "    h ^= this.b();\n"
                + "    return h;\n"
                + "  }\n"
                + "}"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(nullable, source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }
}
