# Branch Protection Guide

The `main` branch must be protected.

## Required Settings

Enable branch protection for:

```text
main
```

Recommended settings:

- Require a pull request before merging
- Require status checks to pass before merging
- Require the CI workflow to pass
- Require conversation resolution before merging
- Disable force pushes
- Disable branch deletion
- Use squash merge by default

## Direct Pushes

Direct pushes to `main` must be disabled.

All changes should go through pull requests.

## Reviews

If the repository has multiple maintainers, require at least one maintainer review before merging.

For single-maintainer development, PRs should still be used to preserve review history and CI checks.
