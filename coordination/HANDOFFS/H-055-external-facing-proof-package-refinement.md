# H-055 external-facing proof package refinement

Owner: WT-55 (`codex/h055-external-facing-proof-package-refinement`)
Priority: Highest

## 목표
- H-054에서 만든 `README -> case study -> walkthrough -> evidence bundle` 흐름을 실제 외부 전달 패키지 관점에서 한 번 더 다듬는다.
- `docs/evidence-report-export-bundle.md`의 source mapping, export tier, suggested export order, folder layout, post-walkthrough usage flow가 서로 같은 package logic를 말하도록 정렬한다.
- starter set / selective deep-dive / audit trail / governance add-on 경계를 더 분명히 하고, 필요하면 README / case study / walkthrough의 연결 문구를 최소 polishing 한다.

## 작업 범위
- 신규/수정 허용:
  - `docs/evidence-report-export-bundle.md`
  - `README.md` (필요 시 discovery / current-limits / docs-map 수준의 최소 수정만 허용)
  - `docs/portfolio-case-study.md` (필요 시 read-together / current-limits 수준의 최소 수정만 허용)
  - `docs/demo-showcase-walkthrough.md` (필요 시 read-next / after-demo handoff 수준의 최소 수정만 허용)
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
  - `coordination/REPORTS/H-054-result.md`
  - `coordination/REPORTS/H-054-review.md`
  - `coordination/RELAYS/H-054-review-to-main.md`
  - `docs/codex-ops-workflow.md`
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
  - `coordination/REPORTS/H-050-result.md`
  - `coordination/REPORTS/H-051-result.md`
  - `coordination/REPORTS/H-051-review.md`
  - `coordination/REPORTS/H-052-result.md`
  - `coordination/REPORTS/H-052-review.md`
  - `coordination/REPORTS/H-053-result.md`
  - `coordination/REPORTS/H-053-review.md`
  - `coordination/RELAYS/H-053-review-to-main.md`

## 구현 지침
- H-054 산출물은 유효한 baseline으로 간주한다. 이번 라운드는 재작성보다 refinement에 집중한다.
- H-054 review P3를 직접 흡수한다. 특히 `docs/codex-ops-workflow.md`의 위치/분류를 하나의 tier로 통일하고, mapping table / suggested export order / folder layout / how-to-use-after-the-walkthrough 전반에서 같은 분류를 유지한다.
- `starter set`, `technical deep-dive add-on`, `audit trail add-on`, `governance add-on` 각각의 목적과 포함 시점을 겹치지 않게 정리한다. 같은 문서를 여러 묶음에 걸치게 둘 거라면 이유를 명시하고, 가능하면 한 묶음으로 고정한다.
- sender가 "첫 메일/메시지에는 무엇을 보내고, 어떤 질문이 나오면 무엇을 추가할지"를 빠르게 판단할 수 있도록 문구를 다듬는다. 필요하면 `docs/evidence-report-export-bundle.md` 안에 짧은 cover-note 스타일 안내를 추가해도 되지만 별도 새 문서 신설은 하지 않는다.
- `README.md`, `docs/portfolio-case-study.md`, `docs/demo-showcase-walkthrough.md` 수정이 필요하더라도 본문 전면 재작성이나 새로운 claim 추가로 번지지 않게 한다. 현재 저장소가 이미 말하고 있는 범위를 더 일관되게 이어 주는 수준에 머문다.
- fabricated output, 새 metrics, screenshot, live run 재실행 증거, export 폴더/zip 생성은 금지한다.
- parked fallback-warning 트랙은 이번 라운드에서도 default proof package content가 아니다.

## 수용 기준
1. `docs/evidence-report-export-bundle.md`의 source mapping, export tier, suggested export order, folder layout, post-walkthrough usage flow가 서로 충돌하지 않는다.
2. H-054 리뷰에서 지적된 `docs/codex-ops-workflow.md` 분류 불일치가 해소된다.
3. starter set / selective deep-dive / audit trail / governance add-on 경계가 분명해, 다른 사람이 같은 문서를 기준으로도 거의 같은 bundle을 만들 수 있다.
4. `README.md`, `docs/portfolio-case-study.md`, `docs/demo-showcase-walkthrough.md` 수정이 있더라도 discovery / read-next / current-limits alignment 수준의 최소 변경에 머문다.
5. fabricated output, 존재하지 않는 실적, parked fallback-warning 전면화가 없다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- 새 external-facing 문서 종류 추가
- export 폴더/zip 생성
- live demo 재실행, screenshot 제작, metrics 생성
- README / case study / walkthrough 전면 재작성
- 코드/설정 변경
- fallback-warning parked 트랙 재개

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- 이번 라운드의 목적은 새 evidence를 만드는 것이 아니라, existing evidence package를 더 일관되게 전달 가능하게 만드는 것이다
- 결과적으로 문서 수를 늘리기보다, 이미 있는 4개 external-facing 문서의 흐름과 분류 체계를 더 명확하게 만드는 쪽을 우선한다

## 보고서
- 완료 시 `coordination/REPORTS/H-055-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-055-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - 어떤 분류 충돌을 어떻게 정리했는지 요약
  - starter set / selective / governance 경계 재정의 요약
  - 링크/카피 수정 여부와 이유
  - 새 claim 또는 새 artifact를 만들지 않았다는 근거
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안
