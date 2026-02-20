# H-037 결과 보고서 (fallback-warning `KEEP_FROZEN` seeding follow-up + workspace hygiene 정합화)

## 상태
- 현재 상태: **완료 (워크트리 위생 정합화 + 반복 시딩 누적 + 최신 게이트 재집계 + 테스트 통과)**
- 실행일(KST): `2026-02-20`
- 점검 구간(KST):
  - 최신 14일: `2026-02-07 ~ 2026-02-20` (`today-13 ~ today`)
  - 최근 7일: `2026-02-14 ~ 2026-02-20` (`today-6 ~ today`)
  - 직전 7일: `2026-02-07 ~ 2026-02-13` (`today-13 ~ today-7`)
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-037-fallback-warning-keep-frozen-seeding-followup-hygiene.md`
  - main relay: `coordination/RELAYS/H-037-main-to-executor.md`
  - 참고: `coordination/REPORTS/H-036-result.md`, `coordination/REPORTS/H-036-review.md`, `coordination/RELAYS/H-036-review-to-main.md`, `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 변경 파일 목록
- `.gitignore`
- `coordination/REPORTS/H-037-result.md`
- `coordination/RELAYS/H-037-executor-to-review.md`

## 구현 요약
- `.gradle-local/` 워크트리 노이즈(P3) 해소를 위해 `.gitignore`에 `.gradle-local/`를 추가했습니다.
- fail-fast 정책(`SEED_FAIL_FAST=true`)을 유지한 상태로 시딩을 실행해 H-037 기본 목표(`direct >= 6`, `chain >= 3`)를 충족했습니다.
  - 본 실행 배치: `direct=6, chain=0` + `direct=0, chain=3`
  - 목표 달성: direct 성공 `6회`, chain 성공 `3회`
- fail-fast 실패 케이스(non-zero)도 별도 진단 실행에서 재현되어 종료코드/오류 증빙을 확보했습니다.

## 시딩 실행 명령(파라미터 포함) 및 요약 결과
1. fail-fast 진단 실행(실패 증빙 확보)
   - 명령:
     - `SEED_DIRECT_RUNS=1 SEED_CHAIN_RUNS=1 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ./scripts/seed-fallback-warning-workload.sh`
   - 결과: `total=2`, `success=1`, `failed=1` (`CHAIN#1` fail-fast 중단, `exit=1`)
   - 근거:
     - `storage/fallback-warning-seed/seed-20260220-150422.log`
     - `storage/fallback-warning-seed/seed-20260220-150422-records.jsonl`
     - `storage/fallback-warning-seed/seed-20260220-150422-chain-1.stdout.json`
2. 본 실행(직접 호출 목표치)
   - 명령:
     - `SEED_DIRECT_RUNS=6 SEED_CHAIN_RUNS=0 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ./scripts/seed-fallback-warning-workload.sh`
   - 결과: `total=6`, `success=6`, `failed=0`
   - 근거:
     - `storage/fallback-warning-seed/seed-20260220-150659.log`
     - `storage/fallback-warning-seed/seed-20260220-150659-records.jsonl`
     - `storage/fallback-warning-seed/seed-20260220-150659-summary.json`
3. 본 실행(체인 호출 목표치)
   - 명령:
     - `SEED_DIRECT_RUNS=0 SEED_CHAIN_RUNS=3 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ./scripts/seed-fallback-warning-workload.sh`
   - 결과: `total=3`, `success=3`, `failed=0`
   - 근거:
     - `storage/fallback-warning-seed/seed-20260220-150804.log`
     - `storage/fallback-warning-seed/seed-20260220-150804-records.jsonl`
     - `storage/fallback-warning-seed/seed-20260220-150804-summary.json`

## 생성된 runId 목록 + direct/chain 분류

