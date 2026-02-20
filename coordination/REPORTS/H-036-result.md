# H-036 결과 보고서 (fallback-warning `KEEP_FROZEN` seeding throughput 추적 점검)

## 상태
- 현재 상태: **완료 (시딩 throughput 추적 실행 + fail-fast 실패 증빙 + 게이트 재집계 + 테스트 통과)**
- 실행일(KST): `2026-02-20`
- 점검 구간(KST):
  - 최신 14일: `2026-02-07 ~ 2026-02-20` (`today-13 ~ today`)
  - 최근 7일: `2026-02-14 ~ 2026-02-20` (`today-6 ~ today`)
  - 직전 7일: `2026-02-07 ~ 2026-02-13` (`today-13 ~ today-7`)
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-036-fallback-warning-keep-frozen-seeding-throughput-tracking.md`
  - main relay: `coordination/RELAYS/H-036-main-to-executor.md`
  - 참고: `coordination/REPORTS/H-035-result.md`, `coordination/REPORTS/H-035-1-result.md`, `coordination/REPORTS/H-035-1-review.md`

## 변경 파일 목록
- `scripts/seed-fallback-warning-workload.sh`
- `coordination/REPORTS/H-036-result.md`
- `coordination/RELAYS/H-036-executor-to-review.md`

## 구현 요약
- H-036 기본 목표(`direct >= 6`, `chain >= 3`) 달성을 위해 fail-fast 시딩을 2회 배치로 실행했습니다.
  - 1차 배치: `direct=6`, `chain=3`, fail-fast 활성
  - 2차 배치(재시도): `direct=0`, `chain=1`, fail-fast 활성
- 1차 배치의 `CHAIN#3`에서 모델 후보 전부 실패로 중단(exit 1)되었고, 동일 정책으로 chain 재시도 1회를 추가 실행해 체인 성공 증거를 3건으로 맞췄습니다.
- 실실행 중 확인된 권한 이슈를 방지하기 위해 시딩 스크립트를 최소 보강했습니다.
  - `GRADLE_USER_HOME` 기본값을 워크스페이스 로컬(`.gradle-local`)로 설정
  - 요청 prefix/log 시작 문구의 `H-035` 하드코딩을 라운드 중립 문자열로 정리

## 시딩 실행 명령(파라미터 포함) 및 요약 결과
- 사전 실패(권한 이슈 재현):
  - `SEED_DIRECT_RUNS=6 SEED_CHAIN_RUNS=3 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ... ./scripts/seed-fallback-warning-workload.sh`
  - 결과: Gradle lock 파일 접근 오류로 `DIRECT#1 exit=1` 즉시 중단
  - 근거: `storage/fallback-warning-seed/seed-20260220-142703-direct-1.stderr.log`
- 본 실행(1차 배치):
  - `GRADLE_USER_HOME=$PWD/.gradle-local SEED_DIRECT_RUNS=6 SEED_CHAIN_RUNS=3 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ... ./scripts/seed-fallback-warning-workload.sh`
  - 결과: `total=9`, `success=8`, `failed=1` (`CHAIN#3` fail-fast 중단)
  - 근거: `storage/fallback-warning-seed/seed-20260220-142752.log`, `storage/fallback-warning-seed/seed-20260220-142752-records.jsonl`
- 본 실행(2차 재시도):
  - `GRADLE_USER_HOME=$PWD/.gradle-local SEED_DIRECT_RUNS=0 SEED_CHAIN_RUNS=1 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ... ./scripts/seed-fallback-warning-workload.sh`
  - 결과: `total=1`, `success=1`, `failed=0`
  - 근거: `storage/fallback-warning-seed/seed-20260220-143838.log`, `storage/fallback-warning-seed/seed-20260220-143838-summary.json`
- H-036 목표 달성 여부:
  - direct 성공 `6회` 달성 (`>= 6` 충족)
  - chain 성공 `3회` 달성 (`>= 3` 충족)
  - 전체 시도 `10회`(성공 9, 실패 1)

## 생성된 runId 목록 + direct/chain 분류

| source | 분류 | index | runId | exitCode |
|---|---|---:|---|---:|
| `seed-20260220-142752` | DIRECT | 1 | `81620ccd-c5f6-4ac4-9b2a-bf123390ad0f` | 0 |
| `seed-20260220-142752` | DIRECT | 2 | `d35442da-989c-4391-bf3e-9ffe7c22d536` | 0 |
| `seed-20260220-142752` | DIRECT | 3 | `3b5939c4-26fe-4407-90fa-a76b1eea57e6` | 0 |
| `seed-20260220-142752` | DIRECT | 4 | `fe044064-e5b3-439a-b7de-85bb92e12c80` | 0 |
| `seed-20260220-142752` | DIRECT | 5 | `9313d958-08c9-4615-875a-efc26a1ebd4d` | 0 |
| `seed-20260220-142752` | DIRECT | 6 | `000f02bc-0596-408a-8c16-31559f295c62` | 0 |
| `seed-20260220-142752` | CHAIN | 1 | `194df10f-2b11-411a-a460-0bb28470c6fd` | 0 |
| `seed-20260220-142752` | CHAIN | 2 | `08fe0a72-4fb3-4403-a951-af6b667981ad` | 0 |
| `seed-20260220-142752` | CHAIN | 3 | `N/A` | 1 |
| `seed-20260220-143838` | CHAIN | 1 | `f4d77bb7-0784-4930-b1d1-45d08ca9e728` | 0 |

