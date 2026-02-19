# [H-010.1] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-010-1-error-contract-alignment.md`
- 참고 result(있으면): `coordination/REPORTS/H-010-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-010-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-010-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-010-1-error-contract-alignment.md`
5. `coordination/REPORTS/H-010-result.md`, `coordination/REPORTS/H-010-review.md`, `coordination/RELAYS/H-010-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-010-1-error-contract-alignment.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-010-1-result.md`
- `coordination/RELAYS/H-010-1-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - 성공 응답 계약(runId/결과 payload/`chainFailures[]`)은 절대 변경하지 말 것
  - 복합 필수조건 계약 추가 시 단일 필드 누락 계약 하위 호환 유지
- 알려진 리스크:
  - 오류 분류가 메시지 패턴 의존 시 프레임워크/문구 변경에 취약할 수 있음
  - 문서와 구현의 오류 코드 집합이 다시 분리될 가능성
- 리뷰 집중 포인트:
  - `INVALID_JSON_REQUEST` 포함 여부 등 400 코드 매핑의 문서-구현 정합성
  - 복합 필수조건(`any-of`) 오류의 `code/details[]` 구조가 테스트로 고정됐는지 여부
