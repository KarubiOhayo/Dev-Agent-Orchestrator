# H-038 결과 보고서 (fallback-warning `KEEP_FROZEN` seeding failure pattern follow-up)

## 상태
- 현재 상태: **완료 (fail-fast 반복 시딩 + 실패 원인 분류 + 최신 게이트 재집계 + 운영 가이드 반영 + 테스트 통과)**
- 실행일(KST): `2026-02-20`
- 점검 구간(KST):
  - 최신 14일: `2026-02-07 ~ 2026-02-20` (`today-13 ~ today`)
  - 최근 7일: `2026-02-14 ~ 2026-02-20` (`today-6 ~ today`)
  - 직전 7일: `2026-02-07 ~ 2026-02-13` (`today-13 ~ today-7`)
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-038-fallback-warning-keep-frozen-seeding-failure-pattern-followup.md`
  - main relay: `coordination/RELAYS/H-038-main-to-executor.md`
  - 참고: `coordination/REPORTS/H-037-result.md`, `coordination/REPORTS/H-037-review.md`, `coordination/RELAYS/H-037-review-to-main.md`, `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 변경 파일 목록
- `docs/cli-quickstart.md`
- `coordination/REPORTS/H-038-result.md`
- `coordination/RELAYS/H-038-executor-to-review.md`

## 구현 요약
- fail-fast 정책(`SEED_FAIL_FAST=true`)을 유지한 채 진단 배치 1회 후 본 배치를 수행했습니다.
- 본 배치에서 chain 구간이 fail-fast로 중단(`seed-20260220-153200`)되어, 동일 파라미터로 `SEED_CHAIN_RUNS=1` 재시도 배치를 반복해 목표치를 충족했습니다.
- 최종 누적 결과(이번 라운드 실행분):
  - 총 실행 `14회` (`DIRECT 7`, `CHAIN 7`)
  - 성공 `10회` (`DIRECT 7`, `CHAIN 3`)
  - 실패 `4회` (`CHAIN 4`, 모두 fail-fast non-zero)
  - 기본 목표(`DIRECT >= 6`, `CHAIN >= 3`) **충족**
- 체인 실패 원인을 분류해 재발 빈도를 정량화했고, 운영 완화 가이드를 `docs/cli-quickstart.md`에 동기화했습니다.
- 집계 스냅샷/증빙 집계를 위해 `storage/fallback-warning-seed/h038-metrics.json`를 생성해 결과 표를 산출했습니다.

## 시딩 실행 명령(파라미터 포함) 및 요약 결과
1. 진단 배치
   - 명령:
     - `SEED_DIRECT_RUNS=1 SEED_CHAIN_RUNS=1 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ./scripts/seed-fallback-warning-workload.sh`
   - 결과: `total=2`, `success=2`, `failed=0`
   - 근거:
     - `storage/fallback-warning-seed/seed-20260220-152857.log`
     - `storage/fallback-warning-seed/seed-20260220-152857-records.jsonl`
2. 본 배치(direct)
   - 명령:
     - `SEED_DIRECT_RUNS=6 SEED_CHAIN_RUNS=0 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ./scripts/seed-fallback-warning-workload.sh`
   - 결과: `total=6`, `success=6`, `failed=0`
   - 근거:
     - `storage/fallback-warning-seed/seed-20260220-153106.log`
     - `storage/fallback-warning-seed/seed-20260220-153106-records.jsonl`
3. 본 배치(chain)
   - 명령:
     - `SEED_DIRECT_RUNS=0 SEED_CHAIN_RUNS=3 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ./scripts/seed-fallback-warning-workload.sh`
   - 결과: `CHAIN#1`에서 fail-fast 중단(`exit=1`, `runId=N/A`)
   - 근거:
     - `storage/fallback-warning-seed/seed-20260220-153200.log`
     - `storage/fallback-warning-seed/seed-20260220-153200-chain-1.stdout.json`
4. 보강 재시도(chain 1회씩 동일 명령 반복)
   - 명령(반복):
     - `SEED_DIRECT_RUNS=0 SEED_CHAIN_RUNS=1 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ./scripts/seed-fallback-warning-workload.sh`
   - 결과:
     - 실패: `seed-20260220-153410`, `seed-20260220-153610`, `seed-20260220-154023`
     - 성공: `seed-20260220-153837`, `seed-20260220-154214`
   - 근거:
     - `storage/fallback-warning-seed/seed-20260220-153410-records.jsonl`
     - `storage/fallback-warning-seed/seed-20260220-153610-records.jsonl`
     - `storage/fallback-warning-seed/seed-20260220-153837-records.jsonl`
     - `storage/fallback-warning-seed/seed-20260220-154023-records.jsonl`
     - `storage/fallback-warning-seed/seed-20260220-154214-records.jsonl`

