# [H-028] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-028-cli-guardrail-consumer-readiness-check.md`
- 참고 result: `coordination/REPORTS/H-027-result.md`
- 참고 review: `coordination/REPORTS/H-027-review.md`
- 참고 relay: `coordination/RELAYS/H-027-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-028-cli-guardrail-consumer-readiness-check.md`
6. `coordination/REPORTS/H-027-result.md`, `coordination/REPORTS/H-027-review.md`, `coordination/RELAYS/H-027-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - `src/main/java/me/karubidev/devagent/cli/**`
  - `src/test/java/me/karubidev/devagent/cli/**`
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
- `coordination/REPORTS/H-028-result.md`
- `coordination/RELAYS/H-028-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- `guardrailTriggered` 보조 필드는 추가하되, 기존 JSON 필드(`summary`, `chainFailures[]`, `hasChainFailures`) 호환성을 반드시 유지할 것.
- `--fail-on-chain-failures` opt-in 정책(default=`false`)과 종료코드 계약(`3`/`0`) 분기가 `generate/spec` 모두에서 일관되는지 확인할 것.
- 문서의 자동화/CI 체크리스트는 `exit code 3` 처리 누락 안티패턴(`continue-on-error`, 종료코드 무시)을 명확히 경고하도록 작성할 것.
