# H-017 결과 보고서 (fallback warning 보정 재착수용 샘플 확보 계획 수립)

## 상태
- 현재 상태: **완료 (샘플 확보 계획/추적 지표 반영 + 테스트 게이트 통과)**

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `coordination/REPORTS/H-017-result.md`
- `coordination/RELAYS/H-017-executor-to-review.md`

## 구현 요약
- `docs/code-agent-api.md`에 `H-017 샘플 확보 계획 (재보정 착수 준비)` 섹션을 추가했습니다.
  - H-016 기준선 수치(집계 성공/샘플 부족 비율/집계 불가/agent별 모수)를 고정했습니다.
  - 재보정 착수 목표(`집계 성공 >= 10`, `INSUFFICIENT_SAMPLE <= 0.50`, `집계 불가 < 3`, `샘플 충분 일수 >= 7`)를 명시했습니다.
  - 샘플 확보 실행안(직접 호출 + Spec->Code 체인 + Code->Doc/Review 체인)과 일일 최소 모수 목표를 정의했습니다.
  - 목표 대비 진행률 산식, 게이트 충족 예상치(Projection) 산식, 미충족 원인 분류를 명시했습니다.
  - 재보정 착수 가능/보류 분기 규칙과 다음 액션을 문서화했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 H-017 추적 관점으로 확장했습니다.
  - 기준선(H-016) 고정 출력, 14일 `parseEligibleRunCount` 추세(전체+agent), 목표 대비 진행률 출력 항목을 추가했습니다.
  - Projection(`requiredSufficientDays`, 예상 재보정 착수 가능일)과 지연 원인 분류(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`, `COLLECTION_FAILURE`)를 추가했습니다.
  - 재보정 착수 가능/보류 판정과 다음 액션 출력을 필수화했습니다.

## H-016 기준선 대비 H-017 정량 목표

| 항목 | H-016 기준선 | H-017 목표 |
|---|---:|---:|
| 집계 성공 일수(최근 14일) | 14일 | >= 10일 |
| `INSUFFICIENT_SAMPLE` 비율 | 1.00 | <= 0.50 |
| `집계 불가` 일수 | 0일 | < 3일 |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 |
| 누적 `parseEligibleRunCount` (CODE/SPEC/DOC/REVIEW/전체) | 4 / 1 / 0 / 0 / 5 | 일일 목표 기반 점진 확보 (`16 / 4 / 6 / 6 / 32`) |

## 목표 대비 진행률/예상치(Projection) 산출 방식
- 진행률 산식:
  - `집계 성공 진행률 = min(1, 집계성공일수 / 10)`
  - `샘플 부족 개선 진행률 = min(1, (1.00 - insuffRatio) / 0.50)`
  - `집계 불가 안정성 진행률 = min(1, (3 - 집계불가일수) / 3)`
- 예상치 산식:
  - `requiredSufficientDays = max(0, insufficientDays - 7)`
  - 최근 3일 평균 전체 `parseEligibleRunCount >= 32` 충족 시
    - `예상 재보정 착수 가능일 = 오늘(KST) + requiredSufficientDays`
- 지연 원인 분류:
  - `LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`, `COLLECTION_FAILURE`

## 재보정 착수 가능/보류 판정 규칙
- 착수 가능:
  - 최근 14일 기준 `집계 성공 >= 10` AND `INSUFFICIENT_SAMPLE <= 0.50` AND `집계 불가 < 3`
  - 다음 액션: 임계치/알림 룰 후보값 산정 + 적용 전/후 영향 비교 라운드 진행
- 보류:
  - 위 게이트 중 1개 이상 미충족
  - 다음 액션: 샘플 확보 시나리오 유지/확대 + 원인 분류 기반 보완
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않음
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙은 유지

## 수용기준 점검
1. `docs/code-agent-api.md`에 H-016 기준선 + H-017 정량 목표 + 분기 규칙 명시: **충족**
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 진행률/Projection/다음 액션 출력 반영: **충족**
3. 임계치/알림 룰 수치 및 `INSUFFICIENT_SAMPLE` 제외 규칙 유지: **충족**
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경 없음: **충족**
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 샘플 확보 실행안은 계획 수립 단계이며, 실제 트래픽 미확보 시 재보정 보류가 반복될 수 있습니다.
- Projection은 최근 3일 평균 모수 유지 가정(베스트 케이스)이므로 운영 변동 시 착수 시점이 지연될 수 있습니다.
- `DOC`/`REVIEW` 체인 실행량이 낮으면 agent 간 분포 기반 비교 신뢰도가 계속 제한될 수 있습니다.

## 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 추가 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 H-017 섹션에 기준선 수치, 정량 목표, 분기 규칙이 함께 명시되었는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 추세/진행률/Projection/다음 액션 출력 규칙이 누락 없이 반영되었는지
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지
