# H-035.1 결과 보고서 (traffic seeding fail-fast 종료코드 신뢰성 보강)

## 상태
- 현재 상태: **완료 (P1 fail-fast 종료코드 결함 보완)**
- 실행일(KST): `2026-02-20`
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-035-1-fallback-warning-seeding-fail-fast-exitcode-hardening.md`
  - main relay: `coordination/RELAYS/H-035-1-main-to-executor.md`
  - 참고 result/review/relay: `coordination/REPORTS/H-035-result.md`, `coordination/REPORTS/H-035-review.md`, `coordination/RELAYS/H-035-review-to-main.md`

## 변경 파일 목록
- `scripts/seed-fallback-warning-workload.sh`
- `coordination/REPORTS/H-035-1-result.md`
- `coordination/RELAYS/H-035-1-executor-to-review.md`

## 구현 요약
- 수정 위치: `scripts/seed-fallback-warning-workload.sh`의 fail-fast 분기(`run_single` 내부)
- 보강 내용:
  - 실패 조건(`exit_code != 0` 또는 `runId` 누락)은 기존과 동일 유지
  - fail-fast 활성 시 실제 종료코드를 별도 계산하도록 보강
  - `runId` 누락 + `exit_code=0`이면 종료코드를 `1`로 치환해 non-zero 강제
  - `exit_code != 0` 실패는 기존 종료코드를 그대로 유지

## fail-fast 실패 판정/종료코드 보강 전후 비교
| 시나리오 | 보강 전 | 보강 후 |
|---|---|---|
| `runId` 누락 + `exit_code=0` + fail-fast=true | `exit 0` 가능 | **`exit 1` 강제 (non-zero)** |
| 명령 실패(`exit_code=7`) + fail-fast=true | `exit 7` | **`exit 7` 유지** |
| non fail-fast(`SEED_FAIL_FAST=false`) | 실패 카운트 후 계속 실행 | **동일(회귀 없음)** |

## `runId` 누락 실패 시나리오 재현 명령 + 종료코드 증빙
- 재현 방식: 임시 하네스(`/tmp/h0351-evidence-iuhY`)에서 `./devagent`를 fake stub으로 대체해 `runId` 없는 JSON + `exit 0` 응답을 강제
- 실행 명령:
```bash
(
  cd /tmp/h0351-evidence-iuhY
  FAKE_DEVAGENT_MODE=missing-runid \
  SEED_DIRECT_RUNS=1 SEED_CHAIN_RUNS=0 SEED_FAIL_FAST=true \
  SEED_DB_PATH="/Users/apple/dev_source/Dev-Agent Orchestrator/storage/devagent.db" \
  SEED_TIMESTAMP=case-a ./scripts/seed-fallback-warning-workload.sh
)
```
- 관측 결과:
  - 종료코드: `CASE_A_EXIT=1`
  - 로그 근거: `중단: fail-fast 활성화 상태에서 실패 발생(kind=DIRECT index=1 exit=1)`

## 명령 실패(`exit_code != 0`) 종료코드 유지 증빙
- 실행 명령:
```bash
(
  cd /tmp/h0351-evidence-iuhY
  FAKE_DEVAGENT_MODE=nonzero FAKE_DEVAGENT_EXIT_CODE=7 \
  SEED_DIRECT_RUNS=1 SEED_CHAIN_RUNS=0 SEED_FAIL_FAST=true \
  SEED_DB_PATH="/Users/apple/dev_source/Dev-Agent Orchestrator/storage/devagent.db" \
  SEED_TIMESTAMP=case-b ./scripts/seed-fallback-warning-workload.sh
)
```
- 관측 결과:
  - 종료코드: `CASE_B_EXIT=7`
  - 로그 근거: `중단: fail-fast 활성화 상태에서 실패 발생(kind=DIRECT index=1 exit=7)`

## 비 fail-fast 경로 동작 유지 증빙(실패 카운트/계속 실행)
- 실행 명령:
```bash
(
  cd /tmp/h0351-evidence-iuhY
  FAKE_DEVAGENT_MODE=missing-runid \
  SEED_DIRECT_RUNS=2 SEED_CHAIN_RUNS=0 SEED_FAIL_FAST=false \
  SEED_DB_PATH="/Users/apple/dev_source/Dev-Agent Orchestrator/storage/devagent.db" \
  SEED_TIMESTAMP=case-c ./scripts/seed-fallback-warning-workload.sh
)
```
- 관측 결과:
  - `DIRECT#1`, `DIRECT#2` 모두 기록되어 실패 이후에도 후속 실행 지속
  - 요약 로그: `요약: total=2, failures=2`
  - 종료코드: `CASE_C_EXIT=1` (fail-fast 미사용이므로 즉시 중단 없이 최종 실패 반환)

## 테스트 명령/결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 스크립트 동작은 Bash 레벨 검증 중심이며 전용 자동화 테스트(예: shell test framework)는 아직 없다.
- 재현 증빙은 stub 기반 시뮬레이션이므로, 실제 `devagent` 비정상 응답 케이스는 운영 점검에서 추가 확인이 필요하다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `scripts/seed-fallback-warning-workload.sh`에서 `runId` 누락 + `exit_code=0` 시 fail-fast 종료코드를 `1`로 강제했는지
2. fail-fast에서 `exit_code != 0`는 기존 코드를 유지하는지(회귀 여부)
3. non fail-fast에서 실패 카운트 증가 + 후속 실행 지속 동작이 유지되는지
