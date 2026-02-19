# H-027 CLI `PARTIAL_SUCCESS` 소비 가드레일 보강
Owner: WT-27 (`codex/h027-cli-partial-success-consumption-guardrail`)
Priority: High

## 목표
- `PARTIAL_SUCCESS`에서 `chainFailures[]`를 확인하지 않아 체인 실패를 놓치는 리스크를 CLI 레벨에서 줄인다.
- 기존 기본 동작(옵션 미사용 시 성공 응답/출력 계약)을 유지하면서 자동화에서 선택적으로 실패 신호를 강제할 수 있게 한다.
- 운영 문서에 소비 체크리스트를 반영해 API/CLI 사용자 가이드를 정합화한다.

## 작업 범위
### A) CLI 옵션/종료코드 가드레일
- `generate`, `spec` 명령에 `--fail-on-chain-failures=<true|false>` 옵션을 추가한다(기본값: `false`).
- `--fail-on-chain-failures=true`이고 실제 `chainFailures[]` 개수가 1 이상이면:
  - 기존 성공 출력(human/json)은 유지한다.
  - 프로세스 종료코드를 비-0으로 설정한다(권장: `3`, 기존 `unknown option=2`와 구분).
- `--fail-on-chain-failures=false`(기본값)에서는 기존 종료코드 동작을 유지한다.

### B) 출력 가시성(호환 유지)
- human 출력에서 `chainFailures > 0`일 때 체인 실패 존재를 한 줄 경고로 명확히 노출한다.
- json 출력은 기존 필드 호환(`data.summary`, `data.chainFailures[]`)을 유지한다.
- 필요 시 json `data`에 소비 편의용 boolean 보조 필드(예: `hasChainFailures`)를 추가하되, 기존 소비 경로를 깨지 않도록 한다.

### C) 테스트 보강
- `DevAgentCliRunnerTest` 중심으로 아래 케이스를 추가/보강한다.
  1. `generate` + `PARTIAL_SUCCESS` + `chainFailures>0` + `--fail-on-chain-failures=true` -> 종료코드 `3`
  2. `spec` + `PARTIAL_SUCCESS` + `chainFailures>0` + `--fail-on-chain-failures=true` -> 종료코드 `3`
  3. 옵션 미사용(default) 또는 `false` -> 기존 성공 종료코드/출력 유지
  4. `chainFailures=0` -> `true`여도 종료코드 `0`
- 기존 옵션 파싱/unknown option/JSON envelope 회귀를 깨지 않는지 확인한다.

### D) 문서 정합화
- `docs/cli-quickstart.md`
  - `--fail-on-chain-failures` 옵션 설명 및 사용 예시(자동화/CI 관점) 추가
  - `PARTIAL_SUCCESS` 소비 체크리스트(기본 모드 vs 가드레일 모드) 반영
- `docs/code-agent-api.md`
  - `PARTIAL_SUCCESS` 소비 규약에 CLI 가드레일 사용 권고를 추가

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-026-result.md`
  - `coordination/REPORTS/H-026-review.md`
  - `coordination/RELAYS/H-026-review-to-main.md`
- 유지 원칙:
  - API 서버 응답 계약(`chainFailures[]`, `PARTIAL_SUCCESS` 의미)은 변경하지 않는다.
  - CLI 출력 계약은 하위호환을 유지하고, 가드레일은 명시 옵션(opt-in)으로만 동작시킨다.

## 수용 기준
1. `generate/spec`에서 `--fail-on-chain-failures` 옵션이 정상 파싱되고 도움말/문서에 반영된다.
2. `PARTIAL_SUCCESS` + 체인 실패 존재 시 옵션 `true`에서 종료코드 `3`이 반환된다.
3. 옵션 미사용(default) 또는 `false`에서는 기존 성공 동작/출력 계약이 유지된다.
4. human/json 출력에서 체인 실패 존재를 소비자가 놓치지 않도록 가시성(경고/보조 필드)이 보강된다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- API endpoint 응답 구조 대개편
- run-state 스키마 변경
- fallback-warning(H-024) 동결 트랙 재개/정책 변경

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경 필요 시 즉시 중단 후 Main 승인 요청만 남길 것.

## 보고서
- 완료 시 `coordination/REPORTS/H-027-result.md` 생성
- `coordination/RELAYS/H-027-executor-to-review.md` 생성(템플릿 기반)
- 보고서 필수 포함:
  - 변경 파일 목록
  - 신규 옵션 파싱/종료코드 계약 검증 결과
  - `generate/spec` 경로별 `chainFailures` 가드레일 동작 검증 요약
  - 기본 동작(옵션 미사용) 하위호환 회귀 검증 결과
  - 문서 업데이트 요약
  - 테스트 명령 및 결과
  - 남은 리스크
  - 공통 파일 변경 승인 여부
