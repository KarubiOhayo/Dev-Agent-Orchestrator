# [H-036] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-036-fallback-warning-keep-frozen-seeding-throughput-tracking.md`
- main relay: `coordination/RELAYS/H-036-main-to-executor.md`
- result: `coordination/REPORTS/H-036-result.md`

## 구현 요약
- 핵심 변경:
  - `scripts/seed-fallback-warning-workload.sh` 보강
    - `GRADLE_USER_HOME` 로컬 기본값(`.gradle-local`) 및 디렉터리 생성 추가
    - 요청 prefix/시작 로그의 `H-035` 하드코딩을 라운드 중립 문자열로 교체
  - H-036 시딩 throughput 실행
    - 1차: `direct=6`, `chain=3` (fail-fast, `CHAIN#3` 실패로 중단)
    - 2차: `direct=0`, `chain=1` 재시도 성공
  - 최신 14일 게이트/최근 7일 대비 delta 재집계 + 단일 판정(`KEEP_FROZEN`) 보고
- 변경 파일:
  - `scripts/seed-fallback-warning-workload.sh`
  - `coordination/REPORTS/H-036-result.md`
  - `coordination/RELAYS/H-036-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `GRADLE_USER_HOME=$PWD/.gradle-local ./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 시딩 본 실행 중 `CHAIN#3`에서 `All model candidates failed`로 fail-fast 중단(exit 1) 발생
  - 동일 정책으로 chain 재시도 1회 추가 실행해 체인 성공 증거 3건 확보

## 리뷰 집중 포인트
1. `scripts/seed-fallback-warning-workload.sh` 변경이 기존 출력 계약(records/summary/fail-fast)과 충돌하지 않는지
2. H-036 결과 보고의 runId/체인 매핑/`CHAIN_*_DONE` 이벤트 표가 실제 로그(`storage/fallback-warning-seed/seed-20260220-142752*`, `seed-20260220-143838*`)와 일치하는지
3. 재집계 수치(`INSUFFICIENT_SAMPLE_RATIO=0.9286`, `SUFFICIENT_DAYS=1`, `executionGapDelta=-26`, `chainShareGapDelta=-48.39%p`) 및 `resumeDecision=KEEP_FROZEN` 판정 근거가 타당한지

## 알려진 리스크 / 오픈 이슈
- 게이트 4개 중 2개(`INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`) 미충족이 지속되어 `RESUME_H024` 전환 근거는 아직 부족함.
- 모델 후보 실패가 간헐적으로 발생해 fail-fast 실행 중단 가능성은 잔존함(재시도로 완화).

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-036-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
