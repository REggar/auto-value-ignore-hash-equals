package com.github.reggar.ignorehashequals;

import java.util.Set;

public enum AnnotationType {
    ERROR(""), // Error case
    INCLUDE("IncludeHashEquals"),
    IGNORE("IgnoreHashEquals"),
    NOT_PRESENT(""); // Empty case

    private final String annotationName;

    AnnotationType(String annotationName) {
        this.annotationName = annotationName;
    }

    public boolean shouldBeIncluded(Set<String> propertyAnnotations) {
        boolean annotationPresent = propertyAnnotations.contains(annotationName);
        switch (this) {
            case INCLUDE:
                return annotationPresent;
            case IGNORE:
                return !annotationPresent;
            default:
                return true;
        }
    }

    public static AnnotationType from(Set<String> annotations) {
        boolean ignoreHashEqualsPresent = annotations.contains(IGNORE.annotationName);
        boolean includeHashEqualsPresent = annotations.contains(INCLUDE.annotationName);

        if (ignoreHashEqualsPresent && includeHashEqualsPresent) {
            return ERROR;
        } else if (ignoreHashEqualsPresent) {
            return IGNORE;
        } else if (includeHashEqualsPresent) {
            return INCLUDE;
        } else {
            return NOT_PRESENT;
        }
    }
}
