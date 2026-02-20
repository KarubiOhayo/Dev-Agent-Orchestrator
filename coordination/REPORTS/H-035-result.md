# H-035 결과 보고서 (fallback-warning 실행량/체인 커버리지 traffic seeding 부트스트랩)

## 상태
- 현재 상태: **완료 (시딩 스크립트 추가 + 운영 문서 동기화 + 테스트 게이트 통과)**
- 실행일(KST): `2026-02-20`
- 점검 구간(KST):
  - 최신 14일: `2026-02-07 ~ 2026-02-20` (`today-13 ~ today`)
  - 최근 7일: `2026-02-14 ~ 2026-02-20` (`today-6 ~ today`)
  - 직전 7일: `2026-02-07 ~ 2026-02-13` (`today-13 ~ today-7`)
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md`
  - main relay: `coordination/RELAYS/H-035-main-to-executor.md` **(미생성, handoff 기준으로 실행)**

## 변경 파일 목록
- `scripts/seed-fallback-warning-workload.sh`
- `docs/cli-quickstart.md`
- `docs/code-agent-api.md`
- `docs/PROJECT_OVERVIEW.md`
- `coordination/REPORTS/H-035-result.md`
- `coordination/RELAYS/H-035-executor-to-review.md`

## 구현 요약
- `scripts/seed-fallback-warning-workload.sh`를 신규 추가했습니다.
  - direct 시드(`devagent generate`) + chain 시드(`devagent spec --chain-to-code true --code-chain-to-doc true --code-chain-to-review true`)를 재현 가능하게 실행합니다.
  - `SEED_DIRECT_RUNS`, `SEED_CHAIN_RUNS`, `SEED_APPLY`, `SEED_MODE`, `SEED_FAIL_FAST` 환경 변수로 실행 제어가 가능합니다.
  - `--json` 출력 + `python3`(`json`, `sqlite3`)만 사용해 runId/체인 매핑/이벤트를 추출하며 `jq` 의존이 없습니다.
  - 결과를 `storage/fallback-warning-seed/` 경로의 log/records/snapshot/summary 파일로 남깁니다.
- `docs/cli-quickstart.md`에 H-035 traffic seeding 실행 섹션을 추가했습니다.
- `docs/code-agent-api.md`에 H-035 섹션을 추가해 "문서 필드 확장보다 데이터 생성 우선" 원칙, runId 매핑 규칙, 실측 요약을 고정했습니다.
- `docs/PROJECT_OVERVIEW.md` 완료 항목에 H-035를 반영했습니다.

## 시딩 스크립트 실행 명령/요약 결과
- 실행 명령:
  - `SEED_DIRECT_RUNS=1 SEED_CHAIN_RUNS=1 SEED_APPLY=false SEED_MODE=BALANCED SEED_FAIL_FAST=true ./scripts/seed-fallback-warning-workload.sh`
- 요약 결과:
  - total runs: `2`
  - success: `2`
  - failures: `0`
  - chain run: `1`
- 산출 로그:
  - `storage/fallback-warning-seed/seed-20260220-124801.log`
  - `storage/fallback-warning-seed/seed-20260220-124801-records.jsonl`
  - `storage/fallback-warning-seed/seed-20260220-124801-before.json`
  - `storage/fallback-warning-seed/seed-20260220-124801-after.json`
  - `storage/fallback-warning-seed/seed-20260220-124801-summary.json`

## 생성된 runId 목록 + direct/chain 분류

| 분류 | 실행 유형 | runId | exitCode |
|---|---|---|---:|
| precheck | direct (`generate`) | `879eb4eb-f519-4761-a68e-434d85c678d8` | 0 |
| precheck | chain (`spec`) | `bc8551da-38e4-4198-a3aa-f6d4ce8d86a3` | 0 |
| script | direct (`generate`) | `bc7eeed6-0c74-42af-89d6-e6c752554069` | 0 |
| script | chain (`spec`) | `3f5581d1-0c7e-45cb-84b6-bd6cb2cdaa36` | 0 |

## `specRunId -> codeRunId -> docRunId/reviewRunId` 매핑

| source | specRunId | codeRunId | docRunId | reviewRunId |
|---|---|---|---|---|
| precheck | `bc8551da-38e4-4198-a3aa-f6d4ce8d86a3` | `5599a762-7de2-44cb-befb-fe3d92f41ba4` | `47615b80-9a3a-4354-adb0-49e7440795d1` | `0267aa55-0434-4a63-91c9-443a3a06a402` |
| script | `3f5581d1-0c7e-45cb-84b6-bd6cb2cdaa36` | `c42e851b-77e9-474b-88d5-40f0357166c3` | `bcb546ea-23db-498f-90a1-feb2e52abb22` | `217f6ea2-f7a8-4c1f-8b97-3efc9db4462e` |

## `run_events` 근거 (`codeRunId` 기준)

| 기준 runId | eventType | payload | createdAt (KST) |
|---|---|---|---|
| `3f5581d1-0c7e-45cb-84b6-bd6cb2cdaa36` | `CHAIN_CODE_DONE` | `codeRunId=c42e851b-77e9-474b-88d5-40f0357166c3` | `2026-02-20 12:50:23` |
| `c42e851b-77e9-474b-88d5-40f0357166c3` | `CHAIN_DOC_TRIGGERED` | `files=2` | `2026-02-20 12:49:37` |
| `c42e851b-77e9-474b-88d5-40f0357166c3` | `CHAIN_DOC_DONE` | `docRunId=bcb546ea-23db-498f-90a1-feb2e52abb22` | `2026-02-20 12:49:57` |
| `c42e851b-77e9-474b-88d5-40f0357166c3` | `CHAIN_REVIEW_TRIGGERED` | `files=2` | `2026-02-20 12:49:57` |
| `c42e851b-77e9-474b-88d5-40f0357166c3` | `CHAIN_REVIEW_DONE` | `reviewRunId=217f6ea2-f7a8-4c1f-8b97-3efc9db4462e` | `2026-02-20 12:50:23` |

## before/after 비교 (실행량, chainRuns, freshness)

| 항목 | before | after | delta |
|---|---:|---:|---:|
| 최근 7일 CODE run 수 | 2 | 4 | +2 |
| 최근 7일 SPEC run 수 | 1 | 2 | +1 |
| 최근 7일 DOC run 수 | 1 | 2 | +1 |
| 최근 7일 REVIEW run 수 | 1 | 2 | +1 |
| 최근 7일 CHAIN_CODE_DONE | 1 | 2 | +1 |
| 최근 7일 CHAIN_DOC_DONE | 1 | 2 | +1 |
| 최근 7일 CHAIN_REVIEW_DONE | 1 | 2 | +1 |
| 48시간 fresh CODE run 수 | 2 | 4 | +2 |
| 48시간 fresh CHAIN_DOC_DONE | 1 | 2 | +1 |
| 48시간 fresh CHAIN_REVIEW_DONE | 1 | 2 | +1 |

## 최신 14일 게이트 4개 PASS/FAIL

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

- 최신 14일 누적 `parseEligibleRunCount`: `CODE 8`, `SPEC 3`, `DOC 2`, `REVIEW 2`, `전체 15`

## 최근 7일/직전 7일 비교 (`executionGapDelta`, `chainShareGapDelta`)

| 구분 | executionGap(최근7일) | executionGap(직전7일) | executionGapDelta | chainShareGap(최근7일) | chainShareGap(직전7일) | chainShareGapDelta |
|---|---:|---:|---:|---:|---:|---:|
| CODE | 108 | 108 | 0 | -25.00%p | 25.00%p | -50.00%p |
| SPEC | 26 | 27 | -1 | 0.00%p | 0.00%p | 0.00%p |
| DOC | 40 | 42 | -2 | 0.00%p | 100.00%p | -100.00%p |
| REVIEW | 40 | 42 | -2 | 0.00%p | 100.00%p | -100.00%p |
| 전체 | 214 | 219 | -5 | -10.00%p | 50.00%p | -60.00%p |

- 최근 7일 `DOC/REVIEW actualChainRuns`: `2/2`
- 최근 7일 `dailyCompliance`: `1/7` PASS (`weeklyComplianceRate=0.14`)
- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `3.3333` (기준 `>= 32` 미충족)

## 단일 판정 및 근거
- `resumeDecision`: **`KEEP_FROZEN`**
- 판정 근거:
  - 시딩 실행으로 fresh runId/체인 이벤트 증거와 gap 개선 신호(`executionGapDelta=-5`, `chainShareGapDelta=-60.00%p`)는 확보되었습니다.
  - 그러나 게이트 4개 중 `INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS` 2개가 계속 미충족(`1.00`, `0`)입니다.
  - 따라서 H-024 재개(`RESUME_H024`) 근거는 아직 부족하며 `KEEP_FROZEN` 유지가 필요합니다.
- `unmetReadinessSignals`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`1.00 > 0.50`)
  - `SUFFICIENT_DAYS` (`0 < 7`)
- `nextCheckTrigger`:
  - 필수 충족 조건: `집계 성공 >= 10`, `INSUFFICIENT_SAMPLE <= 0.50`, `집계 불가 < 3`, `샘플 충분 일수 >= 7`
  - 다음 점검 시점: `2026-02-21 09:00 KST`
  - 우선 액션: 시딩 스크립트 반복 실행으로 `parseEligibleRunCount >= 20` 일수 누적

## 테스트 명령/결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최신 14일 `INSUFFICIENT_SAMPLE` 비율이 여전히 `1.00`이며 `샘플 충분 일수=0`이라 재개 게이트 2종 미충족 상태가 지속됩니다.
- 시딩 1회 배치만으로는 최근 3일 평균 모수(`3.3333`)가 목표(`>=32`) 대비 크게 부족합니다.
- `KEEP_FROZEN` 상태를 유지하는 동안 run-state 데이터 누적이 멈추면 개선 신호가 다시 stale로 전환될 수 있습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `scripts/seed-fallback-warning-workload.sh`가 runId 매핑 규칙(`specRunId -> CHAIN_CODE_DONE -> codeRunId -> CHAIN_DOC/REVIEW_DONE`)을 `jq` 없이 재현 가능하게 구현했는지
2. `docs/cli-quickstart.md`, `docs/code-agent-api.md`가 H-035 원칙(데이터 생성 우선, 계약 필드 신규 추가 금지)으로 동기화됐는지
3. 결과 보고의 수치(`executionGapDelta=-5`, `chainShareGapDelta=-60.00%p`, 게이트 2개 미충족, `resumeDecision=KEEP_FROZEN`)가 DB 실측 및 릴레이와 일치하는지
