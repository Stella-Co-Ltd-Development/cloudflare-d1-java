# 08. Maven and Release Requirements

## Maven Coordinates

```xml
<groupId>io.github.xxvw</groupId>
<artifactId>cloudflare-d1-java</artifactId>
<version>0.1.0-SNAPSHOT</version>
```

Release version:

```xml
<version>0.1.0</version>
```

## Java

```xml
<maven.compiler.release>17</maven.compiler.release>
```

## Dependencies

Runtime:

```xml
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>${jackson.version}</version>
</dependency>
```

Test:

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>${junit.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.assertj</groupId>
  <artifactId>assertj-core</artifactId>
  <version>${assertj.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>com.squareup.okhttp3</groupId>
  <artifactId>mockwebserver3</artifactId>
  <version>${mockwebserver.version}</version>
  <scope>test</scope>
</dependency>
```

## Required Maven Metadata

The POM must include:

- `name`
- `description`
- `url`
- `licenses`
- `developers`
- `scm`
- `issueManagement`

## Required Plugins

- `maven-compiler-plugin`
- `maven-surefire-plugin`
- `maven-source-plugin`
- `maven-javadoc-plugin`
- `maven-gpg-plugin`
- `central-publishing-maven-plugin`

## Release Profile

Release profile must:

- generate sources jar
- generate javadoc jar
- sign artifacts
- publish to Maven Central through Central Portal

## GitHub Actions

CI workflow:

- run on pull requests
- run on pushes to `main`
- test Java 17 and Java 21
- run `mvn -B clean verify`

Release workflow:

- run on tags matching `v*`
- use Java 17
- use Maven Central credentials from secrets
- use GPG private key from secrets
- run `mvn -B clean deploy -Prelease`

## Required Secrets

```text
CENTRAL_USERNAME
CENTRAL_PASSWORD
GPG_PRIVATE_KEY
GPG_PASSPHRASE
```

## Release Checklist

- CI passes on Java 17 and Java 21
- README is complete
- CHANGELOG.md is updated
- LICENSE exists
- SECURITY.md exists
- Maven metadata is complete
- Sources jar is generated
- Javadoc jar is generated
- Artifacts are signed
- Maven Central namespace is verified
- GitHub secrets are configured
- Version is changed from `0.1.0-SNAPSHOT` to `0.1.0`
- Git tag `v0.1.0` is created
- Artifact appears on Maven Central
