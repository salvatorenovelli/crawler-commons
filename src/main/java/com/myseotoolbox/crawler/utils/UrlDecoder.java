package com.myseotoolbox.crawler.utils;

import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class UrlDecoder {
    @SneakyThrows
    public static String decode(String str) {
        try {
            return StringUtils.uriDecode(str, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new URISyntaxException(str, e.toString());
        }
    }
}
