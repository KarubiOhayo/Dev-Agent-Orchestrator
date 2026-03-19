# [H-059] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-059-portfolio-case-study-followup-path-alignment.md`
- 참고 result(있으면): `coordination/REPORTS/H-058-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-058-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-058-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-059-portfolio-case-study-followup-path-alignment.md`
5. 참고 `coordination/REPORTS/H-058-result.md`, `coordination/REPORTS/H-058-review.md`, `coordination/RELAYS/H-058-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `docs/portfolio-case-study.md`
  - `README.md` (필요 시 docs map / current-limits 수준의 최소 정렬만 허용)
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-059-result.md`
- `coordination/RELAYS/H-059-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-058에서 닫힌 checklist/evidence bundle canonical flow를 다시 설계하지 말고, case study가 그 follow-up path를 직접 가리키게 만드는 최소 정렬에 집중한다.
- 알려진 리스크:
  - `docs/portfolio-case-study.md`의 현재 next-step copy는 starter set/add-on polishing이 아직 열린 과제처럼 읽히고, sender-facing checklist 링크도 없다.
- 리뷰 집중 포인트:
  - case study가 `docs/proof-package-delivery-checklist.md`를 follow-up control doc로 분명히 가리키는지
  - evidence bundle이 detailed mapping / read-next reference 역할로 구분되는지
  - 남은 리스크가 shareability/redaction 최종 판단과 의미 품질 운영 점검으로만 좁혀졌는지
