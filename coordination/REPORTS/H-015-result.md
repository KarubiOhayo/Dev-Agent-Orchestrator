# H-015 결과 보고서 (fallback warning 임계치/알림 룰 실측 보정 준비)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`

## 구현 요약
- `docs/code-agent-api.md`에 `실측 보정 준비 체크 (H-015)` 섹션을 추가했습니다.
  - 최근 14일(KST) 관측 구간 점검 절차를 명시했습니다.
  - 일별 상태를 `집계 성공`/`집계 불가`/`INSUFFICIENT_SAMPLE`로 분류하는 기준을 고정했습니다.
  - `집계 불가` 원인 분류와 보정 후보 수집 범위를 분리하도록 명시했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 보정 준비 관점으로 정합화했습니다.
  - 최근 14일의 집계 성공/실패/샘플 부족 일수 및 비율 산출을 필수 출력으로 추가했습니다.
  - `집계 불가` 원인 분류 출력과 임계치 보정 판단 보류 조건을 추가했습니다.
  - 임계치/알림 수치(`0.05`, `0.15`, `0.10`)를 변경하지 않도록 명시했습니다.

## 14일 데이터 가용성 점검 기준 요약
- 관측 구간: 최근 14일 KST 일 단위 집계
- `집계 성공`: `parseEligibleRunCount`, `warningEventCount` 산출 성공
- `집계 불가`: run-state 부재, 조회/파싱 실패, 필수 필드 누락 등으로 산출 불가
- `INSUFFICIENT_SAMPLE`: 집계 성공이지만 `parseEligibleRunCount < 20`
- 보고 항목:
  - 최근 14일 집계 성공/실패 일수
  - 최근 14일 `INSUFFICIENT_SAMPLE` 일수 및 비율
  - 최근 14일 `집계 불가` 원인 분류
  - 임계치 보정 판단(진행/보류)과 근거

## 임계치 보정 후보 수집 항목 요약 (수치 변경 없음)
- 샘플 충분(`parseEligibleRunCount >= 20`) 구간에서 agent별 `warningRate` 분포와 `warningEventCount` 추세를 수집
- 연속 `WARNING` 발생 빈도, 급증 규칙 충족 빈도, 전체 집계 보호 규칙 트리거 빈도를 수집
- `INSUFFICIENT_SAMPLE`/`집계 불가`는 임계치 후보 계산에서 분리해 별도 통계로 관리
- 임계치/알림 수치(`0.05`, `0.15`, `0.10`) 및 이벤트/모수 계약은 유지

## 수용기준 점검
1. `docs/code-agent-api.md`에 실측 보정 준비 절차(14일 가용성 + 집계 불가 분류 + 후보 수집 기준) 명시: **충족**
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 14일 데이터 가용성/샘플 충분성 출력 항목 추가: **충족**
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙 유지: **충족**
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경 없음: **충족**
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 실제 운영 구간에서 run-state 원천 데이터 품질이 낮으면 `집계 불가` 비중이 높아져 보정 판단이 반복 보류될 수 있습니다.
- 최근 14일 중 `INSUFFICIENT_SAMPLE` 비율이 높으면 임계치 보정 후보의 통계적 신뢰도가 낮아질 수 있습니다.

## 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 추가 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`에 14일 가용성 점검/집계 불가 분류/보정 후보 수집 기준이 명확히 반영되었는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 14일 성공/실패/샘플 부족 및 보정 보류 조건이 일관되게 추가되었는지
3. 임계치/알림 수치(`0.05`, `0.15`, `0.10`)와 기존 이벤트/모수 계약이 변경되지 않았는지
