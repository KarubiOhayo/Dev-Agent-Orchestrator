# [H-032] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-032-fallback-warning-keep-frozen-signal-recovery-evidence-acquisition.md`
- 참고 result: `coordination/REPORTS/H-031-result.md`
- 참고 review: `coordination/REPORTS/H-031-review.md`
- 참고 relay: `coordination/RELAYS/H-031-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-032-fallback-warning-keep-frozen-signal-recovery-evidence-acquisition.md`
6. `coordination/REPORTS/H-031-result.md`, `coordination/REPORTS/H-031-review.md`, `coordination/RELAYS/H-031-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `docs/PROJECT_OVERVIEW.md` (필요 시 최소 동기화)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-032-result.md`
- `coordination/RELAYS/H-032-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-031 결론(`resumeDecision=KEEP_FROZEN`, `recoveryActionCompletionRate=0.00`, `blockedActionCount=2`)을 기준선으로 유지하고, 임계치/게이트/제외 규칙(`INSUFFICIENT_SAMPLE`)은 변경하지 말 것.
- `signalRecoveryEvidenceLedger[]`의 필수 필드(`signal`, `requiredEvidence`, `observedEvidence`, `evidenceRefs`, `status`, `gapSummary`, `nextAction`, `updatedAt`) 누락 없이 문서/템플릿/결과 보고에 동일 반영할 것.
- 최종 판정은 `RESUME_H024|KEEP_FROZEN` 단일값으로만 기록하고, 게이트 4종 + 핵심 신호(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`) 실증 근거를 함께 제시할 것.
