# Phase 06: Release and Maven Central Requirements

## Maven Central Requirements

The POM must include:

- `name`
- `description`
- `url`
- `licenses`
- `developers`
- `scm`
- `issueManagement`

## Required Artifacts

- main jar
- sources jar
- javadoc jar
- signatures

## GitHub Actions

CI must test Java 8, 11, 15, 17, and 21.

Release workflow must deploy to Maven Central on tags matching:

```text
v*
```

Automatic Maven Central publication is enabled after Central Portal validation. Maintainers should
review release inputs before tagging because the tag workflow publishes validated artifacts.

## Required Secrets

```text
CENTRAL_USERNAME
CENTRAL_PASSWORD
GPG_PRIVATE_KEY
GPG_PASSPHRASE
```

## Release Checklist

- CI passes on Java 8, 11, 15, 17, and 21
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
- Maven Central deployment is validated and published by the release workflow
- Version is changed from `0.1.0-SNAPSHOT` to `0.1.0`
- Git tag `v0.1.0` is created
- Artifact appears on Maven Central
