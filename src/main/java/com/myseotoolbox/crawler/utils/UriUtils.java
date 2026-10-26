package com.myseotoolbox.crawler.utils;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;

import javax.annotation.Nullable;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.myseotoolbox.crawler.utils.RemoveUrlFragment.removeFragment;
import static com.myseotoolbox.crawler.utils.SafeStringEscaper.containsUnicodeCharacters;


@Slf4j
public class UriUtils {

    public static final int MAX_URL_LEN = 1000;
    private static final Escaper ESCAPER = UrlEscapers.urlFragmentEscaper();
    private static final Set<String> HTTP_SCHEMES = Set.of("http", "https");
    private static final Pattern IP_LITERAL_HOST = Pattern.compile("(?<beforeHost>(?:[^:/?#]+:)?//(?:[^/?#@]*@)?)\\[(?<address>[^]/?#]*)](?<afterHost>.*)", Pattern.DOTALL);

    public static Optional<URI> toValidUri(String uriStr) {

        if (!isValidUrl(uriStr)) {
            return Optional.empty();
        }

        uriStr = normaliseUrl(uriStr);
        uriStr = removeFragment(uriStr);

        try {
            String decoded = UrlDecoder.decode(uriStr);
            String transCoded = escape(decoded);

            URI uri = new URI(transCoded);

            return Optional.of(normalise(uri));
        } catch (URISyntaxException e) {
            log.debug("Invalid link: '{}'. {}", uriStr, e.getMessage());
            return Optional.empty();
        }

    }

    // RFC 3986 3.2.2: the brackets around an IP literal host are syntax, so they stay unescaped; anywhere else they are escaped
    private static String escape(String decoded) {
        Matcher ipLiteralHost = IP_LITERAL_HOST.matcher(decoded);
        if (!ipLiteralHost.matches()) return ESCAPER.escape(decoded);
        return ESCAPER.escape(ipLiteralHost.group("beforeHost"))
                + "[" + ESCAPER.escape(ipLiteralHost.group("address")) + "]"
                + ESCAPER.escape(ipLiteralHost.group("afterHost"));
    }

    /**
     * For http and https, the URL as a browser's URL parser (WHATWG) normalises it before sending the request: the
     * scheme and the host are lower-cased, a non-ASCII host becomes punycode, an empty path becomes "/", the scheme's
     * default port is removed and "." and ".." path segments are resolved. That is what a browser requests and what
     * Google reports; java.net.URI keeps the forms apart, so without this one page is crawled and stored under several
     * URLs. A URL with no authority, or one the browser parser refuses, is left as it is.
     */
    public static URI normalise(URI uri) {
        if (uri.getScheme() == null || uri.getRawAuthority() == null) return uri;
        if (!HTTP_SCHEMES.contains(uri.getScheme().toLowerCase(Locale.ROOT))) return uri;
        HttpUrl httpUrl = HttpUrl.parse(uri.toString());
        return httpUrl == null ? uri : httpUrl.uri();
    }

    /**
     * this is used to do basic validation on potentially relative uris
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.length() >= MAX_URL_LEN || url.startsWith("javascript:")) {
            log.trace("URL is not valid: '{}'", url);
            return false;
        }

        return true;
    }

    public static String normaliseUrl(String url) {
        return url.trim();
    }

    public static String getFolder(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        String fullPath = url.getPath();

        int lastSlashIndex = fullPath.lastIndexOf("/");
        if (lastSlashIndex > 0) {
            return fullPath.substring(0, lastSlashIndex + 1);
        } else {
            return "/";
        }
    }

    public static Optional<URI> toAbsoluteUri(String sourceUriString, String destinationUriString) {
        try {
            URI sourceUri = toValidUri(sourceUriString).orElseThrow(IllegalArgumentException::new);
            URI destinationUri = toValidUri(destinationUriString).orElseThrow(IllegalArgumentException::new);
            return toAbsoluteUri(sourceUri, destinationUri);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static Optional<URI> toAbsoluteUri(URI sourceUri, URI potentiallyRelativeUri) {
        if (potentiallyRelativeUri.isAbsolute()) return Optional.of(LinkResolver.resolve(sourceUri, potentiallyRelativeUri));

        String path = potentiallyRelativeUri.toString();
        if (containsUnicodeCharacters(path)) {
            log.debug("URI {} contains non ASCII characters (as required by the standard)", path);
            path = SafeStringEscaper.escapeString(path);
        }

        try {
            return Optional.of(LinkResolver.resolve(sourceUri, URI.create(path)));
        } catch (IllegalArgumentException e) {
            log.warn("Error while converting to absolute '{}' (source: '{}'). {}", potentiallyRelativeUri, sourceUri, e.getMessage());
            return Optional.empty();
        }
    }

    public static boolean isEmptyLink(@Nullable URI uri) {
        if (uri == null) return true;
        String s = URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8);
        s = normaliseUrl(s);
        return s.isEmpty();
    }

    public static List<URI> filterValidUrls(List<String> urls) {
        List<URI> filtered = new ArrayList<>();

        if (urls != null) {
            filtered = urls
                    .stream()
                    .map(UriUtils::toValidUri)
                    .flatMap(UriUtils::stream)
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    private static <T> Stream<T> stream(Optional<T> opt) {
        return opt.stream();
    }

    public static boolean isAbsolute(String uriString) {
        return UriCreator.create(uriString).isAbsolute();
    }

    public static Optional<String> extractHost(String uri) {
        return toValidUri(uri).map(URI::getHost);
    }

    public static boolean isValidHost(String host) {
        if (host == null || host.isEmpty()) {
            return false;
        }

        // Check that host is not a URL
        if (host.contains("://") || host.contains("/")) {
            return false;
        }

        return true;
    }
}
