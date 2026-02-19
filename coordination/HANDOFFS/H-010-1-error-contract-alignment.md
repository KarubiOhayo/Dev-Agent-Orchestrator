# H-010.1 오류 계약 정합성 보강

Owner: WT-10-1 (`codex/h010-1-error-contract-alignment`)
Priority: Highest

## 목표
- H-010 리뷰에서 보고된 문서-구현 오류 코드 매핑 불일치(P2)를 해소한다.
- 복합 필수조건 오류(`userRequest or specInputPath is required`)의 `details.field` 표현 계약을 구조화해 파싱 일관성을 고정한다.
- H-010 승인 게이트를 `Go`로 전환할 수 있도록 오류 계약 회귀 테스트를 보강한다.

## 작업 범위
- 코드 보강
  - `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java`
- 테스트 보강
  - `src/test/java/me/karubidev/devagent/api/ApiExceptionHandlerTest.java`
- 문서 동기화
  - `docs/code-agent-api.md`
  - 필요 시 `docs/cli-quickstart.md` (오류 코드/계약 설명 보완 범위)

## 구현 지침
- `docs/code-agent-api.md`의 400 오류 코드 매핑 표에 구현에서 반환 가능한 코드를 누락 없이 반영한다.
  - 최소 반영: `INVALID_JSON_REQUEST`
- 복합 필수조건 누락 메시지(`"... or ... is required"`)는 단일 필드 누락과 구분한다.
  - 권장 계약: `code=MISSING_REQUIRED_ANY_OF`
  - `details[]`에는 후보 필드 각각을 별도 항목으로 기록하고 `reason=any_of_required`를 사용한다.
- 단일 필드 필수 누락은 기존 계약(`MISSING_REQUIRED_FIELD`)을 유지해 하위 호환을 보장한다.
- `PARTIAL_SUCCESS` 성공 경로 계약(`chainFailures[]`)은 변경하지 않는다.

## 수용 기준
1. API 문서의 400 오류 코드 매핑이 구현 반환 코드 집합과 일치한다(`INVALID_JSON_REQUEST` 포함).
2. 복합 필수조건 누락 시 `MISSING_REQUIRED_ANY_OF` + 다중 `details[]` 계약이 응답/테스트로 고정된다.
3. 단일 필드 누락은 기존 `MISSING_REQUIRED_FIELD` 계약을 유지한다.
4. 기존 성공 응답 계약(runId, 결과 payload, `chainFailures[]`) 회귀가 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 신규 Agent/엔드포인트 추가
- 모델 라우팅 정책 재설계
- 프롬프트 자산 확장(H-011)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 오류 계약 보강은 하위 호환(성공 응답 계약 유지)을 우선한다.

## 보고서
- 완료 시 `coordination/REPORTS/H-010-1-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-010-1-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 오류 코드 매핑 동기화 근거(문서/구현 대조)
  - 복합 필수조건 오류 응답 예시 + 테스트 결과
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
