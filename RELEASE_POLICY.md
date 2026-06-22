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

The release workflow must upload the deployment with automatic Maven Central publication disabled.
Maintainers should review and publish the uploaded deployment from Maven Central after the workflow
succeeds.

The release workflow should use the `maven-central` environment so maintainers can add environment
protection rules before publishing credentials are used.

## Required Artifacts

Release artifacts must include:

- main jar
- sources jar
- javadoc jar
- signatures

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
- Maven Central namespace is verified
- GitHub secrets are configured
- The `maven-central` environment protection rules are reviewed
- The Maven Central deployment is reviewed before publication
- Version is changed from `0.1.0-SNAPSHOT` to `0.1.0`
- Git tag is created

## Release Notes

Release notes must not include AI tool names, assistant names, or generation metadata.
