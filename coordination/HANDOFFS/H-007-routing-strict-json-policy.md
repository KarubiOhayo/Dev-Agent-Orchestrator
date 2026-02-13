# H-007 라우팅 strict-json 정책 재설계

Owner: WT-7 (`codex/routing-strict-json-policy`)
Priority: Highest

## 목표
- `RouteRequest`의 strict-json 기본 동작을 재정의해 의도치 않은 모델 우선순위 역전을 방지한다.
- `ModelRouter` escalation 우선순위를 명시 정책으로 고정한다.
- strict-json 관련 회귀 테스트를 추가해 라우팅 안정성을 확보한다.

## 작업 범위
- 라우팅 입력 계약 정리
  - `src/main/java/me/karubidev/devagent/orchestration/routing/RouteRequest.java`
  - `strictJsonRequired` 기본값/명시값 해석 규칙 정리(기본값, null/미지정, 명시 true/false)
- 라우팅 우선순위 재설계
  - `src/main/java/me/karubidev/devagent/orchestration/routing/ModelRouter.java`
  - escalation 적용 순서와 reason 기록 순서를 정책대로 고정
  - 중복 후보 제거(LinkedHashSet) 시 우선순위가 보존되는지 확인
- strict-json 적용조건/우선순위 반영
  - 명시 요청(`strictJsonRequired=true`)과 기본 동작을 구분
  - 고위험 리뷰/대용량 컨텍스트와 strict-json 동시 적용 시 우선순위 규칙 반영
- 회귀 테스트 보강
  - `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java`
  - 필요 시 라우팅 요청/컨트롤러 단위 테스트 추가

## 수용 기준
1. strict-json 미지정 기본 요청에서 모드 기본 primary가 의도대로 선택된다(불필요 escalation 없음).
2. strict-json 명시 요청에서 strict-json 모델이 정책 우선순위대로 후보열에 반영된다.
3. `review-high-risk`, `large-context`, `strict-json` 동시 조건에서 primary/fallback/reasons 순서가 테스트로 고정된다.
4. 기존 라우팅 동작(예: `COST_SAVER`, canary fallback)의 회귀가 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 체인 실패 전파 정책(API 계약) 최종 확정
- 파일 적용 경계 입력 방어 강화 구현
- API 입력검증/에러계약 전면 표준화

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경이 필요하면 Main-Control 사전 승인 후 진행.
- 정책 가이드는 `coordination/DECISIONS.md`의 D-018(초안)과 정합성을 유지하고, 확정 전에는 draft 상태를 명시한다.

## 보고서
- 완료 시 `coordination/REPORTS/H-007-result.md` 생성
- 리뷰 스레드 결과는 `coordination/REPORTS/H-007-review.md` 작성
- 필수 항목:
  - 변경 파일 목록
  - strict-json/우선순위 정책 diff 요약
  - 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
