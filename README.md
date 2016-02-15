## Introduction

`portal-core` is a library of common services, tools, and widgets, used to
facilitate development of web portal applications that consume OGC services. It
is actively maintained by CSIRO, and it is currently used by AuScope,
Geoscience, and VGL portals.

See:

* https://twiki.auscope.org/wiki/Grid/AuScopePortalCoreArchitecture
* https://twiki.auscope.org/wiki/Grid/AuscopeResearchPortalDevelopment

## Geoscience Australia

Geoscience Australia is developing Geoscience Portal, a customised and
re-branded copy of the AuScope Portal. This fork of `AuScope/portal-core` is
our means of contributing to the development of the shared `portal-core` library.

CSIRO owns `portal-core`. At GA, our intention is not to introduce a permanent
fork of `portal-core`. We will try to contribute every change we require back into
`AuScope/portal-core`.

### Branching Strategy

We assume the following setup of remotes in your local clone of
`GeoscienceAustralia/portal-core`:

```
$ git remote -v
origin  https://github.com/GeoscienceAustralia/portal-core.git (fetch)
origin  https://github.com/GeoscienceAustralia/portal-core.git (push)
csiro   https://github.com/AuScope/portal-core.git (fetch)
csiro   https://github.com/AuScope/portal-core.git (push)
```

### `master`
Our `master` branch tracks `AuScope/portal-core#master`. We don't ourselves commit to
it, we use it as a base for our feature branches. Anyone can at
any time fast-forward the `master` branch to include the latest upstream work from
`AuScope/portal-core#master`.

```
git checkout master
git pull csiro master
git push origin master
```

### `master-ga`
While we wait for our pull requests to be merged upstream into
`AuScope/portal-core`, we integrate and test our work in
`master-ga`. Our continuous integration job polls this branch and deploys
changes to http://portal-dev.geoscience.gov.au.

### Feature branches
To begin work on a new feature, update `master` and branch.

```
git checkout master
git pull csiro master
git push origin master
git checkout -b feature-X
```

At anytime, you can request that your work be peer-reviewed and integrated by
sending a pull request to merge your feature branch into `master-ga`. Do not
merge other people's work into your feature branches. Do not keep your feature
branches up-to-date with upstream by merging, rebase instead.
Unrelated commits in your feature branches will make your work harder to merge
in isolation into `AuScope/portal-core`.

When your feature is complete, integrated and tested, send a pull request to
`AuScope/portal-core#master`. Eventually, your changes will arrive back into
our `master` branch when someone updates it and become part of our
subsequent work.




