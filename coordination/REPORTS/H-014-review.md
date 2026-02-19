# H-014 리뷰 보고서

## 대상
- handoff: `coordination/HANDOFFS/H-014-fallback-warning-baseline-alignment.md`
- result: `coordination/REPORTS/H-014-result.md`
- relay: `coordination/RELAYS/H-014-executor-to-review.md`

## Findings (P1 > P2 > P3)
### P2. `parseEligibleRunCount`의 Code 모수 정의가 체인 실행 경로와 충돌
- 문서는 모수를 "agent 서비스 run(직접 호출 + 체인 호출 포함)"으로 정의하면서도, Code는 직접 `POST /api/agents/code/generate` 호출 run만 포함하는 것으로 표기되어 있습니다.
  - `docs/code-agent-api.md:126`
  - `docs/code-agent-api.md:127`
- 하지만 실제 구현에서 Spec 체인(`chainToCode=true`)은 내부적으로 `CodeAgentService.generate(...)`를 호출해 Code run을 생성하며, 해당 run에서 `CODE_OUTPUT_FALLBACK_WARNING`이 기록될 수 있습니다.
  - `src/main/java/me/karubidev/devagent/agents/spec/SpecCodeChainService.java:54`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java:87`
  - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java:122`
- 영향: 운영자가 Code 모수를 "직접 API 호출 run"으로만 집계하면 Spec 체인으로 발생한 Code run이 분모에서 누락되어 Code `warningRate`가 과대 계산될 수 있습니다.
- 권고: Code 모수 정의를 체인 실행 포함 기준으로 정렬하거나, 의도적으로 제외할 경우 집계 단위를 "endpoint 호출 run"으로 명확히 재정의해 충돌을 제거해야 합니다.

## 검증 근거 (파일/라인)
1. H-014 변경 범위 및 테스트 게이트 보고
- `coordination/REPORTS/H-014-result.md:7`
- `coordination/REPORTS/H-014-result.md:47`
- `coordination/REPORTS/H-014-result.md:48`
- `coordination/RELAYS/H-014-executor-to-review.md:21`
- `coordination/RELAYS/H-014-executor-to-review.md:22`

2. `INSUFFICIENT_SAMPLE` 제외 규칙 템플릿 반영 확인
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:27`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:55`

3. 공통 파일/코드 변경 없음(Executor 보고 인용)
- `coordination/REPORTS/H-014-result.md:43`
- `coordination/REPORTS/H-014-result.md:55`

## 심각도 집계
- P1: 0
- P2: 1
- P3: 0

## 수용기준 검증
1. `docs/code-agent-api.md` 직접 호출/체인 호출 포함 기준 명시: **부분 충족** (Code 모수 정의와 체인 실행 경로 간 충돌 잔존)
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md` `INSUFFICIENT_SAMPLE` 제외 규칙 명시: **충족**
3. H-013 리뷰 P2/P3 지적 사항 해소: **부분 충족** (Doc/Review 해소, Code 모수 해석 충돌 신규 확인)
4. 코드/설정 변경 없음: **충족** (Executor 보고 인용)
5. `./gradlew clean test --no-daemon` 통과: **충족** (`BUILD SUCCESSFUL`, Executor 보고 인용)

## 승인 게이트 체크
- 테스트 게이트 상태(Executor 보고 인용):
  - `./gradlew clean test --no-daemon` -> `BUILD SUCCESSFUL`
  - 근거: `coordination/REPORTS/H-014-result.md:47`, `coordination/REPORTS/H-014-result.md:48`
- Review-Control 제약상 테스트 재실행은 수행하지 않았고, 결과 리포트/릴레이/구현 근거를 대조 검증했습니다.
- 공통 파일 변경 승인 절차 준수 여부:
  - 공통 승인 대상(`application.yml`, 공용 모델/계약, 빌드 설정) 변경 없음(Executor 보고 인용)
  - 근거: `coordination/REPORTS/H-014-result.md:55`

## 리뷰 결론
- 리스크 수준: `MEDIUM`
- 최종 권고: `Conditional Go`
- 조건:
  1. `docs/code-agent-api.md`의 Code `parseEligibleRunCount` 정의를 Spec 체인 실행(`chainToCode`) 포함 여부와 일치하도록 명시 정렬
