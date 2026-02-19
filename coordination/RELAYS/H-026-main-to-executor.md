# [H-026] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-026-spec-cli-chain-e2e-contract-hardening.md`
- 참고 result: `coordination/REPORTS/H-025-result.md`
- 참고 review: `coordination/REPORTS/H-025-review.md`
- 참고 relay: `coordination/RELAYS/H-025-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-026-spec-cli-chain-e2e-contract-hardening.md`
6. `coordination/REPORTS/H-025-result.md`, `coordination/REPORTS/H-025-review.md`, `coordination/RELAYS/H-025-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - `src/test/java/me/karubidev/devagent/agents/spec/**`
  - `src/test/java/me/karubidev/devagent/cli/**`
  - `src/main/java/me/karubidev/devagent/agents/spec/**` (테스트 보강을 위한 최소 범위)
  - `src/main/java/me/karubidev/devagent/cli/**` (테스트 보강을 위한 최소 범위)
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-026-result.md`
- `coordination/RELAYS/H-026-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- `PARTIAL_SUCCESS`에서도 `chainFailures[]`가 API/CLI 출력에서 누락 없이 노출되는지 E2E 성격 테스트로 고정할 것
- CLI human/json 출력 계약(`chainedDoc`, `chainedReview`, `chainFailures`, `data.chainFailures[]`)이 기존 필드 호환성과 함께 유지되는지 확인할 것
- 신규 옵션 미사용(default) 경로 회귀가 깨지지 않는지 확인할 것
