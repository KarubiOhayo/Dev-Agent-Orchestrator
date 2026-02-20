# H-035 리뷰 보고서

## 대상
- handoff: `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md`
- result: `coordination/REPORTS/H-035-result.md`
- relay: `coordination/RELAYS/H-035-executor-to-review.md`

## Findings (P1 > P2 > P3)

### [P1] fail-fast 모드에서 `runId` 누락 실패가 성공(0)으로 종료될 수 있음
- 증상:
  - 스크립트는 `exit_code != 0` 또는 `runId` 누락(`-z "$run_id"`)을 실패로 판정한다.
  - 그러나 fail-fast 분기에서 프로세스 종료코드를 `exit_code` 그대로 사용해, `runId`가 비어 있어도 `exit_code=0`이면 `exit 0`으로 종료될 수 있다.
- 근거:
  - `scripts/seed-fallback-warning-workload.sh:471`
  - `scripts/seed-fallback-warning-workload.sh:475`
  - `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md:39`
- 영향:
  - 시딩 실패(`runId` 미확보)가 성공으로 오인될 수 있어, 운영/자동화가 증거 확보를 잘못 판정할 위험이 있다.
- 권고:
  - fail-fast 분기에서 `runId` 누락 시 종료코드를 최소 `1`로 강제(예: `exit_code==0`이면 `1`로 치환)하도록 보완 필요.

## 검증 근거 (파일/라인)
1. 결과 보고서에 H-035 필수 산출(변경 파일, runId 분류, 체인 매핑, 이벤트 근거, 게이트, 단일 판정, 테스트 게이트)이 포함되어 있음
- `coordination/REPORTS/H-035-result.md:14`
- `coordination/REPORTS/H-035-result.md:47`
- `coordination/REPORTS/H-035-result.md:56`
- `coordination/REPORTS/H-035-result.md:63`
- `coordination/REPORTS/H-035-result.md:88`
- `coordination/REPORTS/H-035-result.md:113`
- `coordination/REPORTS/H-035-result.md:127`

2. 문서 동기화 범위가 handoff 지시 범위와 정합함
- `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md:12`
- `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md:14`
- `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md:52`
- `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md:53`
- `docs/cli-quickstart.md:127`
- `docs/code-agent-api.md:1495`

3. 공통 승인 대상 파일 변경 없음(결과 보고 인용)
- `coordination/REPORTS/H-035-result.md:137`

## 심각도 집계
- P1: 1
- P2: 0
- P3: 0

## 수용기준 검증
1. `scripts/seed-fallback-warning-workload.sh` 추가 및 direct/chain 시딩 재현 가능 실행: **충족**
2. 결과 보고 runId 및 `specRunId -> codeRunId -> docRunId/reviewRunId` 매핑 표: **충족**
3. `CHAIN_DOC_*`/`CHAIN_REVIEW_*` 이벤트 근거(`codeRunId` 기준) 포함: **충족**
4. 최신 14일 게이트 + 최근 7일 대비 변화량 포함: **충족**
5. `docs/cli-quickstart.md`, `docs/code-agent-api.md` 동기화: **충족**
6. fallback-warning 계약 필드 신규 추가 없음: **충족**
7. 금지 파일 변경 없음: **충족**
8. `./gradlew clean test --no-daemon` 통과 보고: **충족** (Executor 결과 보고 인용)
- 단, 스크립트 fail-fast 종료코드 경로에 P1 결함이 존재해 운영 신뢰성 관점에서 보완 필요

## 승인 게이트 체크
- 테스트 게이트 상태(Executor 보고 인용):
  - `./gradlew clean test --no-daemon` -> `BUILD SUCCESSFUL`
  - 근거: `coordination/REPORTS/H-035-result.md:128`, `coordination/REPORTS/H-035-result.md:129`
- Review-Control 제약상 테스트 재실행은 수행하지 않았고, result/relay/실제 변경 파일 대조로 판단함.
- 공통 파일 변경 승인 절차 준수 여부:
  - 공통 승인 대상 파일 변경 없음
  - 근거: `coordination/REPORTS/H-035-result.md:137`

## 리뷰 결론
- 리스크 수준: `HIGH`
- 최종 권고: `No-Go` (P1 보완 후 재검토 권장)