| source | 분류 | index | runId | exitCode |
|---|---|---:|---|---:|
| `seed-20260220-150422` | DIRECT | 1 | `ecdf2d4b-17bc-42aa-a522-69b938b84ade` | 0 |
| `seed-20260220-150422` | CHAIN | 1 | `N/A` | 1 |
| `seed-20260220-150659` | DIRECT | 1 | `daf25914-1e29-437c-b819-b58ba93ea64b` | 0 |
| `seed-20260220-150659` | DIRECT | 2 | `ede28a8a-48d2-49c6-b953-88dbb85eb948` | 0 |
| `seed-20260220-150659` | DIRECT | 3 | `c8a36138-ec55-427d-becf-3f9cb5a701d9` | 0 |
| `seed-20260220-150659` | DIRECT | 4 | `a86866a8-d408-407b-80d3-11bc46996dfb` | 0 |
| `seed-20260220-150659` | DIRECT | 5 | `a95ff0e3-8b0f-4a78-b415-e6057d0efc53` | 0 |
| `seed-20260220-150659` | DIRECT | 6 | `c276a1eb-c259-42e2-ba1c-e6e858d9e7f2` | 0 |
| `seed-20260220-150804` | CHAIN | 1 | `f86f6719-e3cd-49c0-991c-ecc87442e149` | 0 |
| `seed-20260220-150804` | CHAIN | 2 | `0d3ac119-8643-4402-b4ad-b87c76cb6452` | 0 |
| `seed-20260220-150804` | CHAIN | 3 | `5a84451e-d60f-4f7d-87fb-30468ce7b0d6` | 0 |

## `specRunId -> codeRunId -> docRunId/reviewRunId` 매핑

| source | specRunId | codeRunId | docRunId | reviewRunId |
|---|---|---|---|---|
| `seed-20260220-150804` | `f86f6719-e3cd-49c0-991c-ecc87442e149` | `e93b6e8a-bbc7-48d2-b0ac-32d462a2ddd0` | `6f8d5cac-1621-42c0-b92c-e88719f4a3cd` | `ee94573b-bdaf-497e-9b2c-5f5f9e354923` |
| `seed-20260220-150804` | `0d3ac119-8643-4402-b4ad-b87c76cb6452` | `1da5c3d9-4297-42c5-8b0b-ac469dd78207` | `83e5714b-31c8-4467-bda2-fa9c2105ad5f` | `48f4edec-6b7f-4d4b-a635-6c9724ff115f` |
| `seed-20260220-150804` | `5a84451e-d60f-4f7d-87fb-30468ce7b0d6` | `6cfc5a17-23d8-4061-b245-21b4a0725497` | `4225f9c1-a92a-4878-8617-2c3ea001d4a4` | `c38a50b5-e198-4400-a21e-e02233f0dc7e` |

## `run_events` 근거 (`CHAIN_CODE_DONE`, `CHAIN_DOC_DONE`, `CHAIN_REVIEW_DONE`)

