# AutoValue: Ignore Hash Equals Extension [![Build Status](https://travis-ci.org/REggar/auto-value-ignore-hash-equals.svg?branch=master)](https://travis-ci.org/REggar/auto-value-ignore-hash-equals)
An extension for Google's [AutoValue](https://github.com/google/auto/tree/master/value) that omits
`@IgnoreHashEquals` field values from `hashCode()` and `equals()`.


## Basic Usage

Include the extension in your project, define an `@IgnoreHashEquals` annotation, and apply it to any
fields that you wish to be ignored from the generated `hashCode` and `equals` implementation.

```java
@Retention(SOURCE)
@Target({METHOD, PARAMETER, FIELD})
public @interface IgnoreHashEquals {
}
```

```java
@AutoValue
public abstract class User {
  public abstract String id();
  @IgnoreHashEquals public abstract String name();
}
```

When you call `hashCode()` or `equals()` any properties **with** `@IgnoreHashEquals` will be ignored
from the calculation.

## @IncludeHashEquals Usage

Include the extension in your project, define an `@IncludeHashEquals` annotation, and apply it to any
fields that you wish to be included from the generated `hashCode` and `equals` implementation.

```java
@Retention(SOURCE)
@Target({METHOD, PARAMETER, FIELD})
public @interface IncludeHashEquals {
}
```

```java
@AutoValue
public abstract class User {
  @IncludeHashEquals public abstract String id();
  public abstract String name();
}
```

When you call `hashCode()` or `equals()` any properties **without** `@IncludeHashEquals` will be ignored
from the calculation.

## Download

Add a Gradle dependency:

```groovy
apt 'com.github.reggar:auto-value-ignore-hash-equals:1.1.1'
```
(Using the [android-apt](https://bitbucket.org/hvisser/android-apt) plugin)

or Maven:
```xml
<dependency>
  <groupId>com.github.reggar</groupId>
  <artifactId>auto-value-ignore-hash-equals</artifactId>
  <version>1.1.1</version>
  <scope>provided</scope>
</dependency>
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/).


## Notes

This library is heavily inspired by Square's [AutoValue: Redacted Extension](https://github.com/square/auto-value-redacted).


## License

```
Copyright 2016 Robert Eggar.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```