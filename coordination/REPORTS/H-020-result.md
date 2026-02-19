# H-020 결과 보고서 (fallback warning 샘플 확보 실행률 추적 정합화)

## 상태
- 현재 상태: **완료 (운영 문서/야간 템플릿 실행률 계약 동기화 + 테스트 게이트 통과)**
- 실행일(KST): `2026-02-19`
- 점검 구간(KST):
  - 최신 14일: `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
  - 최근 7일: `2026-02-13 ~ 2026-02-19`

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `coordination/REPORTS/H-020-result.md`
- `coordination/RELAYS/H-020-executor-to-review.md`

## 구현 요약
- `docs/code-agent-api.md`에 H-020 섹션을 추가해 최근 7일 실행률 추적(`achievementRate`, `overallExecutionRate`)과 최신 14일 게이트 판정을 함께 유지하도록 정렬했습니다.
- 최근 7일 일자별 실행량/달성률 표와 `agentExecution`(target/actual/achievementRate) 누적 요약을 문서에 고정했습니다.
- `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 근거를 실행률 지표(최근 3일 평균 전체 모수/전체 실행률, 최근 7일 DOC/REVIEW 실행률)로 명시했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화해 `READY/HOLD`, `unmetGates`, `agentExecution`, `overallExecutionRate`를 필수 출력으로 고정했습니다.
- 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙은 유지했습니다.

## 최근 14일 게이트 4개 실측 + PASS/FAIL

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

## 최근 7일 agent별 일일 목표 대비 실제 실행량/달성률

| 일자(KST) | CODE (`actual/16`) | SPEC (`actual/4`) | DOC (`actual/6`) | REVIEW (`actual/6`) | `totalActualRuns` | `overallExecutionRate` |
|---|---:|---:|---:|---:|---:|---:|
| `2026-02-13` | `1/16 (6.25%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 1 | 3.13% |
| `2026-02-14` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-15` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-16` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-17` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-18` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-19` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |

## `overallExecutionRate` 추세 + `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 근거
- 최근 7일 `overallExecutionRate` 추세:
  - `2026-02-13`: `3.13%`
  - `2026-02-14 ~ 2026-02-19`: `0.00%` 유지
- 최근 7일 누적 `overallExecutionRate`(목표합 224 대비): `0.45%` (`1/224`)
- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `0.0000` (기준 `>= 32` 미충족)
- 최근 3일 평균 `overallExecutionRate`: `0.0000`
- 최근 7일 `DOC`/`REVIEW` 실행률:
  - `DOC`: `0/42` (`0.00%`)
  - `REVIEW`: `0/42` (`0.00%`)
- 해석:
  - 실행량 절대치가 일일 목표 대비 극히 낮아 `LOW_TRAFFIC`가 지속됩니다.
  - 체인 기반 실행(Doc/Review) 부재가 `CHAIN_COVERAGE_GAP`로 고착되어 있습니다.

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
  - 최신 14일 게이트 4개 중 2개 미충족 상태가 유지되고 있습니다.
  - 최근 7일 실행률과 최근 3일 평균 실행률 모두 저조해 착수 전환 신호가 없습니다.

## `HOLD` 원인 분류/보완 액션 우선순위
1. `LOW_TRAFFIC` (우선순위 1)
   - 근거: 최근 3일 평균 전체 모수 `0.0000`, 평균 `overallExecutionRate` `0.0000`
   - 액션: CODE/SPEC 직접 호출 및 체인 호출 실행량을 일일 목표(`16/4`)까지 우선 증량
2. `CHAIN_COVERAGE_GAP` (우선순위 2)
   - 근거: 최근 7일 `DOC`/`REVIEW` 실행률 `0.00%`
   - 액션: `chainToDoc=true`, `chainToReview=true` 비중 상향 + `DOC/REVIEW` 목표(`6/6`) 일일 추적 고정
3. `COLLECTION_FAILURE` (우선순위 3)
   - 근거: 최신 14일 `집계 불가 0일`
   - 액션: 현행 조회/파싱 실패 감시 규칙 유지

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최신 14일 기준 샘플 충분 일수(`parseEligibleRunCount >= 20`)가 `0일`로 유지되어 재보정 착수 지연 리스크가 지속됩니다.
- 최근 7일 `DOC`/`REVIEW` 실행이 0건이라 체인 기반 모수 균형 복구가 아직 시작되지 않았습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 H-020 섹션이 최근 7일 실행률 표 + 최신 14일 게이트 판정을 함께 충족하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 출력 계약에 `agentExecution`/`overallExecutionRate`/`unmetGates`가 동일 반영됐는지
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지