## 생성된 runId 목록 + direct/chain 분류

| source | 분류 | index | runId | exitCode |
|---|---|---:|---|---:|
| `seed-20260220-152857` | DIRECT | 1 | `3e4444c6-0477-48ae-b42b-4923a2ea2f32` | 0 |
| `seed-20260220-152857` | CHAIN | 1 | `6a4cda9f-c973-4dd5-a42d-9170293a843c` | 0 |
| `seed-20260220-153106` | DIRECT | 1 | `8801a8d2-98ed-4dec-b578-14e4a609a17c` | 0 |
| `seed-20260220-153106` | DIRECT | 2 | `99bd18e4-1b8e-40fc-b4ec-87b68732ad6b` | 0 |
| `seed-20260220-153106` | DIRECT | 3 | `068d61b2-44aa-4cd1-9be9-e36f834aa228` | 0 |
| `seed-20260220-153106` | DIRECT | 4 | `b1d4a440-f0d3-4cf7-bc9e-b2a621e7a6c8` | 0 |
| `seed-20260220-153106` | DIRECT | 5 | `069e179b-5792-4078-9c5d-8de46549132a` | 0 |
| `seed-20260220-153106` | DIRECT | 6 | `4966e8cc-c1e3-4b07-a74c-3bfe755f0214` | 0 |
| `seed-20260220-153200` | CHAIN | 1 | `N/A` | 1 |
| `seed-20260220-153410` | CHAIN | 1 | `N/A` | 1 |
| `seed-20260220-153610` | CHAIN | 1 | `N/A` | 1 |
| `seed-20260220-153837` | CHAIN | 1 | `336efe2e-5574-4543-860d-736e50e3dde6` | 0 |
| `seed-20260220-154023` | CHAIN | 1 | `N/A` | 1 |
| `seed-20260220-154214` | CHAIN | 1 | `b55b09e5-04d3-4e88-ad05-e643a78b708d` | 0 |

## `specRunId -> codeRunId -> docRunId/reviewRunId` 매핑

| source | specRunId | codeRunId | docRunId | reviewRunId |
|---|---|---|---|---|
| `seed-20260220-152857` | `6a4cda9f-c973-4dd5-a42d-9170293a843c` | `185fe085-a00d-482c-951d-b2a612d63812` | `3e841bf0-4d3e-426f-9de8-a3508b5d3061` | `7f45a5f2-e66e-49d5-93a2-11dc3e39b2a0` |
| `seed-20260220-153837` | `336efe2e-5574-4543-860d-736e50e3dde6` | `86e5e8cd-5ee6-43f7-a057-46a69d984bbd` | `0dbcdf5a-8bd6-4de2-b864-51c3dd7936e4` | `fbc647be-551e-4eeb-950d-5292df1987b7` |
| `seed-20260220-154214` | `b55b09e5-04d3-4e88-ad05-e643a78b708d` | `07e33bae-2c7b-49b7-8b4e-5416715784b7` | `e6435dbd-3a41-4559-b04c-7316da76dd70` | `f3e20953-1a32-46dd-8cfd-099ac7a13f33` |

## `run_events` 근거 (`CHAIN_CODE_DONE`, `CHAIN_DOC_DONE`, `CHAIN_REVIEW_DONE`)

