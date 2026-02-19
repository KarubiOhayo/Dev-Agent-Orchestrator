# H-014 결과 보고서 (fallback warning 집계 기준 문구 정합화)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`

## 구현 요약
- `docs/code-agent-api.md`의 `parseEligibleRunCount` 정의를 API 엔드포인트 호출 중심 표현에서 **agent 서비스 run 기준**으로 보정했습니다.
  - 해석 기준을 `직접 호출 run + 체인 호출 run 포함`으로 명시했습니다.
  - agent별 기준을 아래처럼 고정했습니다.
    - Code/Spec: 직접 API 호출 run 기준
    - Doc/Review: 직접 API 호출 run + Code 체인 호출 run 기준
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `INSUFFICIENT_SAMPLE` 제외 규칙을 명시했습니다.
  - `parseEligibleRunCount < 20`이면 `INSUFFICIENT_SAMPLE`
  - `INSUFFICIENT_SAMPLE` 대상은 임계치 판정/알림 룰 계산에서 제외
  - 보고 형식에 `INSUFFICIENT_SAMPLE 제외 내역(대상 agent, 제외 사유)` 항목을 추가
- 문구 정합화 범위만 수정했으며 임계치 수치(`0.05`, `0.15`)와 알림 조건 정의는 변경하지 않았습니다.

## `parseEligibleRunCount` 정의 보정 전/후 요약
- 보정 전:
  - `POST /api/agents/{agent}/generate` 직접 호출 run 기준으로 읽힐 수 있는 문구
  - DOC/REVIEW의 체인 실행분 누락 해석 가능성 존재
- 보정 후:
  - 동일 집계 단위의 `agent 서비스 run` 기준으로 정의 고정
  - DOC/REVIEW는 직접 호출 + 체인 호출 run 모두 포함한다고 명시

## `INSUFFICIENT_SAMPLE` 제외 규칙 반영 위치/보고 예시
- 반영 위치:
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
    - fallback warning 점검 절차: `INSUFFICIENT_SAMPLE` 임계치/알림 계산 제외 규칙 명시
    - 보고 형식: `INSUFFICIENT_SAMPLE 제외 내역` 항목 추가
- 보고 예시:
  - `DOC`: `parseEligibleRunCount=12` -> `INSUFFICIENT_SAMPLE` (임계치 판정/알림 계산 제외)
  - 제외 내역: `DOC`, 사유=`parseEligibleRunCount < 20`

## 수용기준 점검
1. `docs/code-agent-api.md`에 직접 호출/체인 호출 포함 기준 명시: **충족**
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `INSUFFICIENT_SAMPLE` 제외 규칙 명시: **충족**
3. H-013 리뷰 P2/P3 지적 사항 해소: **충족**
4. 코드/설정 변경 없음: **충족** (`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml` 미변경)
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 임계치/알림 룰은 초기 기준값이므로 실제 운영 데이터 분포 변화에 따라 오탐/미탐 가능성이 남아 있습니다.
- run-state 원천 데이터 조회 경로가 환경별로 다를 경우 야간 보고의 집계 일관성 점검이 추가로 필요할 수 있습니다.

## 승인 필요 항목
- 공통 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, 빌드 설정) 변경: **없음**
- 사전 승인 필요 항목: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 `parseEligibleRunCount` 정의가 DOC/REVIEW 체인 호출 포함 기준으로 충분히 명시되었는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `INSUFFICIENT_SAMPLE`의 임계치/알림 제외 규칙과 별도 보고 항목이 함께 반영되었는지
3. 임계치 수치/알림 룰 정의가 변경되지 않았는지
