# H-002 결과 보고서

## 작업 요약
CodeAgent 출력 1순위를 JSON `files[]` 스키마로 고정하고, 파서를 JSON 우선으로 변경했습니다. JSON 파싱 실패 시 기존 markdown 파싱으로 fallback 하도록 유지했으며, fallback 발생 시 run-state 경고 이벤트를 기록하도록 구현했습니다.

## 스키마 예시
```json
{
  "files": [
    {
      "path": "src/main/java/com/example/A.java",
      "content": "class A {}"
    }
  ],
  "notes": [
    "가정/주의사항"
  ]
}
```

## 구현 상세
- Code 프롬프트를 JSON 단일 객체 응답 규격으로 변경
- `CodeOutputParser`를 JSON 우선 파싱으로 확장
  - 직접 JSON 객체 파싱
  - ` ```json ` 코드블록 파싱
  - 실패 시 markdown(`### \`path\`` + 코드블록) fallback
- 파서 결과에 소스(`JSON`, `JSON_CODE_BLOCK`, `MARKDOWN_FALLBACK`, `EMPTY`)를 포함
- `CodeAgentService`에서 fallback 감지 시 run-state 이벤트 기록
  - 이벤트: `CODE_OUTPUT_FALLBACK_WARNING`
  - payload: `source=MARKDOWN_FALLBACK`
- `CodeGenerateResponse`에 최상위 `files` 필드 추가

## 실패 케이스
- 케이스: LLM 출력이 JSON 문법 오류 포함
- 동작: JSON 파싱 실패 후 markdown fallback 시도
- 결과: markdown 구조가 유효하면 파일 추출 성공 + `CODE_OUTPUT_FALLBACK_WARNING` 이벤트 기록
- 추가 실패: JSON/markdown 모두 파싱 실패 시 `files=[]` 반환(적용 파일 없음)

## 변경 파일
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/code/CodeGenerateResponse.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/prompts/agents/code.md`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/agents/code/apply/CodeOutputParserTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/agents/spec/SpecCodeChainServiceTest.java`

## 테스트 결과
- 실행 명령: `./gradlew clean test`
- 결과: `BUILD SUCCESSFUL`
- 검증 포인트:
  - parse 성공 시나리오 3개 통과
    - direct JSON
    - JSON code block
    - markdown fallback
  - fallback 발생 시 run-state 경고 이벤트 기록 테스트 통과
  - 응답 `files[]` 포함 검증 테스트 통과

## 후속 제안
1. `files[]` 스키마 검증(예: path/content 타입, 금지 경로 패턴)을 별도 validator로 분리
2. `EMPTY` 결과(파일 0개) 발생 시 warning 이벤트를 추가해 운영 관측성 강화
3. API 문서(OpenAPI/README)에 `CodeGenerateResponse.files` 계약 명시

## 남은 리스크
- 모델이 JSON 외 부가 텍스트를 섞어 응답할 경우 direct JSON 파싱은 실패할 수 있음(현재는 JSON code block/markdown fallback으로 보완)
- JSON 구조는 맞지만 `files[]` 항목 품질(빈 content, 잘못된 path 의미)은 파서 레벨에서 의미 검증하지 않음
- fallback이 잦은 경우 프롬프트 품질/모델 설정 문제일 수 있어 운영 지표 관찰이 필요
