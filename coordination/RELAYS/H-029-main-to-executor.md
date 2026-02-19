# [H-029] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-029-fallback-warning-h024-resume-readiness-check.md`
- 참고 result: `coordination/REPORTS/H-028-result.md`
- 참고 review: `coordination/REPORTS/H-028-review.md`
- 참고 relay: `coordination/RELAYS/H-028-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-029-fallback-warning-h024-resume-readiness-check.md`
6. `coordination/REPORTS/H-028-result.md`, `coordination/REPORTS/H-028-review.md`, `coordination/RELAYS/H-028-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `docs/PROJECT_OVERVIEW.md` (필요 시 최소 동기화)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-029-result.md`
- `coordination/RELAYS/H-029-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- 기존 fallback warning 산식/임계치/제외 규칙(`INSUFFICIENT_SAMPLE`)은 변경하지 않고, 최신 실측 기반 재개 판정(`RESUME_H024/KEEP_FROZEN`)만 고정할 것.
- 최종 판정은 반드시 1개만 선택하고, 미충족 신호와 근거 수치를 문서/자동화 템플릿에 동일하게 반영할 것.
- 코드 동작 변경 없이 운영 문서/템플릿 정합성에 집중하고, 공통 승인 대상 파일 변경 필요 시 즉시 중단할 것.
