# [H-040] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md`
- 참고 result: `coordination/REPORTS/H-038-result.md`
- 참고 review: `coordination/REPORTS/H-038-review.md`
- 참고 relay: `coordination/RELAYS/H-038-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md`
6. `coordination/REPORTS/H-038-result.md`, `coordination/REPORTS/H-038-review.md`, `coordination/RELAYS/H-038-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `src/main/java/me/karubidev/devagent/llm/providers/OpenAiProviderClient.java`
  - `src/main/java/me/karubidev/devagent/llm/providers/AnthropicProviderClient.java`
  - `src/main/java/me/karubidev/devagent/llm/providers/GoogleGeminiProviderClient.java`
  - `src/main/java/me/karubidev/devagent/llm/LlmJsonExtractor.java`
  - `src/main/java/me/karubidev/devagent/llm/LlmOrchestratorService.java`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java`
  - `src/main/java/me/karubidev/devagent/orchestration/routing/RouteRequest.java`
  - `src/main/java/me/karubidev/devagent/orchestration/routing/ModelRouter.java`
  - `src/main/resources/application.yml` (Main 사전 승인 범위 내)
  - `docs/model-routing-policy.md`, `docs/code-agent-api.md`
  - 관련 테스트 파일(`src/test/java/**`)
- 수정 금지:
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 승인 상태:
  - `src/main/resources/application.yml`: 본 라운드 범위 내 사전 승인됨
  - 그 외 공통 승인 대상 파일 변경 필요 시 즉시 중단 후 Main 승인 요청

## 완료 산출물
- `coordination/REPORTS/H-040-result.md`
- `coordination/RELAYS/H-040-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- FocusBar 재현 케이스는 "모델 후보 전체 실패"와 "호출 성공이지만 parsedFiles=0"이 분리된 결함입니다. A/B 복구 후 D를 반드시 별도로 검증하세요.
- 벤더 모델명/파라미터는 반드시 공식 문서(핸드오프 링크) 기준으로 확인하고, 선택 이유를 결과 보고서에 남겨야 합니다.
- `apply=true`에서 `writtenFiles=0`이 성공처럼 보이는 경로가 다시 나오지 않도록 경고/실패 신호를 명확히 남기는 방향으로 정리하세요.
- H-039 fallback-warning 트랙은 본 라운드에서 선행 우선순위가 아닙니다(긴급 복구 후 재개).
