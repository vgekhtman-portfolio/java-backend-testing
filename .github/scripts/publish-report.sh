#!/usr/bin/env bash
# Publishes an Allure report to the gh-pages branch: the main report at the site root, any other branch's report in
# a subfolder of its own, so one branch's run never overwrites another's.
#
# usage: publish-report.sh <remote-url> <report-dir> [subfolder]
set -euo pipefail

remote=$1
report=$(cd "$2" && pwd)
subfolder=${3:-}

site=$(mktemp -d)
cd "$site"
git init -q -b gh-pages
git remote add origin "$remote"
if git fetch -q --depth 1 origin gh-pages 2>/dev/null; then
  git checkout -q -B gh-pages FETCH_HEAD
fi

if [ -z "$subfolder" ]; then
  # Root report: replace everything except the subfolder reports.
  find . -mindepth 1 -maxdepth 1 ! -name .git ! -name develop -exec rm -rf {} +
  cp -R "$report"/. .
else
  rm -rf "$subfolder"
  mkdir -p "$subfolder"
  cp -R "$report"/. "$subfolder"/
fi
touch .nojekyll

# One commit with no history: the reports are replaced wholesale on every run, so keeping old versions would only
# grow the repository.
git add -A
git checkout -q --orphan publish
git -c user.name="github-actions[bot]" -c user.email="41898282+github-actions[bot]@users.noreply.github.com" \
  commit -q -m "Publish Allure report${subfolder:+ ($subfolder)}"
git push -q --force origin publish:gh-pages
