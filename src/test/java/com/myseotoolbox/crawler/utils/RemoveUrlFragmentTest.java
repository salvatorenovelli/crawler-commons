package com.myseotoolbox.crawler.utils;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RemoveUrlFragmentTest {

    @Test
    public void shouldRemoveLongFragments() {
        assertThat(RemoveUrlFragment.removeFragment("http://host/somePage#with long fragments"), is("http://host/somePage"));
    }

    @Test
    public void canRemoveEmptyFragment() {
        assertThat(RemoveUrlFragment.removeFragment("http://host/somePage#"), is("http://host/somePage"));
    }
}