# [H-038] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-038-fallback-warning-keep-frozen-seeding-failure-pattern-followup.md`
- main relay: `coordination/RELAYS/H-038-main-to-executor.md`
- result: `coordination/REPORTS/H-038-result.md`

## 구현 요약
- 핵심 변경:
  - fail-fast 유지(`SEED_FAIL_FAST=true`) 상태로 시딩 수행
    - 진단 배치: `direct=1`, `chain=1` 성공
    - 본 배치: `direct=6` 성공, `chain=3`는 첫 시도 fail-fast 중단 후 `chain=1` 재시도 배치로 보강
  - 이번 라운드 실행 총계: `direct 성공 7`, `chain 성공 3`, `chain 실패 4` (모두 non-zero)
  - fail-fast 실패 원인 분류(`TEMPERATURE_UNSUPPORTED`, `MODEL_NOT_FOUND_OR_UNAVAILABLE`, `ALL_CANDIDATES_FAILED`, `OTHER`) 표를 결과 보고에 반영
  - 운영 완화 가이드 동기화: `docs/cli-quickstart.md`에 `fail-fast 실패 패턴 점검 가이드 (H-038)` 추가
  - 최신 14일 게이트/최근 7일 vs 직전 7일 delta 재집계 + 단일 판정(`KEEP_FROZEN`) 보고
- 변경 파일:
  - `docs/cli-quickstart.md`
  - `coordination/REPORTS/H-038-result.md`
  - `coordination/RELAYS/H-038-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - fail-fast 체인 재시도 4건에서 `runId` 미생성 + `exit=1` 재발
  - 공통 메시지: `All model candidates failed`, `temperature` 비호환, `not_found_error(model: claude-sonnet-4.5)`
  - 재시도로 유효 chain 증거 3건을 확보해 목표(`direct>=6`, `chain>=3`)는 충족

## 리뷰 집중 포인트
1. `coordination/REPORTS/H-038-result.md`의 runId/체인 매핑/`CHAIN_*_DONE` 표가 `storage/fallback-warning-seed/seed-20260220-152857*`, `seed-20260220-153837*`, `seed-20260220-154214*`와 일치하는지
2. 실패 원인 분류 count(`각 4건`)와 최신 근거(`seed-20260220-154023-chain-1.stdout.json`)가 분류 규칙과 정합한지
3. 게이트 재집계(`INSUFFICIENT_SAMPLE_RATIO=0.9286`, `SUFFICIENT_DAYS=1`) 및 `resumeDecision=KEEP_FROZEN` 판단이 타당한지

## 알려진 리스크 / 오픈 이슈
- 게이트 4개 중 2개(`INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`) 미충족으로 `RESUME_H024` 전환 근거는 아직 부족합니다.
- fail-fast 체인 경로에서 후보 모델 전부 실패가 반복적으로 재발합니다(이번 라운드 `4건`).

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-038-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
