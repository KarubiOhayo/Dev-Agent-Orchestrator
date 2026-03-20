# [H-060] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-060-sender-facing-shareability-redaction-final-judgment-hygiene.md`
- 참고 result(있으면): `coordination/REPORTS/H-059-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-059-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-059-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-060-sender-facing-shareability-redaction-final-judgment-hygiene.md`
5. 참고 `coordination/REPORTS/CURRENT_STATUS_2026-03-20.md`, `coordination/REPORTS/H-059-result.md`, `coordination/REPORTS/H-059-review.md`, `coordination/RELAYS/H-059-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `docs/proof-package-delivery-checklist.md`
  - `docs/evidence-report-export-bundle.md`
  - `README.md` (필요 시 docs map / current-limits 수준의 최소 정렬만 허용)
  - `docs/portfolio-case-study.md` (필요 시 current-limits / read-together 수준의 최소 정렬만 허용)
  - `docs/demo-showcase-walkthrough.md` (필요 시 follow-up 안내 수준의 최소 정렬만 허용)
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-060-result.md`
- `coordination/RELAYS/H-060-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-058/H-059에서 닫힌 starter set canonical flow와 checklist authority를 다시 설계하지 말고, final judgment hygiene만 보강한다.
- 알려진 리스크:
  - 실제 외부 발송 직전의 shareability/redaction 판단과 excerpt/hold 기준이 checklist와 evidence bundle에 분산돼 있어 sender가 행동 기준을 다시 조합해야 한다.
- 리뷰 집중 포인트:
  - checklist가 sender에게 `지금 보낸다`, `redact/excerpt 후 보낸다`, `이번 라운드에서는 보내지 않는다` 수준의 결정을 더 직접적으로 돕는지
  - evidence bundle의 shareability taxonomy와 add-on 안내가 checklist final judgment와 같은 행동 기준을 말하는지
  - 새 artifact 생성, parked fallback-warning 전면화, `coordination/` 전체 전송 암시가 없는지
