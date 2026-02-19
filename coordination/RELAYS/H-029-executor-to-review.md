# [H-029] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-029-fallback-warning-h024-resume-readiness-check.md`
- main relay: `coordination/RELAYS/H-029-main-to-executor.md`
- result: `coordination/REPORTS/H-029-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-029 재개 판단 섹션을 추가해 최신 14일/7일 실측값, `dailyCompliance`, `weeklyComplianceRate`, `executionGapDelta`, `chainShareGapDelta`, 단일 판정(`KEEP_FROZEN`)을 고정했습니다.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `resumeDecision(RESUME_H024|KEEP_FROZEN)`, `unmetReadinessSignals`, `nextCheckTrigger` 필수 출력 계약을 추가했습니다.
  - 기존 fallback warning 게이트/산식/임계치/제외 규칙(`INSUFFICIENT_SAMPLE`)은 변경하지 않았습니다.
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-029-result.md`
  - `coordination/RELAYS/H-029-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. H-029 섹션 수치가 `storage/devagent.db` 기준 최신 14일/7일 집계와 정합한지 (`KEEP_FROZEN` 판정 포함)
2. 자동화 템플릿의 신규 출력 3종(`resumeDecision`, `unmetReadinessSignals`, `nextCheckTrigger`)이 기존 계약(`recalibrationReadiness`, `unmetGates`, `executionRecovery*`)과 충돌 없이 공존하는지
3. 문서/리포트에서 `executionGapDelta=+3`, `chainShareGapDelta=0.00%p`, `weeklyComplianceRate=0.00` 근거가 일관되게 반영되었는지

## 알려진 리스크 / 오픈 이슈
- 최신 14일에서 `INSUFFICIENT_SAMPLE` 비율이 `1.00`으로 유지되어 재개(`RESUME_H024`) 전환 근거가 부족합니다.
- 최근 7일 `DOC/REVIEW` 체인 실행이 0건이라 `CHAIN_COVERAGE_GAP` 해소 증거가 누적되지 않았습니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-029-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
