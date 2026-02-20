# H-040 code-generate provider compatibility + files JSON hardening

Owner: WT-40 (`codex/h040-code-generate-provider-compat-and-files-json-hardening`)
Priority: Highest

## 목표
- FocusBar 재현 케이스에서 `devagent generate`가 모델 후보 실패 없이 최소 1개 후보 성공으로 진행되도록 복구한다.
- `strict-json-required=true`/`false` 모두에서 Code 생성 결과의 `files[]` 파싱 성공률을 높이고, `apply=true`에서 무파일 성공(`writtenFiles=0`)이 조용히 통과되는 경로를 제거한다.
- 벤더 공식 문서 기준으로 모델명/파라미터 호환성을 동기화한다.

## 작업 범위
- LLM provider 요청/응답 처리
  - `src/main/java/me/karubidev/devagent/llm/providers/OpenAiProviderClient.java`
  - `src/main/java/me/karubidev/devagent/llm/providers/AnthropicProviderClient.java`
  - `src/main/java/me/karubidev/devagent/llm/providers/GoogleGeminiProviderClient.java`
  - `src/main/java/me/karubidev/devagent/llm/LlmJsonExtractor.java`
  - `src/main/java/me/karubidev/devagent/llm/LlmOrchestratorService.java`
- Code 라우팅/요청 기본값/파싱 강건화
  - `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java`
  - `src/main/java/me/karubidev/devagent/orchestration/routing/RouteRequest.java`
  - `src/main/java/me/karubidev/devagent/orchestration/routing/ModelRouter.java`
- 설정/문서/테스트 동기화
  - `src/main/resources/application.yml`
  - `docs/model-routing-policy.md`
  - `docs/code-agent-api.md`
  - 관련 단위/통합 테스트 전반(`src/test/java/**`)

## Main 사전 승인(공통 파일)
- 아래 공통 승인 대상 파일 변경을 **본 라운드 범위로 사전 승인**한다.
  - `src/main/resources/application.yml`
- 승인 범위:
  1. Anthropic fallback 모델명(현재 `claude-sonnet-4.5`)을 공식 유효 모델명으로 교체
  2. strict-json escalation/Code 기본 라우팅 정합화를 위한 최소 설정 변경
- 승인 범위 밖(금지): 빌드 설정(`build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경

## 구현 지침
- A) OpenAI codex `temperature` 비호환 우선 복구(최우선)
  - `OpenAiProviderClient`에서 codex 계열 모델 호출 시 `temperature`를 request body에 포함하지 않는다.
  - codex 판별 규칙은 하드코딩 최소화(예: 모델명 패턴 기반 helper + 테스트 고정).
  - non-codex(OpenAI) 경로는 기존 temperature 동작을 유지한다.

- B) Anthropic fallback 모델명 404 복구
  - `application.yml` 및 관련 테스트/문서의 `claude-sonnet-4.5`를 공식 사용 가능한 식별자로 교체한다.
  - 모델 식별자는 Anthropic 공식 문서(아래 근거) 기준으로 선택하고, 선택 근거를 result에 기록한다.

- C) Google "text output 없음" 경로 관측성/복원력 보강(우선순위 낮음)
  - `LlmJsonExtractor.geminiText`가 text part 부재 시 즉시 일반 예외만 던지지 말고,
    - part 타입(functionCall 등)과 finish reason을 포함한 진단 가능한 오류 메시지를 남긴다.
    - 필요 시 "텍스트 없음"을 provider 실패로 처리하되, fallback 후보로 정상 진행되는지 테스트로 고정한다.

- D) `parsedFiles=0` 개선(핵심)
  - `CodeOutputParser` 허용 입력을 강화한다(직접 JSON + JSON code block 외 실사용 패턴 보강).
  - `prompts/agents/code.md` 계약은 유지하되, Code 경로 프롬프트 합성 시 `{"files":[...]}` 단일 객체 강제를 더 명확히 고정한다.
  - `apply=true` + `parsedFiles=0` 케이스는 최소한 warning 이벤트/요약에 명시하고, 운영 관점에서 "성공처럼 보이는 무반영"이 재발하지 않게 처리한다.

- strict-json 라우팅 정합화 점검
  - 현재 `CodeGenerateRequest.strictJsonRequired` 기본값/CLI 기본 처리 때문에 strict-json escalation이 상시 발동하는지 확인한다.
  - CODE 요청에서 `strict-json-required` 미지정 시 기본 BALANCED primary(`gpt-5.2-codex`)가 유지되도록 정책/DTO/CLI 중 한 곳에서 정합화한다.
  - `strict-json-required=true` 명시 요청의 우선 모델 정책도 Code 생성 목적에 맞게 재검토하고 테스트로 고정한다.

## 벤더 공식 근거(2026-02-20 확인)
- OpenAI:
  - GPT-5.2 파라미터 호환성(temperature는 `reasoning_effort=none`에서만 지원):
    - https://platform.openai.com/docs/guides/reasoning
  - GPT-5.2 Codex 모델 페이지(지원 reasoning effort 표시):
    - https://platform.openai.com/docs/models/gpt-5.2-codex
- Anthropic:
  - 모델 deprecations/active 모델 ID 표:
    - https://docs.anthropic.com/en/docs/resources/model-deprecations
- Google Gemini:
  - Function calling 문서(텍스트 외 part/functionCall 응답 가능):
    - https://ai.google.dev/gemini-api/docs/function-calling
  - Safety settings 문서(텍스트 미생성 시 처리 예시):
    - https://ai.google.dev/gemini-api/docs/safety-settings

## 수용 기준
1. 아래 dry-run 재현 커맨드가 `exitCode=0`으로 성공하고 `parsedFiles > 0`을 만족한다.
   - `./devagent generate --project-id focusbar --target-root "/Users/apple/dev_source/focusbar" --user-request "$(cat /Users/apple/dev_source/focusbar/focusbar_request.txt)" --mode BALANCED --risk-level MEDIUM --strict-json-required false --apply false --overwrite-existing false --json true`
2. 같은 입력으로 `--apply true` 재실행 시 `writtenFiles > 0`이며, `pyproject.toml` 포함 다수 파일 생성이 확인된다.
3. OpenAI codex 호출 payload에 `temperature`가 제거되었음을 테스트로 검증한다(비-codex는 유지).
4. Anthropic fallback 모델명이 공식 유효 식별자로 교체되고, 관련 라우팅/테스트/문서가 일관된다.
5. strict-json 미지정/명시(true) 라우팅 케이스가 의도한 primary/fallback 순서를 유지함을 테스트로 고정한다.
6. `apply=true + parsedFiles=0` 케이스가 운영 관점에서 실패 원인을 즉시 식별 가능하도록 경고/오류 신호를 남긴다.
7. `./gradlew clean test --no-daemon` 통과.

## 비범위
- fallback-warning(H-024/H-039) 운영 트랙의 게이트/임계치 변경
- 자동 커밋/PR/웹훅 자동화

## 제약
- handoff 범위 밖 파일 수정 금지.
- 승인된 공통 파일 외 공통 계약 파일/빌드 설정 변경 필요 시 중단 후 Main 승인 요청만 남긴다.

## 보고서
- 완료 시 `coordination/REPORTS/H-040-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-040-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - A/B/C/D 항목별 수정 요약 + 공식 문서 근거 링크
  - 재현 커맨드 2종(dry-run/apply) 실행 결과(`exitCode`, `parsedFiles`, `writtenFiles`)
  - 라우팅/파라미터 검증 결과(strict-json 미지정/명시)
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안
