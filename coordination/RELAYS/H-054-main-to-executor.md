# [H-054] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-054-evidence-report-export-bundle-packaging.md`
- 참고 close-out result: `coordination/REPORTS/H-053-result.md`
- 참고 close-out review: `coordination/REPORTS/H-053-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-053-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-054-evidence-report-export-bundle-packaging.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
7. `README.md`, `docs/portfolio-case-study.md`, `docs/demo-showcase-walkthrough.md`
8. `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `docs/codex-ops-workflow.md`
9. `coordination/REPORTS/H-050-result.md`, `coordination/REPORTS/H-051-result.md`, `coordination/REPORTS/H-051-review.md`, `coordination/REPORTS/H-052-result.md`, `coordination/REPORTS/H-052-review.md`, `coordination/REPORTS/H-053-result.md`, `coordination/REPORTS/H-053-review.md`
10. `coordination/RELAYS/H-053-review-to-main.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `docs/evidence-report-export-bundle.md`
  - `README.md` (필요 시 최소 링크 수정만)
  - `docs/demo-showcase-walkthrough.md` (필요 시 최소 링크 수정만)
  - `docs/portfolio-case-study.md` (필요 시 최소 링크 수정만)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.
- bundle은 현재 저장소의 실제 문서/결과 보고/리뷰/릴레이만 source로 사용하고, fabricated output이나 새 실적을 추가하지 않는다.

## 완료 산출물
- `coordination/REPORTS/H-054-result.md`
- `coordination/RELAYS/H-054-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- 이번 라운드의 핵심은 "무엇을 export할 것인가"보다 "무엇을 어떤 근거로 건네면 되는가"를 문서로 고정하는 것이다. source 문서를 복제한 bundle 폴더 생성 작업으로 범위를 넓히지 말 것.
- bundle은 README -> case study -> walkthrough가 설명한 narrative를 보강해야 한다. narrative docs, demo companion, proof artifacts, governance evidence를 구분하고 shareability note를 명시하라.
- 외부 공유 친화성을 위해 redaction / honesty guardrail을 분명히 적어라. 예: 비밀값, 내부 전용 맥락, 꾸며낸 스크린샷, parked fallback-warning 전면화 금지.
- 링크 수정이 필요해도 discovery/read-next 수준의 최소 변경에 머물러라. README나 case study, walkthrough 본문을 다시 쓰는 작업으로 번지지 않게 범위를 지켜라.
