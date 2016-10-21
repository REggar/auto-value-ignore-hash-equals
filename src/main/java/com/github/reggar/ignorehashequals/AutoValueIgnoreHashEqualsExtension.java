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

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

@AutoService(AutoValueExtension.class) public final class AutoValueIgnoreHashEqualsExtension
    extends AutoValueExtension {

  @Override public boolean applicable(Context context) {
    Map<String, ExecutableElement> properties = context.properties();
    for (ExecutableElement element : properties.values()) {
      if (getAnnotations(element).contains("IgnoreHashEquals")) {
        return true;
      }
    }

    return false;
  }

  @Override public String generateClass(Context context, String className, String classToExtend,
      boolean isFinal) {
    String packageName = context.packageName();
    Name superName = context.autoValueClass().getSimpleName();
    Map<String, ExecutableElement> properties = context.properties();

    TypeSpec subclass = TypeSpec.classBuilder(className) //
        .addModifiers(isFinal ? Modifier.FINAL : Modifier.ABSTRACT) //
        .superclass(ClassName.get(packageName, classToExtend)) //
        .addMethod(generateConstructor(properties)) //
        .addMethod(generateEquals(superName, properties)) //
        .addMethod(generateHashCode(superName, properties)) //
        .build();

    JavaFile javaFile = JavaFile.builder(packageName, subclass).build();
    return javaFile.toString();
  }

  private static MethodSpec generateConstructor(Map<String, ExecutableElement> properties) {
    List<ParameterSpec> params = new ArrayList<>();
    for (Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
      TypeName typeName = TypeName.get(entry.getValue().getReturnType());
      params.add(ParameterSpec.builder(typeName, entry.getKey()).build());
    }

    StringBuilder body = new StringBuilder("super(");
    for (int i = properties.size(); i > 0; i--) {
      body.append("$N");
      if (i > 1) body.append(", ");
    }
    body.append(")");

    return MethodSpec.constructorBuilder() //
        .addParameters(params) //
        .addStatement(body.toString(), properties.keySet().toArray()) //
        .build();
  }

  private static MethodSpec generateHashCode(Name superName,
      Map<String, ExecutableElement> properties) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("hashCode") //
        .addAnnotation(Override.class) //
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL) //
        .returns(TypeName.INT) //
        .addCode("int h = 1;\n");

    for (Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
      ExecutableElement propertyElement = entry.getValue();
      Set<String> propertyAnnotations = getAnnotations(propertyElement);

      boolean ignored = propertyAnnotations.contains("IgnoreHashEquals");

      if (!ignored) {
        builder.addCode("h *= 1000003;\n");
        builder.addCode("h ^= " + generateHashCodeExpression(propertyElement) + ";");
      }

      builder.addCode("\n");
    }

    return builder
        .addCode("return h;\n")
        .build();
  }

  private static CodeBlock generateHashCodeExpression(ExecutableElement propertyElement) {
    String methodName = propertyElement.getSimpleName().toString();
    TypeName propertyType = TypeName.get(propertyElement.getReturnType());
    Set<String> propertyAnnotations = getAnnotations(propertyElement);

    boolean nullable = propertyAnnotations.contains("Nullable");

    if (propertyType.equals(TypeName.BYTE) || propertyType.equals(TypeName.SHORT) ||
        propertyType.equals(TypeName.CHAR) || propertyType.equals(TypeName.INT)) {
      return CodeBlock.of("this.$N()", methodName);
    } else if (propertyType.equals(TypeName.LONG)) {
      return CodeBlock.of("(this.$N() >>> 32) ^ this.$N()", methodName, methodName);
    } else if (propertyType.equals(TypeName.FLOAT)) {
      return CodeBlock.of("Float.floatToIntBits(this.$N())", methodName);
    } else if (propertyType.equals(TypeName.DOUBLE)) {
      return CodeBlock.of(
          "(Double.doubleToLongBits(this.$N()) >>> 32) ^ Double.doubleToLongBits(this.$N())",
          methodName, methodName);
    } else if (propertyType.equals(TypeName.BOOLEAN)) {
      return CodeBlock.of("this.$N() ? 1231 : 1237", methodName);
    } else if (propertyType instanceof ArrayTypeName) {
      return CodeBlock.of("$T.hashCode(this.$N())", Arrays.class, methodName);
    } else {
      if (nullable) {
        return CodeBlock.of("($N() == null) ? 0 : this.$N().hashCode()", methodName, methodName);
      } else {
        return CodeBlock.of("this.$N().hashCode()", methodName);
      }
    }
  }

  private static MethodSpec generateEquals(Name superName,
      Map<String, ExecutableElement> properties) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("equals") //
        .addAnnotation(Override.class) //
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL) //
        .returns(TypeName.BOOLEAN) //
        .addParameter(TypeName.OBJECT, "o")
        .addCode("if (o == this) {\n")
        .addCode("  return true;\n")
        .addCode("}\n")
        .addCode("if (o instanceof $L) {\n", superName)
        .addCode("  $L that = ($L) o;\n", superName, superName);

    if (properties.size() == 0) {
      builder.addCode("return true;\n");
    } else {
      builder.addCode("  return ");
    }

    List<ExecutableElement> nonIgnoredExecutableElements = new ArrayList<>();

    for(Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
      ExecutableElement propertyElement = entry.getValue();
      Set<String> propertyAnnotations = getAnnotations(propertyElement);

      boolean ignored = propertyAnnotations.contains("IgnoreHashEquals");

      if(!ignored) {
        nonIgnoredExecutableElements.add(propertyElement);
      }
    }

    boolean last = false;
    for (int i = 0; i < nonIgnoredExecutableElements.size(); i++) {
      ExecutableElement propertyElement = nonIgnoredExecutableElements.get(i);

      if(i == nonIgnoredExecutableElements.size() - 1) {
        last = true;
      }

      builder.addCode(generateEqualsExpression(propertyElement));
      if (!last) {
        builder.addCode("\n      && ");
      }
    }

    return builder
        .addCode(";\n")
        .addCode("}\n")
        .addCode("return false;\n")
        .build();
  }

  private static CodeBlock generateEqualsExpression(ExecutableElement propertyElement) {
    String methodName = propertyElement.getSimpleName().toString();
    TypeName propertyType = TypeName.get(propertyElement.getReturnType());
    Set<String> propertyAnnotations = getAnnotations(propertyElement);

    boolean nullable = propertyAnnotations.contains("Nullable");

    if (propertyType.equals(TypeName.FLOAT)) {
      return CodeBlock.of("(Float.floatToIntBits(this.$N()) == Float.floatToIntBits(that.$N()))",
          methodName, methodName);
    } else if (propertyType.equals(TypeName.DOUBLE)) {
      return CodeBlock.of(
          "(Double.doubleToLongBits(this.$N()) == Double.doubleToLongBits(that.$N()))", methodName,
          methodName);
    } else if (propertyType.isPrimitive()) {
      return CodeBlock.of("(this.$N() == that.$N())", methodName, methodName);
    } else if (propertyType instanceof ArrayTypeName) {
      return CodeBlock.of("($T.equals(this.$N(), that.$N()))", Arrays.class, methodName,
          methodName);
    } else {
      if (nullable) {
        return CodeBlock.of(
            "((this.$N() == null) ? (that.$N() == null) : this.$N().equals(that.$N()))", methodName,
            methodName, methodName, methodName);
      } else {
        return CodeBlock.of("(this.$N().equals(that.$N()))", methodName, methodName);
      }
    }
  }

  private static Set<String> getAnnotations(ExecutableElement element) {
    Set<String> set = new LinkedHashSet<>();

    List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
    for (AnnotationMirror annotation : annotations) {
      set.add(annotation.getAnnotationType().asElement().getSimpleName().toString());
    }

    return Collections.unmodifiableSet(set);
  }
}
