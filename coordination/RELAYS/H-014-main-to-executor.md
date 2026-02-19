# [H-014] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-014-fallback-warning-baseline-alignment.md`
- 참고 result(있으면): `coordination/REPORTS/H-013-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-013-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-013-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-014-fallback-warning-baseline-alignment.md`
5. `coordination/REPORTS/H-013-result.md`, `coordination/REPORTS/H-013-review.md`, `coordination/RELAYS/H-013-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-014-fallback-warning-baseline-alignment.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-014-result.md`
- `coordination/RELAYS/H-014-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - 이번 라운드는 문구 정합성 보정 범위이며 코드 동작/계약 수치 변경을 포함하지 않는다.
  - `parseEligibleRunCount`는 agent 서비스 run 기준(직접 호출 + 체인 호출 포함)으로 명시를 고정한다.
- 알려진 리스크:
  - `DOC/REVIEW` 모수 정의가 호출 경로별로 다르게 해석되면 경고율이 과대/과소 계산될 수 있다.
  - `INSUFFICIENT_SAMPLE` 제외 규칙이 템플릿에 누락되면 운영 보고 해석이 담당자별로 달라질 수 있다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`의 모수 정의가 체인 실행 경로를 명시적으로 포함하는지
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 샘플 부족 제외 규칙이 임계치/알림 계산과 함께 고정되었는지
