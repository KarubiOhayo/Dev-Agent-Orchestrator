# [H-013] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-013-fallback-warning-runstate-baseline.md`
- 참고 result(있으면): `coordination/REPORTS/H-012-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-012-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-012-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-013-fallback-warning-runstate-baseline.md`
5. `coordination/REPORTS/H-012-result.md`, `coordination/REPORTS/H-012-review.md`, `coordination/RELAYS/H-012-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-013-fallback-warning-runstate-baseline.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-013-result.md`
- `coordination/RELAYS/H-013-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - 이번 라운드는 운영 기준 문서화 범위이며 코드 동작/계약 변경을 포함하지 않는다.
  - fallback warning 이벤트명은 기존 구현 계약(`CODE/SPEC/DOC/REVIEW`)을 그대로 사용한다.
- 알려진 리스크:
  - 임계치가 실측 데이터 없이 과도하게 설정되면 오탐/미탐이 발생할 수 있다.
  - 자동 점검 템플릿이 집계 기준과 어긋나면 운영 보고의 비교 가능성이 떨어질 수 있다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`의 집계 기준(모수/경고율/임계치/알림 룰)이 이벤트 계약과 충돌 없는지
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 보고 형식이 Plan A 제약을 유지하면서도 실행 가능하게 정의됐는지
