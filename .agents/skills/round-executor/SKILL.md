---
name: round-executor
description: "트리거: 지정된 handoff 범위를 구현하고 테스트 후 result/relay 산출물을 만들어야 할 때 사용. 비트리거: 승인 판단(Main), 읽기 전용 리뷰(Review), 운영 현황 요약 전용 작업에는 사용하지 않는다."
---

# round-executor

## 목적
- handoff 범위 내 구현을 완료하고, 테스트 게이트 통과 상태로 결과를 전달한다.
- 리뷰 스레드가 즉시 검토 가능한 표준 산출물(result + relay)을 남긴다.

## 입력
- 대상 handoff: `coordination/HANDOFFS/H-00N-*.md`
- Main 지시 릴레이: `coordination/RELAYS/H-00N-main-to-executor.md`
- 운영 기준 문서:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`

## 수행 절차
1. handoff 범위를 벗어나는 변경이 필요한지 먼저 점검한다.
2. 범위 내 구현만 수행하고 테스트를 실행한다.
   - 승인 게이트: `./gradlew clean test --no-daemon`
3. 공통 파일 변경 필요 시 즉시 중단하고 승인 요청만 남긴다.
4. 결과 리포트를 작성한다.
   - `coordination/REPORTS/H-00N-result.md`
5. Review 전달 릴레이를 템플릿 기반으로 작성한다.
   - `coordination/RELAYS/H-00N-executor-to-review.md`
   - 템플릿: `coordination/RELAYS/TEMPLATE-executor-to-review.md`
6. 라운드 완료 전 구현 변경 + 결과 산출물(`coordination/REPORTS/`, `coordination/RELAYS/`)을 커밋/푸시한다.

## 금지 사항
- handoff 범위 밖 수정 금지
- Main 사전 승인 없는 공통 파일 변경 금지
- 테스트 실패 상태에서 완료 처리 금지

## 표준 출력/파일 산출 규칙
- 최종 텍스트 출력에는 아래를 포함한다.
  - 변경 파일 목록
  - 테스트 명령/결과
  - 남은 리스크
  - 추가 승인 필요 항목(있다면)
  - 리뷰 집중 포인트
- 파일 산출(필수 2개):
  - `coordination/REPORTS/H-00N-result.md`
  - `coordination/RELAYS/H-00N-executor-to-review.md`
