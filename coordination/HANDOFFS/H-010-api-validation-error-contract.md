# H-010 API 입력검증/에러계약 표준화

Owner: WT-10 (`codex/api-validation-error-contract`)
Priority: Highest

## 목표
- Agent API 전반의 입력검증 기준을 일관화해 요청 실패 원인을 예측 가능하게 만든다.
- 예외 유형별 오류 응답을 단일 envelope 계약으로 표준화한다.
- `PARTIAL_SUCCESS` 사용 시 클라이언트 필수 확인 규약(`chainFailures[]`)을 문서 계약으로 고정한다.

## 작업 범위
- API 오류 응답 표준화
  - 대상 엔드포인트:
    - `POST /api/routing/resolve`
    - `POST /api/agents/code/generate`
    - `POST /api/agents/spec/generate`
    - `POST /api/agents/doc/generate`
    - `POST /api/agents/review/generate`
  - 구현 후보 파일:
    - `src/main/java/me/karubidev/devagent/api/CodeAgentController.java`
    - `src/main/java/me/karubidev/devagent/api/SpecAgentController.java`
    - `src/main/java/me/karubidev/devagent/api/DocAgentController.java`
    - `src/main/java/me/karubidev/devagent/api/ReviewAgentController.java`
    - `src/main/java/me/karubidev/devagent/api/RoutingController.java`
    - 필요 시 신규 예외 처리 파일(`src/main/java/me/karubidev/devagent/api/error/**`)
- 입력검증 기준 정리
  - 필수 입력 누락/공백/잘못된 enum/JSON 파싱 오류를 400 계열로 일관 처리
  - 서비스 레이어의 `IllegalArgumentException` 메시지를 오류 코드와 함께 매핑 가능하도록 정리
  - `PARTIAL_SUCCESS`는 성공 응답 계약 유지(실패 응답으로 변환 금지)
- 문서/계약 반영
  - `docs/code-agent-api.md`에 오류 envelope 형식, 상태코드 매핑, `chainFailures[]` 필수 확인 규약 명시
  - 필요 시 `docs/cli-quickstart.md`에 `PARTIAL_SUCCESS` 해석 주의사항 반영
- 테스트 보강
  - 컨트롤러 단위 오류 계약 테스트(필수 입력 누락, enum 오류, malformed JSON)
  - 기존 서비스 테스트 회귀 보강(성공 응답 하위 호환 + `PARTIAL_SUCCESS` 계약 유지)

## 수용 기준
1. 필수 입력 누락/공백 요청은 HTTP 400으로 반환되고, 오류 응답 구조가 모든 Agent API에서 동일하다.
2. enum 역직렬화 실패/JSON 파싱 실패도 동일한 오류 envelope 구조로 반환된다.
3. 예상치 못한 서버 오류는 HTTP 500으로 반환되며, 내부 스택트레이스가 응답 본문에 노출되지 않는다.
4. 기존 성공 응답 필드 계약(runId, 결과 payload, `chainFailures[]`)은 깨지지 않는다.
5. `docs/code-agent-api.md`에 오류 응답 스키마 + `PARTIAL_SUCCESS`의 클라이언트 필수 확인 규약이 반영된다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 신규 Agent/엔드포인트 추가
- 프롬프트 자산 확장(H-011)
- 모델 라우팅 정책 재설계(H-007 범위)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 오류 응답 표준화는 하위 호환을 우선하며, 기존 성공 응답을 실패 계약으로 변경하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-010-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-010-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 오류 케이스별 응답 예시(필수 입력 누락/enum 오류/JSON 파싱 오류/서버 오류)
  - 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
