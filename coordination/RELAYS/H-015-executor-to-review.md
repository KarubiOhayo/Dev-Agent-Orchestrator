# [H-015] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-015-fallback-warning-calibration-prep.md`
- main relay: `coordination/RELAYS/H-015-main-to-executor.md`
- result: `coordination/REPORTS/H-015-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 `실측 보정 준비 체크 (H-015)` 섹션 추가
    - 최근 14일(KST) 가용성 점검 절차
    - `집계 성공`/`집계 불가`/`INSUFFICIENT_SAMPLE` 분류 기준
    - 수치 변경 없이 보정 후보 수집 항목(분포/연속 WARNING/급증 빈도) 명시
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 보강
    - 최근 14일 집계 성공/실패 일수, `INSUFFICIENT_SAMPLE` 일수/비율, `집계 불가` 원인 분류 출력 항목 추가
    - 임계치 보정 판단 보류 조건(유효 샘플 부족/집계 불가 과다) 명시
    - 임계치/알림 수치(`0.05`, `0.15`, `0.10`) 유지 원칙 명시
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-015-result.md`
  - `coordination/RELAYS/H-015-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`에 14일 가용성 점검/집계 불가 분류/보정 후보 수집 절차가 handoff 요구 수준으로 구체화되었는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 14일 성공/실패/샘플 부족 및 보정 보류 조건이 보고 형식까지 일관되게 반영됐는지
3. 임계치/알림 수치(`0.05`, `0.15`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙, 이벤트/모수 계약이 변경되지 않았는지

## 알려진 리스크 / 오픈 이슈
- 실제 운영 데이터에서 `집계 불가` 또는 `INSUFFICIENT_SAMPLE` 비중이 높으면 후속 임계치 보정 라운드의 결론이 보류될 가능성이 있음
- 본 라운드는 준비 단계 문서화 범위로, 임계치/알림 룰 수치 조정은 수행하지 않음

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-015-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
