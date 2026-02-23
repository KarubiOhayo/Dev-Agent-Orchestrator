# [H-041] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-041-code-output-parser-safety-and-apply-verification.md`
- main relay: `coordination/RELAYS/H-041-main-to-executor.md`
- result: `coordination/REPORTS/H-041-result.md`

## 구현 요약
- 핵심 변경:
  - `CodeOutputParser`의 `LOOSE_JSON_FALLBACK`를 전역 선형 스캔에서 `files[]` 컨텍스트 기반 복구로 재설계
  - `path/content`를 동일 객체(depth=1) 경계 내 쌍으로만 승격하도록 제한
  - truncated JSON 복구는 유지하되 `files[]` 외부 메타 토큰/분리 객체/escaped 토큰 오탐 경로 차단
  - parser safety 가드를 `docs/code-agent-api.md`에 반영
- 변경 파일:
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java`
  - `src/test/java/me/karubidev/devagent/agents/code/apply/CodeOutputParserTest.java`
  - `docs/code-agent-api.md`
  - `coordination/REPORTS/H-041-result.md`
  - `coordination/RELAYS/H-041-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --tests me.karubidev.devagent.agents.code.apply.CodeOutputParserTest --no-daemon`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 모두 `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - FocusBar 절대경로(`/Users/apple/dev_source/focusbar`)는 sandbox 쓰기 제한이 재현되어 `writtenFiles=0` + `Operation not permitted`
  - writable 경로(`tmp/h041-apply-target-2`)에서 `--apply true` 실증은 `writtenFiles=2`로 완료

## 리뷰 집중 포인트
1. `CodeOutputParser`가 `files[]` 배열 외부의 `path/content`를 더 이상 파일로 승격하지 않는지(특히 H-040 P2 시나리오)
2. 객체 경계 제약(분리 객체 `path`/`content` 미매칭)과 escaped 토큰 무시 동작이 테스트와 코드 모두 정합한지
3. apply 실증 결론(writable 성공 + FocusBar 권한 오류 증빙)이 handoff 수용기준 #3/#4 해석에 충분한지

## 알려진 리스크 / 오픈 이슈
- parser safety는 강화됐지만 모델 출력 변형 패턴이 계속 발생할 수 있어 추가 샘플 기반 회귀 케이스 축적이 필요
- H-039 fallback-warning 라운드는 H-041 리뷰/메인 판단 이후 재개 예정

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-041-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
