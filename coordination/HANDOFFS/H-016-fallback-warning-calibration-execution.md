# H-016 fallback warning 임계치/알림 룰 실측 기반 보정 실행

Owner: WT-16 (`codex/h016-fallback-warning-calibration-execution`)
Priority: Highest

## 목표
- 최근 14일(KST) 실측 데이터를 기준으로 fallback warning 임계치/알림 룰 보정안을 도출한다.
- H-015에서 고정한 가용성 게이트(집계 성공/집계 불가/샘플 부족)를 적용해 보정 `진행/보류` 판단을 명시한다.
- 보정안 적용 전/후 영향(경고 등급 분포, 알림 트리거 빈도)을 비교해 오탐/미탐 리스크를 근거 기반으로 보고한다.

## 작업 범위
- 운영 문서 갱신
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 데이터 기준:
  - 최근 14일 `KST 00:00~23:59` 일 단위
  - agent별 + 전체 집계 동시 확인
  - 대상 이벤트: `CODE/SPEC/DOC/REVIEW_OUTPUT_FALLBACK_WARNING`
- 가용성/진행 게이트(보정 실행 전 선판정):
  - 최근 14일 집계 성공 일수 `>= 10`
  - 최근 14일 `INSUFFICIENT_SAMPLE` 비율 `<= 0.5`
  - 최근 14일 `집계 불가` 일수 `< 3`
  - 하나라도 미충족이면 `보정 보류`로 판정하고 수치 변경을 수행하지 않는다.
- 보정 진행 시 산출(게이트 충족 조건):
  - 현행 임계치(`0.05`, `0.15`) 및 알림 룰(`+0.10p`, 전체 `0.10`) 대비 후보값을 제시한다.
  - 후보값은 적용 전/후 비교 표와 함께 제시한다.
    - agent별 등급 분포 변화(`NORMAL/CAUTION/WARNING`)
    - 알림 룰 트리거 건수 변화(연속 초과/급증/전체 보호)
    - 예상 영향(오탐 완화/미탐 증가 가능성) 요약
- 보정 보류 시 산출(게이트 미충족 조건):
  - `보정 보류`를 명시하고, 실패한 게이트/원인(`집계 불가` 분류, 샘플 부족 비율)을 수치로 보고한다.
  - 임계치/알림 룰 수치는 기존값 유지로 고정한다.
- 유지 원칙:
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(`parseEligibleRunCount`)는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-016 보정 실행 결과(14일 집계 요약 + 진행/보류 판정 + 적용 전/후 비교 또는 보류 근거)가 명시된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 보정 진행/보류 판정과 후속 보고 항목(전/후 비교 또는 보류 사유)이 반영된다.
3. 게이트 미충족 시 임계치/알림 룰 수치가 유지되고, 미충족 근거가 수치로 보고된다.
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)
- fallback warning 외 다른 운영 지표 임계치 조정

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 보정안은 최근 14일 근거와 함께 제시해야 하며, 근거 부족 시 보류 판단을 우선한다.

## 보고서
- 완료 시 `coordination/REPORTS/H-016-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-016-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최근 14일 집계 요약(성공/불가/샘플 부족)
  - 보정 진행/보류 판정 및 근거
  - 임계치/알림 룰 후보값(진행 시) 또는 유지 사유(보류 시)
  - 적용 전/후 영향 비교(진행 시)
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
