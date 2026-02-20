# [H-040] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md`
- main relay: `coordination/RELAYS/H-040-main-to-executor.md`
- result: `coordination/REPORTS/H-040-result.md`

## 구현 요약
- 핵심 변경:
  - OpenAI codex 호출에서 `temperature` 제거(비-codex 유지)
  - Anthropic 모델 ID를 `claude-sonnet-4-5-20250929`로 교체(설정/테스트/문서 동기화)
  - Gemini non-text 응답 진단 메시지(`finishReason`, `partTypes`) 보강
  - Code parser 강화(wrapper/generic fence/embedded/truncated JSON 복구)
  - `CodeGenerateRequest.strictJsonRequired` 기본값 false 정렬
  - `apply=true + parsedFiles=0` 실패 처리 + `CODE_OUTPUT_EMPTY_WARNING` 이벤트 추가
- 변경 파일:
  - `src/main/java/me/karubidev/devagent/llm/providers/OpenAiProviderClient.java`
  - `src/main/java/me/karubidev/devagent/llm/LlmJsonExtractor.java`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java`
  - `src/main/resources/application.yml`
  - `docs/model-routing-policy.md`
  - `docs/code-agent-api.md`
  - 관련 테스트 파일 다수(`src/test/java/**`)

## 테스트 게이트
- 실행 명령:
  - `./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - handoff 재현 `apply=true` 커맨드는 sandbox 외부 경로(`/Users/apple/dev_source/focusbar`) 쓰기 제한으로 `writtenFiles=0` (`Operation not permitted`)

## 리뷰 집중 포인트
1. codex 모델 분기(`OpenAiProviderClient`)에서 `temperature` 미포함 처리의 회귀 여부
2. `CodeOutputParser`의 `LOOSE_JSON_FALLBACK` 선형 스캔이 과매칭 없이 안정적인지
3. `apply=true + parsedFiles=0` 실패 전환과 `CODE_OUTPUT_EMPTY_WARNING` 이벤트가 운영/CLI 관점에서 명확한지

## 알려진 리스크 / 오픈 이슈
- focusbar apply 실파일 반영(`writtenFiles > 0`)은 현재 sandbox 제약으로 미검증이며, 쓰기 권한이 있는 환경에서 재확인 필요

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-040-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
