# H-011 결과 보고서 (spec/doc/review 프롬프트 자산 보강)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `prompts/agents/spec.md`
- `prompts/agents/doc.md`
- `prompts/agents/review.md`
- `src/test/java/me/karubidev/devagent/prompt/PromptRegistryTest.java`

## 구현 요약
- spec/doc/review 에이전트 전용 프롬프트 자산 3종을 신규 추가해 `AGENT_BASE` 계층에서 에이전트별 JSON 출력 계약을 분리했습니다.
- 각 프롬프트에 "JSON 단일 객체만 출력" 규칙과 "JSON 밖 텍스트 금지" 규칙을 명시해 비정형 출력 가능성을 낮췄습니다.
- `PromptRegistryTest`를 신규 추가해 spec/doc/review 에이전트 프롬프트가 `buildPrompt()` 결과에 실제 반영되는지 회귀 고정했습니다.
- 기존 code 프롬프트 계약(`files[]` 중심 출력) 회귀 방지를 위해 code 프롬프트 핵심 토큰(`"files"`, `` `files[].path` ``) 포함 여부를 테스트로 고정했습니다.

## 에이전트별 프롬프트 핵심 규칙 요약
- spec (`prompts/agents/spec.md`)
  - JSON 단일 객체 출력 강제
  - 스키마 키 고정: `title`, `overview`, `constraints`, `acceptanceCriteria`, `tasks[]`
  - `tasks[].files` 상대경로 강제 + JSON 외 텍스트 금지
- doc (`prompts/agents/doc.md`)
  - JSON 단일 객체 출력 강제
  - 스키마 키 고정: `title`, `summary`, `sections[{heading,content}]`, `relatedFiles`, `notes`
  - `relatedFiles` 상대경로 강제 + JSON 외 텍스트 금지
- review (`prompts/agents/review.md`)
  - JSON 단일 객체 출력 강제
  - 스키마 키 고정: `summary`, `overallRisk`, `findings[{title,severity,file,line,description,suggestion}]`, `strengths`, `nextActions`
  - `findings[].file` 상대경로 우선, `findings[].line` 미상 시 `0` 사용, JSON 외 텍스트 금지

## 프롬프트 로딩 회귀 테스트 결과
- 신규 테스트 클래스:
  - `src/test/java/me/karubidev/devagent/prompt/PromptRegistryTest.java`
- 검증 항목:
  - spec/doc/review `AGENT_BASE` 로딩 반영 확인 (`Contract ID` 토큰 기준)
  - code `AGENT_BASE` 계약 토큰(`"files"`, `` `files[].path` ``) 유지 확인
- 결과:
  - 단건 테스트/전체 테스트 모두 통과

## 수용기준 점검
1. `prompts/agents/spec.md`, `prompts/agents/doc.md`, `prompts/agents/review.md` 추가 + JSON 단일 객체/스키마 지시 포함: **충족**
2. `PromptRegistry` 조립 시 spec/doc/review `AGENT_BASE` 반영 테스트 검증: **충족**
3. 기존 code 프롬프트 계약(`files[]` 중심 JSON 객체 출력) 회귀 없음: **충족**
4. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 프롬프트 로딩 회귀 단건:
  - 명령: `./gradlew test --no-daemon --tests 'me.karubidev.devagent.prompt.PromptRegistryTest'`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 프롬프트 계약이 강화되어도 모델/벤더별 생성 편차로 JSON code block 출력이 발생할 수 있어, 운영에서는 fallback warning 이벤트 추이를 지속 관찰할 필요가 있습니다.
- `PromptRegistryTest`는 프롬프트 로딩 반영 여부를 검증하지만 실제 모델 응답 품질(키 누락/의미 품질)은 별도 통합 시나리오로 관찰이 필요합니다.

## 승인 필요 항목
- 공통 파일(`src/main/resources/application.yml`, 공용 모델, 빌드 설정) 변경: **없음**
- 사전 승인 필요 항목: **해당 없음**
