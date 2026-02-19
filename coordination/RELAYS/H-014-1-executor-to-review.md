# [H-014.1] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-014-1-code-parse-eligible-alignment.md`
- main relay: `coordination/RELAYS/H-014-1-main-to-executor.md`
- result: `coordination/REPORTS/H-014-1-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`의 `parseEligibleRunCount`에서 Code 모수를 직접 호출 + Spec `chainToCode=true` 체인 실행 포함으로 명시
  - 체인 포함 원칙(타 agent 흐름에서 내부 서비스 실행 시 해당 agent 모수 포함) 추가
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 동일한 agent별 모수 정의를 추가해 문서/템플릿 정합화
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-014-1-result.md`
  - `coordination/RELAYS/H-014-1-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 Code 모수 정의가 Spec 체인 실행 포함 기준으로 해석 여지 없이 고정됐는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 agent별 `parseEligibleRunCount` 정의가 API 문서와 일치하는지
3. 임계치/알림 룰 수치 및 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지

## 알려진 리스크 / 오픈 이슈
- 현재 라운드는 문서 정합화만 수행했으므로 임계치 타당성 자체는 운영 데이터 누적 후 별도 검증이 필요함

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-014-1-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
