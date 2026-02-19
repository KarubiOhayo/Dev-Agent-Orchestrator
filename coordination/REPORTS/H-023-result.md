# H-023 결과 보고서 (fallback warning 실행량 회복 액션 이행률 추적/검증)

## 상태
- 현재 상태: **완료 (운영 문서/야간 템플릿 H-023 계약 동기화 + 테스트 게이트 통과)**
- 실행일(KST): `2026-02-19`
- 점검 구간(KST):
  - 최신 14일: `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
  - 최근 7일: `2026-02-13 ~ 2026-02-19` (`today-6 ~ today`)
  - 직전 7일: `2026-02-06 ~ 2026-02-12` (`today-13 ~ today-7`)

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `coordination/REPORTS/H-023-result.md`
- `coordination/RELAYS/H-023-executor-to-review.md`

## 구현 요약
- `docs/code-agent-api.md`에 H-023 섹션을 추가해 기존 `executionRecoveryPlan`/`executionRecoveryProgress` 계약은 유지하고, `executionRecoveryTrend`/`recoveryActionStatus`를 신규 고정 출력으로 반영했습니다.
- delta 산식(`executionGapDelta`, `chainShareGapDelta`)과 해석 규칙(개선: `< 0`, 미개선: `>= 0`)을 문서에 명시했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화해 H-023 점검 문구, 필수 출력 항목, `HOLD` 시 상태 필드(`IN_PROGRESS|BLOCKED|DONE`)와 근거(runId/집계표) 규칙을 반영했습니다.
- 실측은 `storage/devagent.db`의 KST 기준 집계로 확인했으며, 최근 7일 대비 직전 7일에서 `executionGap`은 악화(`+3`), `chainShareGap`은 정체(`0.00%p`)로 확인됐습니다.

## 최신 14일 게이트 4개 실측 + PASS/FAIL

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

## 최근 7일 agent별 목표-실적(`target*`, `actual*`, `executionGap`, `chainShareGap`)

| 구분 | targetDirectRuns(7d) | targetChainRuns(7d) | targetTotalRuns(7d) | targetChainShare(7d) | actualDirectRuns(7d) | actualChainRuns(7d) | actualTotalRuns(7d) | actualChainShare(7d) | executionGap(7d) | chainShareGap(7d) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| CODE | 84 | 28 | 112 | 25.00% | 1 | 0 | 1 | 0.00% | 111 | 25.00%p |
| SPEC | 28 | 0 | 28 | 0.00% | 0 | 0 | 0 | 0.00% | 28 | 0.00%p |
| DOC | 0 | 42 | 42 | 100.00% | 0 | 0 | 0 | 0.00% | 42 | 100.00%p |
| REVIEW | 0 | 42 | 42 | 100.00% | 0 | 0 | 0 | 0.00% | 42 | 100.00%p |
| 전체 | 112 | 112 | 224 | 50.00% | 1 | 0 | 1 | 0.00% | 223 | 50.00%p |

## 최근 7일 vs 직전 7일 `executionGapDelta`/`chainShareGapDelta`

| 구분 | executionGap(최근7일) | executionGap(직전7일) | executionGapDelta | chainShareGap(최근7일) | chainShareGap(직전7일) | chainShareGapDelta | 해석 |
|---|---:|---:|---:|---:|---:|---:|---|
| CODE | 111 | 109 | +2 | 25.00%p | 25.00%p | 0.00%p | 미개선 |
| SPEC | 28 | 27 | +1 | 0.00%p | 0.00%p | 0.00%p | 미개선 |
| DOC | 42 | 42 | 0 | 100.00%p | 100.00%p | 0.00%p | 미개선 |
| REVIEW | 42 | 42 | 0 | 100.00%p | 100.00%p | 0.00%p | 미개선 |
| 전체 | 223 | 220 | +3 | 50.00%p | 50.00%p | 0.00%p | 미개선 |

- 해석 규칙:
  - `executionGapDelta < 0` 또는 `chainShareGapDelta < 0`면 개선
  - 두 delta가 모두 `>= 0`이면 미개선

## `overallExecutionRate` 추세 + `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 근거
- 최근 7일 일자별 전체 `overallExecutionRate`:
  - `2026-02-13`: `3.13%`
  - `2026-02-14`: `0.00%`
  - `2026-02-15`: `0.00%`
  - `2026-02-16`: `0.00%`
  - `2026-02-17`: `0.00%`
  - `2026-02-18`: `0.00%`
  - `2026-02-19`: `0.00%`
- 누적 비교:
  - 최근 7일: `0.45%` (`1/224`)
  - 직전 7일: `1.79%` (`4/224`)
- `LOW_TRAFFIC` 근거:
  - 전체 `executionGapDelta=+3` (악화)
  - 최근 3일 평균 `parseEligibleRunCount=0.0000` (기준 `>= 32` 미충족)
  - 근거 runId(최근 7일): `ca487d6f-fa8c-4935-8781-ebe0048abb50`
- `CHAIN_COVERAGE_GAP` 근거:
  - 최근 7일 `DOC/REVIEW actualChainRuns=0`
  - `DOC/REVIEW chainShareGapDelta=0.00%p` (개선 없음)

## Projection 재산정 결과
- `requiredSufficientDays = max(0, insufficientDays - 7) = max(0, 14 - 7) = 7일`
- `예상 착수 가능일`: **미산정**
  - 미산정 사유: 최근 3일 평균 전체 모수 전제조건(`>= 32`) 미충족
  - 참고(조건부 최소값): `2026-02-26` (`2026-02-19 + 7일`)

## `READY/HOLD` 최종 판정 및 근거
- `recalibrationReadiness`: **HOLD**
- `unmetGates`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`1.00 > 0.50`)
  - `SUFFICIENT_DAYS` (`0 < 7`)
- 판정 근거:
  - 최신 14일 게이트 4개 중 2개 미충족이 지속됩니다.
  - 최근 7일 vs 직전 7일 delta에서 개선 신호(`< 0`)가 없습니다.

## `HOLD` 시 원인 분류/보완 액션 우선순위 + 상태

| cause | priority | status | evidence |
|---|---:|---|---|
| `LOW_TRAFFIC` | 1 | `BLOCKED` | `executionGapDelta=+3`, 최근 3일 평균 `parseEligibleRunCount=0.0000`, runId `ca487d6f-fa8c-4935-8781-ebe0048abb50` |
| `CHAIN_COVERAGE_GAP` | 2 | `BLOCKED` | 최근 7일 `DOC/REVIEW actualChainRuns=0`, `chainShareGapDelta=0.00%p` |
| `COLLECTION_FAILURE` | 3 | `DONE` | 최신 14일 `집계 불가 0일`, 집계 성공 14일 |

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최신 14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0일`이 유지되어 재보정 착수 지연 리스크가 큽니다.
- 최근 7일 대비 직전 7일 `executionGapDelta=+3`으로 실행량 회복 추세가 악화되었습니다.
- `DOC/REVIEW` 체인 실행 부재(`actualChainRuns=0`)로 `CHAIN_COVERAGE_GAP` 해소 근거가 부족합니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `executionRecoveryTrend`/`recoveryActionStatus` 출력 계약이 동일하게 반영됐는지
2. delta 산식(`executionGapDelta`, `chainShareGapDelta`) 및 개선/미개선 해석 규칙이 두 문서에서 일치하는지
3. `recoveryActionStatus`의 상태(`IN_PROGRESS|BLOCKED|DONE`)와 근거(runId/집계표)가 `HOLD` 판정 근거와 정합한지
