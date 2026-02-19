# [H-021] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-021-fallback-warning-execution-mix-recovery-tracking.md`
- main relay: `coordination/RELAYS/H-021-main-to-executor.md`
- result: `coordination/REPORTS/H-021-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-021 호출 믹스 추적 섹션을 추가해 최근 7일 `directRuns`/`chainRuns`/`totalActualRuns`/`chainShare`와 최신 14일 게이트 판정을 연계했습니다.
  - 호출 믹스/실행률 산식(`totalActualRuns`, `chainShare`, `achievementRate`, `overallExecutionRate`)과 `totalActualRuns=0` 분모 0 처리 규칙을 문서에 고정했습니다.
  - `LOW_TRAFFIC`(최근 3일 평균 전체 모수/실행률)와 `CHAIN_COVERAGE_GAP`(`DOC`/`REVIEW` `chainRuns`/`chainShare`) 근거를 분리해 `HOLD` 원인 우선순위를 정렬했습니다.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화해 `executionMix` 필수 출력과 HOLD 시 실행률+호출 믹스 근거 명시 규칙을 반영했습니다.
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-021-result.md`
  - `coordination/RELAYS/H-021-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md` H-021 섹션의 호출 믹스 표/산식(`directRuns`, `chainRuns`, `totalActualRuns`, `chainShare`, `achievementRate`, `overallExecutionRate`)이 handoff 요구와 일치하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `executionMix` 출력 계약과 `CHAIN_COVERAGE_GAP` 기준(`chainRuns`/`chainShare`)이 동일하게 반영됐는지
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 실측에서 `INSUFFICIENT_SAMPLE` 비율(`1.00`) 및 샘플 충분 일수(`0일`) 미충족이 지속되어 `recalibrationReadiness=HOLD`가 유지됩니다.
- 최근 7일 `DOC`/`REVIEW`의 체인 유입(`chainRuns`)이 0건으로 `CHAIN_COVERAGE_GAP` 해소 신호가 없습니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-021-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
