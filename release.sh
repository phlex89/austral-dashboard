#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
REPO="phlex89/austral-dashboard"
IMPRONTA_CHIAVE="7412986ffb55150cc5c0e6cb8fc7fb1462eec5292c5d7cfdf119478d2b269e36"
LIVELLO="${1:-patch}"

[ -f keystore.properties ] || { echo "Manca keystore.properties: senza la chiave di firma il box rifiuta l'aggiornamento." >&2; exit 1; }
[ -z "$(git status --porcelain)" ] || { echo "Ci sono modifiche non committate: committale prima di pubblicare." >&2; exit 1; }
[ "$(git rev-parse --abbrev-ref HEAD)" = main ] || { echo "Si pubblica solo da main." >&2; exit 1; }

versionCode=$(grep '^versionCode=' version.properties | cut -d= -f2)
versionName=$(grep '^versionName=' version.properties | cut -d= -f2)
if gh release view "v$versionName" --repo "$REPO" >/dev/null 2>&1; then
  IFS=. read -r maggiore minore correzione <<< "$versionName"
  case "$LIVELLO" in
    major) maggiore=$((maggiore + 1)); minore=0; correzione=0 ;;
    minor) minore=$((minore + 1)); correzione=0 ;;
    patch) correzione=$((correzione + 1)) ;;
    *) echo "Uso: ./release.sh [major|minor|patch]" >&2; exit 1 ;;
  esac
  codice=$((versionCode + 1))
  nome="$maggiore.$minore.$correzione"
else
  codice=$versionCode
  nome=$versionName
fi

trap 'git checkout -q -- version.properties' ERR
printf 'versionCode=%s\nversionName=%s\n' "$codice" "$nome" > version.properties
./gradlew -q :app:testDebugUnitTest :app:assembleRelease
apksigner=$(ls "${ANDROID_HOME:-$HOME/Library/Android/sdk}"/build-tools/*/apksigner | tail -1)
impronta=$("$apksigner" verify --print-certs app/build/outputs/apk/release/app-release.apk 2>/dev/null \
  | sed -n 's/^Signer #1 certificate SHA-256 digest: //p')
[ "$impronta" = "$IMPRONTA_CHIAVE" ] || { echo "L'APK non è firmato con la chiave di sempre: il box rifiuterebbe l'aggiornamento." >&2; false; }
trap - ERR

uscita=build/release
mkdir -p "$uscita"
cp app/build/outputs/apk/release/app-release.apk "$uscita/austral-dashboard.apk"
sha=$(shasum -a 256 "$uscita/austral-dashboard.apk" | cut -d' ' -f1)
printf '{"versionCode":%s,"versionName":"%s","apk":"https://github.com/%s/releases/download/v%s/austral-dashboard.apk","sha256":"%s"}\n' \
  "$codice" "$nome" "$REPO" "$nome" "$sha" > "$uscita/versione.json"

git diff --quiet -- version.properties || git commit -q -m "Versione $nome" -- version.properties
git rev-parse -q --verify "refs/tags/v$nome" >/dev/null || git tag "v$nome"
git push -q origin main "v$nome"
gh release create "v$nome" "$uscita/austral-dashboard.apk" "$uscita/versione.json" \
  --repo "$REPO" --title "v$nome" --notes "Versione $nome (codice $codice)"

echo "Pubblicata v$nome (codice $codice), $(stat -f%z "$uscita/austral-dashboard.apk") byte"
