# [H-011] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-011-prompt-assets-spec-doc-review.md`
- main relay: `coordination/RELAYS/H-011-main-to-executor.md`
- result: `coordination/REPORTS/H-011-result.md`

## 구현 요약
- 핵심 변경:
  - spec/doc/review 에이전트 전용 프롬프트 파일 3종 추가
  - 각 프롬프트에 JSON 단일 객체 출력 강제 + 스키마 키 고정 + JSON 외 텍스트 금지 규칙 반영
  - `PromptRegistryTest` 신규 추가로 spec/doc/review `AGENT_BASE` 반영 및 code 계약 회귀 방지 검증
- 변경 파일:
  - `prompts/agents/spec.md`
  - `prompts/agents/doc.md`
  - `prompts/agents/review.md`
  - `src/test/java/me/karubidev/devagent/prompt/PromptRegistryTest.java`

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --no-daemon --tests 'me.karubidev.devagent.prompt.PromptRegistryTest'`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 두 명령 모두 `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `PromptRegistryTest`가 spec/doc/review `AGENT_BASE` 반영을 실제 파일 로딩 기준으로 충분히 고정하는지 (`PromptRegistryTest.java:13`)
2. code 프롬프트 계약 회귀 방지 검증(`"files"`, `` `files[].path` ``)이 최소 안전망으로 적절한지 (`PromptRegistryTest.java:34`)
3. 신규 프롬프트 3종의 스키마 키가 파서 기대 키와 정합하고, JSON 외 텍스트 금지 규칙이 명확한지 (`prompts/agents/spec.md:5`, `prompts/agents/doc.md:5`, `prompts/agents/review.md:5`)

## 알려진 리스크 / 오픈 이슈
- 프롬프트 계약 보강만으로는 모델별 출력 편차를 완전히 제거할 수 없어 fallback warning 이벤트 모니터링이 필요함
- 실제 품질(의미 정확도)은 프롬프트 로딩 테스트 범위 밖이므로 API 통합 시나리오 기반 관찰이 필요함

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-011-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
