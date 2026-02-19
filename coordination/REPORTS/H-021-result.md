# H-021 결과 보고서 (fallback warning 실행량 증대 검증용 호출 믹스 추적)

## 상태
- 현재 상태: **완료 (운영 문서/야간 템플릿 호출 믹스 계약 동기화 + 테스트 게이트 통과)**
- 실행일(KST): `2026-02-19`
- 점검 구간(KST):
  - 최신 14일: `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
  - 최근 7일: `2026-02-13 ~ 2026-02-19` (`today-6 ~ today`)

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `coordination/REPORTS/H-021-result.md`
- `coordination/RELAYS/H-021-executor-to-review.md`

## 구현 요약
- `docs/code-agent-api.md`에 H-021 호출 믹스 추적 섹션을 추가해 최근 7일 `directRuns`/`chainRuns`/`totalActualRuns`/`chainShare`와 최신 14일 게이트 판정을 동일 문맥으로 고정했습니다.
- H-021 산식(`totalActualRuns`, `chainShare`, `achievementRate`, `overallExecutionRate`)을 문서에 명시하고 `totalActualRuns=0`일 때 `chainShare=0` 처리 규칙을 고정했습니다.
- `LOW_TRAFFIC` 근거(최근 3일 평균 모수/실행률)와 `CHAIN_COVERAGE_GAP` 근거(`DOC`/`REVIEW` `chainRuns`·`chainShare`)를 분리 표기로 고정했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화해 `executionMix` 출력(`directRuns`, `chainRuns`, `totalActualRuns`, `chainShare`)과 HOLD 원인 분류 기준(실행률 + 호출 믹스)을 반영했습니다.

## 최신 14일 게이트 4개 실측 + PASS/FAIL

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

## 최근 7일 agent별 호출 믹스 (`directRuns`/`chainRuns`/`chainShare`)

| 구분 | targetRuns(7d) | directRuns(7d) | chainRuns(7d) | totalActualRuns(7d) | chainShare(7d) | achievementRate(7d) |
|---|---:|---:|---:|---:|---:|---:|
| CODE | 112 | 1 | 0 | 1 | 0.00% | 0.89% |
| SPEC | 28 | 0 | 0 | 0 | 0.00% | 0.00% |
| DOC | 42 | 0 | 0 | 0 | 0.00% | 0.00% |
| REVIEW | 42 | 0 | 0 | 0 | 0.00% | 0.00% |
| 전체 | 224 | 1 | 0 | 1 | 0.00% | 0.45% |

## `overallExecutionRate` 추세 + `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 근거
- 최근 7일 일자별 전체 `overallExecutionRate`:
  - `2026-02-13`: `3.13%`
  - `2026-02-14`: `0.00%`
  - `2026-02-15`: `0.00%`
  - `2026-02-16`: `0.00%`
  - `2026-02-17`: `0.00%`
  - `2026-02-18`: `0.00%`
  - `2026-02-19`: `0.00%`
- 최근 7일 누적 `overallExecutionRate`: `0.45%` (`1/224`)
- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `0.0000` (기준 `>= 32` 미충족)
- 최근 3일 평균 `overallExecutionRate`: `0.0000`
- `DOC`/`REVIEW` 체인 커버리지 지표(최근 7일):
  - `DOC`: `chainRuns=0`, `chainShare=0.00%`
  - `REVIEW`: `chainRuns=0`, `chainShare=0.00%`
- 해석:
  - 실행 총량이 목표 대비 매우 낮아 `LOW_TRAFFIC`가 지속됩니다.
  - `DOC`/`REVIEW` 체인 유입이 0건으로 고정되어 `CHAIN_COVERAGE_GAP`가 지속됩니다.

## Projection 재산정 결과
- `requiredSufficientDays = max(0, insufficientDays - 7) = max(0, 14 - 7) = 7일`
- `예상 재보정 착수 가능일`: **미산정**
  - 미산정 사유: 최근 3일 평균 전체 모수 전제조건(`>= 32`) 미충족
  - 참고(조건부 최소값): `2026-02-26` (`2026-02-19 + 7일`)

## `READY/HOLD` 최종 판정 및 근거
- `recalibrationReadiness`: **HOLD**
- `unmetGates`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`1.00 > 0.50`)
  - `SUFFICIENT_DAYS` (`0 < 7`)
- 판정 근거:
  - 최신 14일 게이트 4개 중 2개 미충족 상태가 지속됩니다.
  - 최근 7일 호출 믹스에서 `DOC`/`REVIEW` 체인 유입이 없어 호출 믹스 기반 `CHAIN_COVERAGE_GAP`이 해소되지 않았습니다.

## `HOLD` 원인 분류/보완 액션 우선순위
1. `LOW_TRAFFIC` (우선순위 1)
   - 근거: 최근 3일 평균 전체 모수 `0.0000`, 최근 3일 평균 `overallExecutionRate` `0.0000`
   - 액션: CODE/SPEC 직접 호출 + 체인 호출 총량을 일일 목표(`16/4`)까지 우선 증량
2. `CHAIN_COVERAGE_GAP` (우선순위 2)
   - 근거: 최근 7일 `DOC`/`REVIEW` `chainRuns=0`, `chainShare=0.00%`
   - 액션: `chainToDoc=true`, `chainToReview=true` 비중 상향 + `DOC/REVIEW` 목표(`6/6`) 일일 추적 고정
3. `COLLECTION_FAILURE` (우선순위 3)
   - 근거: 최신 14일 `집계 불가 0일`
   - 액션: 현행 조회/파싱 실패 감시 규칙 유지

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최신 14일 기준 샘플 충분 일수(`parseEligibleRunCount >= 20`)가 `0일`로 유지되어 재보정 착수 지연 리스크가 지속됩니다.
- 최근 7일 `DOC`/`REVIEW`의 직접/체인 실행 모두 0건이라 호출 믹스 복구 신호가 아직 없습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 H-021 섹션에서 호출 믹스 산식(`totalActualRuns`, `chainShare`, `achievementRate`, `overallExecutionRate`)과 분모 0 처리 규칙이 명시되어 있는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `executionMix` 출력 계약(`directRuns`, `chainRuns`, `totalActualRuns`, `chainShare`)과 HOLD 원인 기준이 동일하게 반영됐는지
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙이 유지됐는지