## `specRunId -> codeRunId -> docRunId/reviewRunId` 매핑

| source | specRunId | codeRunId | docRunId | reviewRunId |
|---|---|---|---|---|
| `seed-20260220-142752` | `194df10f-2b11-411a-a460-0bb28470c6fd` | `0744f06d-0e86-4800-9215-8ba8cebabc1f` | `5b304499-cffd-48a8-a09d-8ff1932b0899` | `b470dbcd-426f-49c0-85df-72d512cc50d8` |
| `seed-20260220-142752` | `08fe0a72-4fb3-4403-a951-af6b667981ad` | `0876a733-d61a-47af-ae02-d4c828a8392a` | `e68151c7-86cd-4284-85db-af7809a36d3e` | `15ec4718-eeca-4936-82ed-bc05ad6efe0f` |
| `seed-20260220-143838` | `f4d77bb7-0784-4930-b1d1-45d08ca9e728` | `e152b7c5-c079-4584-be6a-189792193919` | `c85bfc68-bef9-4045-aecd-bd8b4f4f2f78` | `d880d148-e895-46de-976f-f38fa0e5916c` |

## `run_events` 근거 (`CHAIN_CODE_DONE`, `CHAIN_DOC_DONE`, `CHAIN_REVIEW_DONE`)

| specRunId | 기준 runId | eventType | payload | createdAt (UTC) |
|---|---|---|---|---|
| `194df10f-2b11-411a-a460-0bb28470c6fd` | `194df10f-2b11-411a-a460-0bb28470c6fd` | `CHAIN_CODE_DONE` | `codeRunId=0744f06d-0e86-4800-9215-8ba8cebabc1f` | `2026-02-20T05:32:54.513702Z` |
| `194df10f-2b11-411a-a460-0bb28470c6fd` | `0744f06d-0e86-4800-9215-8ba8cebabc1f` | `CHAIN_DOC_DONE` | `docRunId=5b304499-cffd-48a8-a09d-8ff1932b0899` | `2026-02-20T05:32:33.460648Z` |
| `194df10f-2b11-411a-a460-0bb28470c6fd` | `0744f06d-0e86-4800-9215-8ba8cebabc1f` | `CHAIN_REVIEW_DONE` | `reviewRunId=b470dbcd-426f-49c0-85df-72d512cc50d8` | `2026-02-20T05:32:54.511480Z` |
| `08fe0a72-4fb3-4403-a951-af6b667981ad` | `08fe0a72-4fb3-4403-a951-af6b667981ad` | `CHAIN_CODE_DONE` | `codeRunId=0876a733-d61a-47af-ae02-d4c828a8392a` | `2026-02-20T05:36:04.765656Z` |
| `08fe0a72-4fb3-4403-a951-af6b667981ad` | `0876a733-d61a-47af-ae02-d4c828a8392a` | `CHAIN_DOC_DONE` | `docRunId=e68151c7-86cd-4284-85db-af7809a36d3e` | `2026-02-20T05:35:29.055792Z` |
| `08fe0a72-4fb3-4403-a951-af6b667981ad` | `0876a733-d61a-47af-ae02-d4c828a8392a` | `CHAIN_REVIEW_DONE` | `reviewRunId=15ec4718-eeca-4936-82ed-bc05ad6efe0f` | `2026-02-20T05:36:04.762786Z` |
| `f4d77bb7-0784-4930-b1d1-45d08ca9e728` | `f4d77bb7-0784-4930-b1d1-45d08ca9e728` | `CHAIN_CODE_DONE` | `codeRunId=e152b7c5-c079-4584-be6a-189792193919` | `2026-02-20T05:41:53.065054Z` |
| `f4d77bb7-0784-4930-b1d1-45d08ca9e728` | `e152b7c5-c079-4584-be6a-189792193919` | `CHAIN_DOC_DONE` | `docRunId=c85bfc68-bef9-4045-aecd-bd8b4f4f2f78` | `2026-02-20T05:41:17.060759Z` |
| `f4d77bb7-0784-4930-b1d1-45d08ca9e728` | `e152b7c5-c079-4584-be6a-189792193919` | `CHAIN_REVIEW_DONE` | `reviewRunId=d880d148-e895-46de-976f-f38fa0e5916c` | `2026-02-20T05:41:53.062923Z` |

## fail-fast 실패 케이스(non-zero 종료코드) 증빙
- 대상: `seed-20260220-142752`의 `CHAIN#3`
- 결과: `runId` 미생성 + `exitCode=1`로 즉시 중단
- 근거 파일:
  - `storage/fallback-warning-seed/seed-20260220-142752.log`
  - `storage/fallback-warning-seed/seed-20260220-142752-chain-3.stdout.json`
- 오류 메시지 요약:
  - `All model candidates failed` (OpenAI 1차 실패 + fallback 후보 오류 누적)
