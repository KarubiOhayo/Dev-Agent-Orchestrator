# H-019 fallback warning 재보정 착수 가능 시점 재점검

Owner: WT-19 (`codex/h019-fallback-warning-recalibration-readiness-check`)
Priority: Highest

## 목표
- H-018.1에서 고정한 진행률 산식/4개 게이트 계약을 기준으로 최신 14일 실측을 재점검한다.
- 재보정 착수 가능 여부를 `READY/HOLD`로 단일 판정하고, 근거 수치와 미충족 게이트를 명시한다.
- `READY` 조건 충족 시 임계치 후보 산정 라운드 착수 입력을 고정하고, `HOLD` 시 샘플 확보 보완 액션 우선순위를 갱신한다.

## 작업 범위
- 운영 문서 갱신
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-018-1-result.md`
  - `coordination/REPORTS/H-018-1-review.md`
  - `coordination/RELAYS/H-018-1-review-to-main.md`
  - `docs/code-agent-api.md`의 H-017/H-018 운영 점검 섹션
- 집계 구간:
  - 실행일 기준 최근 14일(KST, `today-13` ~ `today`)로 고정한다.
  - 데이터 소스는 기존 운영 기준(`storage/devagent.db`의 `runs`, `run_events`)을 따른다.
- `docs/code-agent-api.md`에 H-019 운영 재점검 섹션을 추가/갱신한다.
  - 게이트 4개 실측값(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`)과 PASS/FAIL을 표로 보고한다.
  - 진행률은 `min(1, 집계 성공 일수 / 10)`로 계산해 `0~100%`로 표기하고, `목표 초과 일수`를 분리 표기한다.
  - Projection(`requiredSufficientDays`, `예상 재보정 착수 가능일`)을 최신 데이터로 재산정한다(전제조건 미충족 시 미산정 사유 명시).
  - `READY/HOLD` 최종 판정과 근거(충족/미충족 게이트, 오차/추세)를 명시한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화한다.
  - 야간 리포트 출력에 `recalibrationReadiness(READY/HOLD)`와 미충족 게이트 목록을 필수 항목으로 고정한다.
  - `READY` 시 다음 라운드 제안 문구를 임계치 후보 산정 착수로 고정한다.
  - `HOLD` 시 원인 분류(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`, `COLLECTION_FAILURE`) 기반 다음 액션을 출력하도록 유지한다.
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-019 최신 14일 실측 + 4개 게이트 판정 + `READY/HOLD` 결론이 반영된다.
2. 진행률은 `0~100%` 상한을 준수하고, `목표 초과 일수`가 분리 표기된다.
3. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 동일 판정 계약(`READY/HOLD`, 미충족 게이트, 다음 액션)으로 정렬된다.
4. `READY` 또는 `HOLD` 판정 근거가 수치 기반으로 보고된다.
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 재보정 착수 가능성 재점검 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-019-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-019-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최신 14일 게이트 4개 실측 + PASS/FAIL 표
  - 진행률(상한 적용) + 목표 초과 일수
  - Projection 재산정 결과(`requiredSufficientDays`, `예상 착수 가능일` 또는 미산정 사유)
  - `READY/HOLD` 최종 판정 및 근거
  - `HOLD` 시 원인 분류/보완 액션 우선순위
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
