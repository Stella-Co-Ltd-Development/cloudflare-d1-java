# v0.1.0 Release Readiness

This checklist prepares the project for a future v0.1.0 release review. It does not publish
artifacts, create a tag, or change the project version.

## Current Version

```text
0.1.0-SNAPSHOT
```

The version must remain `0.1.0-SNAPSHOT` until the maintainer starts the actual release process.

## Required Validations

Run these commands before opening a release pull request and again before tagging:

```bash
mvn -B clean verify
mvn -B -Prelease -Dgpg.skip=true verify
```

The release profile command must build the main jar, sources jar, and javadoc jar. A real release
run must not use `-Dgpg.skip=true`.

## Maven Central Prerequisites

- Maven Central namespace is verified for `io.github.xxvw`.
- Repository metadata in `pom.xml` is complete.
- `LICENSE`, `SECURITY.md`, `README.md`, and `CHANGELOG.md` are present and current.
- Sources and javadocs build from a clean working tree.
- Signing is configured for the release workflow.

## GitHub Secrets Required

Configure these repository secrets before publishing:

```text
CENTRAL_USERNAME
CENTRAL_PASSWORD
GPG_PRIVATE_KEY
GPG_PASSPHRASE
```

Do not place secret values in issues, pull requests, logs, screenshots, or local documentation.

## Release Workflow Expectation

The release workflow runs for tags matching:

```text
v*
```

The workflow should use Java 17 and publish through Maven Central Portal with:

```bash
mvn -B clean deploy -Prelease
```

## Tag Command For Later

After maintainers approve the release, update the version to `0.1.0`, update `CHANGELOG.md`, commit
the release changes, and create the release tag:

```bash
git tag v0.1.0
git push origin v0.1.0
```

Do not create this tag during release-readiness review.

## Pre-Release Checklist

- [ ] Confirm `main` is clean and up to date.
- [ ] Confirm all open pull requests that should be included are merged.
- [ ] Confirm CI passes on Java 17 and Java 21.
- [ ] Confirm `mvn -B clean verify` passes locally.
- [ ] Confirm `mvn -B -Prelease -Dgpg.skip=true verify` passes locally.
- [ ] Confirm javadocs do not expose internal implementation packages.
- [ ] Confirm test fixtures use only fake token, account, and database values.
- [ ] Confirm no secret values are present in documentation, tests, or workflow files.
- [ ] Confirm Maven Central credentials and signing secrets are configured.
- [ ] Update version from `0.1.0-SNAPSHOT` to `0.1.0` only in the release pull request.
- [ ] Move changelog entries from `Unreleased` to `0.1.0` only in the release pull request.
- [ ] Create and push `v0.1.0` only after the release pull request is merged.
