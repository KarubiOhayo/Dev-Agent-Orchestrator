# [H-014] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-014-fallback-warning-baseline-alignment.md`
- main relay: `coordination/RELAYS/H-014-main-to-executor.md`
- result: `coordination/REPORTS/H-014-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`의 `parseEligibleRunCount`를 agent 서비스 run 기준으로 보정
    - 기준: 직접 호출 run + 체인 호출 run 포함
    - Doc/Review는 직접 호출 + Code 체인 호출을 모수에 포함하도록 명시
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `INSUFFICIENT_SAMPLE` 제외 규칙 명시
    - `parseEligibleRunCount < 20`은 임계치 판정/알림 계산에서 제외
    - 보고 형식에 `INSUFFICIENT_SAMPLE` 제외 내역 항목 추가
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 모수 정의가 DOC/REVIEW 체인 호출 포함 기준으로 해석 모호성 없이 고정되었는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에서 `INSUFFICIENT_SAMPLE` 임계치/알림 제외 규칙과 보고 항목이 함께 반영되었는지
3. 임계치 수치(`0.05`, `0.15`)와 알림 룰 정의가 변경되지 않았는지

## 알려진 리스크 / 오픈 이슈
- 임계치/알림 룰은 초기 기준값이므로 실측 데이터 분포에 따라 후속 보정 필요성이 남아 있음
- 문서 정합화 라운드로 코드 동작 변경은 없음

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-014-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
