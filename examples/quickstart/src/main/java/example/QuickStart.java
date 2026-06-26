package example;

import io.github.xxvw.cloudflare.d1.D1AuthenticationException;
import io.github.xxvw.cloudflare.d1.D1AuthorizationException;
import io.github.xxvw.cloudflare.d1.D1Client;
import io.github.xxvw.cloudflare.d1.D1Exception;
import io.github.xxvw.cloudflare.d1.D1QueryException;
import io.github.xxvw.cloudflare.d1.D1RateLimitException;
import io.github.xxvw.cloudflare.d1.D1Result;
import io.github.xxvw.cloudflare.d1.D1TimeoutException;
import io.github.xxvw.cloudflare.d1.D1TransportException;
import java.util.Map;

public final class QuickStart {
  private static final String DEFAULT_SQL = "SELECT 1 AS value";

  private QuickStart() {}

  public static void main(String[] args) {
    String sql = args.length == 0 ? DEFAULT_SQL : joinArgs(args);

    try (D1Client d1 = D1Client.fromEnv()) {
      D1Result result = d1.query(sql);
      System.out.println("SQL: " + sql);
      System.out.println("Rows: " + result.rowCount());
      for (Map<String, Object> row : result.rows()) {
        System.out.println(row);
      }
    } catch (IllegalStateException exception) {
      fail(2, "Missing required environment variable: " + exception.getMessage());
    } catch (D1AuthenticationException exception) {
      fail(2, "Authentication failed. Check CLOUDFLARE_API_TOKEN.");
    } catch (D1AuthorizationException exception) {
      fail(3, "Authorization failed. Check token permissions, account ID, and database ID.");
    } catch (D1RateLimitException exception) {
      String retryAfter = exception.retryAfter()
          .map(Object::toString)
          .orElse("not provided");
      fail(4, "Rate limited by Cloudflare. Retry-After: " + retryAfter);
    } catch (D1QueryException exception) {
      fail(5, "D1 query failed. Check the SQL statement and target database.");
    } catch (D1TimeoutException exception) {
      fail(6, "D1 request timed out.");
    } catch (D1TransportException exception) {
      fail(6, "D1 transport failed.");
    } catch (D1Exception exception) {
      fail(1, "D1 request failed.");
    }
  }

  private static String joinArgs(String[] args) {
    StringBuilder joined = new StringBuilder();
    for (String arg : args) {
      if (joined.length() > 0) {
        joined.append(' ');
      }
      joined.append(arg);
    }
    return joined.toString();
  }

  private static void fail(int exitCode, String message) {
    System.err.println(message);
    System.exit(exitCode);
  }
}