| specRunId | 기준 runId | eventType | payload | createdAt (UTC) |
|---|---|---|---|---|
| `f86f6719-e3cd-49c0-991c-ecc87442e149` | `f86f6719-e3cd-49c0-991c-ecc87442e149` | `CHAIN_CODE_DONE` | `codeRunId=e93b6e8a-bbc7-48d2-b0ac-32d462a2ddd0` | `2026-02-20T06:10:16.325666Z` |
| `f86f6719-e3cd-49c0-991c-ecc87442e149` | `e93b6e8a-bbc7-48d2-b0ac-32d462a2ddd0` | `CHAIN_DOC_DONE` | `docRunId=6f8d5cac-1621-42c0-b92c-e88719f4a3cd` | `2026-02-20T06:09:47.953291Z` |
| `f86f6719-e3cd-49c0-991c-ecc87442e149` | `e93b6e8a-bbc7-48d2-b0ac-32d462a2ddd0` | `CHAIN_REVIEW_DONE` | `reviewRunId=ee94573b-bdaf-497e-9b2c-5f5f9e354923` | `2026-02-20T06:10:16.322417Z` |
| `0d3ac119-8643-4402-b4ad-b87c76cb6452` | `0d3ac119-8643-4402-b4ad-b87c76cb6452` | `CHAIN_CODE_DONE` | `codeRunId=1da5c3d9-4297-42c5-8b0b-ac469dd78207` | `2026-02-20T06:12:37.939890Z` |
| `0d3ac119-8643-4402-b4ad-b87c76cb6452` | `1da5c3d9-4297-42c5-8b0b-ac469dd78207` | `CHAIN_DOC_DONE` | `docRunId=83e5714b-31c8-4467-bda2-fa9c2105ad5f` | `2026-02-20T06:12:14.907498Z` |
| `0d3ac119-8643-4402-b4ad-b87c76cb6452` | `1da5c3d9-4297-42c5-8b0b-ac469dd78207` | `CHAIN_REVIEW_DONE` | `reviewRunId=48f4edec-6b7f-4d4b-a635-6c9724ff115f` | `2026-02-20T06:12:37.936916Z` |
| `5a84451e-d60f-4f7d-87fb-30468ce7b0d6` | `5a84451e-d60f-4f7d-87fb-30468ce7b0d6` | `CHAIN_CODE_DONE` | `codeRunId=6cfc5a17-23d8-4061-b245-21b4a0725497` | `2026-02-20T06:14:57.081404Z` |
| `5a84451e-d60f-4f7d-87fb-30468ce7b0d6` | `6cfc5a17-23d8-4061-b245-21b4a0725497` | `CHAIN_DOC_DONE` | `docRunId=4225f9c1-a92a-4878-8617-2c3ea001d4a4` | `2026-02-20T06:14:32.084078Z` |
| `5a84451e-d60f-4f7d-87fb-30468ce7b0d6` | `6cfc5a17-23d8-4061-b245-21b4a0725497` | `CHAIN_REVIEW_DONE` | `reviewRunId=c38a50b5-e198-4400-a21e-e02233f0dc7e` | `2026-02-20T06:14:57.078124Z` |

## fail-fast 실패 케이스(non-zero 종료코드) 증빙
- 대상: `seed-20260220-150422`의 `CHAIN#1`
- 결과: `runId` 미생성 + `exitCode=1`로 즉시 중단
- 근거 파일:
  - `storage/fallback-warning-seed/seed-20260220-150422.log`
  - `storage/fallback-warning-seed/seed-20260220-150422-chain-1.stdout.json`
- 오류 메시지 요약:
  - `All model candidates failed`
  - OpenAI 후보 중 `gpt-5.2-codex`에서 `temperature` 파라미터 미지원(HTTP 400)
- 후속 조치:
  - 동일 fail-fast 정책 유지 상태로 본 실행 배치를 분리(`direct 6회`, `chain 3회`)하여 목표를 충족

## before/after 비교 (H-037 라운드 기준)
- 기준:
  - before: `storage/fallback-warning-seed/seed-20260220-150422-before.json`
  - after: `storage/fallback-warning-seed/seed-20260220-150804-after.json`

| 항목 | before | after | delta |
|---|---:|---:|---:|
| 최근 7일 CODE run 수 | 15 | 26 | +11 |
| 최근 7일 SPEC run 수 | 6 | 10 | +4 |
| 최근 7일 DOC run 수 | 5 | 8 | +3 |
| 최근 7일 REVIEW run 수 | 5 | 8 | +3 |
| 최근 7일 CHAIN_CODE_DONE | 5 | 8 | +3 |
| 최근 7일 CHAIN_DOC_DONE | 5 | 8 | +3 |
| 최근 7일 CHAIN_REVIEW_DONE | 5 | 8 | +3 |
| 48시간 fresh CODE run 수 | 15 | 26 | +11 |
| 48시간 fresh CHAIN_DOC_DONE | 5 | 8 | +3 |
| 48시간 fresh CHAIN_REVIEW_DONE | 5 | 8 | +3 |

