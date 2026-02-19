# H-022 결과 보고서 (fallback warning 실행량 회복 액션 플랜 수립/운영 점검)

## 상태
- 현재 상태: **완료 (운영 문서/야간 템플릿 H-022 계약 동기화 + 테스트 게이트 통과)**
- 실행일(KST): `2026-02-19`
- 점검 구간(KST):
  - 최신 14일: `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
  - 최근 7일: `2026-02-13 ~ 2026-02-19` (`today-6 ~ today`)

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `coordination/REPORTS/H-022-result.md`
- `coordination/RELAYS/H-022-executor-to-review.md`

## 구현 요약
- `docs/code-agent-api.md`에 H-022 섹션을 추가해 `executionRecoveryPlan`/`executionRecoveryProgress` 출력 계약과 산식(`targetTotalRuns`, `actualTotalRuns`, `executionGap`, `chainShareGap`)을 고정했습니다.
- 최신 14일 게이트 판정과 최근 7일 목표-실적 gap 표를 동일 문맥으로 배치해 `READY/HOLD` 판정 근거를 구조화했습니다.
- `LOW_TRAFFIC`(총량 gap 중심)와 `CHAIN_COVERAGE_GAP`(체인 비중 gap + `DOC/REVIEW chainRuns`) 분리 판정 규칙 및 `HOLD` 시 일일 우선 액션(직접/체인 증량, 점검 시각/담당)을 문서화했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동기화해 기존 출력(`executionMix`, `agentExecution`, `overallExecutionRate`, `recalibrationReadiness`)을 유지하면서 신규 필드(`executionRecoveryPlan`, `executionRecoveryProgress`)를 필수 출력으로 추가했습니다.
- 실측 수치(14일/7일)는 handoff 입력 기준선(`coordination/REPORTS/H-021-result.md`)과 동일 계약으로 정렬했습니다.

## 최신 14일 게이트 4개 실측 + PASS/FAIL

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

## 최근 7일 agent별 목표-실적(`targetDirectRuns`, `targetChainRuns`, `directRuns`, `chainRuns`, `executionGap`, `chainShareGap`)

| 구분 | targetDirectRuns(7d) | targetChainRuns(7d) | directRuns(7d) | chainRuns(7d) | targetTotalRuns(7d) | actualTotalRuns(7d) | executionGap(7d) | targetChainShare(7d) | actualChainShare(7d) | chainShareGap(7d) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| CODE | 84 | 28 | 1 | 0 | 112 | 1 | 111 | 25.00% | 0.00% | 25.00%p |
| SPEC | 28 | 0 | 0 | 0 | 28 | 0 | 28 | 0.00% | 0.00% | 0.00%p |
| DOC | 0 | 42 | 0 | 0 | 42 | 0 | 42 | 100.00% | 0.00% | 100.00%p |
| REVIEW | 0 | 42 | 0 | 0 | 42 | 0 | 42 | 100.00% | 0.00% | 100.00%p |
| 전체 | 112 | 112 | 1 | 0 | 224 | 1 | 223 | 50.00% | 0.00% | 50.00%p |

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
- `LOW_TRAFFIC` 근거:
  - 전체 `executionGap(7d)=223`
  - 최근 3일 평균 `parseEligibleRunCount=0.0000` (기준 `>= 32` 미충족)
  - 최근 3일 평균 `overallExecutionRate=0.0000`
- `CHAIN_COVERAGE_GAP` 근거:
  - `DOC`: `chainRuns(7d)=0`, `chainShareGap(7d)=100.00%p`
  - `REVIEW`: `chainRuns(7d)=0`, `chainShareGap(7d)=100.00%p`

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
  - 최신 14일 게이트 4개 중 2개 미충족 상태가 지속됩니다.
  - 전체 `executionGap` 대규모 미달(`223`)로 실행량 회복 신호가 부재합니다.
  - `DOC/REVIEW` 체인 실행 부재(`chainRuns=0`)와 높은 `chainShareGap`로 체인 커버리지 결손이 지속됩니다.

## `HOLD` 시 원인 분류/보완 액션 우선순위
1. `LOW_TRAFFIC` (우선순위 1)
   - 근거: `executionGap(7d)=223`, 최근 3일 평균 `overallExecutionRate=0.0000`
   - 액션: `CODE`/`SPEC` 직접 호출을 일일 목표(`CODE 12`, `SPEC 4`)까지 우선 증량
   - 점검: `09:00 KST`(야간 리포트) + `17:00 KST`(중간 수동 점검), 담당 `운영 온콜`
2. `CHAIN_COVERAGE_GAP` (우선순위 2)
   - 근거: `DOC`/`REVIEW chainRuns(7d)=0`, `chainShareGap(7d)=100.00%p`
   - 액션: `chainToDoc=true`, `chainToReview=true` 실행을 일일 목표(`DOC 6`, `REVIEW 6`)까지 증량
   - 점검: `executionRecoveryProgress.actualChainRuns`/`chainShareGap` 일일 추적 고정
3. `COLLECTION_FAILURE` (우선순위 3)
   - 근거: 최신 14일 `집계 불가 0일`
   - 액션: 현행 집계 모니터링 유지, 오류 발생 시 당일 원인 분류/복구

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최신 14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0일` 상태가 유지되어 재보정 착수 지연 리스크가 큽니다.
- 최근 7일 `DOC/REVIEW` 체인 실행이 `0건`으로 고정돼 체인 커버리지 결손이 장기화될 수 있습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에서 `executionRecoveryPlan`/`executionRecoveryProgress` 키와 산식이 동일한지
2. `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 분리 판정이 `executionGap`/`chainShareGap`/`DOC-REVIEW chainRuns` 근거와 일치하는지
3. 유지 원칙(임계치/알림 룰 수치, `INSUFFICIENT_SAMPLE` 제외 규칙, 이벤트/모수 정의 불변)이 보존됐는지