| source | specRunId | 기준 runId | eventType | payload | createdAt (UTC) |
|---|---|---|---|---|---|
| `seed-20260220-152857` | `6a4cda9f-c973-4dd5-a42d-9170293a843c` | `6a4cda9f-c973-4dd5-a42d-9170293a843c` | `CHAIN_CODE_DONE` | `codeRunId=185fe085-a00d-482c-951d-b2a612d63812` | `2026-02-20T06:31:00.453943Z` |
| `seed-20260220-152857` | `6a4cda9f-c973-4dd5-a42d-9170293a843c` | `185fe085-a00d-482c-951d-b2a612d63812` | `CHAIN_DOC_DONE` | `docRunId=3e841bf0-4d3e-426f-9de8-a3508b5d3061` | `2026-02-20T06:30:39.242751Z` |
| `seed-20260220-152857` | `6a4cda9f-c973-4dd5-a42d-9170293a843c` | `185fe085-a00d-482c-951d-b2a612d63812` | `CHAIN_REVIEW_DONE` | `reviewRunId=7f45a5f2-e66e-49d5-93a2-11dc3e39b2a0` | `2026-02-20T06:31:00.451049Z` |
| `seed-20260220-153837` | `336efe2e-5574-4543-860d-736e50e3dde6` | `336efe2e-5574-4543-860d-736e50e3dde6` | `CHAIN_CODE_DONE` | `codeRunId=86e5e8cd-5ee6-43f7-a057-46a69d984bbd` | `2026-02-20T06:40:19.047776Z` |
| `seed-20260220-153837` | `336efe2e-5574-4543-860d-736e50e3dde6` | `86e5e8cd-5ee6-43f7-a057-46a69d984bbd` | `CHAIN_DOC_DONE` | `docRunId=0dbcdf5a-8bd6-4de2-b864-51c3dd7936e4` | `2026-02-20T06:39:50.072706Z` |
| `seed-20260220-153837` | `336efe2e-5574-4543-860d-736e50e3dde6` | `86e5e8cd-5ee6-43f7-a057-46a69d984bbd` | `CHAIN_REVIEW_DONE` | `reviewRunId=fbc647be-551e-4eeb-950d-5292df1987b7` | `2026-02-20T06:40:19.044970Z` |
| `seed-20260220-154214` | `b55b09e5-04d3-4e88-ad05-e643a78b708d` | `b55b09e5-04d3-4e88-ad05-e643a78b708d` | `CHAIN_CODE_DONE` | `codeRunId=07e33bae-2c7b-49b7-8b4e-5416715784b7` | `2026-02-20T06:45:00.101889Z` |
| `seed-20260220-154214` | `b55b09e5-04d3-4e88-ad05-e643a78b708d` | `07e33bae-2c7b-49b7-8b4e-5416715784b7` | `CHAIN_DOC_DONE` | `docRunId=e6435dbd-3a41-4559-b04c-7316da76dd70` | `2026-02-20T06:44:36.238955Z` |
| `seed-20260220-154214` | `b55b09e5-04d3-4e88-ad05-e643a78b708d` | `07e33bae-2c7b-49b7-8b4e-5416715784b7` | `CHAIN_REVIEW_DONE` | `reviewRunId=f3e20953-1a32-46dd-8cfd-099ac7a13f33` | `2026-02-20T06:45:00.098812Z` |

## fail-fast 실패 원인 분류 표
- 분류 기준: fail-fast 실패 케이스(`exitCode != 0` 또는 `runId` 미생성)별 `seed-*-chain-1.stdout.json`의 `error.message` 패턴 포함 여부

| rootCause | count | latestEvidenceRef | impact |
|---|---:|---|---|
| `TEMPERATURE_UNSUPPORTED` | 4 | `storage/fallback-warning-seed/seed-20260220-154023-chain-1.stdout.json` | `CHAIN` |
| `MODEL_NOT_FOUND_OR_UNAVAILABLE` | 4 | `storage/fallback-warning-seed/seed-20260220-154023-chain-1.stdout.json` | `CHAIN` |
| `ALL_CANDIDATES_FAILED` | 4 | `storage/fallback-warning-seed/seed-20260220-154023-chain-1.stdout.json` | `CHAIN` |
| `OTHER` | 4 | `storage/fallback-warning-seed/seed-20260220-154023-chain-1.stdout.json` | `CHAIN` |

## before/after 비교 (H-038 라운드 기준)
- 기준:
  - before: `storage/fallback-warning-seed/seed-20260220-152857-before.json`
  - after: `storage/fallback-warning-seed/seed-20260220-154214-after.json`

| 항목 | before | after | delta |
|---|---:|---:|---:|
| 최근 7일 CODE run 수 | 26 | 40 | +14 |
| 최근 7일 SPEC run 수 | 10 | 17 | +7 |
| 최근 7일 DOC run 수 | 8 | 11 | +3 |
| 최근 7일 REVIEW run 수 | 8 | 11 | +3 |
| 최근 7일 CHAIN_CODE_DONE | 8 | 11 | +3 |
| 최근 7일 CHAIN_DOC_DONE | 8 | 11 | +3 |
| 최근 7일 CHAIN_REVIEW_DONE | 8 | 11 | +3 |
| 48시간 fresh CODE run 수 | 26 | 40 | +14 |
| 48시간 fresh CHAIN_DOC_DONE | 8 | 11 | +3 |
| 48시간 fresh CHAIN_REVIEW_DONE | 8 | 11 | +3 |

## 최신 14일 게이트 4개 PASS/FAIL

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 13일 / 0.9286 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 1일 | >= 7일 | FAIL |

- 최신 14일 누적 `parseEligibleRunCount`: `CODE 44`, `SPEC 18`, `DOC 11`, `REVIEW 11`, `전체 84`

## 최근 7일/직전 7일 비교 (`executionGapDelta`, `chainShareGapDelta`)

