package com.myseotoolbox.crawler.utils;

import java.net.URI;

import static com.myseotoolbox.crawler.utils.UriUtils.toValidUri;

public class UriCreator {
    public static URI create(String link) {
        return toValidUri(link).orElseThrow(() -> new IllegalArgumentException("Invalid url: '" + link + "'"));
    }
}
