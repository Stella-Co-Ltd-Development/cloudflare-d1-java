package example;

import io.github.xxvw.cloudflare.d1.D1AuthenticationException;
import io.github.xxvw.cloudflare.d1.D1AuthorizationException;
import io.github.xxvw.cloudflare.d1.D1Client;
import io.github.xxvw.cloudflare.d1.D1Exception;
import io.github.xxvw.cloudflare.d1.D1MappingException;
import io.github.xxvw.cloudflare.d1.D1QueryException;
import io.github.xxvw.cloudflare.d1.D1RateLimitException;
import io.github.xxvw.cloudflare.d1.D1Result;
import io.github.xxvw.cloudflare.d1.D1TimeoutException;
import io.github.xxvw.cloudflare.d1.D1TransportException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MappingAndWriteExamples {
  private MappingAndWriteExamples() {}

  public static void main(String[] args) {
    boolean runWriteExample = args.length == 1 && "--write".equals(args[0]);

    try (D1Client d1 = D1Client.fromEnv()) {
      runReadExample(d1);
      runTypedMappingExample(d1);
      if (runWriteExample) {
        runWriteExample(d1);
      }
    } catch (IllegalStateException exception) {
      fail(2, "Missing required environment variable: " + exception.getMessage());
    } catch (D1Exception exception) {
      handleFailure(exception);
    }
  }

  private static void runReadExample(D1Client d1) {
    Optional<Map<String, Object>> row = d1.queryFirst("SELECT ? AS value", 42);
    System.out.println("read-first: " + row.orElse(null));
  }

  private static void runTypedMappingExample(D1Client d1) {
    List<ExampleUser> users = d1.query(
        "SELECT 1 AS id, 'Taro' AS name, 'taro@example.com' AS email",
        ExampleUser.class);
    System.out.println("typed-users: " + users);
  }

  private static void runWriteExample(D1Client d1) {
    String tableName = "d1_java_example_" + System.currentTimeMillis();
    try {
      d1.execute("CREATE TABLE " + tableName + " (id INTEGER PRIMARY KEY, name TEXT NOT NULL)");
      D1Result inserted = d1.execute("INSERT INTO " + tableName + " (name) VALUES (?)", "write-example");
      D1Result selected = d1.query("SELECT id, name FROM " + tableName + " ORDER BY id");

      System.out.println("write-changes: " + inserted.meta().changes());
      System.out.println("write-first: " + selected.firstRow().orElse(null));
    } finally {
      d1.execute("DROP TABLE IF EXISTS " + tableName);
    }
  }

  private static void handleFailure(D1Exception exception) {
    if (exception instanceof D1AuthenticationException) {
      fail(2, "Authentication failed. Check CLOUDFLARE_API_TOKEN.");
    }
    if (exception instanceof D1AuthorizationException) {
      fail(3, "Authorization failed. Check token permissions, account ID, and database ID.");
    }
    if (exception instanceof D1RateLimitException) {
      D1RateLimitException rateLimit = (D1RateLimitException) exception;
      String retryAfter = rateLimit.retryAfter()
          .map(Object::toString)
          .orElse("not provided");
      fail(4, "Rate limited by Cloudflare. Retry-After: " + retryAfter);
    }
    if (exception instanceof D1QueryException) {
      fail(5, "D1 query failed. Check the SQL statement and target database.");
    }
    if (exception instanceof D1MappingException) {
      fail(5, "D1 mapping failed. Check selected columns and the target Java type.");
    }
    if (exception instanceof D1TimeoutException) {
      fail(6, "D1 request timed out.");
    }
    if (exception instanceof D1TransportException) {
      fail(6, "D1 transport failed.");
    }
    fail(1, "D1 request failed.");
  }

  private static void fail(int exitCode, String message) {
    System.err.println(message);
    System.exit(exitCode);
  }

  public static final class ExampleUser {
    public long id;
    public String name;
    public String email;

    public ExampleUser() {}

    @Override
    public String toString() {
      return "ExampleUser{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
  }
}
