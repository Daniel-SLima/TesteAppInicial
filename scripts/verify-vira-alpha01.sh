#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "ERRO: $1"
  exit 1
}

grep -q 'versionName = "1.0.0-alpha01"' app/build.gradle.kts || fail "versionName incorreto"
grep -q 'versionCode = 20' app/build.gradle.kts || fail "versionCode incorreto"
grep -q 'applicationId = "com.danielslima.testeappinicial"' app/build.gradle.kts || fail "applicationId mudou"
grep -q '<string name="app_name">Vira</string>' app/src/main/res/values/strings.xml || fail "nome Vira ausente"
grep -q '<string name="brand_public_name">Vira Finanças</string>' app/src/main/res/values/strings.xml || fail "nome público ausente"
grep -q 'feature/vira-1-ui' .github/workflows/android-apk.yml || fail "branch do redesign não está no workflow"
grep -Fq 'APK_NAME="Vira-v${VERSION_NAME}.apk"' .github/workflows/android-apk.yml || fail "nome do APK assinado incorreto"

for icon in mark home movements planning settings; do
  test -f "app/src/main/res/drawable/ic_vira_${icon}.xml" || fail "ícone ic_vira_${icon}.xml ausente"
done

grep -q '<color name="brand_primary">#153F37</color>' app/src/main/res/values/colors.xml || fail "brand_primary incorreto"
grep -q '<color name="background">#F5F1E9</color>' app/src/main/res/values/colors.xml || fail "background incorreto"

layout="app/src/main/res/layout/activity_main.xml"
for id in   homeScreen mainScroll planningScreen settingsScreen   navHome navMovements navPlanning navSettings   descriptionInput valueInput categorySpinner   installmentCheckBox recurringCheckBox registerButton   manageRecurringButton manageBudgetsButton   movementSearchInput filterTypeSpinner filterStatusSpinner filterCategorySpinner   movementsContainer carryBalanceCheckBox securityCheckBox   exportCsvButton backupButton restoreBackupButton; do
  count=$(grep -o "android:id=\"@+id/${id}\"" "$layout" | wc -l | tr -d ' ')
  test "$count" = "1" || fail "id ${id} aparece ${count} vezes"
done

if grep -Rni --exclude='*.md' 'FinTest' app/src/main >/tmp/vira-old-name.txt; then
  cat /tmp/vira-old-name.txt
  fail "nome antigo ainda aparece em recursos/código visível do app"
fi

echo "Vira Alpha 01: estrutura validada"
