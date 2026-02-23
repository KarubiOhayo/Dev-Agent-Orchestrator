# [H-039] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-039-fallback-warning-keep-frozen-resume-readiness-followup-check.md`
- main relay: `coordination/RELAYS/H-039-main-to-executor.md`
- result: `coordination/REPORTS/H-039-result.md`

## 구현 요약
- 핵심 변경:
  - fail-fast 유지(`SEED_FAIL_FAST=true`)로 진단 배치(`direct=1`, `chain=1`) 후 본 배치(`direct=6`, `chain=3`) 실행
  - 이번 라운드 실행 총계: `total=11`, `direct=7`, `chain=4`, `failed=0` (모든 실행 `exitCode=0`)
  - 기본 목표(`DIRECT >= 6`, `CHAIN >= 3`, 총 9회) 충족
  - `specRunId -> codeRunId -> docRunId/reviewRunId` 매핑 4건 및 `CHAIN_CODE_DONE/CHAIN_DOC_DONE/CHAIN_REVIEW_DONE` 이벤트 근거 확보
  - 최신 14일 게이트/최근 7일 vs 직전 7일 delta 재집계 + H-036~H-039 추세 비교 + 단일 판정(`KEEP_FROZEN`) 보고
- 변경 파일:
  - `coordination/REPORTS/H-039-result.md`
  - `coordination/RELAYS/H-039-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 이번 라운드 fail-fast 실패(non-zero) 케이스 없음(`failedRuns=0`)
  - 게이트 4개 중 2개(`INSUFFICIENT_SAMPLE_RATIO=0.8571`, `SUFFICIENT_DAYS=2`) 미충족으로 `resumeDecision=KEEP_FROZEN` 유지

## 리뷰 집중 포인트
1. `coordination/REPORTS/H-039-result.md`의 runId/체인 매핑/`CHAIN_*_DONE` 표가 `storage/fallback-warning-seed/seed-20260223-134151*`, `seed-20260223-134407*`, `seed-20260223-134509*`, `storage/fallback-warning-seed/h039-metrics.json`과 일치하는지
2. 게이트 재집계(`INSUFFICIENT_SAMPLE_RATIO=0.8571`, `SUFFICIENT_DAYS=2`) 및 최근 7일/직전 7일 delta(`executionGapDelta=-135`, `chainShareGapDelta=-43.57%p`) 계산이 타당한지
3. H-036~H-039 추세 판독(정체/부분 개선)과 최종 판정(`KEEP_FROZEN`)이 handoff 수용기준/정책 고정 항목과 충돌 없는지

## 알려진 리스크 / 오픈 이슈
- 게이트 2개 미충족(`INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`)으로 H-024 재개 근거는 여전히 부족합니다.
- 최근 3일 평균 전체 모수(`9.0000`)가 기준(`>=32`)에 미달해, 단기 실행 공백 시 개선 추세가 다시 약화될 수 있습니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-039-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
