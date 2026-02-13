# Relay Prompt Rules

스레드 간 전달 프롬프트는 이 폴더에 라운드 단위로 저장한다.

## 파일 규칙
- Main -> Executor: `H-00N-main-to-executor.md`
- Executor -> Review: `H-00N-executor-to-review.md`
- Review -> Main: `H-00N-review-to-main.md`
- 템플릿:
  - `TEMPLATE-main-to-executor.md`
  - `TEMPLATE-executor-to-review.md`
  - `TEMPLATE-review-to-main.md`

## 생성 타이밍
1. Main은 다음 라운드 시작 시 handoff 확정 직후 `H-00N-main-to-executor.md`를 생성한다.
2. Executor는 `coordination/REPORTS/H-00N-result.md` 작성 직후 `H-00N-executor-to-review.md`를 생성한다.
3. Review는 `coordination/REPORTS/H-00N-review.md` 작성 직후 `H-00N-review-to-main.md`를 생성한다.

## 검증 규칙
- 릴레이 프롬프트는 요약 전달용이며, 최종 판단은 항상 원본 report 파일 대조로 확정한다.
- 릴레이 파일이 누락되면 다음 스레드는 누락 사실을 보고하고, 가능한 범위에서 원본 report를 기준으로 계속 진행한다.
- `H-XXX`는 placeholder이며 실행 시 실제 번호(`H-00N`)로 치환한다.
