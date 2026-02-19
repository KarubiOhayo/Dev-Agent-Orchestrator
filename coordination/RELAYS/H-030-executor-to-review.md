# [H-030] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-030-fallback-warning-keep-frozen-recovery-tracking.md`
- main relay: `coordination/RELAYS/H-030-main-to-executor.md`
- result: `coordination/REPORTS/H-030-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-030 섹션을 추가해 최신 14일/7일 실측, `dailyCompliance`/`weeklyComplianceRate`, 단일 판정(`KEEP_FROZEN`)을 고정했습니다.
  - H-030 신규 계약 `recoveryActionTracking[]`(`signal`, `priority`, `status`, `owner`, `evidenceRef`, `nextAction`, `updatedAt`) + `recoveryActionCompletionRate` + `blockedActionCount` + `latestDecisionReason`를 문서화했습니다.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화하고, `resumeDecision=KEEP_FROZEN`일 때 신규 출력 3종 누락 금지 규칙을 추가했습니다.
  - `docs/PROJECT_OVERVIEW.md`를 최소 동기화해 H-030 완료 상태를 반영했습니다.
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/REPORTS/H-030-result.md`
  - `coordination/RELAYS/H-030-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md` H-030 섹션의 `recoveryActionTracking[]` 필드 구성이 handoff 필수 필드(`signal`, `priority`, `status`, `owner`, `evidenceRef`, `nextAction`, `updatedAt`)를 충족하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에서 기존 계약(`recalibrationReadiness`, `unmetGates`, `resumeDecision`, `executionRecovery*`) 유지 상태로 신규 출력(`recoveryActionTracking[]`, `recoveryActionCompletionRate`, `blockedActionCount`)이 충돌 없이 추가되었는지
3. H-030 단일 판정(`KEEP_FROZEN`)과 근거 수치(`executionGapDelta=+3`, `chainShareGapDelta=0.00%p`, `weeklyComplianceRate=0.00`, `recoveryActionCompletionRate=0.00`)가 문서/리포트 간 일치하는지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0`으로 재개 조건 미충족이 지속됩니다.
- 최근 7일 `DOC/REVIEW actualChainRuns=0`이라 `CHAIN_COVERAGE_GAP` 해소 증거 누적이 부족합니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-030-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
