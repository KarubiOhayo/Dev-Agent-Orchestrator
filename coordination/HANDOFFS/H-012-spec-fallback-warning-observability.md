# H-012 spec fallback warning 관측성 정합화

Owner: WT-12 (`codex/h012-spec-fallback-warning`)
Priority: Highest

## 목표
- spec 에이전트의 출력 파싱 fallback 경로를 run-state 이벤트로 관측 가능하게 만들어 code/doc/review와 운영 지표를 정합화한다.
- spec 출력 파서의 소스 분류(직접 JSON / JSON 코드블록 / 구조 fallback)를 테스트로 고정해 회귀를 방지한다.
- 운영 문서에 spec fallback 이벤트 계약을 반영해 해석 불일치를 줄인다.

## 작업 범위
- 코드 보강
  - `src/main/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParser.java`
  - `src/main/java/me/karubidev/devagent/agents/spec/SpecAgentService.java`
- 테스트 보강
  - `src/test/java/me/karubidev/devagent/agents/spec/SpecAgentServiceTest.java`
  - 필요 시 `src/test/java/me/karubidev/devagent/agents/spec/SpecOutputSchemaParserTest.java` 신규 추가
- 문서 동기화
  - `docs/code-agent-api.md`
  - 필요 시 `docs/PROJECT_OVERVIEW.md`의 운영 리스크/관측 포인트 최소 보강

## 구현 지침
- spec 파서는 parse source를 명시적으로 반환할 수 있어야 한다.
  - 예: `DIRECT_JSON`, `JSON_CODE_BLOCK`, `FALLBACK_SCHEMA`
- spec 서비스는 direct JSON이 아닌 경로 사용 시 run-state 이벤트를 기록한다.
  - 이벤트명: `SPEC_OUTPUT_FALLBACK_WARNING`
  - 메시지 형식: `source=<PARSE_SOURCE>`
- 기존 spec 응답 스키마(`title`, `overview`, `constraints`, `acceptanceCriteria`, `tasks`, `notes`)와 성공 응답 계약은 변경하지 않는다.
- 기존 체인 계약(`chainToCode`, `chainFailures[]`)에는 영향이 없어야 한다.

## 수용 기준
1. spec 출력이 direct JSON이 아닌 경우 `SPEC_OUTPUT_FALLBACK_WARNING` 이벤트가 기록된다.
2. spec 출력이 direct JSON인 경우에는 fallback warning 이벤트가 기록되지 않는다.
3. parse source 분기와 이벤트 기록 분기가 테스트로 고정된다.
4. `docs/code-agent-api.md`에 spec fallback warning 관측 포인트가 반영된다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 신규 Agent/엔드포인트 추가
- 모델 라우팅 정책 재설계
- API 오류 응답 계약(H-010/H-010.1) 재수정
- spec/doc/review 프롬프트 자산 재작성(H-011 범위)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이벤트 추가는 관측성 목적이며 기존 성공/실패 HTTP 계약을 변경하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-012-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-012-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - parse source 분기 설계와 이벤트 기록 조건 요약
  - fallback/non-fallback 테스트 케이스와 결과
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
