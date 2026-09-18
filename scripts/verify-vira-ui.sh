#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "ERRO: $1"; exit 1; }
grep -q 'versionName = "1.0.0-alpha06"' app/build.gradle.kts || fail "versionName Alpha 06 incorreto"
grep -q 'versionCode = 26' app/build.gradle.kts || fail "versionCode Alpha 06 incorreto"
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
if grep -A 5 'android:id="@+id/screenHost"' app/src/main/res/layout/activity_main.xml | grep -q 'android:paddingTop="12dp"'; then fail "padding fixo antigo ainda presente no screenHost"; fi
grep -q 'aplicarInsetsSistema' app/src/main/java/com/danielslima/testeappinicial/MainActivity.kt || fail "insets reais do Android ausentes"
grep -q 'homeChooseMonthAction' app/src/main/res/layout/activity_main.xml || fail "atalho de mês na Home ausente"
grep -q 'movementsChooseMonthAction' app/src/main/res/layout/activity_main.xml || fail "atalho de mês em Movimentações ausente"
grep -q 'planningChooseMonthAction' app/src/main/res/layout/activity_main.xml || fail "atalho de mês no Planejamento ausente"
grep -q 'notificationCheckBox' app/src/main/res/layout/activity_main.xml || fail "controle de notificações ausente"
grep -q 'POST_NOTIFICATIONS' app/src/main/AndroidManifest.xml || fail "permissão de notificações ausente"
grep -q 'ViraReminderReceiver' app/src/main/AndroidManifest.xml || fail "receiver de lembretes ausente"
grep -q 'ViraBootReceiver' app/src/main/AndroidManifest.xml || fail "receiver de reinício ausente"
test -f app/src/main/java/com/danielslima/testeappinicial/ViraReminderScheduler.kt || fail "scheduler de lembretes ausente"
test -f app/src/main/java/com/danielslima/testeappinicial/ViraReminderReceiver.kt || fail "receiver de lembretes ausente"
test -f app/src/main/java/com/danielslima/testeappinicial/ViraBootReceiver.kt || fail "receiver de boot ausente"
if grep -q 'Movimentação atualizada' app/src/main/java/com/danielslima/testeappinicial/MainActivity.kt; then fail "toast de atualização ainda presente"; fi
if grep -q 'take(4)' app/src/main/java/com/danielslima/testeappinicial/MainActivity.kt; then fail "gráfico de categorias ainda truncado"; fi
grep -q 'feedbackBanner' app/src/main/res/layout/activity_main.xml || fail "feedback próprio do Vira ausente"
grep -q 'mostrarFeedbackVira' app/src/main/java/com/danielslima/testeappinicial/MainActivity.kt || fail "helper de feedback próprio ausente"
for texto in 'Fixo atualizado para as próximas projeções' 'Fixo desativado. Histórico mantido e projeções futuras removidas.' 'Backup do Vira criado com sucesso' 'Backup restaurado com sucesso'; do
  if grep -q "$texto" app/src/main/java/com/danielslima/testeappinicial/MainActivity.kt; then fail "toast de sucesso antigo ainda presente: $texto"; fi
done
echo "Vira Alpha 06: feedback próprio e polimento de sucesso validados"
