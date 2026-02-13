# [H-009] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-009-chain-failure-api-contract.md`
- result: `coordination/REPORTS/H-009-result.md`

## 구현 요약
- `CodeGenerateRequest`에 `chainFailurePolicy` 추가 (`FAIL_FAST` 기본, `PARTIAL_SUCCESS` 선택).
- `CodeGenerateResponse`에 `chainFailures[]` 추가 (`agent`, `failedStage`, `errorMessage`).
- `CodeAgentService`에서 정책 분기 구현:
  - `FAIL_FAST`: 체인 예외 즉시 전파(기존 동작 유지)
  - `PARTIAL_SUCCESS`: 체인 예외를 `chainFailures`에 누적하고 Code 요청은 성공 반환
- `docs/code-agent-api.md`에 정책 및 응답 계약 반영.
- `CodeAgentServiceTest`에 정책 회귀 테스트 추가:
  - 기본 fail-fast 유지 검증
  - partial success 시 체인 실패 구조화 응답 검증
  - partial success + 체인 성공 공존 검증(`chainFailures` empty)

## 변경 파일
- `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateResponse.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
- `docs/code-agent-api.md`
- `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`

## 테스트 게이트
- 정책/회귀 중심:
  - `./gradlew test --no-daemon --tests 'me.karubidev.devagent.agents.code.CodeAgentServiceTest' --tests 'me.karubidev.devagent.agents.doc.CodeDocChainServiceTest' --tests 'me.karubidev.devagent.agents.review.CodeReviewChainServiceTest'`
  - 결과: **BUILD SUCCESSFUL**
- 전체 게이트:
  - `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 리뷰 집중 포인트
1. 기본 미지정 요청이 완전히 fail-fast 하위 호환인지 (`chainFailurePolicy` null/미지정 포함)
2. `PARTIAL_SUCCESS`에서 실패 체인 누적 시 응답 계약(`chainFailures`, 기존 chained 결과 필드) 충돌이 없는지
3. run-state 이벤트(`CHAIN_*_FAILED`) 관측성 계약이 기존과 동일하게 유지되는지
4. `CodeGenerateResponse` 보조 생성자 추가가 기존 호출부와 직렬화 계약에 부작용이 없는지

## 남은 리스크 / 오픈 이슈
- `PARTIAL_SUCCESS` 사용 시 클라이언트가 `chainFailures`를 누락 확인하면 체인 실패를 간과할 수 있음
- 실패 단계는 현재 `CHAIN_DOC`/`CHAIN_REVIEW` 수준으로만 제공됨

## 승인 필요 항목
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 없음
