# H-013 결과 보고서 (fallback warning run-state 집계 기준 문서화)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `docs/PROJECT_OVERVIEW.md`

## 구현 요약
- `docs/code-agent-api.md`에 fallback warning run-state 집계 기준(운영 계약) 섹션을 추가했습니다.
  - 대상 이벤트 4종(`CODE/SPEC/DOC/REVIEW_OUTPUT_FALLBACK_WARNING`)을 고정했습니다.
  - 집계 단위를 `agent별 일 단위(KST)` + `전체 집계`로 정의했습니다.
  - 경고율 산식 `warningRate = warningEventCount / parseEligibleRunCount`를 명시하고, `parseEligibleRunCount`의 agent별 의미를 고정했습니다.
  - 최소 샘플 수 조건(`parseEligibleRunCount < 20` -> `INSUFFICIENT_SAMPLE`)을 추가했습니다.
  - 임계치(`NORMAL < 0.05`, `CAUTION 0.05~0.15 미만`, `WARNING >= 0.15`)를 표로 정의했습니다.
  - 알림 룰(연속 초과/급증/전체 집계 보호 규칙)을 명시했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 fallback warning 점검 섹션을 추가했습니다.
  - 필수 점검 항목: 집계 기간, 이벤트 목록, agent별/전체 집계, 임계치 판정, 알림 룰 충족 여부.
  - 필수 출력 형식에 이벤트별 건수, agent별/전체 경고율, 후속조치 권고를 포함하도록 갱신했습니다.
  - Plan A 제약(파일 수정/커밋/PR 금지)을 유지했습니다.
- `docs/PROJECT_OVERVIEW.md`를 H-013 결과와 정합화했습니다.
  - 완료 항목에 H-013 반영
  - 운영 리스크에 "초기 임계치 오탐/미탐 가능성" 반영
  - 다음 우선순위를 "실측 데이터 기반 임계치/알림 룰 보정"으로 갱신

## 집계 기준(모수/경고율/임계치/알림 룰) 요약
- 모수:
  - `warningEventCount`: 해당 집계 단위에서 발생한 warning 이벤트 수
  - `parseEligibleRunCount`: 해당 집계 단위에서 파싱 대상 출력을 생성한 실행 수
- 경고율:
  - `warningRate = warningEventCount / parseEligibleRunCount`
- 최소 샘플 수:
  - `parseEligibleRunCount < 20`이면 `INSUFFICIENT_SAMPLE`
- 임계치:
  - `NORMAL < 0.05`
  - `CAUTION >= 0.05 && < 0.15`
  - `WARNING >= 0.15`
- 알림 룰:
  - 동일 agent `WARNING` 2일 연속
  - 전일 대비 `warningRate +0.10`p 이상 상승 + `warningEventCount` 5건 이상 증가
  - 전체 집계 `warningRate >= 0.10`

## 자동 점검 템플릿 반영 내용
- 야간 자동 점검 프롬프트에 fallback warning 집계 규약을 직접 포함했습니다.
- 보고서 필수 형식에 아래 항목을 고정했습니다.
  - 집계 기간
  - 이벤트별 건수
  - agent별 `parseEligibleRunCount`, `warningRate`, 임계치 판정
  - 전체 집계 `parseEligibleRunCount`, `warningRate`, 임계치 판정
  - 알림 룰 충족 여부 및 수동 후속조치

## 수용기준 점검
1. `docs/code-agent-api.md` 집계 기준 반영: **충족**
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 동일 기준 반영: **충족**
3. `docs/PROJECT_OVERVIEW.md` 리스크/우선순위 정합화: **충족**
4. 코드/설정 변경 없음: **충족** (`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml` 미변경)
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 임계치와 알림 룰은 초기 기준값이므로 실제 운영 데이터 분포에 따라 오탐/미탐이 발생할 수 있습니다.
- `parseEligibleRunCount` 산출 경로가 환경별로 다를 수 있어, 자동 점검 실행 환경별 데이터 접근 일관성 점검이 후속으로 필요합니다.

## 승인 필요 항목
- 공통 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, 빌드 설정) 변경: **없음**
- 사전 승인 필요 항목: **해당 없음**
