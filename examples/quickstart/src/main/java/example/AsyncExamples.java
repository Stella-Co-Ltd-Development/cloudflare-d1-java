package example;

import io.github.xxvw.cloudflare.d1.D1AsyncClient;
import io.github.xxvw.cloudflare.d1.D1AuthenticationException;
import io.github.xxvw.cloudflare.d1.D1AuthorizationException;
import io.github.xxvw.cloudflare.d1.D1Exception;
import io.github.xxvw.cloudflare.d1.D1Query;
import io.github.xxvw.cloudflare.d1.D1QueryException;
import io.github.xxvw.cloudflare.d1.D1RateLimitException;
import io.github.xxvw.cloudflare.d1.D1Result;
import io.github.xxvw.cloudflare.d1.D1TimeoutException;
import io.github.xxvw.cloudflare.d1.D1TransportException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class AsyncExamples {
  private AsyncExamples() {}

  public static void main(String[] args) {
    boolean runWriteExample = args.length == 1 && "--write".equals(args[0]);

    try (D1AsyncClient d1 = D1AsyncClient.fromEnv()) {
      runReadOnlyExamples(d1);
      if (runWriteExample) {
        runWriteExample(d1);
      }
    } catch (IllegalStateException exception) {
      fail(2, "Missing required environment variable: " + exception.getMessage());
    } catch (CompletionException exception) {
      handleFailure(exception.getCause());
    } catch (D1Exception exception) {
      handleFailure(exception);
    }
  }

  private static void runReadOnlyExamples(D1AsyncClient d1) {
    D1Result simple = d1.queryAsync("SELECT 1 AS value").join();
    Optional<Map<String, Object>> parameterized =
        d1.queryFirstAsync("SELECT ? AS value", 42).join();
    Optional<ExampleRow> typed =
        d1.queryFirstAsync("SELECT 7 AS id, 'async' AS name", ExampleRow.class).join();

    CompletableFuture<D1Result> left = d1.queryAsync("SELECT 10 AS value");
    CompletableFuture<D1Result> right = d1.queryAsync("SELECT 20 AS value");
    CompletableFuture.allOf(left, right).join();

    List<D1Result> batch = d1.batchAsync(Arrays.asList(
        D1Query.of("SELECT 100 AS value"),
        D1Query.of("SELECT 200 AS value"))).join();

    System.out.println("simple: " + firstRow(simple));
    System.out.println("parameterized: " + parameterized.orElse(null));
    System.out.println("typed-first: " + typed.orElse(null));
    System.out.println("parallel: " + firstRow(left.join()) + ", " + firstRow(right.join()));
    System.out.println("batch: " + firstRow(batch.get(0)) + ", " + firstRow(batch.get(1)));
  }

  private static void runWriteExample(D1AsyncClient d1) {
    String tableName = "d1_java_async_example_" + System.currentTimeMillis();
    try {
      d1.executeAsync("CREATE TABLE " + tableName + " (id INTEGER PRIMARY KEY, name TEXT NOT NULL)").join();
      d1.executeAsync("INSERT INTO " + tableName + " (name) VALUES (?)", "write-example").join();
      D1Result selected = d1.queryAsync("SELECT id, name FROM " + tableName + " ORDER BY id").join();
      System.out.println("write-select: " + firstRow(selected));
    } finally {
      d1.executeAsync("DROP TABLE IF EXISTS " + tableName).join();
    }
  }

  private static Map<String, Object> firstRow(D1Result result) {
    return result.firstRow().orElse(null);
  }

  private static void handleFailure(Throwable cause) {
    if (cause instanceof D1AuthenticationException) {
      fail(2, "Authentication failed. Check CLOUDFLARE_API_TOKEN.");
    }
    if (cause instanceof D1AuthorizationException) {
      fail(3, "Authorization failed. Check token permissions, account ID, and database ID.");
    }
    if (cause instanceof D1RateLimitException) {
      D1RateLimitException rateLimit = (D1RateLimitException) cause;
      String retryAfter = rateLimit.retryAfter()
          .map(Object::toString)
          .orElse("not provided");
      fail(4, "Rate limited by Cloudflare. Retry-After: " + retryAfter);
    }
    if (cause instanceof D1QueryException) {
      fail(5, "D1 query failed. Check the SQL statement and target database.");
    }
    if (cause instanceof D1TimeoutException) {
      fail(6, "D1 request timed out.");
    }
    if (cause instanceof D1TransportException) {
      fail(6, "D1 transport failed.");
    }
    if (cause instanceof D1Exception) {
      fail(1, "D1 request failed.");
    }
    fail(1, "Unexpected failure.");
  }

  private static void fail(int exitCode, String message) {
    System.err.println(message);
    System.exit(exitCode);
  }

  public static final class ExampleRow {
    public long id;
    public String name;

    public ExampleRow() {}

    @Override
    public String toString() {
      return "ExampleRow{id=" + id + ", name='" + name + "'}";
    }
  }
}
