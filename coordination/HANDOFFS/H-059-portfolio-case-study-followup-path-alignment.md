# H-059 portfolio case study follow-up path alignment

Owner: WT-59 (`codex/h059-portfolio-case-study-followup-path-alignment`)
Priority: Highest

## 목표
- H-058에서 닫힌 sender-facing canonical flow를 baseline으로 유지한 채, `docs/portfolio-case-study.md`가 post-walkthrough follow-up path를 현재 branch 상태와 같은 방식으로 설명하게 만든다.
- case study가 sender-facing checklist를 실제 follow-up control doc로 직접 가리키고, `docs/evidence-report-export-bundle.md`를 네 번째 문서 이후의 detailed mapping / read-next reference로 구분하도록 최소 정렬한다.
- case study의 `Current Limits And Next Steps`가 starter set/add-on package logic 자체가 아직 미완료인 것처럼 읽히지 않게 좁히고, 남은 작업을 shareability/redaction 최종 판단과 의미 품질 운영 점검으로 재고정한다.

## 작업 범위
- 신규/수정 허용:
  - `docs/portfolio-case-study.md`
  - `README.md` (필요 시 docs map / current-limits 수준의 최소 링크 또는 상태 문구 정렬만 허용)
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
  - `docs/proof-package-delivery-checklist.md`
  - `docs/evidence-report-export-bundle.md`
  - `docs/demo-showcase-walkthrough.md`
  - `README.md`
  - `coordination/REPORTS/H-058-result.md`
  - `coordination/REPORTS/H-058-review.md`
  - `coordination/RELAYS/H-058-review-to-main.md`
  - `coordination/REPORTS/H-056-result.md`
  - `coordination/REPORTS/H-056-review.md`

## 구현 지침
- H-058에서 고정한 checklist canonical authority와 evidence bundle의 네 번째 문서/read-next 역할은 baseline으로 유지한다. 이번 라운드는 그 baseline을 case study 서사에 반영하는 최소 정렬이지, proof package logic 재설계가 아니다.
- `docs/portfolio-case-study.md`는 walkthrough 이후 follow-up path를 설명할 때 `docs/proof-package-delivery-checklist.md`를 sender-facing control doc로 먼저 가리키고, `docs/evidence-report-export-bundle.md`는 detailed mapping / read-next reference로 뒤에 두어야 한다.
- `Current Limits And Next Steps`에서는 starter set/add-on 설명 자체가 아직 미완료인 것처럼 읽히는 문구를 줄이고, 남은 작업을 shareability/redaction 최종 판단, 문맥별 전달 밀도 조절, 생성 결과 의미 품질 운영 점검 쪽으로 재정렬한다.
- `README.md` 수정이 필요하더라도 새 claim 추가나 구조 재작성으로 번지지 않게 한다. docs map 또는 current-limits 수준의 최소 정렬만 허용한다.
- 새 external-facing 문서, export 폴더/zip, screenshot, metrics, fabricated output은 만들지 않는다.
- parked fallback-warning 트랙은 이번 라운드에서도 default package content가 아니다.

## 수용 기준
1. `docs/portfolio-case-study.md`가 sender-facing follow-up path를 `docs/proof-package-delivery-checklist.md` -> `docs/evidence-report-export-bundle.md` 순서로 직접 가리킨다.
2. case study가 evidence bundle을 starter set의 네 번째 문서 이후에 보는 detailed mapping / read-next reference로 설명하며, checklist authority와 충돌하지 않는다.
3. case study의 `Current Limits And Next Steps`가 H-058 이후에도 starter set/add-on package logic 자체가 열린 과제처럼 읽히지 않는다.
4. `README.md`가 수정되더라도 docs map / current-limits 수준의 최소 정렬에 머문다.
5. 새 claim, 새 evidence artifact, export 폴더/zip, screenshot, metrics, fabricated output, parked fallback-warning 전면화가 없다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- proof package checklist / evidence bundle canonical flow 재설계
- 새 external-facing 문서 종류 추가
- export 폴더/zip 생성
- live demo 재실행, screenshot 제작, metrics 생성
- README / case study 전면 재작성
- 코드/설정 변경
- fallback-warning parked 트랙 재개

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- 이번 라운드의 목적은 새 narrative를 여는 것이 아니라, 이미 닫힌 follow-up path를 second-layer narrative에도 같은 방식으로 반영하는 것이다
- external-facing copy는 과장보다 정합성을 우선한다. checklist/evidence bundle의 역할을 뒤집거나 실제로 없는 전달 artifact를 암시하지 않는다

## 보고서
- 완료 시 `coordination/REPORTS/H-059-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-059-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - case study follow-up path를 어떻게 정렬했는지 요약
  - `Current Limits And Next Steps`를 어떤 상태로 좁혔는지
  - `README.md` 수정 여부와 이유
  - 유지한 baseline과 건드리지 않은 이유
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안
