# [H-012] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-012-spec-fallback-warning-observability.md`
- main relay: `coordination/RELAYS/H-012-main-to-executor.md`
- result: `coordination/REPORTS/H-012-result.md`

## 구현 요약
- 핵심 변경:
  - spec 출력 파서가 parse source(`DIRECT_JSON`, `JSON_CODE_BLOCK`, `FALLBACK_SCHEMA`)를 반환하도록 확장
  - spec 서비스에서 source가 `DIRECT_JSON`이 아닐 때 `SPEC_OUTPUT_FALLBACK_WARNING` + `source=<PARSE_SOURCE>` 기록
  - parser/service 테스트 보강 + parser 단위 테스트 신규 추가
  - 운영 문서(`docs/code-agent-api.md`)에 spec fallback warning 관측 포인트 반영
- 변경 파일:
  - `src/main/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParser.java`
  - `src/main/java/me/karubidev/devagent/agents/spec/SpecAgentService.java`
  - `src/test/java/me/karubidev/devagent/agents/spec/SpecAgentServiceTest.java`
  - `src/test/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParserTest.java`
  - `docs/code-agent-api.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --no-daemon --tests 'me.karubidev.devagent.agents.spec.SpecOutputSchemaParserTest' --tests 'me.karubidev.devagent.agents.spec.SpecAgentServiceTest'`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 두 명령 모두 `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. warning 이벤트 분기 조건이 handoff 요구사항(직접 JSON 제외 경로 경고)과 일치하는지 (`src/main/java/me/karubidev/devagent/agents/spec/SpecAgentService.java:87`)
2. parse source 분기값과 테스트 케이스가 1:1로 고정되어 회귀 여지가 없는지 (`src/main/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParser.java:25`, `src/test/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParserTest.java:12`)
3. 문서화된 관측 계약(spec/doc/review/code)이 실제 구현 이벤트명/메시지 형식과 정합한지 (`docs/code-agent-api.md:92`)

## 알려진 리스크 / 오픈 이슈
- `JSON_CODE_BLOCK`도 warning으로 기록되므로 모델 출력 스타일 변화에 따라 이벤트 비율이 높아질 수 있음
- `parseOrFallback` 호환 메서드가 남아 있어 단일 진입점 수렴은 추후 정리 대상

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-012-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
