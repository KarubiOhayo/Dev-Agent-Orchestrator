# [H-057] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-057-proof-package-delivery-checklist-finalization.md`
- 참고 close-out result: `coordination/REPORTS/H-056-result.md`
- 참고 close-out review: `coordination/REPORTS/H-056-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-056-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-057-proof-package-delivery-checklist-finalization.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
7. `README.md`
8. `docs/portfolio-case-study.md`
9. `docs/demo-showcase-walkthrough.md`
10. `docs/evidence-report-export-bundle.md`
11. `docs/codex-ops-workflow.md`
12. `docs/cli-quickstart.md`, `docs/code-agent-api.md`
13. `coordination/REPORTS/H-055-result.md`, `coordination/REPORTS/H-055-review.md`
14. `coordination/REPORTS/H-056-result.md`, `coordination/REPORTS/H-056-review.md`
15. `coordination/RELAYS/H-056-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `docs/proof-package-delivery-checklist.md`
  - `docs/evidence-report-export-bundle.md` (필요 시 최소 조정만)
  - `docs/demo-showcase-walkthrough.md` (필요 시 최소 조정만)
  - `README.md` (필요 시 최소 조정만)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.
- docs-only finalization 라운드이므로 새 evidence artifact / export 폴더 / screenshot / metrics / fabricated output 생성으로 범위를 넓히지 않는다.

## 완료 산출물
- `coordination/REPORTS/H-057-result.md`
- `coordination/RELAYS/H-057-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-055/H-056에서 닫힌 4개 묶음 package logic와 provenance baseline은 valid baseline이다. 이번 라운드에서 bundle 체계를 다시 설계하지 말 것.
- checklist는 새 narrative 문서가 아니라 실제 발송 직전 운영 체크를 더 짧게 재사용하게 만드는 문서여야 한다.
- `starter set`, `technical deep-dive add-on`, `audit trail add-on`, `governance add-on`의 질문별 트리거와 pre-send gate를 한 장에서 재현 가능하게 만들되, 자세한 설명은 기존 evidence bundle 문서로 넘겨라.
- shareability / redaction / stale-reference / 최신 테스트 근거 확인을 명시적으로 넣어라. starter set ready 상태와 "아무거나 다 보내지 않는다"는 가드레일이 함께 드러나야 한다.
- parked fallback-warning 트랙은 이번 라운드에서도 default package content로 올리지 말 것.
