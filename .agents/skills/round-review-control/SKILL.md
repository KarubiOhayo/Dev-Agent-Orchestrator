---
name: round-review-control
description: "트리거: Executor 결과물에 대한 읽기 전용 리뷰를 수행하고 review/relay 파일을 생성해야 할 때 사용. 비트리거: 구현 수정, 테스트 수정, 병합 실행이 필요한 작업에는 사용하지 않는다."
---

# round-review-control

## 목적
- Executor 산출물을 read-only 관점에서 검증하고 Main 승인 판단 근거를 만든다.
- 리뷰 리포트와 Review->Main 릴레이를 표준 포맷으로 고정한다.

## 입력
- 대상 handoff: `coordination/HANDOFFS/H-00N-*.md`
- 결과 리포트: `coordination/REPORTS/H-00N-result.md`
- Executor 전달 릴레이: `coordination/RELAYS/H-00N-executor-to-review.md`
- 운영 기준 문서:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`

## 수행 절차
1. 라운드 시작 시 핵심 문서를 재로딩한다(stateless).
2. 결과 리포트와 실제 변경 코드를 대조한다.
3. 이슈를 `P1 > P2 > P3` 순으로 정리한다(파일/라인 근거 필수).
4. 리뷰 리포트를 작성한다.
   - `coordination/REPORTS/H-00N-review.md`
5. Main 전달 릴레이를 템플릿 기반으로 작성한다.
   - `coordination/RELAYS/H-00N-review-to-main.md`
   - 템플릿: `coordination/RELAYS/TEMPLATE-review-to-main.md`
   - `Go | Conditional Go | No-Go` 권고 포함
6. 리뷰 산출물 작성이 끝나면 운영 문서 변경분을 커밋/푸시한다.
   - 커밋 범위: `coordination/`, 필요 시 `docs/`
   - 코드/설정 파일은 커밋 금지

## 금지 사항
- 코드 수정/병합/리팩토링 금지
- 승인 최종 판단 대행 금지(Main 전용)

## 표준 출력/파일 산출 규칙
- 최종 텍스트 출력에는 아래를 포함한다.
  - P1/P2/P3 개수
  - 핵심 이슈 요약(근거 파일/라인)
  - 테스트 게이트 상태 인용
  - 리스크 결론
- 파일 산출(필수 2개):
  - `coordination/REPORTS/H-00N-review.md`
  - `coordination/RELAYS/H-00N-review-to-main.md`
