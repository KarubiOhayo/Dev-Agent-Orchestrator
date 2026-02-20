# H-035.1 fallback-warning traffic seeding fail-fast 종료코드 신뢰성 보강

Owner: WT-35-1 (`codex/h035-1-fallback-warning-seeding-fail-fast-exitcode-hardening`)
Priority: Highest

## 목표
- H-035 리뷰 P1 이슈를 해소한다.
- fail-fast 모드에서 `runId` 누락 실패가 성공(종료코드 0)으로 관찰될 수 있는 경로를 제거한다.
- traffic seeding 스크립트의 실패 판정/종료코드 계약을 운영 자동화가 신뢰할 수 있도록 고정한다.

## 작업 범위
- 스크립트 보강
  - `scripts/seed-fallback-warning-workload.sh`
- 사용 가이드 최소 동기화(필요 시)
  - `docs/cli-quickstart.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md`
  - `coordination/REPORTS/H-035-result.md`
  - `coordination/REPORTS/H-035-review.md`
  - `coordination/RELAYS/H-035-review-to-main.md`
- P1 근거 위치를 기준으로 fail-fast 분기만 정확히 보강한다.
  - 근거 라인: `scripts/seed-fallback-warning-workload.sh:471`, `scripts/seed-fallback-warning-workload.sh:475`
- 실패 판정 규칙(고정):
  1. `exit_code != 0` 이면 실패
  2. `runId`가 비어 있으면 실패
- fail-fast 종료코드 규칙(필수):
  - 실패 원인이 `exit_code != 0`이면 기존 `exit_code`를 유지한다.
  - 실패 원인이 `runId` 누락이고 `exit_code == 0`이면 종료코드를 `1` 이상(non-zero)으로 강제한다.
  - 어떤 실패 원인이든 fail-fast 활성(`SEED_FAIL_FAST=true`)이면 성공 코드(`0`)로 종료되면 안 된다.
- 비 fail-fast(`SEED_FAIL_FAST=false`) 경로에서는 기존처럼 실패를 카운트하고 다음 실행을 계속한다.
- 기존 runId/체인 매핑 규칙(`specRunId -> CHAIN_CODE_DONE -> codeRunId -> CHAIN_DOC/REVIEW_DONE`)과 summary/records 출력 포맷은 변경하지 않는다.
- 신규 API/스키마/fallback-warning 계약 필드 추가는 금지한다.

## 수용 기준
1. fail-fast 모드에서 `runId` 누락 실패가 발생하면 스크립트가 반드시 non-zero 종료코드를 반환한다.
2. fail-fast 모드에서 명령 자체 실패(`exit_code != 0`)는 기존 종료코드를 유지한다.
3. non fail-fast 모드에서 실패 카운트/후속 실행 지속 동작이 유지된다.
4. 결과 보고에 보완 전/후 동작 차이를 확인할 수 있는 재현 근거(명령, 종료코드, 로그 요약)가 포함된다.
5. H-035에서 고정한 runId/체인 매핑 및 before/after 집계 보고 형식 회귀가 없다.
6. 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경이 없다.
7. `./gradlew clean test --no-daemon` 통과.

## 비범위
- fallback warning 임계치/알림 룰 수치 조정
- run-state 이벤트 스키마 변경
- traffic seeding 워크로드 종류/계약 확장

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 P1 결함 보완 라운드이며, 기능 확장보다 종료코드 신뢰성 보장을 우선한다.

## 보고서
- 완료 시 `coordination/REPORTS/H-035-1-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-035-1-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - fail-fast 실패 판정/종료코드 보강 전후 비교 요약
  - `runId` 누락 실패 시나리오 재현 명령 + 종료코드 증빙
  - 비 fail-fast 경로 동작 유지 증빙(실패 카운트/계속 실행)
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
