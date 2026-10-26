package com.myseotoolbox.crawler.utils;


import org.junit.jupiter.api.Test;

import java.net.URI;

import static com.myseotoolbox.crawler.utils.LinkResolver.resolve;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinkResolverTest {

    @Test
    public void canResolveNormalLinks() {
        assertThat(resolve(URI.create("http://host"), uriOf("/linkWithPath")), is(URI.create("http://host/linkWithPath")));
    }

    @Test
    public void canResolveRelativeLinksInRoot() {
        assertThat(resolve(URI.create("http://host"), uriOf("relativeLink")), is(URI.create("http://host/relativeLink")));
    }

    @Test
    void testFromRootBase() throws Exception {
        assertEquals("http://host/", resolve(uriOf("http://host"), uriOf("")).toString());
        assertEquals("http://host/a", resolve(uriOf("http://host"), uriOf("a")).toString());
        assertEquals("http://host/a", resolve(uriOf("http://host"), uriOf("/a")).toString());
        assertEquals("http://otherhost/path", resolve(uriOf("http://host"), uriOf("http://otherhost/path")).toString());
    }

    @Test
    void testFromFileBase() throws Exception {
        assertEquals("http://host/salve", resolve(uriOf("http://host/salve"), uriOf("")).toString());
        assertEquals("http://host/a", resolve(uriOf("http://host/salve"), uriOf("a")).toString());
        assertEquals("http://host/a", resolve(uriOf("http://host/salve"), uriOf("/a")).toString());
        assertEquals("http://otherhost/path", resolve(uriOf("http://host/salve"), uriOf("http://otherhost/path")).toString());
    }

    @Test
    void testFromDirectoryBase() throws Exception {
        assertEquals("http://host/salve/", resolve(uriOf("http://host/salve/"), uriOf("")).toString());
        assertEquals("http://host/salve/a", resolve(uriOf("http://host/salve/"), uriOf("a")).toString());
        assertEquals("http://host/a", resolve(uriOf("http://host/salve/"), uriOf("/a")).toString());
        assertEquals("http://otherhost/path", resolve(uriOf("http://host/salve/"), uriOf("http://otherhost/path")).toString());
    }

    @Test
    void testAbsoluteLinkOverridesBase() throws Exception {
        assertEquals("https://securehost/secure", resolve(uriOf("http://host/some/path"), uriOf("https://securehost/secure")).toString());
    }

    @Test
    void testQueryWithParameters() throws Exception {
        assertEquals("http://host/page?param=1", resolve(uriOf("http://host/page"), uriOf("?param=1")).toString());
        assertEquals("http://host/a?param=1", resolve(uriOf("http://host/page"), uriOf("a?param=1")).toString());
    }

    @Test
    void testQueryWithFragment() throws Exception {
        assertEquals("http://host/page#section", resolve(uriOf("http://host/page"), uriOf("#section")).toString());
        assertEquals("http://host/a#section", resolve(uriOf("http://host/page"), uriOf("a#section")).toString());
    }

    @Test
    void shouldNormaliseResolvedRelativeLink() {
        assertEquals("https://host/a", resolve(uriOf("https://host:443/page"), uriOf("a")).toString());
    }

    @Test
    void shouldNormaliseResolvedProtocolRelativeLink() {
        assertEquals("https://otherhost/", resolve(uriOf("https://host/page"), uriOf("//otherhost")).toString());
    }

    @Test
    void shouldNormaliseAbsoluteLink() {
        assertEquals("https://otherhost/", resolve(uriOf("http://host/page"), uriOf("https://otherhost:443")).toString());
    }

    @Test
    void shouldNormaliseResolvedQueryLink() {
        assertEquals("http://host/?param=1", resolve(uriOf("http://host"), uriOf("?param=1")).toString());
    }

    @Test
    void shouldNormaliseResolvedFragmentLink() {
        assertEquals("http://host/#section", resolve(uriOf("http://host"), uriOf("#section")).toString());
    }

    private static URI uriOf(String uri) {
        return URI.create(uri);
    }
}