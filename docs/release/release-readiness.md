# Release Readiness

This checklist prepares the project for a future release review. It does not publish artifacts,
create a tag, or change the project version.

## Current Version

```text
0.1.3
```

The next release version should be chosen by maintainers according to Semantic Versioning. Do not
change `pom.xml`, `README.md`, `CHANGELOG.md`, or `D1Version` until a dedicated release pull request
is opened.

## Required Validations

Run these commands before opening a release pull request and again before tagging:

```bash
mvn -B clean verify
mvn -B -Prelease -Dgpg.skip=true verify
```

The release profile command must build the main jar, sources jar, and javadoc jar. A real release run
must not use `-Dgpg.skip=true`.

The release workflow also creates an offline Javadocs zip, SHA-256 checksums, and GitHub Release
assets for the version tag.

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

The workflow should use Java 17 for the publish job and deploy through Maven Central Portal with:

```bash
mvn -B clean deploy -Prelease
```

Review the current Maven Central publishing plugin settings before tagging so the intended publish
safety model is clear. The current policy is automatic publication after Central Portal validation
when a `v*` tag is pushed.

## Tag Command For Later

After maintainers approve the release, update all version references, update `CHANGELOG.md`, commit
the release changes, and create a tag that matches the release version:

```bash
git tag vX.Y.Z
git push origin vX.Y.Z
```

Do not create a release tag during release-readiness review.

## Pre-Release Checklist

- [ ] Confirm `main` is clean and up to date.
- [ ] Confirm all open pull requests that should be included are merged.
- [ ] Confirm CI passes on Java 8, 11, 15, 17, and 21.
- [ ] Confirm `mvn -B clean verify` passes locally.
- [ ] Confirm `mvn -B -Prelease -Dgpg.skip=true verify` passes locally.
- [ ] Confirm javadocs do not expose internal implementation packages.
- [ ] Confirm test fixtures use only fake token, account, and database values.
- [ ] Confirm no secret values are present in documentation, tests, or workflow files.
- [ ] Confirm Maven Central credentials and signing secrets are configured.
- [ ] Confirm Maven Central publishing settings match automatic publication on release tags.
- [ ] Confirm the release workflow validates and uploads GitHub Release assets.
- [ ] Update version references only in the release pull request.
- [ ] Move changelog entries from `Unreleased` to the selected release version only in the release pull request.
- [ ] Create and push the release tag only after the release pull request is merged.
- [ ] Confirm the GitHub Release includes main jar, sources jar, javadoc jar, offline Javadocs zip, signatures, and checksums.
- [ ] Confirm the Maven Central artifact is available for the release version.
