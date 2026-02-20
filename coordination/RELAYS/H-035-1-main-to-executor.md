# [H-035.1] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-035-1-fallback-warning-seeding-fail-fast-exitcode-hardening.md`
- 참고 result: `coordination/REPORTS/H-035-result.md`
- 참고 review: `coordination/REPORTS/H-035-review.md`
- 참고 relay: `coordination/RELAYS/H-035-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-035-1-fallback-warning-seeding-fail-fast-exitcode-hardening.md`
6. `coordination/REPORTS/H-035-result.md`, `coordination/REPORTS/H-035-review.md`, `coordination/RELAYS/H-035-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - `scripts/seed-fallback-warning-workload.sh`
  - `docs/cli-quickstart.md` (필요 시 최소 동기화)
  - `docs/PROJECT_OVERVIEW.md` (필요 시 최소 동기화)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-035-1-result.md`
- `coordination/RELAYS/H-035-1-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-035 리뷰 P1(`scripts/seed-fallback-warning-workload.sh:471`, `scripts/seed-fallback-warning-workload.sh:475`)을 직접 해소할 것: fail-fast 모드에서 `runId` 누락 실패가 `exit 0`으로 남지 않도록 non-zero 종료를 강제.
- 실패 원인이 명령 실패(`exit_code != 0`)일 때는 기존 종료코드를 유지하고, `runId` 누락 + `exit_code=0`일 때는 종료코드 `1` 이상으로 치환해 fail-fast 신뢰성을 보장할 것.
- 기존 runId/체인 매핑 계약(`specRunId -> codeRunId -> docRunId/reviewRunId`)과 before/after 집계 형식은 변경하지 말 것.
