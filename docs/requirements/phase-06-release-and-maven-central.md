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
- SHA-256 checksums
- offline Javadocs zip

## GitHub Actions

CI must test Java 8, 11, 15, 17, and 21.

Release workflow must deploy to Maven Central on tags matching:

```text
v*
```

Automatic Maven Central publication is enabled after Central Portal validation. Maintainers should
review release inputs before tagging because the tag workflow publishes validated artifacts.

The release workflow must also attach versioned artifacts, signatures, checksums, and offline
Javadocs to the GitHub Release for the tag.

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
- Offline Javadocs zip is generated and contains `index.html`
- SHA-256 checksums are generated
- Maven Central namespace is verified
- GitHub secrets are configured
- Maven Central deployment is validated and published by the release workflow
- Version references are updated for the release version
- Git tag matching the release version is created
- Artifact appears on Maven Central
- GitHub Release contains the expected versioned assets
