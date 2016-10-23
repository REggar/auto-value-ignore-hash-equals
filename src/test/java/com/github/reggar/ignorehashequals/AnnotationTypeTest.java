package com.github.reggar.ignorehashequals;

import com.google.common.collect.Sets;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnotationTypeTest {

    @Test
    public void shouldBeIncluded() throws Exception {
        assertTrue(AnnotationType.INCLUDE.shouldBeIncluded(Collections.singleton("IncludeHashEquals")));
        assertFalse(AnnotationType.IGNORE.shouldBeIncluded(Collections.singleton("IgnoreHashEquals")));

        assertTrue(AnnotationType.IGNORE.shouldBeIncluded(Collections.singleton("IncludeHashEquals")));
        assertFalse(AnnotationType.INCLUDE.shouldBeIncluded(Collections.singleton("IgnoreHashEquals")));

        assertTrue(AnnotationType.IGNORE.shouldBeIncluded(Collections.singleton("SomethingElse")));
        assertFalse(AnnotationType.INCLUDE.shouldBeIncluded(Collections.singleton("SomethingElse")));

        assertTrue(AnnotationType.NOT_PRESENT.shouldBeIncluded(Collections.<String>emptySet()));
        assertTrue(AnnotationType.ERROR.shouldBeIncluded(Collections.<String>emptySet()));
    }

    @Test
    public void from() throws Exception {
        assertEquals(AnnotationType.INCLUDE, AnnotationType.from(Collections.singleton("IncludeHashEquals")));

        assertEquals(AnnotationType.IGNORE, AnnotationType.from(Collections.singleton("IgnoreHashEquals")));

        assertEquals(AnnotationType.NOT_PRESENT, AnnotationType.from(Collections.<String>emptySet()));

        assertEquals(AnnotationType.NOT_PRESENT, AnnotationType.from(Collections.singleton("SomethingElse")));

        assertEquals(AnnotationType.ERROR, AnnotationType.from(Sets.newHashSet("IncludeHashEquals", "IgnoreHashEquals")));
    }
}