# H-015 fallback warning 임계치/알림 룰 실측 보정 준비

Owner: WT-15 (`codex/h015-fallback-warning-calibration-prep`)
Priority: Highest

## 목표
- fallback warning 임계치/알림 룰 보정 전, 운영 데이터(최소 2주) 수집 가능 구간과 집계 공백 구간을 명시적으로 점검한다.
- 임계치 보정 라운드에서 재사용 가능한 입력 산출물(체크리스트/보고 템플릿)을 문서로 고정한다.
- 현재 운영 계약(`warningRate` 산식, `INSUFFICIENT_SAMPLE` 제외 규칙, 임계치 값)은 유지한 채 준비 단계만 완료한다.

## 작업 범위
- 운영 문서 보강
  - `docs/code-agent-api.md`
- 자동 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- `docs/code-agent-api.md`의 fallback warning 집계 기준 섹션에 "실측 보정 준비 체크"를 추가한다.
  - 최소 14일 관측 구간 확인 절차
  - 일별 `parseEligibleRunCount`/`warningEventCount` 확보 실패 시 `집계 불가` 분류 기준
  - 임계치 수치 변경 없이 보정 후보를 수집/기록하는 방법(예: agent별 분포, 연속 WARNING 빈도, 급증 탐지 빈도)
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 데이터 가용성 점검 출력을 명시한다.
  - 최근 14일 중 집계 성공/실패 일수
  - `INSUFFICIENT_SAMPLE` 일수와 비율
  - 임계치 보정 판단 보류 조건(예: 유효 샘플 부족)
- 수치/계약 유지 원칙:
  - 임계치(`0.05`, `0.15`) 및 전체 집계 보호 규칙(`0.10`)은 변경하지 않는다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`) 및 모수 정의(`parseEligibleRunCount`)는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 실측 보정 준비 절차(14일 가용성 점검 + 집계 불가 분류 + 보정 후보 수집 기준)가 명시된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 14일 데이터 가용성/샘플 충분성 출력 항목이 추가된다.
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경되지 않는다.
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 변경
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 "보정 준비" 단계이며 운영 계약 수치 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-015-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-015-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 14일 데이터 가용성 점검 기준(성공/실패/샘플부족) 요약
  - 임계치 보정 후보 수집 항목(수치 변경 없이) 요약
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
