# H-060 sender-facing shareability / redaction final judgment hygiene

Owner: WT-60 (`codex/h060-shareability-redaction-final-judgment-hygiene`)
Priority: Highest

## 목표
- H-057~H-059에서 닫힌 sender-facing package baseline을 유지한 채, 실제 발송 직전의 shareability / redaction 최종 인간 판단을 더 짧고 반복 가능하게 만든다.
- `docs/proof-package-delivery-checklist.md`와 `docs/evidence-report-export-bundle.md`가 starter set / add-on을 언제 그대로 보내고, 언제 redact/excerpt로 낮추고, 언제 보류해야 하는지 같은 행동 기준을 말하게 정렬한다.
- canonical send order(`README -> case study -> walkthrough -> evidence bundle`)와 checklist authority는 그대로 두고, final judgment hygiene만 docs-only 범위에서 보강한다.

## 작업 범위
- 신규/수정 허용:
  - `docs/proof-package-delivery-checklist.md`
  - `docs/evidence-report-export-bundle.md`
  - `README.md` (필요 시 docs map / current-limits 수준의 최소 링크 또는 상태 문구 정렬만 허용)
  - `docs/portfolio-case-study.md` (필요 시 current-limits / read-together 수준의 최소 링크 또는 상태 문구 정렬만 허용)
  - `docs/demo-showcase-walkthrough.md` (필요 시 follow-up 안내 수준의 최소 링크 정렬만 허용)
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-20.md`
  - `coordination/REPORTS/H-059-result.md`
  - `coordination/REPORTS/H-059-review.md`
  - `coordination/RELAYS/H-059-review-to-main.md`
  - `docs/proof-package-delivery-checklist.md`
  - `docs/evidence-report-export-bundle.md`
  - `README.md`
  - `docs/portfolio-case-study.md`
  - `docs/demo-showcase-walkthrough.md`
  - `docs/codex-ops-workflow.md`

## 구현 지침
- H-058/H-059에서 닫힌 canonical flow는 baseline으로 유지한다. 이번 라운드는 starter set이나 add-on 구조를 재설계하는 것이 아니라, sender가 마지막 발송 직전에 같은 판단을 반복하기 쉽게 만드는 hygiene close-out이다.
- `docs/proof-package-delivery-checklist.md`는 pre-send gate를 "점검 항목 나열"에서 끝내지 말고, starter set / add-on에 대해 `지금 보낸다`, `redact/excerpt 후 보낸다`, `이번 라운드에서는 보내지 않는다` 수준의 최종 판단을 더 직접적으로 내릴 수 있게 정리한다. 표현은 문서 톤에 맞게 조정해도 되지만, sender가 행동으로 옮길 수 있어야 한다.
- `docs/evidence-report-export-bundle.md`의 shareability note(`External default`, `External selective`, `Internal-first / excerpt`, `Internal only unless governance review`)와 add-on 설명은 checklist final judgment와 같은 행동 기준을 말해야 한다. label만 설명하는 데 그치지 말고, sender가 실제로 언제 어떤 묶음을 꺼내는지 연결되게 한다.
- audit trail / governance 자료는 여전히 selective 또는 excerpt-first여야 하며, `coordination/` 전체를 통째로 보내는 방향으로 흐르면 안 된다.
- `README.md`, `docs/portfolio-case-study.md`, `docs/demo-showcase-walkthrough.md` 수정이 필요하더라도 새 claim 추가나 구조 재작성으로 번지지 않게 한다. 링크/상태 문구 수준의 최소 정렬만 허용한다.
- 새 external-facing 문서, export 폴더/zip, screenshot, metrics, fabricated output은 만들지 않는다.
- parked fallback-warning 트랙은 이번 라운드에서도 default package content가 아니다.

## 수용 기준
1. `docs/proof-package-delivery-checklist.md`가 sender에게 starter set / add-on을 지금 보내도 되는지, redact/excerpt로 낮춰야 하는지, 보류해야 하는지를 더 직접적으로 판단하게 해 준다.
2. `docs/evidence-report-export-bundle.md`의 shareability taxonomy와 add-on 안내가 checklist final judgment와 같은 행동 기준을 말한다.
3. `README -> case study -> walkthrough -> evidence bundle` starter set 순서와 checklist canonical authority는 유지된다.
4. `README.md`, `docs/portfolio-case-study.md`, `docs/demo-showcase-walkthrough.md`가 수정되더라도 docs map / current-limits / follow-up link 수준의 최소 정렬에 머문다.
5. 새 claim, 새 evidence artifact, export 폴더/zip, screenshot, metrics, fabricated output, parked fallback-warning 전면화가 없다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- starter set / add-on package logic 재설계
- checklist canonical authority나 evidence bundle의 네 번째 문서/read-next 역할 재정의
- 새 external-facing 문서 종류 추가
- export 폴더/zip 생성
- live demo 재실행, screenshot 제작, metrics 생성
- README / case study / walkthrough / evidence bundle 전면 재작성
- 코드/설정 변경
- fallback-warning parked 트랙 재개

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- 목적은 판단 자동화를 주장하는 것이 아니라, 사람이 수행할 최종 judgment를 더 일관되게 지원하는 것이다
- external-facing copy는 과장보다 정합성을 우선한다. 실제로 보내지 않는 문서를 default attachment처럼 암시하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-060-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-060-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - checklist final judgment를 어떻게 더 직접적으로 만들었는지 요약
  - evidence bundle shareability taxonomy를 어떤 행동 기준으로 정렬했는지
  - `README.md` / `docs/portfolio-case-study.md` / `docs/demo-showcase-walkthrough.md` 수정 여부와 이유
  - 유지한 baseline과 건드리지 않은 이유
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안
