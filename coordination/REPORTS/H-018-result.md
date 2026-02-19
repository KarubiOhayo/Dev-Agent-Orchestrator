# H-018 결과 보고서 (fallback warning 샘플 확보 계획 운영 적용 점검)

## 상태
- 현재 상태: **완료 (운영 적용 점검 반영 + 테스트 게이트 통과)**

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `coordination/REPORTS/H-018-result.md`
- `coordination/RELAYS/H-018-executor-to-review.md`

## 구현 요약
- `docs/code-agent-api.md`에 `H-018 운영 적용 점검 결과` 섹션을 추가했습니다.
  - 최근 14일(KST `2026-02-06 ~ 2026-02-19`) 실측값(게이트 4개 + agent별 모수/경고율)을 고정했습니다.
  - H-017 목표 대비 진행률/미달률 표를 추가했습니다.
  - Projection 대비 실측 오차(`deltaSufficientDays`, `deltaInsufficientRatio`, `deltaStartDate`)와 허용 기준 초과 여부를 명시했습니다.
  - 재보정 `보류` 판정과 수치 근거, 원인 분류(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`, `COLLECTION_FAILURE`) 및 우선순위 액션을 문서화했습니다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙 불변을 재확인했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 H-018 운영 점검 기준으로 갱신했습니다.
  - Projection 오차 출력 항목 3종(`deltaSufficientDays`, `deltaInsufficientRatio`, `deltaStartDate`)을 추가했습니다.
  - 오차 허용 기준 초과 시 자동 `보정 보류 + 원인 분류 + 우선순위 액션` 출력 규칙을 추가했습니다.
  - 게이트 충족 시 “임계치 후보 산정 라운드 제안” 문구를 출력하도록 정리했습니다.

## 최근 14일 실측 요약 (KST `2026-02-06 ~ 2026-02-19`)

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

| 구분 | parseEligibleRunCount(14d) | warningEventCount(14d) | warningRate(14d) |
|---|---:|---:|---:|
| CODE | 4 | 0 | 0.0000 |
| SPEC | 1 | 0 | 0.0000 |
| DOC | 0 | 0 | N/A |
| REVIEW | 0 | 0 | N/A |
| 전체 | 5 | 0 | 0.0000 |

- 실측 모수 발생일: `2026-02-11`(전체 4), `2026-02-13`(전체 1)
- 최근 3일 평균 전체 모수: `0.0000` (목표 `>= 32` 미충족)

## 목표 대비 진행률/미달률 (H-017 목표 기준)

| 항목 | 실측 | 목표 | 달성률 | 미달률/갭 |
|---|---:|---:|---:|---:|
| 집계 성공 일수 | 14일 | >= 10일 | 140% | 0일 |
| `INSUFFICIENT_SAMPLE` 비율 | 1.00 | <= 0.50 | 0% | +0.50 |
| `집계 불가` 일수 | 0일 | < 3일 | 100% | 0일 |
| 샘플 충분 일수(`>=20`) | 0일 | >= 7일 | 0% | -7일 |

## Projection 대비 실측 오차 분석

| 오차 항목 | H-017 예상(기준) | H-018 실측 | Delta | 허용 기준 | 판정 |
|---|---|---|---:|---|---|
| `deltaSufficientDays` | 7일(필요 최소) | 0일 | -7일 | 절대오차 2일 | 초과 |
| `deltaInsufficientRatio` | 0.50(상한) | 1.00 | +0.50 | 절대오차 0.10 | 초과 |
| `deltaStartDate` | `2026-02-26`(조건부 최소 예상) | 미산정/보류 | N/A | 절대오차 2일 | 조건 미충족 |

- 종합 판정:
  - `|deltaSufficientDays|=7 > 2`, `|deltaInsufficientRatio|=0.50 > 0.10`으로 허용 기준 초과
  - `deltaStartDate`는 전제조건(최근 3일 평균 전체 모수 `>= 32`) 미충족으로 산정 불가

## 재보정 착수 가능/보류 판정 및 근거
- 최종 판정: **보정 보류**
- 근거:
  - 게이트 4개 중 2개(`INSUFFICIENT_SAMPLE` 비율, 샘플 충분 일수) 미충족
  - Projection 오차 허용 기준(일수/비율) 초과

## 보류 원인 분류 / 보완 액션 우선순위
1. `LOW_TRAFFIC`
   - 근거: 최근 3일 평균 전체 모수 `0.0000` (목표 `>= 32` 대비 -32.0000)
   - 보완 액션: Code 직접 호출/체인 호출 실행량을 일일 목표(`CODE 16`, `SPEC 4`, `DOC 6`, `REVIEW 6`)까지 증량
2. `CHAIN_COVERAGE_GAP`
   - 근거: 14일 누적 `DOC 0`, `REVIEW 0`, `SPEC 1`
   - 보완 액션: Spec->Code, Code->Doc/Review 체인 비중 상향 + agent별 일일 목표 달성 여부 일 단위 점검
3. `COLLECTION_FAILURE`
   - 근거: `집계 불가 0일`로 미발생
   - 보완 액션: 현행 조회/파싱 실패 감시 규칙 유지(발생 시 즉시 복구)

## 수용기준 점검
1. `docs/code-agent-api.md`에 H-018 실측 요약 + 진행률 + Projection 오차 + 착수 가능/보류 판정 명시: **충족**
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 Projection 오차/원인 분류/다음 액션 규칙 반영: **충족**
3. 게이트 미충족 보류 근거와 보완 액션 수치 기반 보고: **충족**
4. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙 유지: **충족**
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경 없음: **충족**
6. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최근 14일 데이터에서 샘플 충분 일수가 0일로 고정되어, 단기적으로 재보정 착수 판단이 계속 지연될 가능성이 큽니다.
- `DOC`/`REVIEW` 체인 모수 미확보 상태가 지속되면 agent 간 편차 분석 신뢰도가 낮게 유지됩니다.
- Projection의 착수일 산정 조건(최근 3일 평균 전체 모수 `>= 32`) 미충족이 지속되면 일정 기반 계획 수립이 어렵습니다.

## 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약 파일, 빌드 설정) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 H-018 섹션에 실측/진행률/Projection 오차/보류 판정이 수치 일관성 있게 반영됐는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 오차 3종 + 오차 초과 시 자동 보류 규칙이 누락 없이 반영됐는지
3. 임계치/알림 룰 수치 및 `INSUFFICIENT_SAMPLE` 제외 규칙, 이벤트/모수 정의가 변경되지 않았는지
