package io.github.xxvw.cloudflare.d1.internal;

import java.net.http.HttpHeaders;

public record D1HttpResponse(int statusCode, HttpHeaders headers, String body) {}
