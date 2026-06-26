# Quick Start

`cloudflare-d1-java` is an unofficial Java client for the Cloudflare D1 REST API.

## Install

```xml
<dependency>
  <groupId>io.github.xxvw</groupId>
  <artifactId>cloudflare-d1-java</artifactId>
  <version>0.1.4</version>
</dependency>
```

Java 8 or newer is required.

Gradle:

```groovy
implementation "io.github.xxvw:cloudflare-d1-java:0.1.4"
```

## Configure Credentials

Set the required environment variables. For local manual testing from this repository, copy the
example file and load it into your shell:

```bash
cp .env.example .env
$EDITOR .env

set -a
. ./.env
set +a
```

Create the API token in the Cloudflare dashboard. For write operations, use an account-scoped D1 token with Edit permission. Store tokens outside source control and never paste production values into issues or logs.

## Run a Query

```java
import io.github.xxvw.cloudflare.d1.D1Client;
import io.github.xxvw.cloudflare.d1.D1Result;
import java.util.Map;

public final class Example {
  public static void main(String[] args) {
    try (D1Client d1 = D1Client.fromEnv()) {
      D1Result result = d1.query("SELECT id, name FROM users WHERE active = ?", true);

      for (Map<String, Object> row : result.rows()) {
        System.out.println(row);
      }
    }
  }
}
```

## Run the Repository Example

The repository includes a standalone Maven example that runs the same flow from environment variables.

```bash
set -a
. ./.env
set +a

mvn -f examples/quickstart/pom.xml compile exec:java
```

Expected output for the default query:

```text
SQL: SELECT 1 AS value
Rows: 1
{value=1}
```

To run a different read query:

```bash
mvn -f examples/quickstart/pom.xml exec:java -Dexec.args="SELECT 42 AS answer"
```

To run the read-only async examples:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.AsyncExamples
```

To run read and typed mapping examples:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.MappingAndWriteExamples
```

To run the opt-in write example:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.MappingAndWriteExamples \
  -Dexec.args="--write"
```

To run a fake custom transport example that does not contact Cloudflare:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.CustomTransportExample
```

To run the opt-in async write example:

```bash
mvn -f examples/quickstart/pom.xml compile exec:java \
  -Dexec.mainClass=example.AsyncExamples \
  -Dexec.args="--write"
```

## Execute a Write

```java
D1Result result = d1.execute(
    "INSERT INTO users(name, email) VALUES (?, ?)",
    "Taro",
    "taro@example.com"
);

System.out.println(result.meta().changes());
```

Writes do not retry by default because the statement may not be idempotent.

## Common Setup Checks

- Confirm the account ID belongs to the account that owns the D1 database.
- Confirm the database ID is the D1 database ID, not the database name.
- Confirm the token has permission for the target account and database.
- Confirm the SQL statement works in the Cloudflare dashboard or Wrangler before debugging Java code.
- Confirm the D1 REST API is the right path for your workflow. For application traffic that needs lower latency, consider a Cloudflare Worker proxy or the Workers D1 binding.
