# H-009 체인 실패 전파 정책(API 계약) 확정

Owner: WT-9 (`codex/chain-failure-contract`)
Priority: Highest

## 목표
- Code -> Doc/Review 체인 실패 시 전파 정책을 API 계약으로 확정한다.
- 기본 동작은 하위 호환을 유지하면서, 부분 성공(partial success) 허용 경로를 명시적으로 제공한다.
- run-state 이벤트/응답 스키마/테스트를 함께 고정해 운영 해석 불일치를 제거한다.

## 작업 범위
- 정책 구현
  - `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateResponse.java`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
  - `src/main/java/me/karubidev/devagent/agents/doc/CodeDocChainService.java`
  - `src/main/java/me/karubidev/devagent/agents/review/CodeReviewChainService.java`
- API/문서 계약 반영
  - `docs/code-agent-api.md`
  - 필요 시 CLI 출력 계약(`--json`) 영향 점검
- 테스트 보강
  - `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`
  - `src/test/java/me/karubidev/devagent/agents/doc/CodeDocChainServiceTest.java`
  - `src/test/java/me/karubidev/devagent/agents/review/CodeReviewChainServiceTest.java`

## 구현 가이드
- 정책 모드 제안
  - 기본값: `FAIL_FAST` (기존 동작 유지)
  - 선택값: `PARTIAL_SUCCESS`
- `PARTIAL_SUCCESS`일 때:
  - Doc/Review 체인 실패가 Code 본요청 전체 실패로 전파되지 않음
  - 대신 응답에 체인 실패 정보(에이전트, 에러 메시지, 실패 단계)를 구조화해 포함
  - run-state에는 기존 `CHAIN_*_FAILED` 이벤트를 유지
- `FAIL_FAST`일 때:
  - 기존처럼 체인 실패를 즉시 상위 예외로 전파

## 수용 기준
1. 기본 요청(정책 미지정)은 기존과 동일하게 fail-fast 동작을 유지한다.
2. `PARTIAL_SUCCESS` 요청에서 Doc/Review 체인 실패 시 Code 응답은 성공으로 반환되며, 체인 실패 정보가 응답에 포함된다.
3. `PARTIAL_SUCCESS` 요청에서 체인 성공 시 기존 응답 필드(`chainedDocResult`, `chainedReviewResult`)와 충돌 없이 공존한다.
4. run-state 이벤트(`CHAIN_*_TRIGGERED/DONE/FAILED`) 계약이 유지된다.
5. `docs/code-agent-api.md`에 정책 필드/응답 계약이 반영된다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 전 에이전트 입력검증/에러계약 표준화(H-010)
- 프롬프트 자산 확장(H-011)
- 모델 라우팅 정책 추가 변경(H-007 범위)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 응답 스키마 변경은 하위 호환 원칙(기존 필드 유지, 신규 필드 추가)을 따른다.

## 보고서
- 완료 시 `coordination/REPORTS/H-009-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-009-executor-to-review.md` 생성
- 필수 항목:
  - 정책 모드별 동작 비교표(`FAIL_FAST` vs `PARTIAL_SUCCESS`)
  - API 요청/응답 예시(성공/체인실패 각각)
  - 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
