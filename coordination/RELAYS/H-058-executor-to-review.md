# [H-058] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-058-proof-package-checklist-canonical-flow-alignment.md`
- main relay: `coordination/RELAYS/H-058-main-to-executor.md`
- result: `coordination/REPORTS/H-058-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/proof-package-delivery-checklist.md`에서 checklist를 sender-facing canonical authority로 명시하고 starter set send order를 `README -> case study -> walkthrough -> evidence bundle`로 고정했습니다.
  - 같은 문서에서 evidence bundle을 첫 앵커/cover note가 아니라 네 번째 문서이자 detailed mapping / read-next reference로 재정의했습니다.
  - `docs/evidence-report-export-bundle.md`의 starter set 설명, source-of-truth mapping, export order, walkthrough 후 사용 흐름을 모두 checklist 기준 순서로 재정렬했습니다.
  - 두 문서의 maintenance / stale check에 `README.md`, `docs/portfolio-case-study.md`, `docs/demo-showcase-walkthrough.md`, `docs/evidence-report-export-bundle.md` 4문서 drift check와 checklist authority 정합 확인을 반영했습니다.
- 변경 파일:
  - `docs/proof-package-delivery-checklist.md`
  - `docs/evidence-report-export-bundle.md`
  - `coordination/REPORTS/H-058-result.md`
  - `coordination/RELAYS/H-058-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. checklist가 실제 canonical authority로 읽히는지, evidence bundle보다 우선하는 통제 문서 역할이 충분히 드러나는지
2. evidence bundle이 self-cover-note 성격을 버리고 starter set의 네 번째 문서 / detailed mapping reference로만 읽히는지
3. 두 문서의 maintenance / stale check가 starter set 4문서 drift와 checklist authority 정합을 동일하게 요구하는지

## 알려진 리스크 / 오픈 이슈
- 실제 외부 발송 직전 shareability / redaction 최종 판단은 여전히 사람 확인이 필요합니다.
- 날짜 기반 참조(`CURRENT_STATUS_2026-03-19`)가 바뀌면 두 문서의 stale check와 send-order 설명을 함께 재확인해야 합니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-058-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
