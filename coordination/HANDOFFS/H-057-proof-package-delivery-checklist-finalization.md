# H-057 proof package delivery checklist finalization

Owner: WT-57 (`codex/h057-proof-package-delivery-checklist`)
Priority: Highest

## 목표
- H-055/H-056에서 닫힌 external-facing proof package baseline을 유지한 채, 실제 발송자가 바로 재사용할 수 있는 sender-facing delivery checklist를 만든다.
- `starter set -> technical deep-dive add-on -> audit trail add-on -> governance add-on` 4개 묶음 package logic를 "무엇을 먼저 보내고, 어떤 질문에 무엇을 추가하며, 무엇을 보내지 말아야 하는가"라는 운영 판단으로 압축한다.
- shareability / redaction / stale-reference / 최신 테스트 근거 확인 같은 발송 직전 게이트를 짧고 반복 가능한 형태로 고정한다.

## 작업 범위
- 신규/수정 허용:
  - `docs/proof-package-delivery-checklist.md` (신규)
  - `docs/evidence-report-export-bundle.md` (필요 시 checklist 링크 또는 짧은 cross-reference 수준의 최소 수정만 허용)
  - `docs/demo-showcase-walkthrough.md` (필요 시 post-walkthrough handoff 링크 수준의 최소 수정만 허용)
  - `README.md` (필요 시 docs map 또는 current-limits 수준의 최소 링크 수정만 허용)
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
  - `docs/evidence-report-export-bundle.md`
  - `docs/demo-showcase-walkthrough.md`
  - `docs/portfolio-case-study.md`
  - `docs/codex-ops-workflow.md`
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
  - `coordination/REPORTS/H-055-result.md`
  - `coordination/REPORTS/H-055-review.md`
  - `coordination/REPORTS/H-056-result.md`
  - `coordination/REPORTS/H-056-review.md`
  - `coordination/RELAYS/H-056-review-to-main.md`

## 구현 지침
- checklist는 새로운 narrative 문서가 아니라, 이미 존재하는 starter set / add-on guide를 실제 발송 동작으로 압축한 운영 문서여야 한다.
- 문서는 아래 질문에 짧게 답할 수 있어야 한다.
  1. 첫 발송에서 기본으로 보내는 것은 무엇인가?
  2. 상대 질문이 CLI/API surface, provenance, governance로 갈릴 때 각각 어떤 add-on을 붙이는가?
  3. 보내기 전에 어떤 shareability / redaction / stale check를 확인해야 하는가?
  4. 보내지 말아야 할 항목은 무엇인가?
  5. 현재 branch 상태와 증빙 최신성을 어떻게 빠르게 확인하는가?
- 권장 섹션:
  1. When to use / who uses it
  2. Pre-send gate
  3. Default send package (`starter set`)
  4. Add-on decision matrix (`technical deep-dive`, `audit trail`, `governance`)
  5. Do-not-send / honesty guardrail
  6. Maintenance / stale check
- checklist는 한 화면이나 한 장에서 훑을 수 있을 정도로 짧게 유지한다. 이미 상세히 설명된 package logic를 길게 다시 쓰지 말고, 자세한 설명은 `docs/evidence-report-export-bundle.md`로 넘긴다.
- H-055/H-056에서 닫힌 4개 묶음 package logic와 `docs/codex-ops-workflow.md` governance add-on 분류는 baseline으로 유지한다. 이번 라운드에서 bundle 체계를 다시 설계하지 않는다.
- parked fallback-warning 트랙은 이번 라운드에서도 default package content가 아니다.
- 새 export 폴더/zip, 실제 live demo 산출물, screenshot, metrics, fabricated output은 만들지 않는다.
- `README.md`, walkthrough, evidence bundle 수정이 있더라도 새 claim 추가가 아니라 checklist 발견성과 read-next 정렬 수준의 최소 수정에 머문다.

## 수용 기준
1. `docs/proof-package-delivery-checklist.md`가 생성되고, 실제 발송자가 starter set과 각 add-on을 어떤 기준으로 선택하는지 한 장에서 재현 가능하게 설명한다.
2. checklist에 shareability / redaction / stale-reference / 최신 테스트 근거 확인 같은 pre-send gate가 포함된다.
3. checklist가 `technical deep-dive add-on`, `audit trail add-on`, `governance add-on`의 질문별 트리거를 분명히 구분한다.
4. `docs/evidence-report-export-bundle.md`, `docs/demo-showcase-walkthrough.md`, `README.md`가 수정될 경우 checklist로 이어지는 최소 링크 수준에 머문다.
5. 새 evidence artifact, export 폴더/zip, screenshot, metrics, fabricated output, parked fallback-warning 전면화가 없다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- proof package 4개 묶음 재설계
- 새 external-facing 문서 종류 추가
- export 폴더/zip 생성
- live demo 재실행, screenshot 제작, metrics 생성
- README / case study / walkthrough / evidence bundle 전면 재작성
- 코드/설정 변경
- fallback-warning parked 트랙 재개

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- checklist는 "새 증거를 만드는 문서"가 아니라 "이미 있는 증거를 어떤 순서와 기준으로 보낼지 정하는 문서"여야 한다
- 발송 가이드는 과장보다 정합성을 우선한다. 보내지 않은 artifact를 보냈다고 암시하지 말고, test evidence와 날짜도 필요 시 그대로 드러낸다

## 보고서
- 완료 시 `coordination/REPORTS/H-057-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-057-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - checklist 섹션별 구성 요약
  - starter set / add-on 질문 매핑 요약
  - pre-send gate를 어떤 항목으로 고정했는지
  - 링크 수정 여부와 이유
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안
