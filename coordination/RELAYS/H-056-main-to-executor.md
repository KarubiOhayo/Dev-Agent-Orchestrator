# [H-056] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-056-proof-package-closeout-copy-and-provenance-alignment.md`
- 참고 close-out result: `coordination/REPORTS/H-055-result.md`
- 참고 close-out review: `coordination/REPORTS/H-055-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-055-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-056-proof-package-closeout-copy-and-provenance-alignment.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
7. `README.md`
8. `docs/portfolio-case-study.md`
9. `docs/evidence-report-export-bundle.md`
10. `coordination/REPORTS/H-055-result.md`, `coordination/REPORTS/H-055-review.md`
11. `coordination/RELAYS/H-055-review-to-main.md`
12. `coordination/REPORTS/H-050-result.md`, `coordination/REPORTS/H-050-review.md`
13. `coordination/REPORTS/H-051-result.md`, `coordination/REPORTS/H-051-review.md`
14. `coordination/REPORTS/H-052-result.md`, `coordination/REPORTS/H-052-review.md`
15. `coordination/REPORTS/H-053-result.md`, `coordination/REPORTS/H-053-review.md`
16. `coordination/RELAYS/H-053-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `README.md`
  - `docs/portfolio-case-study.md`
  - `docs/evidence-report-export-bundle.md`
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.
- docs-only close-out 라운드이므로 fabricated output / 새 metrics / screenshot / export 폴더 생성으로 범위를 넓히지 않는다.

## 완료 산출물
- `coordination/REPORTS/H-056-result.md`
- `coordination/RELAYS/H-056-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-055에서 정리한 4개 묶음 package logic와 `docs/codex-ops-workflow.md` governance add-on 분류는 valid baseline이다. 이번 라운드에서 tier 체계를 다시 설계하지 말 것.
- `README.md`와 `docs/portfolio-case-study.md`는 더 이상 proof package 자체가 미완성처럼 읽히지 않도록 최소 범위로만 수정하라. 남아 있는 것은 마지막 close-out alignment와 shareability/redaction 판단이라는 점이 드러나면 충분하다.
- audit trail provenance는 반드시 한 가지 기준으로 끝까지 맞춰라. `H-050-review.md`를 실제 포함하든지, README provenance 문구를 result 중심으로 낮추든지 둘 중 하나를 택하고 mapping/order/layout/usage 설명 전체에 동일하게 반영하라.
- parked fallback-warning 트랙은 이번 라운드에서도 default bundle content로 올리지 말 것.
