# H-040 리뷰 보고서

## 대상
- handoff: `coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md`
- result: `coordination/REPORTS/H-040-result.md`
- relay: `coordination/RELAYS/H-040-executor-to-review.md`

## Findings (P1 > P2 > P3)

### [P2] `LOOSE_JSON_FALLBACK`가 구조 밖 `path/content` 키쌍을 파일로 오인할 수 있음
- 증상:
  - `parse()`는 JSON 계열 파싱이 실패하면 markdown fallback 전에 `parseLooseJsonFilePairs()`를 실행합니다.
  - `parseLooseJsonFilePairs()`는 전체 출력 문자열을 선형 스캔해 `"path"` 이후 최초 `"content"`를 결합하며, `files[]` 구조/객체 경계 검증이 없습니다.
- 근거:
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java:65`
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java:280`
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java:304`
  - `coordination/RELAYS/H-040-executor-to-review.md:37`
- 영향:
  - 설명 텍스트/메타 JSON/로그에 우연히 포함된 키쌍이 `GeneratedFile`로 승격될 수 있어, `apply=true` 시 의도치 않은 파일 반영 리스크가 있습니다.
- 권고:
  - loose fallback을 `files` 배열 컨텍스트 내부로 제한하거나, 최소한 `path`/`content`를 동일 객체 범위에서만 매칭하도록 보강이 필요합니다.
  - 오탐 방지 회귀 테스트(비-`files` JSON, 분리된 키쌍, escaped 문자열 포함 케이스)를 추가하세요.

### [P3] 수용기준 #2(`apply=true`에서 `writtenFiles > 0`) 실증이 미완료됨
- 증상:
  - handoff는 동일 입력 `--apply true` 재실행 시 `writtenFiles > 0`을 요구하지만, 결과 보고서는 `writtenFiles=0` 및 전건 `ERROR`를 기록했습니다.
- 근거:
  - `coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md:81`
  - `coordination/REPORTS/H-040-result.md:120`
  - `coordination/REPORTS/H-040-result.md:121`
  - `coordination/REPORTS/H-040-result.md:128`
  - `coordination/REPORTS/H-040-result.md:129`
- 영향:
  - 핵심 목표(실파일 반영 복구)의 운영 증빙이 닫히지 않아 Main 승인 판단은 조건부가 됩니다.
- 권고:
  - writable 환경에서 동일 apply 커맨드를 재실행해 `writtenFiles > 0` 및 생성 파일 목록(`pyproject.toml` 포함) 증빙을 추가하세요.

## 검증 근거 (파일/라인)
1. OpenAI codex/non-codex `temperature` 분기 및 테스트 근거가 일치함
- `src/main/java/me/karubidev/devagent/llm/providers/OpenAiProviderClient.java:64`
- `src/main/java/me/karubidev/devagent/llm/providers/OpenAiProviderClient.java:77`
- `src/test/java/me/karubidev/devagent/llm/providers/OpenAiProviderClientTest.java:47`
- `src/test/java/me/karubidev/devagent/llm/providers/OpenAiProviderClientTest.java:58`

2. strict-json 기본값 false 및 명시 true 전달 경로가 코드/CLI 테스트로 고정됨
- `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java:19`
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java:92`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java:616`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java:644`

3. `apply=true + parsedFiles=0` 경고/실패 전환 구현 및 테스트 근거 확인
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java:427`
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java:433`
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java:436`
- `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java:486`
- `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java:544`

4. Anthropic fallback 모델 ID가 설정/테스트에 일관 반영됨
- `src/main/resources/application.yml:73`
- `src/main/resources/application.yml:108`
- `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java:89`
- `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java:174`

## 심각도 집계
- P1: 0
- P2: 1
- P3: 1

## 수용기준 검증
1. dry-run `exitCode=0` + `parsedFiles > 0`: **충족**
2. apply `writtenFiles > 0` + 다수 파일 생성 확인: **미충족(환경 제약으로 미검증)**
3. codex payload `temperature` 제거(비-codex 유지) 테스트 고정: **충족**
4. Anthropic fallback 모델명 교체 + 라우팅/테스트/문서 정합: **충족**
5. strict-json 미지정/명시(true) 라우팅 테스트 고정: **충족**
6. `apply=true + parsedFiles=0` 경고/오류 신호: **충족**
7. `./gradlew clean test --no-daemon` 통과 보고: **충족(Executor 보고 인용)**

## 승인 게이트 체크
- 테스트 게이트 상태(Executor 보고 인용):
  - `./gradlew clean test --no-daemon` -> `BUILD SUCCESSFUL`
  - 근거: `coordination/REPORTS/H-040-result.md:124`, `coordination/REPORTS/H-040-result.md:125`
- Review-Control 제약상 테스트 재실행은 수행하지 않았고, result/relay/실제 변경 파일 대조로 검증했습니다.
- 공통 파일 변경 승인 절차 준수 여부:
  - `src/main/resources/application.yml` 변경은 handoff 사전 승인 범위 내에서 수행됨
  - 근거: `coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md:31`, `coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md:38`

## 리뷰 결론
- 리스크 수준: `MEDIUM`
- 최종 권고: `Conditional Go`
- 메모: 기능 복구의 핵심 축(A/B/C + strict-json 기본값/경고 신호)은 확보됐지만, `LOOSE_JSON_FALLBACK` 오탐 가능성과 apply 실증 미완료가 남아 후속 보강 라운드가 필요합니다.
