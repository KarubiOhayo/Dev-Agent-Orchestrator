# [H-034] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-034-fallback-warning-keep-frozen-fresh-evidence-recovery-check.md`
- main relay: `coordination/RELAYS/H-034-main-to-executor.md`
- result: `coordination/REPORTS/H-034-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-034 섹션을 추가해 `evidenceFreshnessSummary[]` 필수 필드/산식(`freshnessRate`) 및 판정 규칙(`SUFFICIENT/INSUFFICIENT`)을 계약으로 고정함.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `evidenceFreshnessSummary[]` 고정 출력 규칙을 추가하고 `KEEP_FROZEN` 분기 누락 금지 항목을 확장함.
  - `docs/PROJECT_OVERVIEW.md` 완료 항목에 H-034를 반영해 상태 문서 최소 동기화함.
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/REPORTS/H-034-result.md`
  - `coordination/RELAYS/H-034-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md` H-034 섹션에서 `signalRecoveryEvidenceLedger[]`/`evidenceAccumulationSummary[]` 유지 + `evidenceFreshnessSummary[]` 신규 필드/산식/판정 규칙이 함께 정합적으로 반영됐는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 `HOLD|KEEP_FROZEN` 분기에서 `evidenceFreshnessSummary[]`를 필수 출력으로 요구하고, `resumeDecision=KEEP_FROZEN` 누락 금지 목록에 포함했는지
3. H-034 결과 수치/판정(`executionGapDelta=+5`, `chainShareGapDelta=0.00%p`, `LOW_TRAFFIC freshnessRate=0.00/stale=1`, `CHAIN_COVERAGE_GAP freshnessRate=0.00`, `resumeDecision=KEEP_FROZEN`)이 문서-결과보고-릴레이 간 일치하는지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0` 상태가 유지되어 `RESUME_H024` 전환 근거가 부족함
- `LOW_TRAFFIC`는 누적 증거가 stale(`staleEvidenceCount=1`)이고 fresh 증거가 없어(`freshEvidenceCount=0`) 신호 복구가 지연됨
- `CHAIN_COVERAGE_GAP`은 누적/신선 증거 모두 `0`으로 체인 커버리지 개선 근거가 부재함

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-034-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