## 최신 14일 게이트 4개 PASS/FAIL

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 13일 / 0.9286 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 1일 | >= 7일 | FAIL |

- 최신 14일 누적 `parseEligibleRunCount`: `CODE 30`, `SPEC 11`, `DOC 8`, `REVIEW 8`, `전체 57`

## 최근 7일/직전 7일 비교 (`executionGapDelta`, `chainShareGapDelta`)

| 구분 | executionGap(최근7일) | executionGap(직전7일) | executionGapDelta | chainShareGap(최근7일) | chainShareGap(직전7일) | chainShareGapDelta |
|---|---:|---:|---:|---:|---:|---:|
| CODE | 86 | 108 | -22 | -5.77%p | 25.00%p | -30.77%p |
| SPEC | 18 | 27 | -9 | 0.00%p | 0.00%p | 0.00%p |
| DOC | 34 | 42 | -8 | 0.00%p | 100.00%p | -100.00%p |
| REVIEW | 34 | 42 | -8 | 0.00%p | 100.00%p | -100.00%p |
| 전체 | 172 | 219 | -47 | 3.85%p | 50.00%p | -46.15%p |

- 최근 7일 `dailyCompliance`: `1/7` PASS (`weeklyComplianceRate=0.14`)
- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `17.3333` (기준 `>= 32` 미충족)

## 단일 판정 및 근거
- `resumeDecision`: **`KEEP_FROZEN`**
- 판정 근거:
  - 실행량/체인 커버리지는 H-036 대비 개선 신호가 확인되었습니다(`executionGapDelta=-47`, `chainShareGapDelta=-46.15%p`).
  - 그러나 게이트 4개 중 `INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS` 2개가 여전히 미충족(`0.9286`, `1일`)입니다.
  - 따라서 H-024 재개(`RESUME_H024`) 근거는 부족하며 `KEEP_FROZEN`을 유지합니다.
- `unmetReadinessSignals`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`0.9286 > 0.50`)
  - `SUFFICIENT_DAYS` (`1 < 7`)
- `nextCheckTrigger`:
  - 필수 충족 조건: `집계 성공 >= 10`, `INSUFFICIENT_SAMPLE <= 0.50`, `집계 불가 < 3`, `샘플 충분 일수 >= 7`
  - 다음 점검 시점: `2026-02-21 09:00 KST`
  - 우선 액션: fail-fast 유지 상태로 시딩 반복 실행해 `parseEligibleRunCount >= 20` 충족 일수를 추가 누적

## `.gradle-local` 워크트리 위생 정합화
- 적용 내용: `.gitignore`에 `.gradle-local/` 추가
- 적용 전 근거 (`git status --short`):
  - `?? .gradle-local/`
- 적용 후 근거 (`git status --short`):
  - `.gradle-local/` untracked 항목이 사라지고 `.gitignore` 변경만 표시됨

## 테스트 명령/결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최신 14일 게이트 2개(`INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`)가 계속 미충족이므로 `KEEP_FROZEN` 해제 근거가 아직 부족합니다.
- fail-fast 경로에서 모델 후보 실패가 재발할 수 있어, 체인 누적 목표는 외부 모델/키 상태에 영향을 받습니다.
- 최근 3일 평균 전체 모수(`17.3333`)가 재개 기준(`>=32`)에 미달하여 반복 시딩이 중단되면 개선 추세가 다시 둔화될 수 있습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `.gitignore`의 `.gradle-local/` 추가가 H-036 리뷰 P3(워크트리 노이즈) 지적을 충분히 해소하는지
2. 시딩 runId/체인 매핑/`CHAIN_*_DONE` 이벤트 표가 `seed-20260220-150659*`, `seed-20260220-150804*` 산출물과 정확히 일치하는지
3. 최신 게이트 수치(`0.9286`, `1일`) 및 단일 판정(`resumeDecision=KEEP_FROZEN`)이 result/relay 간 일관적인지
