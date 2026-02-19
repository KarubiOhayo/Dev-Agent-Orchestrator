# H-012 결과 보고서 (spec fallback warning 관측성 정합화)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `src/main/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParser.java`
- `src/main/java/me/karubidev/devagent/agents/spec/SpecAgentService.java`
- `src/test/java/me/karubidev/devagent/agents/spec/SpecAgentServiceTest.java`
- `src/test/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParserTest.java` (신규)
- `docs/code-agent-api.md`

## 구현 요약
- spec 출력 파서에 parse source 메타를 추가해 source를 명시적으로 반환하도록 확장했습니다.
  - source enum: `DIRECT_JSON`, `JSON_CODE_BLOCK`, `FALLBACK_SCHEMA`
  - 근거: `src/main/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParser.java:25`, `src/main/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParser.java:107`
- spec 서비스에서 direct JSON이 아닌 경로(`JSON_CODE_BLOCK`, `FALLBACK_SCHEMA`)일 때만 run-state 경고 이벤트를 기록하도록 반영했습니다.
  - 이벤트: `SPEC_OUTPUT_FALLBACK_WARNING`
  - 메시지: `source=<PARSE_SOURCE>`
  - 근거: `src/main/java/me/karubidev/devagent/agents/spec/SpecAgentService.java:83`
- 운영 문서에 spec fallback warning 관측 포인트를 추가해 code/doc/review와 함께 이벤트 해석 기준을 정합화했습니다.
  - 근거: `docs/code-agent-api.md:92`

## parse source 분기 설계와 이벤트 기록 조건
- `DIRECT_JSON`
  - rawText 전체를 JSON object로 파싱 성공한 경로
  - 이벤트 기록: 없음
- `JSON_CODE_BLOCK`
  - direct JSON 실패 후 ```json 코드블록 파싱 성공 경로
  - 이벤트 기록: `SPEC_OUTPUT_FALLBACK_WARNING` (`source=JSON_CODE_BLOCK`)
- `FALLBACK_SCHEMA`
  - direct/코드블록 파싱 모두 실패 시 fallback schema 생성 경로
  - 이벤트 기록: `SPEC_OUTPUT_FALLBACK_WARNING` (`source=FALLBACK_SCHEMA`)

## fallback/non-fallback 테스트 케이스와 결과
- `SpecOutputSchemaParserTest` 신규 추가
  - direct JSON source 분기 검증: `parseReturnsDirectJsonSourceWhenRawTextIsJsonObject`
  - JSON 코드블록 source 분기 검증: `parseReturnsJsonCodeBlockSourceWhenJsonCodeBlockExists`
  - fallback schema source 분기 검증: `parseReturnsFallbackSchemaSourceWhenJsonParsingFails`
  - 근거: `src/test/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParserTest.java:12`
- `SpecAgentServiceTest` 보강
  - direct JSON에서 warning 이벤트 미기록 검증
  - JSON 코드블록에서 warning 이벤트(`JSON_CODE_BLOCK`) 기록 검증
  - fallback schema에서 warning 이벤트(`FALLBACK_SCHEMA`) 기록 검증
  - 근거: `src/test/java/me/karubidev/devagent/agents/spec/SpecAgentServiceTest.java:33`, `src/test/java/me/karubidev/devagent/agents/spec/SpecAgentServiceTest.java:98`, `src/test/java/me/karubidev/devagent/agents/spec/SpecAgentServiceTest.java:166`

## 수용기준 점검
1. spec 출력이 direct JSON이 아닌 경우 `SPEC_OUTPUT_FALLBACK_WARNING` 기록: **충족**
2. spec 출력이 direct JSON인 경우 warning 이벤트 미기록: **충족**
3. parse source 분기 + 이벤트 기록 분기 테스트 고정: **충족**
4. `docs/code-agent-api.md`에 spec fallback warning 관측 포인트 반영: **충족**
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- spec 관련 회귀 테스트:
  - 명령: `./gradlew test --no-daemon --tests 'me.karubidev.devagent.agents.spec.SpecOutputSchemaParserTest' --tests 'me.karubidev.devagent.agents.spec.SpecAgentServiceTest'`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- spec에서 `JSON_CODE_BLOCK`을 warning 대상으로 분류했기 때문에, 모델 출력 형식이 코드블록 중심으로 변하면 경고 이벤트 비율이 일시적으로 높아질 수 있습니다.
- `parseOrFallback` 호환 메서드는 유지되어 있어 기능 회귀는 없지만, 향후 호출부 정리 시 `parse()` 단일 진입점으로 수렴 여부를 검토할 필요가 있습니다.

## 승인 필요 항목
- 공통 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, 빌드 설정) 변경: **없음**
- 사전 승인 필요 항목: **해당 없음**
