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
package com.github.reggar.ignorehashequals;

import com.google.auto.value.processor.AutoValueProcessor;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public final class AutoValueIgnoreHashEqualsExtensionTest {
  private JavaFileObject ignoreHashEquals;
  private JavaFileObject includeHashEquals;
  private JavaFileObject nullable;

  @Before public void setUp() {
    ignoreHashEquals = JavaFileObjects.forSourceString("test.IgnoreHashEquals", ""
        + "package test;\n"
        + "import java.lang.annotation.Retention;\n"
        + "import java.lang.annotation.Target;\n"
        + "import static java.lang.annotation.ElementType.FIELD;\n"
        + "import static java.lang.annotation.ElementType.METHOD;\n"
        + "import static java.lang.annotation.ElementType.PARAMETER;\n"
        + "import static java.lang.annotation.RetentionPolicy.SOURCE;\n"
        + "@Retention(SOURCE)\n"
        + "@Target({METHOD, PARAMETER, FIELD})\n"
        + "public @interface IgnoreHashEquals {\n"
        + "}");
    includeHashEquals = JavaFileObjects.forSourceString("test.IncludeHashEquals", ""
        + "package test;\n"
        + "import java.lang.annotation.Retention;\n"
        + "import java.lang.annotation.Target;\n"
        + "import static java.lang.annotation.ElementType.FIELD;\n"
        + "import static java.lang.annotation.ElementType.METHOD;\n"
        + "import static java.lang.annotation.ElementType.PARAMETER;\n"
        + "import static java.lang.annotation.RetentionPolicy.SOURCE;\n"
        + "@Retention(SOURCE)\n"
        + "@Target({METHOD, PARAMETER, FIELD})\n"
        + "public @interface IncludeHashEquals {\n"
        + "}");
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

  @Test public void ignoreHashEqualsAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
            + "package test;\n"
            + "import com.google.auto.value.AutoValue;\n"
            + "@AutoValue public abstract class Test {\n"
            + "public abstract int a();\n"
            + "@IgnoreHashEquals public abstract String b();\n"
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
                + "import java.util.Arrays;\n"
                + "\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "  AutoValue_Test(int a, String b, long c, float d, double e, boolean f, int[] g, String h, String i) {\n"
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
                + "      return (this.a() == that.a())\n"
                + "          && (this.c() == that.c())\n"
                + "          && (Float.floatToIntBits(this.d()) == Float.floatToIntBits(that.d()))\n"
                + "          && (Double.doubleToLongBits(this.e()) == Double.doubleToLongBits(that.e()))\n"
                + "          && (this.f() == that.f())\n"
                + "          && (Arrays.equals(this.g(), that.g()))\n"
                + "          && (this.h().equals(that.h()))\n"
                + "          && ((this.i() == null) ? (that.i() == null) : this.i().equals(that.i()));\n"
                + "    }\n"
                + "    return false;\n"
                + "  }\n"
                + "\n"
                + "  @Override\n"
                + "  public final int hashCode() {\n"
                + "    int h = 1;\n"
                + "    h *= 1000003;\n"
                + "    h ^= this.a();\n"
                + "    h *= 1000003;\n"
                + "    h ^= (this.c() >>> 32) ^ this.c();\n"
                + "    h *= 1000003;\n"
                + "    h ^= Float.floatToIntBits(this.d());\n"
                + "    h *= 1000003;\n"
                + "    h ^= (Double.doubleToLongBits(this.e()) >>> 32) ^ Double.doubleToLongBits(this.e());\n"
                + "    h *= 1000003;\n"
                + "    h ^= this.f() ? 1231 : 1237;\n"
                + "    h *= 1000003;\n"
                + "    h ^= java.util.Arrays.hashCode(this.g());\n"
                + "    h *= 1000003;\n"
                + "    h ^= this.h().hashCode();\n"
                + "    h *= 1000003;\n"
                + "    h ^= (i() == null) ? 0 : this.i().hashCode();\n"
                + "    return h;\n"
                + "  }\n"
                + "}"
    );

    assertAbout(javaSources())
        .that(Arrays.asList(ignoreHashEquals, nullable, source))
        .processedWith(new AutoValueProcessor())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void ignoreHashEqualsAnnotationForNestedClass() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Parent", ""
            + "package test;\n"
            + "import com.google.auto.value.AutoValue;\n"
            + "public class Parent {\n"
            + "@AutoValue public static abstract class Test {\n"
            + "public abstract int a();\n"
            + "@IgnoreHashEquals public abstract String b();\n"
            + "public abstract long c();\n"
            + "public abstract float d();\n"
            + "public abstract double e();\n"
            + "public abstract boolean f();\n"
            + "public abstract int[] g();\n"
            + "public abstract String h();\n"
            + "@Nullable public abstract String i();\n"
            + "}\n"
            + "}\n"
    );

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Parent_Test", ""
            + "package test;\n"
            + "\n"
            + "import java.lang.Object;\n"
            + "import java.lang.Override;\n"
            + "import java.lang.String;\n"
            + "import java.util.Arrays;\n"
            + "\n"
            + "final class AutoValue_Parent_Test extends $AutoValue_Parent_Test {\n"
            + "  AutoValue_Parent_Test(int a, String b, long c, float d, double e, boolean f, int[] g, String h, String i) {\n"
            + "    super(a, b, c, d, e, f, g, h, i);\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public final boolean equals(Object o) {\n"
            + "    if (o == this) {\n"
            + "      return true;\n"
            + "    }\n"
            + "    if (o instanceof Parent.Test) {\n"
            + "      Parent.Test that = (Parent.Test) o;\n"
            + "      return (this.a() == that.a())\n"
            + "          && (this.c() == that.c())\n"
            + "          && (Float.floatToIntBits(this.d()) == Float.floatToIntBits(that.d()))\n"
            + "          && (Double.doubleToLongBits(this.e()) == Double.doubleToLongBits(that.e()))\n"
            + "          && (this.f() == that.f())\n"
            + "          && (Arrays.equals(this.g(), that.g()))\n"
            + "          && (this.h().equals(that.h()))\n"
            + "          && ((this.i() == null) ? (that.i() == null) : this.i().equals(that.i()));\n"
            + "    }\n"
            + "    return false;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public final int hashCode() {\n"
            + "    int h = 1;\n"
            + "    h *= 1000003;\n"
            + "    h ^= this.a();\n"
            + "    h *= 1000003;\n"
            + "    h ^= (this.c() >>> 32) ^ this.c();\n"
            + "    h *= 1000003;\n"
            + "    h ^= Float.floatToIntBits(this.d());\n"
            + "    h *= 1000003;\n"
            + "    h ^= (Double.doubleToLongBits(this.e()) >>> 32) ^ Double.doubleToLongBits(this.e());\n"
            + "    h *= 1000003;\n"
            + "    h ^= this.f() ? 1231 : 1237;\n"
            + "    h *= 1000003;\n"
            + "    h ^= java.util.Arrays.hashCode(this.g());\n"
            + "    h *= 1000003;\n"
            + "    h ^= this.h().hashCode();\n"
            + "    h *= 1000003;\n"
            + "    h ^= (i() == null) ? 0 : this.i().hashCode();\n"
            + "    return h;\n"
            + "  }\n"
            + "}"
    );

    assertAbout(javaSources())
            .that(Arrays.asList(ignoreHashEquals, nullable, source))
            .processedWith(new AutoValueProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test public void includeHashEqualsAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
            + "package test;\n"
            + "import com.google.auto.value.AutoValue;\n"
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
            .that(Arrays.asList(includeHashEquals, nullable, source))
            .processedWith(new AutoValueProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test public void includeHashEqualsAnnotationForNestedClass() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Parent", ""
            + "package test;\n"
            + "import com.google.auto.value.AutoValue;\n"
            + "public class Parent {\n"
            + "@AutoValue public static abstract class Test {\n"
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
            + "}\n"
    );

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Parent_Test", ""
            + "package test;\n"
            + "\n"
            + "import java.lang.Object;\n"
            + "import java.lang.Override;\n"
            + "import java.lang.String;\n"
            + "\n"
            + "final class AutoValue_Parent_Test extends $AutoValue_Parent_Test {\n"
            + "  AutoValue_Parent_Test(String a, int b, long c, float d, double e, boolean f, int[] g, String h, String i) {\n"
            + "    super(a, b, c, d, e, f, g, h, i);\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public final boolean equals(Object o) {\n"
            + "    if (o == this) {\n"
            + "      return true;\n"
            + "    }\n"
            + "    if (o instanceof Parent.Test) {\n"
            + "      Parent.Test that = (Parent.Test) o;\n"
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
            .that(Arrays.asList(includeHashEquals, nullable, source))
            .processedWith(new AutoValueProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }


  @Test public void allFieldsIgnoredGenerateValidHashcodeEquals() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
            + "package test;\n"
            + "import com.google.auto.value.AutoValue;\n"
            + "@AutoValue public abstract class Test {\n"
            + "@IgnoreHashEquals public abstract int a();\n"
            + "}\n"
    );

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
            + "package test;\n"
            + "\n"
            + "import java.lang.Object;\n"
            + "import java.lang.Override;\n"
            + "\n"
            + "final class AutoValue_Test extends $AutoValue_Test {\n"
            + "  AutoValue_Test(int a) {\n"
            + "    super(a);\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public final boolean equals(Object o) {\n"
            + "    if (o == this) {\n"
            + "      return true;\n"
            + "    }\n"
            + "    if (o instanceof Test) {\n"
            + "      Test that = (Test) o;\n"
            + "      return true;\n"
            + "    }\n"
            + "    return false;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public final int hashCode() {\n"
            + "    int h = 1;\n"
            + "    return h;\n"
            + "  }\n"
            + "}"
    );

    assertAbout(javaSources())
            .that(Arrays.asList(ignoreHashEquals, nullable, source))
            .processedWith(new AutoValueProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
  }

  @Test public void annotationsAreMutuallyExclusive() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
            + "package test;\n"
            + "import com.google.auto.value.AutoValue;\n"
            + "@AutoValue public abstract class Test {\n"
            + "@IncludeHashEquals @Nullable public abstract String a();\n"
            + "@IncludeHashEquals public abstract int b();\n"
            + "@IgnoreHashEquals public abstract long c();\n"
            + "public abstract float d();\n"
            + "public abstract double e();\n"
            + "public abstract boolean f();\n"
            + "public abstract int[] g();\n"
            + "public abstract String h();\n"
            + "@Nullable public abstract String i();\n"
            + "}\n"
    );

    assertAbout(javaSources())
            .that(Arrays.asList(includeHashEquals, nullable, source))
            .processedWith(new AutoValueProcessor())
            .failsToCompile();
  }
}
