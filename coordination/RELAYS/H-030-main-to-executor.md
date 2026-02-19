# [H-030] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-030-fallback-warning-keep-frozen-recovery-tracking.md`
- 참고 result: `coordination/REPORTS/H-029-result.md`
- 참고 review: `coordination/REPORTS/H-029-review.md`
- 참고 relay: `coordination/RELAYS/H-029-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-030-fallback-warning-keep-frozen-recovery-tracking.md`
6. `coordination/REPORTS/H-029-result.md`, `coordination/REPORTS/H-029-review.md`, `coordination/RELAYS/H-029-review-to-main.md`

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
- `coordination/REPORTS/H-030-result.md`
- `coordination/RELAYS/H-030-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-029 결론(`KEEP_FROZEN`)은 유지하고, 임계치/게이트/제외 규칙(`INSUFFICIENT_SAMPLE`)은 변경하지 말 것.
- `recoveryActionTracking[]`/`recoveryActionCompletionRate`/`blockedActionCount` 출력을 문서와 자동화 템플릿에 동일하게 반영할 것.
- 신호별 액션 상태는 `IN_PROGRESS|BLOCKED|DONE` 중 하나로 고정하고, `evidenceRef`/`nextAction`/`updatedAt`을 누락 없이 기록할 것.
