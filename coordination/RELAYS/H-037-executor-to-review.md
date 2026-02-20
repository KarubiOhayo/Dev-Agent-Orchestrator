# [H-037] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-037-fallback-warning-keep-frozen-seeding-followup-hygiene.md`
- main relay: `coordination/RELAYS/H-037-main-to-executor.md`
- result: `coordination/REPORTS/H-037-result.md`

## 구현 요약
- 핵심 변경:
  - `.gitignore`에 `.gradle-local/`를 추가해 워크트리 untracked 노이즈(P3)를 정합화
  - fail-fast 유지(`SEED_FAIL_FAST=true`) 상태로 시딩 실행
    - 진단 실행: `direct=1`, `chain=1`에서 `CHAIN#1 exit=1` 재현(non-zero 증빙)
    - 본 실행: `direct=6`, `chain=0` 성공
    - 본 실행: `direct=0`, `chain=3` 성공
  - 최신 14일 게이트/최근 7일 vs 직전 7일 delta 재집계 + 단일 판정(`KEEP_FROZEN`) 보고
- 변경 파일:
  - `.gitignore`
  - `coordination/REPORTS/H-037-result.md`
  - `coordination/RELAYS/H-037-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - fail-fast 진단 배치(`seed-20260220-150422`)에서 `CHAIN#1`이 `exit=1`로 중단됨
  - 오류 원인: `All model candidates failed` (OpenAI `gpt-5.2-codex`의 `temperature` 파라미터 미지원 포함)
  - 동일 fail-fast 정책 유지 상태에서 본 실행 배치를 분리해 목표(`direct 6`, `chain 3`)를 충족

## 리뷰 집중 포인트
1. `.gitignore`의 `.gradle-local/` 추가가 H-036 리뷰 P3 지적(워크트리 노이즈)을 충분히 해소하는지
2. `coordination/REPORTS/H-037-result.md`의 runId/매핑/`CHAIN_*_DONE` 표가 `storage/fallback-warning-seed/seed-20260220-150659*`, `seed-20260220-150804*` 근거와 정확히 일치하는지
3. 재집계 수치(`INSUFFICIENT_SAMPLE_RATIO=0.9286`, `SUFFICIENT_DAYS=1`, `executionGapDelta=-47`, `chainShareGapDelta=-46.15%p`)와 `resumeDecision=KEEP_FROZEN` 판정 근거가 타당한지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 게이트 4개 중 2개(`INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`) 미충족이 지속되어 `RESUME_H024` 전환 근거는 아직 부족합니다.
- fail-fast 경로의 모델 후보 실패가 간헐적으로 재발할 수 있습니다(진단 배치 `CHAIN#1 exit=1` 재현).

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-037-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
