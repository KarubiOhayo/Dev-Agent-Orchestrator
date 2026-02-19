# H-018 fallback warning 샘플 확보 계획 운영 적용 점검

Owner: WT-18 (`codex/h018-fallback-warning-sample-plan-operations-check`)
Priority: Highest

## 목표
- H-017에서 수립한 샘플 확보 계획/재보정 착수 게이트를 최근 14일 실측 데이터에 적용해 운영 적합성을 점검한다.
- 목표 대비 진행률과 Projection 오차를 계량화해 재보정 착수 가능 시점을 `가능/보류`로 고정한다.
- 게이트 미충족 원인을 분류해 다음 라운드 보완 액션 우선순위를 명시한다.

## 작업 범위
- 운영 문서 갱신
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-017-result.md`
  - `coordination/REPORTS/H-017-review.md`
  - `docs/code-agent-api.md`의 H-017 샘플 확보 계획 섹션
  - 최근 14일 run-state 집계(동일 KST 기준)
- `docs/code-agent-api.md`에 "H-018 운영 적용 점검 결과" 섹션을 추가한다.
  - 최근 14일 실측값(집계 성공 일수, `INSUFFICIENT_SAMPLE` 비율, 집계 불가 일수, 샘플 충분 일수, agent별 모수)을 보고한다.
  - H-017 정량 목표 대비 진행률(항목별 달성률/미달률)을 표로 정리한다.
  - Projection 실측 오차를 계산한다.
    - H-017 예상치와 실제값 차이(`일수`, `비율`, `예상 착수일`)를 함께 기록
    - 오차 허용 기준(예: 절대오차 2일 또는 비율 0.10 초과) 초과 여부를 판정
  - 재보정 착수 가능/보류를 판정하고 근거를 수치로 명시한다.
  - 미충족 시 원인 분류(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`, `COLLECTION_FAILURE`)별 보완 액션을 우선순위로 제시한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 운영 점검 출력 항목을 보강한다.
  - H-017 Projection 대비 실측 오차 항목(`deltaSufficientDays`, `deltaInsufficientRatio`, `deltaStartDate`)
  - 오차 기준 초과 시 자동으로 `보류 + 원인 분류 + 다음 액션`을 출력하는 규칙
  - 착수 가능 판정 시 다음 라운드로 임계치 후보 산정 라운드를 제안하는 출력 문구
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-018 실측 요약 + 목표 대비 진행률 + Projection 오차 + 착수 가능/보류 판정이 명시된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 Projection 오차/원인 분류/다음 액션 출력 규칙이 반영된다.
3. 게이트 미충족 시 보류 근거와 보완 액션이 수치 기반으로 보고된다.
4. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경되지 않는다.
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 운영 적용 점검 단계이며 임계치 수치 조정은 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-018-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-018-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최근 14일 실측 요약(게이트 4개 + agent별 모수)
  - 목표 대비 진행률/미달률
  - Projection 대비 실측 오차 분석
  - 재보정 착수 가능/보류 판정 및 근거
  - 보류 시 원인 분류/보완 액션 우선순위
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
