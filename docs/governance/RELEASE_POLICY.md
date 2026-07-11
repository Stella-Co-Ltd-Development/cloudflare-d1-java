# Release Policy

Only maintainers may publish releases.

## Release Tags

Release tags must match:

```text
v*
```

Example:

```text
v0.1.0
```

## Maven Central

Maven Central publishing must be performed through GitHub Actions.

Local manual publishing is discouraged.

The release workflow publishes validated deployments automatically after a release tag is pushed.
Maintainers must review release inputs, version references, changelog entries, and CI status before
creating the tag.

The release workflow should use the `maven-central` environment so maintainers can add environment
protection rules before publishing credentials are used.

## Required Artifacts

Release artifacts must include:

- main jar
- sources jar
- javadoc jar
- signatures
- SHA-256 checksums
- offline Javadocs zip

The Maven artifacts are published to Maven Central. The same versioned jars, signatures, checksums,
and offline Javadocs zip are attached to the GitHub Release for the tag.

## Release Checklist

Before publishing:

- CI passes on Java 17 and Java 21
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
- The `maven-central` environment protection rules are reviewed
- Version references are updated for the release version
- Git tag is created
- Maven Central publication succeeds
- GitHub Release contains the expected versioned assets

## Release Notes

Release notes must not include AI tool names, assistant names, or generation metadata.
