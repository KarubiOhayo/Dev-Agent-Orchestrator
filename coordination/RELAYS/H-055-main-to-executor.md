# [H-055] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-055-external-facing-proof-package-refinement.md`
- 참고 close-out result: `coordination/REPORTS/H-054-result.md`
- 참고 close-out review: `coordination/REPORTS/H-054-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-054-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-055-external-facing-proof-package-refinement.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
7. `README.md`
8. `docs/portfolio-case-study.md`
9. `docs/demo-showcase-walkthrough.md`
10. `docs/evidence-report-export-bundle.md`
11. `docs/codex-ops-workflow.md`
12. `docs/cli-quickstart.md`, `docs/code-agent-api.md`
13. `coordination/REPORTS/H-054-result.md`, `coordination/REPORTS/H-054-review.md`
14. `coordination/RELAYS/H-054-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `docs/evidence-report-export-bundle.md`
  - `README.md` (필요 시 최소 조정만)
  - `docs/portfolio-case-study.md` (필요 시 최소 조정만)
  - `docs/demo-showcase-walkthrough.md` (필요 시 최소 조정만)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.
- existing package를 refinement하는 라운드이므로, fabricated output / 새 metrics / screenshot / export 폴더 생성으로 범위를 넓히지 않는다.

## 완료 산출물
- `coordination/REPORTS/H-055-result.md`
- `coordination/RELAYS/H-055-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-054는 이미 `Go`로 close-out할 수준의 baseline이다. 이번 라운드는 baseline을 뒤집는 것이 아니라, 실제 전달자가 헷갈릴 여지를 줄이는 refinement에 집중하라.
- Review P3로 지적된 `docs/codex-ops-workflow.md` 분류 불일치를 우선 해소하라. mapping table, export order, folder layout, post-walkthrough usage flow가 같은 분류 체계를 말해야 한다.
- `starter set`, `technical deep-dive add-on`, `audit trail add-on`, `governance add-on`의 포함 기준과 추가 시점을 더 분명히 적어라.
- README / case study / walkthrough 수정이 필요해도 기존 claim을 넓히지 말고, discovery / read-next / after-demo handoff copy를 조금 더 매끈하게 만드는 선에 머물러라.
- parked fallback-warning 트랙은 이번 라운드에서도 default package content로 올리지 말 것.
