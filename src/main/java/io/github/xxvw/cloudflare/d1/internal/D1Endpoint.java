package io.github.xxvw.cloudflare.d1.internal;

import java.nio.charset.StandardCharsets;
import java.net.URI;

public final class D1Endpoint {
  private final URI baseUrl;
  private final String accountId;
  private final String databaseId;

  public D1Endpoint(URI baseUrl, String accountId, String databaseId) {
    if (baseUrl == null) {
      throw new IllegalArgumentException("baseUrl must not be null");
    }
    requireText(accountId, "accountId");
    requireText(databaseId, "databaseId");
    this.baseUrl = normalize(baseUrl);
    this.accountId = accountId;
    this.databaseId = databaseId;
  }

  public static D1Endpoint of(String baseUrl, String accountId, String databaseId) {
    requireText(baseUrl, "baseUrl");
    return new D1Endpoint(URI.create(baseUrl), accountId, databaseId);
  }

  public URI queryUri() {
    String path = baseUrl.getRawPath();
    if (path == null || path.trim().isEmpty()) {
      path = "";
    }
    String fullPath = path
        + "/accounts/"
        + encodePathSegment(accountId)
        + "/d1/database/"
        + encodePathSegment(databaseId)
        + "/query";
    StringBuilder uri = new StringBuilder()
        .append(baseUrl.getScheme())
        .append("://")
        .append(baseUrl.getRawAuthority())
        .append(fullPath);
    if (baseUrl.getRawQuery() != null) {
      uri.append('?').append(baseUrl.getRawQuery());
    }
    if (baseUrl.getRawFragment() != null) {
      uri.append('#').append(baseUrl.getRawFragment());
    }
    return URI.create(uri.toString());
  }

  private static URI normalize(URI baseUrl) {
    if (baseUrl.getScheme() == null || baseUrl.getHost() == null) {
      throw new IllegalArgumentException("baseUrl must be an absolute URI with a host");
    }
    String text = baseUrl.toString();
    while (text.endsWith("/")) {
      text = text.substring(0, text.length() - 1);
    }
    return URI.create(text);
  }

  private static String encodePathSegment(String segment) {
    byte[] bytes = segment.getBytes(StandardCharsets.UTF_8);
    StringBuilder encoded = new StringBuilder(bytes.length);
    for (byte value : bytes) {
      int b = value & 0xff;
      if ((b >= 'a' && b <= 'z')
          || (b >= 'A' && b <= 'Z')
          || (b >= '0' && b <= '9')
          || b == '-'
          || b == '.'
          || b == '_'
          || b == '~') {
        encoded.append((char) b);
      } else {
        encoded.append('%');
        String hex = Integer.toHexString(b).toUpperCase();
        if (hex.length() == 1) {
          encoded.append('0');
        }
        encoded.append(hex);
      }
    }
    return encoded.toString();
  }

  private static void requireText(String value, String name) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(name + " must not be null or blank");
    }
  }
}
