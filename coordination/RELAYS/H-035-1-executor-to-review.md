# [H-035.1] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-035-1-fallback-warning-seeding-fail-fast-exitcode-hardening.md`
- main relay: `coordination/RELAYS/H-035-1-main-to-executor.md`
- result: `coordination/REPORTS/H-035-1-result.md`

## 구현 요약
- 핵심 변경:
  - `scripts/seed-fallback-warning-workload.sh` fail-fast 분기에서 종료코드 계산 보강
  - `runId` 누락 + `exit_code=0` 실패 시 종료코드를 `1`로 치환해 non-zero 강제
  - `exit_code != 0` 실패는 기존 종료코드를 유지
  - non fail-fast 경로(실패 카운트 후 계속 실행)는 기존 동작 유지
- 변경 파일:
  - `scripts/seed-fallback-warning-workload.sh`
  - `coordination/REPORTS/H-035-1-result.md`
  - `coordination/RELAYS/H-035-1-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - fail-fast/non fail-fast 시나리오는 임시 stub 하네스로 재현 검증(`/tmp/h0351-evidence-iuhY`)

## 리뷰 집중 포인트
1. `scripts/seed-fallback-warning-workload.sh`에서 fail-fast 활성 시 성공 종료(`0`)가 더 이상 가능하지 않은지 (`runId` 누락 + `exit_code=0` 케이스)
2. fail-fast에서 명령 실패(`exit_code != 0`)가 기존 종료코드로 유지되는지 (`CASE_B_EXIT=7` 근거)
3. non fail-fast에서 실패 누적 후 후속 실행 지속(`DIRECT#1`, `DIRECT#2`)이 유지되는지

## 알려진 리스크 / 오픈 이슈
- 스크립트 전용 자동화 테스트 프레임워크는 아직 없어 Bash 수준 재현 검증에 의존함.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-035-1-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
