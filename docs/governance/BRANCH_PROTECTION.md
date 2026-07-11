# Branch Protection Guide

The `main` branch must be protected.

## Required Settings

Enable branch protection for:

```text
main
```

Required settings:

- Require a pull request before merging
- Require status checks to pass before merging
- Require branches to be up to date before merging
- Require the CI workflow matrix checks to pass:
  - `Test on Java 8`
  - `Test on Java 11`
  - `Test on Java 15`
  - `Test on Java 17`
  - `Test on Java 21`
- Require conversation resolution before merging
- Disable force pushes
- Disable branch deletion
- Use squash merge by default
- Apply the rules to administrators

## Direct Pushes

Direct pushes to `main` must be disabled.

All changes should go through pull requests.

## Reviews

If the repository has multiple maintainers, require at least one maintainer review before merging.

For single-maintainer development, PRs should still be used to preserve review history and CI checks.

The current branch protection policy requires pull requests but does not require an approving
review, so a single maintainer can still merge after CI and conversation-resolution requirements are
satisfied.
