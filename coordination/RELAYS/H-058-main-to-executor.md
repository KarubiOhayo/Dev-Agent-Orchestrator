# [H-058] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-058-proof-package-checklist-canonical-flow-alignment.md`
- 참고 result: `coordination/REPORTS/H-057-result.md`
- 참고 review: `coordination/REPORTS/H-057-review.md`
- 참고 relay: `coordination/RELAYS/H-057-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-058-proof-package-checklist-canonical-flow-alignment.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
7. `docs/proof-package-delivery-checklist.md`
8. `docs/evidence-report-export-bundle.md`
9. `README.md`
10. `docs/portfolio-case-study.md`
11. `docs/demo-showcase-walkthrough.md`
12. `coordination/REPORTS/H-057-result.md`, `coordination/REPORTS/H-057-review.md`
13. `coordination/RELAYS/H-057-review-to-main.md`
14. `coordination/REPORTS/H-056-result.md`, `coordination/REPORTS/H-056-review.md`
15. `coordination/RELAYS/H-056-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `docs/proof-package-delivery-checklist.md`
  - `docs/evidence-report-export-bundle.md`
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.
- docs-only close-out 라운드이므로 새 external-facing 문서 / export 폴더 / screenshot / metrics / fabricated output 생성으로 범위를 넓히지 않는다.

## 완료 산출물
- `coordination/REPORTS/H-058-result.md`
- `coordination/RELAYS/H-058-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-057의 checklist foundation 자체는 유효하다. pre-send gate, add-on matrix, do-not-send guardrail을 다시 설계하지 말고 canonical flow alignment에만 집중하라.
- 이번 라운드의 기준 문서는 `docs/proof-package-delivery-checklist.md`다. evidence bundle은 detailed mapping / read-next reference로 정렬하되, starter set send order와 역할 설명이 checklist와 다르게 읽히지 않게 만들어라.
- `docs/portfolio-case-study.md`는 수정 대상이 아니지만 maintenance / stale check에서는 반드시 복구돼야 한다. starter set 4문서 drift check가 빠지지 않는지 리뷰어가 바로 확인할 수 있게 적어라.
- parked fallback-warning 트랙은 이번 라운드에서도 default package content로 올리지 말 것.
