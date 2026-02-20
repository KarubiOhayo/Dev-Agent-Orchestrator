# [H-033] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-033-fallback-warning-keep-frozen-evidence-accumulation-check.md`
- main relay: `coordination/RELAYS/H-033-main-to-executor.md`
- result: `coordination/REPORTS/H-033-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-033 섹션을 추가하고 `evidenceAccumulationSummary[]` 필수 필드/산식(`coverageRate`, `freshEvidenceCount`) 및 48시간 stale 판정 기준을 계약으로 고정함.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `evidenceAccumulationSummary[]` 고정 출력 규칙을 추가하고 `KEEP_FROZEN` 분기 누락 금지 항목을 확장함.
  - `docs/PROJECT_OVERVIEW.md` 완료 항목에 H-033 반영 및 상태 날짜를 최소 동기화함.
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/REPORTS/H-033-result.md`
  - `coordination/RELAYS/H-033-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md` H-033 섹션이 `evidenceAccumulationSummary[]` 필수 필드(`requiredEvidenceCount`, `observedEvidenceCount`, `coverageRate`, `staleEvidenceCount`, `freshEvidenceCount`, `status`, `lastObservedAt`)와 산식/48시간 stale 규칙을 정확히 반영했는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에서 `KEEP_FROZEN` 분기 시 `signalRecoveryEvidenceLedger[]` + `evidenceAccumulationSummary[]` + `recoveryActionTracking[]` + `recoveryActionCompletionRate` + `blockedActionCount` 누락 금지 규칙이 반영됐는지
3. H-033 수치/판정(`executionGapDelta=+5`, `chainShareGapDelta=0.00%p`, `LOW_TRAFFIC coverageRate=0.50(stale=1)`, `CHAIN_COVERAGE_GAP coverageRate=0.00`, `resumeDecision=KEEP_FROZEN`)이 문서-리포트 간 일치하는지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 기준 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0`으로 `KEEP_FROZEN` 장기화 가능성이 높음
- 최근 7일 실행량이 전 에이전트 `0`으로 `LOW_TRAFFIC` 증거가 stale 상태로 누적되는 위험이 존재
- `DOC/REVIEW` 체인 실행 실적 부재(`actualChainRuns=0/0`)로 `CHAIN_COVERAGE_GAP` 개선 증거 축적이 지연됨

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-033-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
