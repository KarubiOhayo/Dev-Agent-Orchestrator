# [H-011] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-011-prompt-assets-spec-doc-review.md`
- 참고 result(있으면): `coordination/REPORTS/H-010-1-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-010-1-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-010-1-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-011-prompt-assets-spec-doc-review.md`
5. `coordination/REPORTS/H-010-1-result.md`, `coordination/REPORTS/H-010-1-review.md`, `coordination/RELAYS/H-010-1-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-011-prompt-assets-spec-doc-review.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-011-result.md`
- `coordination/RELAYS/H-011-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - spec/doc/review 프롬프트는 반드시 JSON 단일 객체 출력 규칙과 파서 스키마 키를 명시할 것
  - 기존 code 프롬프트 계약(`prompts/agents/code.md`)은 회귀 없이 유지할 것
- 알려진 리스크:
  - 에이전트별 프롬프트 부재/약화 시 parser fallback 비율이 재상승할 수 있음
  - 프롬프트 문구가 과도하게 장문이면 모델이 스키마 외 텍스트를 출력할 가능성이 있음
- 리뷰 집중 포인트:
  - `PromptRegistry` 조립 결과에 spec/doc/review `AGENT_BASE`가 실제 반영되는지 테스트로 고정되었는지 여부
  - 에이전트별 스키마 키 지시와 현재 파서 정규화 키가 일치하는지 여부
