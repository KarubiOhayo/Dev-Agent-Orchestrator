# H-028 CLI 가드레일 실사용성 점검 (`exit code 3` 소비 체크리스트 + 샘플 파이프라인 검증)

Owner: WT-28 (`codex/h028-cli-guardrail-consumer-readiness-check`)
Priority: High

## 목표
- H-027에서 도입한 `--fail-on-chain-failures` 가드레일이 자동화/CI 소비자 관점에서 누락 없이 사용되도록 운영 계약을 고정한다.
- `PARTIAL_SUCCESS` + `chainFailures[]` 상황에서 종료코드(`3`)와 출력(JSON/human)의 해석 규칙을 일관되게 제공한다.
- 기존 하위호환(옵션 미사용/`false` 경로)과 기존 출력 필드 호환을 유지한다.

## 작업 범위
### A) JSON 소비 신호 보강(호환 유지)
- CLI JSON 출력 `data`에 `guardrailTriggered`(boolean) 보조 필드를 추가한다.
- `guardrailTriggered=true` 조건:
  - `--fail-on-chain-failures=true`
  - `chainFailures[]` 개수 > 0
  - 종료코드 `3`
- 그 외 모든 경로는 `guardrailTriggered=false`로 유지한다.
- 기존 필드(`summary`, `fileResults`, `chainFailures[]`, `hasChainFailures`)는 유지한다.

### B) human 출력 가시성 보강
- `chainFailures > 0`일 때 출력되는 경고 문구에 가드레일 활성화 여부(예: enabled/disabled)를 명시한다.
- 기존 성공 출력 포맷은 유지하고, 추가 문구는 1줄 보강 범위로 제한한다.

### C) 자동화/CI 소비 가이드 문서화
- `docs/cli-quickstart.md`
  - 기본 모드 vs 가드레일 모드 비교표(종료코드/필수 점검 항목) 추가
  - 샘플 파이프라인(예: shell/GitHub Actions)에서 `exit code 3` 처리 예시 추가
  - 안티패턴(`continue-on-error`, 종료코드 무시)과 권장 패턴을 명시
- `docs/code-agent-api.md`
  - `PARTIAL_SUCCESS` 소비 규약에 CLI 가드레일 소비 패턴(필드 + 종료코드 동시 확인) 보강

### D) 테스트 보강
- `DevAgentCliRunnerTest`
  1. `fail-on=true + chainFailures>0` -> 종료코드 `3` + JSON `guardrailTriggered=true`
  2. `fail-on=false(default) + chainFailures>0` -> 종료코드 `0` + JSON `guardrailTriggered=false`
  3. `fail-on=true + chainFailures=0` -> 종료코드 `0` + JSON `guardrailTriggered=false`
- `CliResultFormatterTest`
  - human 경고 문구에 가드레일 활성화 상태가 표시되는지 검증
  - 기존 JSON 필드 호환성(`summary`, `chainFailures[]`)이 깨지지 않는지 회귀 검증

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-027-result.md`
  - `coordination/REPORTS/H-027-review.md`
  - `coordination/RELAYS/H-027-review-to-main.md`
- 유지 원칙:
  - API 서버 응답 계약 자체(`PARTIAL_SUCCESS`, `chainFailures[]`)는 변경하지 않는다.
  - CLI 가드레일은 opt-in 정책(`default=false`)을 유지한다.

## 수용 기준
1. JSON 출력에 `data.guardrailTriggered`가 추가되고, 조건별 true/false가 명확히 검증된다.
2. `fail-on=true + chainFailures>0`에서 종료코드 `3`이 유지된다.
3. 옵션 미사용(default)/`false` 경로에서 기존 성공 종료코드/출력 계약이 유지된다.
4. human 출력 경고 문구에서 가드레일 활성화 상태를 확인할 수 있다.
5. 자동화/CI 소비 체크리스트와 샘플 파이프라인 문서가 반영된다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- API endpoint 응답 스키마 대개편
- run-state 스키마 변경
- fallback-warning(H-024) 동결 트랙 재개/정책 변경

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경 필요 시 즉시 중단 후 Main 승인 요청만 남길 것.

## 보고서
- 완료 시 `coordination/REPORTS/H-028-result.md` 생성
- `coordination/RELAYS/H-028-executor-to-review.md` 생성(템플릿 기반)
- 보고서 필수 포함:
  - 변경 파일 목록
  - JSON `guardrailTriggered` 계약 추가/검증 결과
  - 종료코드 매트릭스(`generate/spec`) 검증 결과
  - human 출력 가시성 보강 검증 결과
  - 문서(체크리스트/샘플 파이프라인) 업데이트 요약
  - 테스트 명령 및 결과
  - 남은 리스크
  - 공통 파일 변경 승인 여부
