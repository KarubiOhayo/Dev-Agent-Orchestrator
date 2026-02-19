# H-016 결과 보고서 (fallback warning 임계치/알림 룰 실측 기반 보정 실행)

## 상태
- 현재 상태: **완료 (보정 보류 판정 + 테스트 게이트 통과)**

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `coordination/REPORTS/H-016-result.md`
- `coordination/RELAYS/H-016-executor-to-review.md`

## 구현 요약
- `docs/code-agent-api.md`에 `실측 보정 실행 결과 (H-016)` 섹션을 추가했습니다.
  - 실측 구간을 `2026-02-06 ~ 2026-02-19`(KST, 14일)로 고정했습니다.
  - 데이터 소스(`storage/devagent.db`)와 대상 이벤트(`CODE/SPEC/DOC/REVIEW_OUTPUT_FALLBACK_WARNING`)를 명시했습니다.
  - 가용성 게이트 실측값(집계 성공/집계 불가/샘플 부족 비율)과 최종 보정 판정을 문서에 고정했습니다.
  - 보정 보류 시 임계치/알림 룰 수치 유지 원칙과 전/후 비교 생략 조건을 명시했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 H-016 실행 기준으로 정합화했습니다.
  - 최근 14일 게이트 판정 기준(성공일수/샘플 부족 비율/집계 불가 일수)을 `진행 조건`으로 명시했습니다.
  - 게이트 미충족 시 `보정 보류 + 수치 유지` 규칙을 명시했습니다.
  - 게이트 충족 시 후보값 제시 및 적용 전/후 비교 항목(등급 분포/알림 트리거/영향 요약)을 추가했습니다.

## 최근 14일 집계 요약 (KST 2026-02-06 ~ 2026-02-19)

### 가용성 게이트 판정

| 항목 | 실측값 | 기준 | 판정 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |

- 최종 판정: **보정 보류**
- 미충족 게이트:
  - `INSUFFICIENT_SAMPLE` 비율 `1.00 > 0.50`

### agent별/전체 14일 누적

| 구분 | parseEligibleRunCount(14d) | warningEventCount(14d) | warningRate(14d) | 샘플 충분 일수(>=20) |
|---|---:|---:|---:|---:|
| CODE | 4 | 0 | 0.0000 | 0 |
| SPEC | 1 | 0 | 0.0000 | 0 |
| DOC | 0 | 0 | N/A | 0 |
| REVIEW | 0 | 0 | N/A | 0 |
| 전체 | 5 | 0 | 0.0000 | 0 |

### 집계 불가 원인 분류
- 해당 없음 (`0일`)

## 보정 진행/보류 판정 및 근거
- 판정: **보류**
- 근거:
  - 최근 14일 전체에서 샘플 충분(`parseEligibleRunCount >= 20`) 일수가 `0일`이라 후보값 검증에 필요한 최소 표본이 확보되지 않았습니다.
  - `INSUFFICIENT_SAMPLE` 비율이 `1.00`으로 게이트 기준(`<= 0.50`)을 초과했습니다.

## 임계치/알림 룰 후보값 또는 유지 사유
- 보정 보류로 후보값 제시는 수행하지 않았습니다.
- 임계치/알림 룰 수치는 기존값을 유지했습니다.
  - 임계치: `0.05`, `0.15`
  - 알림 룰: 급증 기준 `+0.10p`, 전체 보호 기준 `0.10`

## 적용 전/후 영향 비교
- 보정 보류로 적용 전/후 비교는 **미실시**입니다.
- 사유: 게이트 미충족(`INSUFFICIENT_SAMPLE` 비율 초과)으로 후보값 산정 자체를 진행하지 않음.

## 수용기준 점검
1. `docs/code-agent-api.md`에 H-016 보정 실행 결과(14일 요약 + 진행/보류 판정 + 보류 근거) 명시: **충족**
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 진행/보류 판정 및 후속 보고 항목(진행 시 전/후 비교, 보류 시 사유) 반영: **충족**
3. 게이트 미충족 시 임계치/알림 수치 유지 및 미충족 근거 수치 보고: **충족**
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경 없음: **충족**
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최근 14일 표본이 거의 없어(`parseEligibleRunCount` 총 5) 임계치/알림 룰 보정 의사결정이 반복 보류될 수 있습니다.
- Doc/Review run 부재 상태가 지속되면 agent 간 경고율 분포 비교가 불가능해 보정 후보의 균형 검증이 제한됩니다.

## 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 추가 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 H-016 실측값(14일 가용성 게이트, agent별/전체 누적, 보정 보류 근거)이 DB 집계와 일치하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에서 진행/보류 분기와 보고 형식(전/후 비교 vs 보류 사유)이 누락 없이 반영됐는지
3. 임계치/알림 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지
