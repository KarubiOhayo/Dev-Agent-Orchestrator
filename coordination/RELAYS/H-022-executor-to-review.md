# [H-022] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-022-fallback-warning-execution-mix-recovery-action-plan.md`
- main relay: `coordination/RELAYS/H-022-main-to-executor.md`
- result: `coordination/REPORTS/H-022-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-022 실행량 회복 액션 플랜 섹션을 추가해 최근 7일 목표-실적 gap(`targetDirectRuns`, `targetChainRuns`, `targetTotalRuns`, `targetChainShare`, `actual*`, `executionGap`, `chainShareGap`)과 최신 14일 게이트 판정을 고정했습니다.
  - `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 분리 판정 기준을 `executionGap` + `chainShareGap`/`DOC-REVIEW chainRuns` 중심으로 명시하고, `HOLD` 시 일일 우선 액션(직접/체인 증량, 점검 시각/담당)을 문서화했습니다.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화해 `executionRecoveryPlan`/`executionRecoveryProgress`를 필수 출력 항목으로 추가했습니다.
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-022-result.md`
  - `coordination/RELAYS/H-022-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 H-022 섹션에서 산식(`targetTotalRuns`, `actualTotalRuns`, `executionGap`, `chainShareGap`)과 해석 규칙(`LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP`)이 handoff 요구사항과 일치하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 기존 출력(`executionMix`, `agentExecution`, `overallExecutionRate`)을 유지하면서 신규 출력(`executionRecoveryPlan`, `executionRecoveryProgress`)을 필수화했는지
3. `HOLD` 원인 분류/다음 액션이 목표-실적 gap 근거(`executionGap`, `chainShareGap`, `DOC/REVIEW actualChainRuns`)와 함께 보고되도록 템플릿이 고정되었는지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 게이트에서 `INSUFFICIENT_SAMPLE` 비율(`1.00`)과 샘플 충분 일수(`0일`) 미충족이 지속되어 `HOLD` 상태가 유지됨.
- 최근 7일 `DOC`/`REVIEW` 체인 실행이 `0건`으로, 체인 커버리지 회복이 아직 관측되지 않음.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-022-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
