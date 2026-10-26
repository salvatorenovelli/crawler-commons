package com.myseotoolbox.crawler.utils;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private static final Map<String, String> DEFAULT_PORTS = Map.of("http", "80", "https", "443");
    private static final Pattern AUTHORITY = Pattern.compile("(?<userInfo>.*@)?(?<host>[^@]*?)(?::(?<port>\\d*))?");

    public static Optional<URI> toValidUri(String uriStr) {

        if (!isValidUrl(uriStr)) {
            return Optional.empty();
        }

        uriStr = normaliseUrl(uriStr);
        uriStr = removeFragment(uriStr);

        try {
            String decoded = UrlDecoder.decode(uriStr);
            String transCoded = ESCAPER.escape(decoded);

            URI uri = new URI(transCoded);

            return Optional.of(normaliseSchemeBased(uri));
        } catch (URISyntaxException e) {
            log.debug("Invalid link: '{}'. {}", uriStr, e.getMessage());
            return Optional.empty();
        }

    }

    /**
     * RFC 3986 6.2.3 scheme-based normalisation for http and https: an empty path becomes "/", and the scheme's
     * default port (compared as a number) or an empty port is removed; a URL with no host is left as it is. That is
     * what a browser requests and what Google reports; java.net.URI keeps the forms apart, so without this one page
     * is crawled and stored under several URLs. Host and port are read from the authority text because java.net.URI
     * gives neither for a host name it rejects, such as one with an underscore.
     */
    public static URI normaliseSchemeBased(URI uri) {
        if (uri.getScheme() == null || uri.getRawAuthority() == null) return uri;
        String defaultPort = DEFAULT_PORTS.get(uri.getScheme().toLowerCase());
        if (defaultPort == null) return uri;

        Matcher authorityParts = AUTHORITY.matcher(uri.getRawAuthority());
        if (!authorityParts.matches() || authorityParts.group("host").isEmpty()) return uri;
        String port = authorityParts.group("port");
        boolean removePort = port != null && (port.isEmpty() || port.replaceFirst("^0+", "").equals(defaultPort));
        String authority = removePort ? Objects.toString(authorityParts.group("userInfo"), "") + authorityParts.group("host") : uri.getRawAuthority();
        if (authority.equals(uri.getRawAuthority()) && !uri.getRawPath().isEmpty()) return uri;

        String path = uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        String fragment = uri.getRawFragment() == null ? "" : "#" + uri.getRawFragment();
        return URI.create(uri.getScheme() + "://" + authority + path + query + fragment);
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
