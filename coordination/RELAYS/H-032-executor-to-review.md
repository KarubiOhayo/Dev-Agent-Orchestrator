# [H-032] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-032-fallback-warning-keep-frozen-signal-recovery-evidence-acquisition.md`
- main relay: `coordination/RELAYS/H-032-main-to-executor.md`
- result: `coordination/REPORTS/H-032-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-032 섹션을 추가하고 `signalRecoveryEvidenceLedger[]` 필수 필드/대상 신호(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`)를 계약으로 고정함.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `signalRecoveryEvidenceLedger[]` 출력 규칙 및 `KEEP_FROZEN` 시 누락 금지 항목을 동기화함.
  - `docs/PROJECT_OVERVIEW.md` 완료 항목에 H-032 완료를 최소 반영함.
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/REPORTS/H-032-result.md`
  - `coordination/RELAYS/H-032-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md` H-032 섹션의 `signalRecoveryEvidenceLedger[]` 필수 필드(`requiredEvidence`, `observedEvidence`, `evidenceRefs`, `status`, `gapSummary`, `nextAction`, `updatedAt`) 반영 정합성
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에서 `KEEP_FROZEN` 분기 시 `signalRecoveryEvidenceLedger[]` + `recoveryActionTracking[]` + `recoveryActionCompletionRate` + `blockedActionCount` 누락 금지 규칙 반영 여부
3. H-032 결과 수치/판정(`executionGapDelta=+3`, `chainShareGapDelta=0.00%p`, `recoveryActionCompletionRate=0.00`, `blockedActionCount=2`, `resumeDecision=KEEP_FROZEN`)의 문서-리포트 일치 여부

## 알려진 리스크 / 오픈 이슈
- 최신 14일 기준 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0`으로 `KEEP_FROZEN` 장기화 가능성이 여전히 높음
- DOC/REVIEW 체인 실행 실적 부재(`최근 7일 actualChainRuns=0/0`)로 `CHAIN_COVERAGE_GAP` 개선 증거 축적이 지연됨

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-032-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
