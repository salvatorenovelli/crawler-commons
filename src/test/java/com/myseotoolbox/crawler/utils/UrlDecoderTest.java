package com.myseotoolbox.crawler.utils;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UrlDecoderTest {

    @Test
    public void basicDecoding() {
        assertThat(UrlDecoder.decode("http://host/link%20with%20spaces"), is("http://host/link with spaces"));
    }

    @Test
    public void ShouldLeavePlusSignAlone() {
        assertThat(UrlDecoder.decode("http://host/link+link"), is("http://host/link+link"));
    }

    @Test
    public void shouldThrowUriSyntaxExceptionInCaseOfWrongEncoding() {
        assertThrows(URISyntaxException.class, () -> UrlDecoder.decode("http://host?%%20"));
    }
}
