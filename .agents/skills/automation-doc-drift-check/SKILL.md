---
name: automation-doc-drift-check
description: "트리거: Automations에서 운영 문서 간 상태 드리프트를 정기 탐지하고 요약 보고할 때 사용. 비트리거: 문서 자동 수정, 코드 변경, 커밋/PR 자동화가 필요한 작업에는 사용하지 않는다."
---

# automation-doc-drift-check

## 목적
- `PROJECT_OVERVIEW`, `TASK_BOARD`, `DECISIONS`, `CURRENT_STATUS`, 최신 result/review/relay 간 불일치를 탐지한다.
- 누락/충돌 항목을 우선순위와 함께 inbox에 보고한다.

## 수행 절차
1. 기준 문서를 재로딩한다(stateless).
2. 아래 항목을 점검한다.
   - 최신 완료 handoff가 TASK_BOARD/CURRENT_STATUS에 반영되었는가?
   - 신규 정책이 DECISIONS와 PROMPTS/RELAYS 규칙에 반영되었는가?
   - 릴레이 3종 생성 타이밍 누락이 없는가?
3. 드리프트 항목을 `Critical/Warning/Info`로 분류해 보고한다.

## 금지 사항 (Plan A)
- 문서 자동 수정 금지
- 파일 생성/수정/삭제 금지
- `git add/commit/push` 금지

## 표준 출력/파일 산출 규칙
- 출력은 inbox 보고용 텍스트로 작성한다.
  - 실행 시각(KST)
  - 점검 문서 목록
  - 드리프트 항목(심각도/근거 파일)
  - 수동 보정 권장안
- 파일 산출 없음(필수)

