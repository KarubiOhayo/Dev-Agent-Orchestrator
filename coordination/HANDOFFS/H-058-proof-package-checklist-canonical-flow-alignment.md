# H-058 proof package checklist canonical flow alignment

Owner: WT-58 (`codex/h058-proof-package-canonical-flow-alignment`)
Priority: Highest

## 목표
- H-057에서 만든 sender-facing checklist foundation은 유지한 채, checklist와 evidence bundle 사이의 canonical send order / cover-note 역할 / maintenance trigger를 하나로 고정한다.
- 전달자가 "어느 문서를 기준으로 첫 발송 흐름을 따라야 하는가?"를 다시 해석하지 않도록, checklist를 sender-facing canonical authority로 정렬한다.
- `docs/portfolio-case-study.md` 누락을 메워 starter set 4문서 drift check가 같은 라운드에서 계속 유지되게 한다.

## 작업 범위
- 신규/수정 허용:
  - `docs/proof-package-delivery-checklist.md`
  - `docs/evidence-report-export-bundle.md`
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
  - `README.md`
  - `docs/portfolio-case-study.md`
  - `docs/demo-showcase-walkthrough.md`
  - `coordination/REPORTS/H-057-result.md`
  - `coordination/REPORTS/H-057-review.md`
  - `coordination/RELAYS/H-057-review-to-main.md`
  - `coordination/REPORTS/H-056-result.md`
  - `coordination/REPORTS/H-056-review.md`
  - `coordination/RELAYS/H-056-review-to-main.md`

## 구현 지침
- H-057에서 정리한 pre-send gate, add-on decision matrix, do-not-send / honesty guardrail은 유효 baseline으로 유지한다. 이번 라운드는 package logic 재설계가 아니라 close-out alignment다.
- canonical send-order anchor는 `docs/proof-package-delivery-checklist.md`로 고정한다. checklist는 sender가 내부적으로 따르는 control 문서이고, `docs/evidence-report-export-bundle.md`는 external starter set과 add-on mapping을 설명하는 detailed reference / read-next 문서로 정렬한다.
- 따라서 `docs/evidence-report-export-bundle.md`에서는 자기 자신을 starter set cover note나 첫 메시지 anchor처럼 읽히게 하지 말고, `README -> case study -> walkthrough -> evidence bundle` 순서와 checklist 기준을 지원하는 문구로 정리한다.
- `docs/proof-package-delivery-checklist.md`의 maintenance / stale check에는 `README.md`, `docs/portfolio-case-study.md`, `docs/demo-showcase-walkthrough.md`, `docs/evidence-report-export-bundle.md` 4문서 drift check를 명시한다.
- `docs/evidence-report-export-bundle.md`의 maintenance 안내도 같은 4문서와 checklist authority 기준을 따라야 한다.
- docs-only close-out 라운드이므로 새 external-facing 문서, export 폴더/zip, screenshot, metrics, fabricated output을 만들지 않는다.
- parked fallback-warning 트랙은 이번 라운드에서도 default package content가 아니다.
- 수정은 최소 범위에 머문다. README / case study / walkthrough를 다시 건드리지 않고 두 문서 alignment만으로 닫는 것을 우선한다.

## 수용 기준
1. `docs/proof-package-delivery-checklist.md`와 `docs/evidence-report-export-bundle.md`가 같은 starter set send order와 cover-note 역할을 말한다.
2. checklist가 sender-facing canonical authority이고, evidence bundle이 detailed mapping / read-next reference라는 역할 분리가 문구로 드러난다.
3. 두 문서의 maintenance / stale check에 `README.md`, `docs/portfolio-case-study.md`, `docs/demo-showcase-walkthrough.md`, `docs/evidence-report-export-bundle.md` 4문서 drift check가 반영된다.
4. H-057 baseline(pre-send gate, add-on matrix, do-not-send guardrail)은 유지된다.
5. 새 external-facing 문서, export 폴더/zip, screenshot, metrics, fabricated output, parked fallback-warning 전면화가 없다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- starter set / add-on package logic 재설계
- 새 external-facing 문서 종류 추가
- README / case study / walkthrough 전면 재작성
- export 폴더/zip 생성
- live demo 재실행, screenshot 제작, metrics 생성
- 코드/설정 변경
- fallback-warning parked 트랙 재개

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- external-facing copy는 과장보다 정합성을 우선한다. 실제로 보내지 않는 문서를 첫 발송 anchor처럼 암시하지 않는다.
- 목표는 새 증거를 만드는 것이 아니라, 이미 있는 starter set과 add-on guide가 같은 canonical flow를 말하게 맞추는 것이다.

## 보고서
- 완료 시 `coordination/REPORTS/H-058-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-058-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - canonical send order / cover-note 역할을 어떻게 정렬했는지 요약
  - maintenance / stale check에 `docs/portfolio-case-study.md`를 어떻게 복구했는지
  - 유지한 baseline과 건드리지 않은 이유
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안
