# [H-055] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-055-external-facing-proof-package-refinement.md`
- main relay: `coordination/RELAYS/H-055-main-to-executor.md`
- result: `coordination/REPORTS/H-055-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/evidence-report-export-bundle.md`의 package logic를 `starter set` / `technical deep-dive add-on` / `audit trail add-on` / `governance add-on` 4개 묶음으로 통일했습니다.
  - H-054 review P3였던 `docs/codex-ops-workflow.md` 분류 충돌을 해소하기 위해 source mapping, export order, folder layout, post-walkthrough usage flow 전부에서 이 문서를 `governance add-on`으로만 배치했습니다.
  - 질문별로 어떤 add-on을 붙일지 빠르게 판단할 수 있도록 `Bundle Contents Overview`와 질문-대응 표를 추가했습니다.
- 변경 파일:
  - `docs/evidence-report-export-bundle.md`
  - `coordination/REPORTS/H-055-result.md`
  - `coordination/RELAYS/H-055-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 테스트 실패 없음
  - docs-only refinement 라운드이므로 새 export 폴더/zip/screenshot/live evidence는 만들지 않았음

## 리뷰 집중 포인트
1. `docs/codex-ops-workflow.md`가 이제 하나의 묶음(`governance add-on`)으로만 분류돼 실제 전달 시 혼선이 사라졌는지
2. `starter set`, `technical deep-dive add-on`, `audit trail add-on`, `governance add-on` 경계가 질문별 첨부 판단 기준으로 충분히 재현 가능한지
3. README/case study/walkthrough는 건드리지 않고도 H-055 수용 기준이 충족되는지, 그리고 새 claim/새 artifact 없이 refinement에만 머물렀는지

## 알려진 리스크 / 오픈 이슈
- 실제 외부 발송 전 마지막 redaction/shareability 판단은 여전히 수동 체크가 필요함
- 후속 packaging 라운드에서 관련 문서가 다시 변하면, 이번에 고정한 4개 묶음 로직도 함께 재검증해야 함

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-055-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
