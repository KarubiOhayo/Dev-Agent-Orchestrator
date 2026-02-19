# H-026 Spec/CLI 원샷 체이닝 E2E 계약 테스트 보강

Owner: WT-26 (`codex/h026-spec-cli-chain-e2e-contract-hardening`)
Priority: High

## 목표
- H-025에서 추가한 Spec -> Code -> (Doc/Review) 원샷 체이닝 계약을 E2E 테스트 관점에서 고정한다.
- `PARTIAL_SUCCESS` 경로에서 `chainFailures[]`가 API/CLI 출력에 안정적으로 노출되는지 회귀 테스트를 보강한다.
- 기본 동작(신규 옵션 미사용 시 기존 동작) 하위호환을 테스트로 재확인한다.

## 작업 범위
- 테스트 보강
  - `src/test/java/me/karubidev/devagent/agents/spec/**`
  - `src/test/java/me/karubidev/devagent/cli/**`
- 필요 시 최소 구현 보강(테스트 통과를 위한 범위 내)
  - `src/main/java/me/karubidev/devagent/agents/spec/**`
  - `src/main/java/me/karubidev/devagent/cli/**`
- 문서 정합화(필요 시 최소 변경)
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-025-result.md`
  - `coordination/REPORTS/H-025-review.md`
  - `coordination/RELAYS/H-025-review-to-main.md`
- 테스트 시나리오(필수):
  1. Spec -> Code 체인에서 `codeChainToDoc=true`, `codeChainToReview=true`, `codeChainFailurePolicy=PARTIAL_SUCCESS` 설정 시
     - Doc/Review 체인 결과 또는 실패 정보가 `chainedCodeResult` 하위 구조에 기대대로 반영되는지 검증
     - 체인 실패 발생 시 `chainFailures[]`가 누락 없이 노출되는지 검증
  2. CLI `spec`/`generate` 경로에서 신규 체인 옵션이 파싱/매핑되고
     - human summary에 `chainedDoc`, `chainedReview`, `chainFailures`가 반영되는지
     - json 출력의 `data.summary` + `data.chainFailures[]`가 계약대로 제공되는지 검증
  3. 신규 옵션 미사용(default) 경로에서 기존 동작이 유지되는지 회귀 검증
- 유지 원칙:
  - `PARTIAL_SUCCESS`는 성공 응답 + `chainFailures[]` 계약을 유지한다.
  - 임계치/알림 룰(fallback-warning) 트랙 문서/자동화는 변경하지 않는다.

## 수용 기준
1. Spec/CLI 원샷 체이닝 경로의 E2E 성격 회귀 테스트가 추가/보강된다.
2. `PARTIAL_SUCCESS` + `chainFailures[]` 계약 검증 테스트가 실패/성공 분기를 포함해 반영된다.
3. CLI human/json 출력 계약(`chainedDoc`, `chainedReview`, `chainFailures`, `data.chainFailures[]`) 검증이 테스트에 반영된다.
4. 신규 옵션 미사용 경로 하위호환 회귀가 확인된다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- fallback-warning(H-024) 동결 트랙 재개/정책 변경
- run-state 스키마 변경
- 자동 커밋/PR/웹훅 자동화(Plan A 위반)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경 필요 시 즉시 중단 후 Main 승인 요청만 남길 것.

## 보고서
- 완료 시 `coordination/REPORTS/H-026-result.md` 생성
- `coordination/RELAYS/H-026-executor-to-review.md` 생성(템플릿 기반)
- 보고서 필수 포함:
  - 변경 파일 목록
  - E2E 테스트 시나리오/검증 포인트 요약
  - `PARTIAL_SUCCESS` + `chainFailures[]` 검증 결과
  - CLI human/json 출력 계약 검증 결과
  - 기본 동작(신규 옵션 미사용) 회귀 검증 결과
  - 테스트 명령 및 결과
  - 남은 리스크
  - 공통 파일 변경 승인 여부
