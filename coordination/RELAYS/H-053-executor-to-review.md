# [H-053] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-053-demo-showcase-walkthrough-packaging.md`
- main relay: `coordination/RELAYS/H-053-main-to-executor.md`
- result: `coordination/REPORTS/H-053-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/demo-showcase-walkthrough.md`를 새로 작성해 external-facing demo path, live demo 준비사항, dry-run/chain-aware CLI 예시, 관찰 포인트, guardrail, read-next 링크를 고정했습니다.
  - `README.md`에는 walkthrough 링크를 docs map에 추가하고 current-limits 문구를 walkthrough 존재 상태에 맞게 최소 수정했습니다.
  - `docs/portfolio-case-study.md`에는 guided demo path 링크와 남은 packaging 우선순위를 최소 수정으로 반영했습니다.
- 변경 파일:
  - `docs/demo-showcase-walkthrough.md`
  - `README.md`
  - `docs/portfolio-case-study.md`
  - `coordination/REPORTS/H-053-result.md`
  - `coordination/RELAYS/H-053-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 테스트 실패 없음
  - walkthrough 자체는 문서 산출물이라 서사 품질/과장 여부는 source 대조 리뷰가 중요함

## 리뷰 집중 포인트
1. `docs/demo-showcase-walkthrough.md`의 명령 예시와 관찰 포인트가 `README.md`, `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `./devagent help` 기준으로 실제 표면과 맞는지
2. live demo guardrail(`apply=false` 기본, `--fail-on-chain-failures true`, parked fallback-warning 비전면화, fabricated output 금지)이 충분히 분명한지
3. `README.md`와 `docs/portfolio-case-study.md` 수정이 handoff 범위 안에서 walkthrough 발견성과 상태 정렬에 필요한 최소 변경으로 머무는지

## 알려진 리스크 / 오픈 이슈
- `evidence / report export bundle`은 여전히 후속 라운드가 필요합니다.
- API 키나 공급자 응답 상태에 따라 live CLI demo는 편차가 있을 수 있어, walkthrough에 `./devagent help` 기반 fallback path를 함께 넣었습니다.
- 문서성 산출물이므로 실제 설득력과 과장 여부는 리뷰 스레드의 source 대조가 핵심입니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-053-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
