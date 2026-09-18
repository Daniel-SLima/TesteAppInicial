#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "ERRO: $1"; exit 1; }
grep -q 'versionName = "1.0.0-alpha04.1"' app/build.gradle.kts || fail "versionName Alpha 04.1 incorreto"
grep -q 'versionCode = 24' app/build.gradle.kts || fail "versionCode Alpha 04.1 incorreto"
grep -q 'applicationId = "com.danielslima.testeappinicial"' app/build.gradle.kts || fail "applicationId mudou"
for file in app/src/main/res/layout/dialog_vira_budget_manager.xml app/src/main/res/layout/dialog_vira_budget_editor.xml app/src/main/res/drawable/vira_checkbox.xml app/src/main/res/drawable/vira_dialog_surface.xml; do test -f "$file" || fail "arquivo $file ausente"; done
grep -q 'android:button="@drawable/vira_checkbox"' app/src/main/res/layout/activity_main.xml || fail "checkbox Vira não aplicado"
grep -q 'budgetCategoriesContainer' app/src/main/res/layout/dialog_vira_budget_manager.xml || fail "gerenciador de orçamento incompleto"
grep -q 'budgetValueInput' app/src/main/res/layout/dialog_vira_budget_editor.xml || fail "editor de orçamento incompleto"
if grep -n 'setItems(itens)' app/src/main/java/com/danielslima/testeappinicial/MainActivity.kt; then fail "orçamento ainda usa lista nativa"; fi
if grep -Rn --exclude='*.md' 'FinTest' app/src/main >/tmp/vira-old-name.txt; then cat /tmp/vira-old-name.txt; fail "nome visível antigo ainda aparece no app"; fi
for id in newMovementButton filterTypeChip filterStatusChip filterCategoryChip movementsContainer; do
  grep -q "android:id=\"@+id/$id\"" app/src/main/res/layout/activity_main.xml || fail "id $id ausente"
done
test -f app/src/main/res/layout/dialog_vira_new_movement.xml || fail "novo lançamento Vira ausente"
test -f app/src/main/res/layout/dialog_vira_choice.xml || fail "seletor Vira ausente"
test -f app/src/main/res/layout/dialog_vira_edit_movement.xml || fail "editor Vira ausente"
for id in planningMonthNetText planningIncomeExpectedText planningExpenseExpectedText planningMonthsContainer planningCommitmentsContainer planningRecurringCountText planningInstallmentCountText; do
  grep -q "android:id=\"@+id/$id\"" app/src/main/res/layout/activity_main.xml || fail "planejamento sem $id"
done
grep -q 'listarParcelasPendentesAPartirDe' app/src/main/java/com/danielslima/testeappinicial/MovimentacaoDatabase.kt || fail "resumo de parcelas ausente"
test -f app/src/main/res/drawable/ic_vira_repeat.xml || fail "ícone de fixos ausente"
test -f app/src/main/res/drawable/ic_vira_installments.xml || fail "ícone de parcelas ausente"
test -f app/src/main/res/layout/dialog_vira_month_picker.xml || fail "seletor de mês ausente"
grep -q 'abrirSeletorMes' app/src/main/java/com/danielslima/testeappinicial/MainActivity.kt || fail "navegação rápida entre meses ausente"
grep -q 'Voltar ao mês atual' app/src/main/res/values/strings.xml || fail "atalho para mês atual ausente"
if grep -q 'Movimentação salva no aparelho' app/src/main/java/com/danielslima/testeappinicial/MainActivity.kt; then fail "toast de sucesso do lançamento ainda presente"; fi
grep -q 'android:paddingTop="12dp"' app/src/main/res/layout/activity_main.xml || fail "respiro superior ausente"
echo "Vira Alpha 04.1: navegação mensal e feedback validados"