| 구분 | executionGap(최근7일) | executionGap(직전7일) | executionGapDelta | chainShareGap(최근7일) | chainShareGap(직전7일) | chainShareGapDelta |
|---|---:|---:|---:|---:|---:|---:|
| CODE | 72 | 108 | -36 | -2.50%p | 25.00%p | -27.50%p |
| SPEC | 11 | 27 | -16 | 0.00%p | 0.00%p | 0.00%p |
| DOC | 31 | 42 | -11 | 0.00%p | 100.00%p | -100.00%p |
| REVIEW | 31 | 42 | -11 | 0.00%p | 100.00%p | -100.00%p |
| 전체 | 145 | 219 | -74 | 8.23%p | 50.00%p | -41.77%p |

- 최근 7일 `dailyCompliance`: `1/7` PASS (`weeklyComplianceRate=0.14`)
- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `26.3333` (기준 `>= 32` 미충족)

## 단일 판정 및 근거
- `resumeDecision`: **`KEEP_FROZEN`**
- 판정 근거:
  - 실행량/체인 커버리지 지표는 개선되었습니다(`executionGapDelta=-74`, `chainShareGapDelta=-41.77%p`).
  - 하지만 게이트 4개 중 `INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS` 2개가 여전히 미충족(`0.9286`, `1일`)입니다.
  - 따라서 H-024 재개(`RESUME_H024`) 근거는 아직 부족해 `KEEP_FROZEN`을 유지합니다.
- `unmetReadinessSignals`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`0.9286 > 0.50`)
  - `SUFFICIENT_DAYS` (`1 < 7`)
- `nextCheckTrigger`:
  - 필수 충족 조건: `집계 성공 >= 10`, `INSUFFICIENT_SAMPLE <= 0.50`, `집계 불가 < 3`, `샘플 충분 일수 >= 7`
  - 다음 점검 시점: `2026-02-21 09:00 KST`
  - 우선 액션: fail-fast 유지 상태의 시딩 반복으로 `parseEligibleRunCount >= 20` 충족 일수 추가 누적

## 운영 문서 완화 가이드 반영 여부
- 반영: **예**
- 반영 파일: `docs/cli-quickstart.md`
- 반영 내용:
  - `fail-fast 실패 패턴 점검 가이드 (H-038)` 섹션 추가
  - 원인 분류(`TEMPERATURE_UNSUPPORTED`, `MODEL_NOT_FOUND_OR_UNAVAILABLE`, `ALL_CANDIDATES_FAILED`, `OTHER`)와 증빙 수집/재시도 절차 명시
  - 라우팅/파라미터 정책 조정 시 Main 승인 필요(공통 파일 변경 절차) 명시

## fail-fast 실패 케이스(non-zero 종료코드) 증빙
- 대상: `seed-20260220-153200`, `seed-20260220-153410`, `seed-20260220-153610`, `seed-20260220-154023`
- 결과: 모두 `runId` 미생성 + `exitCode=1`로 즉시 중단
- 공통 근거:
  - `storage/fallback-warning-seed/seed-20260220-153200-chain-1.stdout.json`
  - `storage/fallback-warning-seed/seed-20260220-153410-chain-1.stdout.json`
  - `storage/fallback-warning-seed/seed-20260220-153610-chain-1.stdout.json`
  - `storage/fallback-warning-seed/seed-20260220-154023-chain-1.stdout.json`
- 오류 메시지 요약:
  - `All model candidates failed`
  - `Unsupported parameter: 'temperature' is not supported with this model`
  - `not_found_error` (`model: claude-sonnet-4.5`)

## 테스트 명령/결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최신 14일 게이트 2개(`INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`) 미충족 상태가 지속되어 `RESUME_H024` 전환 근거가 부족합니다.
- fail-fast 체인 경로에서 후보 모델 전부 실패가 반복적으로 발생해, chain 누적 효율이 외부 모델 가용성/응답 품질에 영향을 받습니다.
- 최근 3일 평균 전체 모수(`26.3333`)가 재개 기준(`>=32`)에 미달해 추가 누적이 중단되면 개선 추세가 둔화될 수 있습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `coordination/REPORTS/H-038-result.md`의 runId/매핑/`CHAIN_*_DONE` 표가 `seed-20260220-152857*`, `seed-20260220-153837*`, `seed-20260220-154214*` 근거와 정확히 일치하는지
2. fail-fast 실패 원인 분류 표의 count/근거(`seed-20260220-153200*`, `153410*`, `153610*`, `154023*`)가 패턴 추출 기준과 정합한지
3. 최신 게이트 수치(`0.9286`, `1일`) 및 단일 판정(`resumeDecision=KEEP_FROZEN`)이 result/relay/운영 문서 반영 내용과 일관적인지
