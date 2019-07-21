package com.myseotoolbox.crawlercommons;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UriCreatorTest {

    @Test
    public void shouldCreateNormalUri() throws URISyntaxException {
        URI uri = UriCreator.create("http://host/path");

        assertThat(uri.getHost(), is("host"));
        assertThat(uri.getPath(), is("/path"));
    }

    @Test
    public void shouldTolerateSpacesInUri() throws URISyntaxException {
        URI uri = UriCreator.create("http://host/path with spaces");

        assertThat(uri.getHost(), is("host"));
        assertThat(uri.getPath(), is("/path with spaces"));
    }
}