# H-017 fallback warning 보정 재착수용 샘플 확보 계획 수립

Owner: WT-17 (`codex/h017-fallback-warning-sample-acquisition-plan`)
Priority: Highest

## 목표
- H-016에서 확정된 보정 보류 원인(`INSUFFICIENT_SAMPLE` 비율 `1.00`)을 해소하기 위한 샘플 확보 실행 계획을 수립한다.
- 최근 14일 기준으로 재보정 착수 가능 여부를 판단할 수 있도록 정량 목표(모수/일수/비율)와 추적 지표를 문서로 고정한다.
- 보정 재착수 전까지 임계치/알림 룰 수치는 유지하면서, 다음 보정 라운드 입력 품질을 안정화한다.

## 작업 범위
- 운영 문서 갱신
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-016-result.md`
  - `coordination/REPORTS/H-016-review.md`
  - `docs/code-agent-api.md`의 H-016 실측 보정 실행 결과 섹션
- `docs/code-agent-api.md`에 "H-017 샘플 확보 계획" 섹션을 추가한다.
  - H-016 기준선 수치(성공 일수/샘플 부족 비율/집계 불가 일수, agent별 모수)를 명시한다.
  - 재보정 착수 목표를 정량으로 명시한다.
    - 최근 14일 집계 성공 일수 `>= 10`
    - 최근 14일 `INSUFFICIENT_SAMPLE` 비율 `<= 0.50`
    - 최근 14일 `집계 불가` 일수 `< 3`
  - 목표 달성을 위한 샘플 확보 실행안(직접 호출/체인 호출 시나리오, 일일 최소 모수 목표, 점검 주기)을 기술한다.
  - 재보정 착수 조건 충족/미충족 시 다음 액션(진행/보류)을 분기 규칙으로 명시한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 샘플 확보 추적 출력을 추가한다.
  - 최근 14일 기준 `parseEligibleRunCount` 추세(전체 + agent별)와 목표 대비 진행률
  - 게이트 충족 예상치(Projection) 및 미충족 원인 분류
  - 재보정 착수 가능/보류 판단과 다음 액션
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-016 기준선 수치 + H-017 샘플 확보 정량 목표 + 재보정 착수/보류 분기 규칙이 명시된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 목표 대비 진행률/예상치/다음 액션 출력 항목이 반영된다.
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경되지 않는다.
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 보정 재착수 입력(샘플 확보 계획/추적 지표) 고정 단계이며 수치 보정 수행은 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-017-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-017-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - H-016 기준선 대비 샘플 확보 정량 목표
  - 목표 대비 진행률/예상치 산출 방식
  - 재보정 착수 가능/보류 판정 규칙
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
