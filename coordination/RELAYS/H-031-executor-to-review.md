# [H-031] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-031-fallback-warning-keep-frozen-followup-readiness-check.md`
- main relay: `coordination/RELAYS/H-031-main-to-executor.md`
- result: `coordination/REPORTS/H-031-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-031 후속 점검 섹션 추가(최신 14일/7일 실측 + 단일 판정 `KEEP_FROZEN` + `recoveryActionTracking[]`/`completionRate`/`blockedActionCount` 반영)
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 라운드 기준 문구를 H-031 후속 점검/재검증 기준으로 동기화
  - `docs/PROJECT_OVERVIEW.md`에 H-031 완료 항목 최소 동기화
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/REPORTS/H-031-result.md`
  - `coordination/RELAYS/H-031-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md` H-031 섹션의 필수 필드(`signal`, `priority`, `status`, `owner`, `evidenceRef`, `nextAction`, `updatedAt`)와 단일 판정(`resumeDecision`)이 handoff 요구사항과 일치하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 기존 출력 계약 유지 상태로 H-031 기준 문구를 충돌 없이 반영했는지
3. H-031 근거 수치(`executionGapDelta=+3`, `chainShareGapDelta=0.00%p`, `weeklyComplianceRate=0.00`, `recoveryActionCompletionRate=0.00`, `blockedActionCount=2`)가 result/doc 간 일관적인지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0`으로 재개(`RESUME_H024`) 근거는 여전히 부족합니다.
- 최근 7일 `DOC/REVIEW` 체인 실행 0건으로 `CHAIN_COVERAGE_GAP` 완화 추세 검증이 불가능합니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-031-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
