# [H-036] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-036-fallback-warning-keep-frozen-seeding-throughput-tracking.md`
- 참고 result: `coordination/REPORTS/H-035-1-result.md`
- 참고 review: `coordination/REPORTS/H-035-1-review.md`
- 참고 relay: `coordination/RELAYS/H-035-1-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-036-fallback-warning-keep-frozen-seeding-throughput-tracking.md`
6. `coordination/REPORTS/H-035-result.md`, `coordination/REPORTS/H-035-1-result.md`, `coordination/REPORTS/H-035-1-review.md`, `coordination/RELAYS/H-035-1-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - `scripts/seed-fallback-warning-workload.sh` (필요 시)
  - `docs/cli-quickstart.md` (필요 시 최소 동기화)
  - `docs/code-agent-api.md` (필요 시 최소 동기화)
  - `docs/PROJECT_OVERVIEW.md` (필요 시 최소 동기화)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-036-result.md`
- `coordination/RELAYS/H-036-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-035.1에서 보강한 fail-fast 종료코드 계약(`runId` 누락 + `exit_code=0` 시 non-zero 강제)이 회귀되지 않도록 유지할 것.
- 반복 시딩 실행 결과를 바탕으로 최신 14일 게이트 4종 + 최근 7일/직전 7일 delta(`executionGapDelta`, `chainShareGapDelta`)를 반드시 재집계할 것.
- `resumeDecision`은 `RESUME_H024|KEEP_FROZEN` 단일값으로 보고하고, `unmetReadinessSignals`를 함께 명시할 것.
