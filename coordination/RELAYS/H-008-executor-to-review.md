# [H-008] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-008-apply-boundary-hardening.md`
- result: `coordination/REPORTS/H-008-result.md`

## 구현 요약
- `FileApplyService` 경계 입력 차단 로직 강화
  - 빈 경로/절대경로/`..` 포함 경로/invalid path -> `REJECTED`
  - I/O 예외 -> `ERROR`
  - `targetRoot`, `files` null 입력 즉시 거부
- `CodeAgentService`
  - `targetProjectRoot` invalid path를 `IllegalArgumentException("targetProjectRoot is invalid: ...")`로 명확화
  - run 시작 이후 `targetProjectRoot`/`specInputPath` 실패 시 `CODE_FAILED` 이벤트 기록 유지
  - `specInputPath` 실패의 `SPEC_INPUT_FAILED` 이벤트 계약 유지
- `ModelRouterTest`
  - `resolve(null)`
  - `resolve(request with null agentType)`
  실패 경로 회귀 테스트 추가

## 변경 파일
- `src/main/java/me/karubidev/devagent/agents/code/apply/FileApplyService.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
- `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java`
- `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`
- `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java`

## 테스트 게이트
- 대상 회귀:
  - `./gradlew test --no-daemon --tests 'me.karubidev.devagent.agents.code.apply.FileApplyServiceTest' --tests 'me.karubidev.devagent.agents.code.CodeAgentServiceTest' --tests 'me.karubidev.devagent.orchestration.routing.ModelRouterTest'`
  - 결과: **BUILD SUCCESSFUL**
- 전체 게이트:
  - `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 리뷰 집중 포인트
1. `FileApplyService` 경계 판정에서 `REJECTED`/`ERROR` 분리 계약이 일관적인지
2. `targetProjectRoot` invalid-path 실패가 run-state(`CODE_FAILED`)에 항상 남는지
3. 기존 `specInputPath` 실패 계약(`SPEC_INPUT_FAILED` + `CODE_FAILED`)이 회귀 없이 유지되는지
4. `ModelRouter.resolve` null 실패 경로 테스트가 H-007 P3 우려를 충분히 커버하는지

## 알려진 리스크 / 오픈 이슈
- Doc/Review/Spec 서비스의 `targetProjectRoot` invalid-path 계약 통일은 본 handoff 비범위

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-008-review.md` 작성
- P1/P2/P3 심각도 기준, 파일/라인 근거 포함
- 최종 결론을 `No findings` 또는 `LOW/MEDIUM/HIGH`로 명시
