#!/bin/bash
set -e

RELEASES=()
EXPERIMENTAL=()

# Collect releases
if [ -d "./releases" ]; then
  for dir in ./releases/*/; do
    [ -d "$dir" ] || continue
    name=$(basename "$dir")
    RELEASES+=("$name")
  done
fi

# Collect experimental branches
if [ -d "./experimental" ]; then
  for dir in ./experimental/*/; do
    [ -d "$dir" ] || continue
    name=$(basename "$dir")
    EXPERIMENTAL+=("$name")
  done
fi

# Sort releases in reverse semver order (newest first)
if [ ${#RELEASES[@]} -gt 0 ]; then
  IFS=$'\n' RELEASES=($(sort -rV <<< "${RELEASES[*]}")); unset IFS
fi

# ── Store source commit metadata for this deploy ───────────────
# DEPLOY_PATH, SOURCE_HASH, SOURCE_DATE are passed in as env vars from the workflow
if [ -n "$DEPLOY_PATH" ] && [ -n "$SOURCE_HASH" ]; then
  mkdir -p ".doc-meta"
  META_KEY="${DEPLOY_PATH//\//_}"
  cat > ".doc-meta/${META_KEY}.json" << JSON
{"hash":"${SOURCE_HASH}","date":"${SOURCE_DATE}"}
JSON
  echo "Stored source metadata for $DEPLOY_PATH → $SOURCE_HASH @ $SOURCE_DATE"
fi

# ── Helper: get source commit hash + date for a path ──────────
get_commit_info() {
  local path="$1"

  # If this is the path we just deployed, use the injected source commit
  if [ "$path" = "$DEPLOY_PATH" ] && [ -n "$SOURCE_HASH" ]; then
    echo "${SOURCE_HASH} ${SOURCE_DATE}"
    return
  fi

  # Try reading from stored metadata file (set by previous deploys)
  local meta=".doc-meta/${path//\//_}.json"
  if [ -f "$meta" ]; then
    local hash date
    hash=$(python3 -c "import json,sys; d=json.load(open('$meta')); print(d.get('hash',''))" 2>/dev/null || grep -o '"hash":"[^"]*"' "$meta" | cut -d'"' -f4)
    date=$(python3 -c "import json,sys; d=json.load(open('$meta')); print(d.get('date',''))" 2>/dev/null || grep -o '"date":"[^"]*"' "$meta" | cut -d'"' -f4)
    echo "${hash:-} ${date:-}"
    return
  fi

  # Last resort: gh-pages git log (hash will be wrong but better than empty)
  local hash date
  hash=$(git log --oneline -1 -- "$path" 2>/dev/null | awk '{print $1}')
  date=$(git log --format="%cI" -1 -- "$path" 2>/dev/null)
  echo "${hash:-} ${date:-}"
}

# ── Build releases JSON ────────────────────────────────────────
RELEASES_JSON="["

for i in "${!RELEASES[@]}"; do
  version="${RELEASES[$i]}"
  read -r HASH DATE <<< "$(get_commit_info "releases/$version")"
  if [ "$i" -eq 0 ]; then
    IS_LATEST="true"
  else
    IS_LATEST="false"
  fi
  RELEASES_JSON+="{\"id\":\"${version}\",\"version\":\"${version}\",\"path\":\"releases/${version}\",\"date\":\"${DATE}\",\"isLatest\":${IS_LATEST}},"
done

RELEASES_JSON="${RELEASES_JSON%,}]"

# ── Build experimental JSON ────────────────────────────────────
EXPERIMENTAL_JSON="["

for branch in "${EXPERIMENTAL[@]}"; do
  read -r HASH DATE <<< "$(get_commit_info "experimental/$branch")"
  EXPERIMENTAL_JSON+="{\"id\":\"${branch}\",\"branch\":\"${branch}\",\"path\":\"experimental/${branch}\",\"hash\":\"${HASH}\",\"date\":\"${DATE}\"},"
done

EXPERIMENTAL_JSON="${EXPERIMENTAL_JSON%,}]"

# ── Write manifest.json ────────────────────────────────────────
cat > manifest.json << JSON
{
  "releases": ${RELEASES_JSON},
  "experimental": ${EXPERIMENTAL_JSON},
  "generated": "$(date -u '+%Y-%m-%dT%H:%M:%SZ')",
  "source": "manifest"
}
JSON

echo "manifest.json generated:"
cat manifest.json
echo ""
echo "Done."
