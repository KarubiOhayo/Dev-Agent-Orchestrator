# [H-041] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-041-code-output-parser-safety-and-apply-verification.md`
- 참고 result: `coordination/REPORTS/H-040-result.md`
- 참고 review: `coordination/REPORTS/H-040-review.md`
- 참고 relay: `coordination/RELAYS/H-040-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-23.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `docs/OBSERVABILITY_FALLBACK_WARNING.md`
4. `coordination/TASK_BOARD.md`
5. `coordination/DECISIONS.md`
6. `coordination/HANDOFFS/H-041-code-output-parser-safety-and-apply-verification.md`
7. `coordination/REPORTS/H-040-result.md`, `coordination/REPORTS/H-040-review.md`, `coordination/RELAYS/H-040-review-to-main.md`, `coordination/REPORTS/CURRENT_STATUS_2026-02-23.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java`
  - `src/test/java/me/karubidev/devagent/agents/code/apply/CodeOutputParserTest.java`
  - `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java` (필요 시 최소 범위)
  - `docs/code-agent-api.md` (필요 시 최소 동기화)
  - `docs/model-routing-policy.md` (필요 시 최소 동기화)
- 수정 금지:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-041-result.md`
- `coordination/RELAYS/H-041-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-040 리뷰 P2: `LOOSE_JSON_FALLBACK`의 전역 선형 매칭으로 `files[]` 외부 `path/content` 키쌍 오탐 가능성이 있으므로, 반드시 `files[]` 컨텍스트 + 동일 객체 경계 제한으로 보강할 것.
- H-040 리뷰 P3: apply 실증은 writable 경로 기준으로 `writtenFiles > 0` 증빙을 확보해야 하며, FocusBar 절대경로 재검증이 권한 제약으로 실패하면 오류 증빙과 대체 근거를 함께 남길 것.
- fallback-warning 용어는 output parsing fallback 경고만 의미한다. 모델 라우팅 fallback과 혼합 해석하지 않는다(`docs/OBSERVABILITY_FALLBACK_WARNING.md` 기준).
- H-039 fallback-warning 라운드는 H-041 종료 후 재개 예정이므로, 이번 라운드는 parser safety/apply 운영 증빙 닫기에 집중할 것.