- 후속 조치:
  - 동일 fail-fast 정책으로 chain 재시도 1회(`seed-20260220-143838`) 실행 후 성공

## before/after 비교 (H-036 시딩 누적 기준)
- 기준:
  - before: `storage/fallback-warning-seed/seed-20260220-142752-before.json`
  - after: `storage/fallback-warning-seed/seed-20260220-143838-after.json`

| 항목 | before | after | delta |
|---|---:|---:|---:|
| 최근 7일 CODE run 수 | 5 | 15 | +10 |
| 최근 7일 SPEC run 수 | 2 | 6 | +4 |
| 최근 7일 DOC run 수 | 2 | 5 | +3 |
| 최근 7일 REVIEW run 수 | 2 | 5 | +3 |
| 최근 7일 CHAIN_CODE_DONE | 2 | 5 | +3 |
| 최근 7일 CHAIN_DOC_DONE | 2 | 5 | +3 |
| 최근 7일 CHAIN_REVIEW_DONE | 2 | 5 | +3 |
| 48시간 fresh CODE run 수 | 5 | 15 | +10 |
| 48시간 fresh CHAIN_DOC_DONE | 2 | 5 | +3 |
| 48시간 fresh CHAIN_REVIEW_DONE | 2 | 5 | +3 |

## 최신 14일 게이트 4개 PASS/FAIL

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 13일 / 0.9286 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 1일 | >= 7일 | FAIL |

- 최신 14일 누적 `parseEligibleRunCount`: `CODE 19`, `SPEC 7`, `DOC 5`, `REVIEW 5`, `전체 36`

## 최근 7일/직전 7일 비교 (`executionGapDelta`, `chainShareGapDelta`)

| 구분 | executionGap(최근7일) | executionGap(직전7일) | executionGapDelta | chainShareGap(최근7일) | chainShareGap(직전7일) | chainShareGapDelta |
|---|---:|---:|---:|---:|---:|---:|
| CODE | 97 | 108 | -11 | -8.33%p | 25.00%p | -33.33%p |
| SPEC | 22 | 27 | -5 | 0.00%p | 0.00%p | 0.00%p |
| DOC | 37 | 42 | -5 | 0.00%p | 100.00%p | -100.00%p |
| REVIEW | 37 | 42 | -5 | 0.00%p | 100.00%p | -100.00%p |
| 전체 | 193 | 219 | -26 | 1.61%p | 50.00%p | -48.39%p |

- 최근 7일 `dailyCompliance`: `1/7` PASS (`weeklyComplianceRate=0.14`)
- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `10.3333` (기준 `>= 32` 미충족)

## 단일 판정 및 근거
- `resumeDecision`: **`KEEP_FROZEN`**
- 판정 근거:
  - 실행량/체인 커버리지 측면의 개선 신호(`executionGapDelta=-26`, `chainShareGapDelta=-48.39%p`)와 신규 체인 증거는 확보되었습니다.
  - 그러나 게이트 4개 중 `INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS` 2개가 여전히 미충족(`0.9286`, `1일`)입니다.
  - 따라서 H-024 재개(`RESUME_H024`) 근거는 부족하며 `KEEP_FROZEN`을 유지합니다.
- `unmetReadinessSignals`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`0.9286 > 0.50`)
  - `SUFFICIENT_DAYS` (`1 < 7`)
- `nextCheckTrigger`:
  - 필수 충족 조건: `집계 성공 >= 10`, `INSUFFICIENT_SAMPLE <= 0.50`, `집계 불가 < 3`, `샘플 충분 일수 >= 7`
  - 다음 점검 시점: `2026-02-21 09:00 KST`
  - 우선 액션: fail-fast 유지 상태로 시딩 반복 실행해 `parseEligibleRunCount >= 20` 충족 일수 추가 누적

## 테스트 명령/결과
- 실행 명령: `GRADLE_USER_HOME=$PWD/.gradle-local ./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 게이트 미충족 2개(`INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`)가 여전히 `KEEP_FROZEN` 유지 조건으로 남아 있습니다.
- chain 실행은 누적되었지만, 모델 후보 실패가 간헐적으로 발생해 fail-fast 중단 가능성이 존재합니다(이번 라운드 `CHAIN#3` 재현).
- 최근 3일 평균 전체 모수(`10.3333`)가 재개 판단 기준(`>=32`)에 미달해, 반복 시딩이 끊기면 개선 추세가 둔화될 수 있습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `scripts/seed-fallback-warning-workload.sh` 보강(`GRADLE_USER_HOME` 로컬 기본값, 라운드 중립 prefix/log)이 기존 fail-fast/records/summary 계약을 깨지 않는지
2. H-036 시딩 증거(`runId`, `spec->code->doc/review`, `CHAIN_*_DONE`)와 결과 표 수치가 `storage/fallback-warning-seed/*.json*` 및 `storage/devagent.db` 집계와 일치하는지
3. 단일 판정(`resumeDecision=KEEP_FROZEN`)과 게이트 미충족 근거(`0.9286`, `1일`)가 relay/result 간 일관적인지
