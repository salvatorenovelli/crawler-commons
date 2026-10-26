package com.myseotoolbox.crawler.utils;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class UriUtilsTest {

    @Test
    public void shouldGetFolderWhenUrlHasFolderAndFile() throws Exception {
        String result = UriUtils.getFolder("http://example.com/folder/file");
        assertEquals("/folder/", result);
    }

    @Test
    public void shouldReturnRootWhenUrlHasNoFolder() throws Exception {
        String result = UriUtils.getFolder("http://example.com/file");
        assertEquals("/", result);
    }

    @Test
    public void shouldReturnRootWhenUrlIsRootOnly() throws Exception {
        String result = UriUtils.getFolder("http://example.com/");
        assertEquals("/", result);
    }

    @Test
    public void shouldReturnRootWhenUrlHasEmptyPath() throws Exception {
        String result = UriUtils.getFolder("http://example.com");
        assertEquals("/", result);
    }

    @Test
    public void shouldExtractNestedFoldersFromUrl() throws Exception {
        String result = UriUtils.getFolder("http://example.com/folder/subfolder/file");
        assertEquals("/folder/subfolder/", result);
    }

    @Test
    public void shouldIgnoreQueryParamsAndGetFolder() throws Exception {
        String result = UriUtils.getFolder("http://example.com/folder/file?query=value");
        assertEquals("/folder/", result);
    }

    @Test
    public void shouldGetFolderWhenUrlHasEncodedCharacters() throws Exception {
        String result = UriUtils.getFolder("http://example.com/folder%20name/file");
        assertEquals("/folder%20name/", result);
    }

    @Test
    public void shouldThrowExceptionWhenUrlIsInvalid() {
        assertThrows(MalformedURLException.class, () -> UriUtils.getFolder("invalid-url"));
    }

    @Test
    public void shouldThrowExceptionWhenUrlIsMissingSchema() {
        assertThrows(MalformedURLException.class, () -> UriUtils.getFolder("example.com/folder/file"));
    }

    @Test
    public void shouldManageRelativeUnicodeCharacters() {
        assertThat(UriUtils.toValidUri("/linkWithUnicode\u200B  \u200B").get().toString(), is("/linkWithUnicode%E2%80%8B%20%20%E2%80%8B"));
    }

    @Test
    public void invalidUrlShouldBeEscaped() {
        assertThat(UriUtils.toValidUri("http:-/link1__(this is invalid)__").get().toString(), is("http:-/link1__(this%20is%20invalid)__"));
        assertThat(UriUtils.toValidUri("/link1 with spaces/salve").get().toString(), is("/link1%20with%20spaces/salve"));
    }

    @Test
    public void shouldEscapeBracketsInPathAndQuery() {
        assertThat(UriUtils.toValidUri("https://example.com/[page]?key=[value]").get().toString(), is("https://example.com/%5Bpage%5D?key=%5Bvalue%5D"));
    }

    @Test
    public void shouldNormaliseRootPath() {
        assertThat(UriUtils.toValidUri("https://example.com").get().toString(), is("https://example.com/"));
    }

    @Test
    public void shouldNormaliseRootPathBeforeQuery() {
        assertThat(UriUtils.toValidUri("https://example.com?a=1").get().toString(), is("https://example.com/?a=1"));
    }

    @Test
    public void shouldNormaliseRootPathBeforeEmptyQuery() {
        assertThat(UriUtils.toValidUri("https://example.com?").get().toString(), is("https://example.com/?"));
    }

    @Test
    public void shouldNormaliseRootPathAndRemoveFragment() {
        assertThat(UriUtils.toValidUri("https://example.com?a=1#frag").get().toString(), is("https://example.com/?a=1"));
    }

    @Test
    public void shouldNormaliseRootPathOfUntrimmedUrl() {
        assertThat(UriUtils.toValidUri("   https://example.com   ").get().toString(), is("https://example.com/"));
    }

    @Test
    public void shouldNotNormaliseNonRootPath() {
        assertThat(UriUtils.toValidUri("https://example.com/folder").get().toString(), is("https://example.com/folder"));
    }

    @Test
    public void shouldRemoveDotSegments() {
        assertThat(UriUtils.toValidUri("https://example.com/a/../b/./c").get().toString(), is("https://example.com/b/c"));
    }

    @Test
    public void shouldRemoveDefaultPort() {
        assertThat(UriUtils.toValidUri("https://example.com:443/").get().toString(), is("https://example.com/"));
    }

    @Test
    public void shouldRemoveDefaultPortAndNormaliseRootPath() {
        assertThat(UriUtils.toValidUri("http://example.com:80").get().toString(), is("http://example.com/"));
    }

    @Test
    public void shouldRemoveEmptyPort() {
        assertThat(UriUtils.toValidUri("https://example.com:/").get().toString(), is("https://example.com/"));
    }

    @Test
    public void shouldRemoveDefaultPortWithLeadingZeros() {
        assertThat(UriUtils.toValidUri("http://example.com:080/page").get().toString(), is("http://example.com/page"));
    }

    @Test
    public void shouldRemoveHttpsDefaultPortWithLeadingZeros() {
        assertThat(UriUtils.toValidUri("https://example.com:00443/page").get().toString(), is("https://example.com/page"));
    }

    @Test
    public void shouldRemoveDefaultPortWithLeadingZerosAndNormaliseRootPath() {
        assertThat(UriUtils.toValidUri("http://example.com:00080").get().toString(), is("http://example.com/"));
    }

    @Test
    public void shouldNotRemoveNonDefaultPort() {
        assertThat(UriUtils.toValidUri("https://example.com:8443").get().toString(), is("https://example.com:8443/"));
    }

    @Test
    public void shouldNotRemoveDefaultPortOfAnotherScheme() {
        assertThat(UriUtils.toValidUri("http://example.com:443/").get().toString(), is("http://example.com:443/"));
    }

    @Test
    public void shouldNotRemovePortZero() {
        assertThat(UriUtils.toValidUri("http://example.com:0/").get().toString(), is("http://example.com:0/"));
    }

    @Test
    public void shouldLowerCaseScheme() {
        assertThat(UriUtils.toValidUri("HTTPS://example.com/page").get().toString(), is("https://example.com/page"));
    }

    @Test
    public void shouldLowerCaseHost() {
        assertThat(UriUtils.toValidUri("https://EXAMPLE.Com/page").get().toString(), is("https://example.com/page"));
    }

    @Test
    public void shouldNotLowerCaseUserInfoPathOrQuery() {
        assertThat(UriUtils.toValidUri("https://User:Pass@example.com/Page?Key=Value").get().toString(), is("https://User:Pass@example.com/Page?Key=Value"));
    }

    @Test
    public void shouldLowerCaseSchemeAndHostAndRemoveDefaultPortAndNormaliseRootPath() {
        assertThat(UriUtils.toValidUri("HTTP://Example.COM:80").get().toString(), is("http://example.com/"));
    }

    @Test
    public void shouldRemoveDefaultPortOfHostWithUnderscore() {
        assertThat(UriUtils.toValidUri("http://my_host.example.com:80/page").get().toString(), is("http://my_host.example.com/page"));
    }

    @Test
    public void shouldNotRemoveNonDefaultPortOfHostWithUnderscore() {
        assertThat(UriUtils.toValidUri("http://my_host.example.com:8080/page").get().toString(), is("http://my_host.example.com:8080/page"));
    }

    @Test
    public void shouldLowerCaseHostWithUnderscore() {
        assertThat(UriUtils.toValidUri("http://My_Host.Example.com/page").get().toString(), is("http://my_host.example.com/page"));
    }

    @Test
    public void shouldConvertNonAsciiHostToPunycode() {
        assertThat(UriUtils.toValidUri("http://MÜNCHEN.de/").get().toString(), is("http://xn--mnchen-3ya.de/"));
    }

    @Test
    public void shouldLowerCaseIpv6HostAndRemoveDefaultPort() {
        assertThat(UriUtils.normalise(URI.create("http://[FE80::A]:80/page")).toString(), is("http://[fe80::a]/page"));
    }

    @Test
    public void shouldKeepBracketsOfIpv6HostAndLowerCaseIt() {
        assertThat(UriUtils.toValidUri("http://[FE80::A]:8080/p").get().toString(), is("http://[fe80::a]:8080/p"));
    }

    @Test
    public void shouldKeepBracketsOfIpv6HostAndRemoveDefaultPort() {
        assertThat(UriUtils.toValidUri("https://[::1]:443").get().toString(), is("https://[::1]/"));
    }

    @Test
    public void shouldNotNormaliseUrlWithoutHost() {
        assertThat(UriUtils.toValidUri("http://:80/page").get().toString(), is("http://:80/page"));
    }

    @Test
    public void shouldNotNormaliseUrlWithNonNumericPort() {
        assertThat(UriUtils.toValidUri("http://Example.com:ABC/page").get().toString(), is("http://Example.com:ABC/page"));
    }

    @Test
    public void shouldNormaliseRootPathOfResolvedProtocolRelativeLink() {
        assertThat(UriUtils.toAbsoluteUri(URI.create("https://www.x.it/page"), URI.create("//www.y.it")).get().toString(), is("https://www.y.it/"));
    }

    @Test
    public void shouldRemoveDefaultPortAndNormaliseRootPathOfAbsoluteLink() {
        assertThat(UriUtils.toAbsoluteUri(URI.create("https://source.com/page"), URI.create("https://example.com:443")).get().toString(), is("https://example.com/"));
    }

    @Test
    public void shouldRemoveEmptyPortOfAbsoluteLink() {
        assertThat(UriUtils.toAbsoluteUri(URI.create("https://source.com/page"), URI.create("https://example.com:/page")).get().toString(), is("https://example.com/page"));
    }

    @Test
    public void emptyLinksAreValid() {
        assertThat(UriUtils.toValidUri("").get().toString(), is(""));
    }

    //extractHost method
    @Test
    public void extractHostShouldReturnHostFromValidUri() {
        assertThat(UriUtils.extractHost("http://example.com/path").get(), is("example.com"));
    }

    @Test
    public void extractHostShouldReturnHostWithSubdomain() {
        assertThat(UriUtils.extractHost("https://sub.example.com/").get(), is("sub.example.com"));
    }

    @Test
    public void extractHostShouldReturnEmptyOptionalForInvalidUri() {
        assertFalse(UriUtils.extractHost("invalid-url").isPresent());
    }

    @Test
    public void extractHostShouldReturnEmptyOptionalForNullInput() {
        assertFalse(UriUtils.extractHost(null).isPresent());
    }

    @Test
    public void extractHostShouldWorkWithPortNumbers() {
        assertThat(UriUtils.extractHost("http://example.com:8080/path").get(), is("example.com"));
    }

    // Tests for isValidHost method
    @Test
    public void isValidHostShouldReturnTrueForValidHost() {
        assertTrue(UriUtils.isValidHost("example.com"));
    }

    @Test
    public void isValidHostShouldReturnTrueForSubdomain() {
        assertTrue(UriUtils.isValidHost("sub.example.com"));
    }

    @Test
    public void isValidHostShouldReturnFalseForNull() {
        assertFalse(UriUtils.isValidHost(null));
    }

    @Test
    public void isValidHostShouldReturnFalseForEmptyString() {
        assertFalse(UriUtils.isValidHost(""));
    }

    @Test
    public void isValidHostShouldReturnFalseForUrlWithScheme() {
        assertFalse(UriUtils.isValidHost("http://example.com"));
    }

    @Test
    public void isValidHostShouldReturnFalseForUrlWithPath() {
        assertFalse(UriUtils.isValidHost("example.com/path"));
    }
}