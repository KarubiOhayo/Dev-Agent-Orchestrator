# [H-038] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-038-fallback-warning-keep-frozen-seeding-failure-pattern-followup.md`
- 참고 result: `coordination/REPORTS/H-037-result.md`
- 참고 review: `coordination/REPORTS/H-037-review.md`
- 참고 relay: `coordination/RELAYS/H-037-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-038-fallback-warning-keep-frozen-seeding-failure-pattern-followup.md`
6. `coordination/REPORTS/H-037-result.md`, `coordination/REPORTS/H-037-review.md`, `coordination/RELAYS/H-037-review-to-main.md`, `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - `scripts/seed-fallback-warning-workload.sh` (필요 시)
  - `docs/cli-quickstart.md` (필요 시 최소 동기화)
  - `docs/code-agent-api.md` (필요 시 최소 동기화)
  - `docs/PROJECT_OVERVIEW.md` (필요 시 최소 동기화)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-038-result.md`
- `coordination/RELAYS/H-038-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-035.1/H-036/H-037에서 고정한 fail-fast 종료코드 계약(`runId` 누락 + `exit_code=0` 시 non-zero 강제)이 회귀되지 않도록 유지할 것.
- H-037 기준 미충족 신호(`INSUFFICIENT_SAMPLE_RATIO=0.9286`, `SUFFICIENT_DAYS=1`, 최근 3일 평균 전체 `parseEligibleRunCount=17.3333`)를 기준으로 후속 누적 효과를 정량 보고할 것.
- 체인 실패 케이스의 모델 후보 실패 원인(`temperature` 파라미터 비호환, 모델 미존재, all candidates failed) 재발 빈도와 완화 가이드 반영 여부를 결과 보고에 명시할 것.
