package com.smartsparrow.annotation.service;


import com.google.common.collect.ImmutableList;

/***
 * Supported Annotation motivations. These are lowercase to align with the W3C spec.
 *
 */
public enum Motivation {
    commenting,
    replying,
    bookmarking,
    highlighting,
    describing,
    identifying,
    classifying,
    linking,
    tagging;

    public static ImmutableList<Motivation> PUBLISHED_MOTIVATIONS = ImmutableList.of(
          identifying,
          classifying,
          linking,
          tagging
    );
}
