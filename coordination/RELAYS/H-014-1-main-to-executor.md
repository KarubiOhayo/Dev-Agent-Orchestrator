# [H-014.1] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-014-1-code-parse-eligible-alignment.md`
- 참고 result(있으면): `coordination/REPORTS/H-014-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-014-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-014-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-014-1-code-parse-eligible-alignment.md`
5. `coordination/REPORTS/H-014-result.md`, `coordination/REPORTS/H-014-review.md`, `coordination/RELAYS/H-014-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-014-1-code-parse-eligible-alignment.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-014-1-result.md`
- `coordination/RELAYS/H-014-1-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - 이번 라운드는 Code 모수 정의 문구 정합화 범위이며 코드 동작/임계치/알림 규칙 수치 변경을 포함하지 않는다.
  - Code `parseEligibleRunCount`는 직접 호출 + Spec 체인 호출 포함 기준으로 고정한다.
- 알려진 리스크:
  - 모수를 직접 호출 기준으로 축소 해석하면 Code `warningRate`가 과대 계산될 수 있다.
  - 문서/템플릿 간 용어가 다시 분리되면 야간 보고 해석 일관성이 깨질 수 있다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`의 Code 모수 정의가 Spec 체인 호출 포함 기준으로 명시됐는지
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 Code 관련 설명이 같은 기준으로 정렬됐는지
