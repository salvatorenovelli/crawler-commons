package com.myseotoolbox.crawler.utils;

import java.net.URI;

import static com.myseotoolbox.crawler.utils.UriUtils.normaliseSchemeBased;

/*
 * Unfortunately, neither URL nor URI behave like a browser would when resolving links...
 * */
public class LinkResolver {


    public static URI resolve(URI baseUri, URI linkUri) {
        return normaliseSchemeBased(resolveLikeABrowser(baseUri, linkUri));
    }

    private static URI resolveLikeABrowser(URI baseUri, URI linkUri) {
        if (linkUri.toString().isEmpty()) return baseUri;
        if (linkUri.isAbsolute()) return linkUri;

        String link = linkUri.toString();

        if (link.startsWith("?")) {
            return URI.create(stripQueryAndFragment(baseUri) + link);
        }

        if (link.startsWith("#")) {
            String noFragment = baseUri.toString().replaceAll("#.*$", "");
            return URI.create(noFragment + link);
        }

        return baseUri.resolve(linkUri);
    }

    private static String stripQueryAndFragment(URI uri) {
        String result = uri.toString();
        int i = result.indexOf('?');
        if (i == -1) i = result.indexOf('#');
        if (i != -1) result = result.substring(0, i);
        return result;
    }
}
