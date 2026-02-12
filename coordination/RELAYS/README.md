# Relay Prompt Rules

스레드 간 전달 프롬프트는 이 폴더에 라운드 단위로 저장한다.

## 파일 규칙
- Executor -> Review: `H-XXX-executor-to-review.md`
- Review -> Main: `H-XXX-review-to-main.md`
- 템플릿:
  - `TEMPLATE-executor-to-review.md`
  - `TEMPLATE-review-to-main.md`

## 생성 타이밍
1. Executor는 `coordination/REPORTS/H-XXX-result.md` 작성 직후 `H-XXX-executor-to-review.md`를 생성한다.
2. Review는 `coordination/REPORTS/H-XXX-review.md` 작성 직후 `H-XXX-review-to-main.md`를 생성한다.

## 검증 규칙
- 릴레이 프롬프트는 요약 전달용이며, 최종 판단은 항상 원본 report 파일 대조로 확정한다.
- 릴레이 파일이 누락되면 다음 스레드는 누락 사실을 보고하고, 가능한 범위에서 원본 report를 기준으로 계속 진행한다.
