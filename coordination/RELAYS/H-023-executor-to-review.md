# [H-023] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-023-fallback-warning-recovery-action-execution-tracking.md`
- main relay: `coordination/RELAYS/H-023-main-to-executor.md`
- result: `coordination/REPORTS/H-023-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-023 섹션 추가 (`executionRecoveryTrend`, `recoveryActionStatus`, delta 산식/해석 규칙 반영)
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 동기화 (H-023 출력 계약/보고 형식 반영)
  - 최근 7일 vs 직전 7일 실측 반영: 전체 `executionGapDelta=+3`, `chainShareGapDelta=0.00%p` (미개선)
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-023-result.md`
  - `coordination/RELAYS/H-023-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `executionRecoveryTrend`/`recoveryActionStatus` 출력 계약이 운영 문서와 야간 템플릿에 동일하게 반영됐는지
2. `executionGapDelta`/`chainShareGapDelta` 산식 및 개선/미개선 해석 규칙이 문서 간 불일치 없이 적용됐는지
3. `HOLD` 시 원인별 상태(`IN_PROGRESS|BLOCKED|DONE`)와 근거(runId/집계표)가 결과 보고서와 정합한지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0일`로 `HOLD` 지속 리스크가 큼
- 최근 7일 대비 직전 7일 `executionGapDelta=+3`으로 실행량 회복 추세가 악화됨
- `DOC/REVIEW` 체인 실행 부재(`actualChainRuns=0`)로 `CHAIN_COVERAGE_GAP` 개선 신호 없음

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-023-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
