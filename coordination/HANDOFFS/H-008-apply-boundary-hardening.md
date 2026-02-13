# H-008 파일 적용 경계 입력 방어 강화

Owner: WT-8 (`codex/apply-boundary-hardening`)
Priority: Highest

## 목표
- 파일 적용 경로(path) 입력 경계를 강화해 의도치 않은 파일 쓰기/우회 가능성을 줄인다.
- API/서비스 레이어에서 경계 위반 입력에 대한 실패 계약(오류 메시지 + run-state 이벤트)을 일관화한다.
- H-007 리뷰 P3로 남은 라우터 실패 경로 테스트(`request == null`, `agentType == null`)를 함께 보강한다.

## 작업 범위
- 파일 적용 경계 검증 강화
  - `src/main/java/me/karubidev/devagent/agents/code/apply/FileApplyService.java`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
  - 필요 시 `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
- 검증 포인트
  - 빈 경로/절대경로/`..`/정규화 우회 케이스 방어
  - `targetProjectRoot`가 비정상 입력일 때 예외 계약 명확화
  - 경계 위반 시 `REJECTED`/`ERROR` 상태와 메시지 일관성 점검
- 테스트 보강
  - `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java`
  - `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`
  - `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java` (H-007 P3 보강)

## 수용 기준
1. 파일 적용 경계 위반 입력(절대경로/경로탈출/빈 경로)에 대해 쓰기 시도가 차단된다.
2. 경계 위반 시 상태(`REJECTED`/`ERROR`) 및 메시지가 테스트로 고정된다.
3. `targetProjectRoot`/`specInputPath` 관련 실패 케이스의 run-state 이벤트 계약이 유지된다.
4. `ModelRouter.resolve`의 실패 경로(`request == null`, `agentType == null`)가 회귀 테스트로 추가된다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 체인 실패 전파 정책(API 계약) 최종 확정(H-009)
- strict-json 정책 추가 변경(H-007 범위 외)
- spec/doc/review 프롬프트 자산 확장

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 오류 메시지/상태 코드 계약을 변경하는 경우 기존 테스트와 API 문서 영향도를 결과 보고서에 명시한다.

## 보고서
- 완료 시 `coordination/REPORTS/H-008-result.md` 생성
- 리뷰 스레드 결과는 `coordination/REPORTS/H-008-review.md` 작성
- 필수 항목:
  - 변경 파일 목록
  - 경계 입력 케이스별 동작 표(정상/실패/경계)
  - 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
