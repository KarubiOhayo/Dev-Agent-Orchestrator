# [H-035] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md`
- main relay: `coordination/RELAYS/H-035-main-to-executor.md` (미생성, handoff 기준 실행)
- result: `coordination/REPORTS/H-035-result.md`

## 구현 요약
- 핵심 변경:
  - `scripts/seed-fallback-warning-workload.sh` 신규 추가 (direct/chain 시딩, env 파라미터 제어, runId/체인 이벤트 추출, before/after 스냅샷)
  - `docs/cli-quickstart.md` H-035 traffic seeding 실행 섹션 추가
  - `docs/code-agent-api.md` H-035 섹션 추가 (데이터 생성 우선 원칙 + runId 매핑 규칙 + 실측 요약)
  - `docs/PROJECT_OVERVIEW.md` H-035 완료 항목 동기화
- 변경 파일:
  - `scripts/seed-fallback-warning-workload.sh`
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/REPORTS/H-035-result.md`
  - `coordination/RELAYS/H-035-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. 시딩 스크립트가 `specRunId -> codeRunId -> docRunId/reviewRunId` 매핑을 `run_events` 기반으로 정확히 수행하는지
2. 결과 보고의 `CHAIN_CODE_DONE`, `CHAIN_DOC_*`, `CHAIN_REVIEW_*` 근거가 `codeRunId` 기준으로 일관되게 제시되었는지
3. H-035 이후 단일 판정(`resumeDecision=KEEP_FROZEN`) 및 게이트/델타 수치가 문서(`docs/code-agent-api.md`)와 리포트(`coordination/REPORTS/H-035-result.md`) 간 정합한지

## 알려진 리스크 / 오픈 이슈
- 시딩으로 개선 신호는 확보했지만 `INSUFFICIENT_SAMPLE_RATIO=1.00`, `SUFFICIENT_DAYS=0`으로 재개 게이트 2종 미충족
- 최근 3일 평균 `parseEligibleRunCount=3.3333`으로 목표(`>=32`) 대비 여전히 낮음

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-035-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
