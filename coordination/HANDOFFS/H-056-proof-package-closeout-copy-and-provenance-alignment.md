# H-056 proof package close-out copy and provenance alignment

Owner: WT-56 (`codex/h056-proof-package-closeout-alignment`)
Priority: Highest

## 목표
- H-055에서 정리한 external-facing proof package의 package logic baseline은 유지한 채, starter set status copy와 audit trail provenance 설명의 마지막 불일치를 닫는다.
- `README.md`, `docs/portfolio-case-study.md`, `docs/evidence-report-export-bundle.md`가 같은 branch 상태를 말하도록 맞춘다.
- 전달자가 "starter set을 지금 보내도 되는가?"와 "README round는 어떤 provenance까지 함께 제시되는가?"를 헷갈리지 않도록 docs-only close-out을 마무리한다.

## 작업 범위
- 신규/수정 허용:
  - `README.md` (Current Limits / Next Focus 수준의 최소 수정만 허용)
  - `docs/portfolio-case-study.md` (Current Limits / Read Together 수준의 최소 수정만 허용)
  - `docs/evidence-report-export-bundle.md`
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
  - `coordination/REPORTS/H-055-result.md`
  - `coordination/REPORTS/H-055-review.md`
  - `coordination/RELAYS/H-055-review-to-main.md`
  - `coordination/REPORTS/H-050-result.md`
  - `coordination/REPORTS/H-050-review.md`
  - `coordination/REPORTS/H-051-result.md`
  - `coordination/REPORTS/H-051-review.md`
  - `coordination/REPORTS/H-052-result.md`
  - `coordination/REPORTS/H-052-review.md`
  - `coordination/REPORTS/H-053-result.md`
  - `coordination/REPORTS/H-053-review.md`
  - `coordination/RELAYS/H-053-review-to-main.md`

## 구현 지침
- H-055에서 닫은 `docs/codex-ops-workflow.md` governance add-on 분류 일관성과 4개 묶음 package logic는 baseline으로 유지한다. 이번 라운드에서 tier 체계 자체를 다시 흔들지 않는다.
- `README.md`와 `docs/portfolio-case-study.md`는 더 이상 external-facing proof package refinement 자체가 "아직 없는 작업"처럼 읽히지 않도록 정리한다. 대신 마지막 polishing, shareability/redaction 판단, close-out alignment가 남아 있다는 수준으로 좁힌다.
- `docs/evidence-report-export-bundle.md`의 audit trail provenance는 실제 추천 bundle 구성과 같은 수준으로 맞춘다. 선택지는 둘 중 하나다.
  1. `coordination/REPORTS/H-050-review.md`를 audit trail mapping / export order / folder layout / usage narrative에 포함한다.
  2. README packaging proof 설명을 result 중심 provenance로 낮추고, review-backed라는 뉘앙스를 제거한다.
- 어떤 선택을 하든 mapping table, suggested export order, folder layout, post-walkthrough usage flow, 관련 설명 문구가 모두 같은 기준을 말해야 한다.
- 이번 라운드는 close-out 목적이므로 새 external-facing 문서, export 폴더/zip, live demo evidence, screenshot, metrics, fabricated output을 만들지 않는다.
- parked fallback-warning 트랙은 이번 라운드에서도 default bundle content가 아니다.
- 수정은 최소 범위에 머문다. README / case study / evidence bundle 전면 재작성이나 새로운 claim 추가로 번지지 않게 한다.

## 수용 기준
1. `README.md`와 `docs/portfolio-case-study.md`가 starter set의 현재 상태를 `docs/evidence-report-export-bundle.md`와 모순 없이 설명한다.
2. starter set 구성 문서들이 "지금 보낼 수 있는 기본 외부 공유 세트"와 "남아 있는 마지막 polishing"를 같은 수준으로 서술한다.
3. audit trail provenance 설명, mapping, export order, folder layout, usage narrative가 README round 근거 범위를 정확하게 반영한다.
4. H-055에서 닫힌 governance add-on 분류와 4개 묶음 package logic는 유지된다.
5. fabricated output, 새 metrics, 새 screenshot, 새 export artifact, parked fallback-warning 전면화가 없다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- 새 external-facing 문서 종류 추가
- export 폴더/zip 생성
- live demo 재실행, screenshot 제작, metrics 생성
- README / case study / evidence bundle 전면 재작성
- 코드/설정 변경
- fallback-warning parked 트랙 재개

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- 이번 라운드의 목적은 새 package logic를 만드는 것이 아니라, 이미 만들어진 starter set과 audit trail 설명이 같은 상태를 말하게 만드는 것이다
- external-facing copy는 과장보다 정합성을 우선한다. 실제 추천 bundle에 없는 review artifact를 암시하지 말고, 포함한다면 표/순서/레이아웃에 모두 반영한다

## 보고서
- 완료 시 `coordination/REPORTS/H-056-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-056-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - starter set status copy를 어떻게 정렬했는지 요약
  - audit trail provenance를 어떤 방식(artifact 추가 또는 문구 하향)으로 정합화했는지 요약
  - H-055 baseline에서 유지한 항목과 건드리지 않은 이유
  - 새 claim / 새 artifact를 만들지 않았다는 근거
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안
