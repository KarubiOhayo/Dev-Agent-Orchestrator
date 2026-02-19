# Codex 운영 가이드 (3-스레드 + Skills + Relay + Automations Plan A)

## 1) 목적
- Main/Executor/Review 3-스레드 운영을 표준화한다.
- 라운드별 stateless 실행을 유지하면서 복붙/누락/문서 드리프트를 줄인다.
- Automations는 Plan A(report-only)로 안전하게 운영한다.

## 2) 스킬 기반 3-스레드 운영 방법
- Main 스레드:
  - 사용 스킬: `$round-main-control`
  - 입력: 대상 `H-00N` + 최신 result/review/relay
  - 출력: 3개 출력(판단, 다음 handoff 1건(+main->executor relay 생성), 문서 갱신 항목)
- Executor 스레드:
  - 사용 스킬: `$round-executor`
  - 입력: handoff + `H-00N-main-to-executor` 릴레이
  - 완료 조건: `./gradlew clean test --no-daemon` 통과 + result/relay 파일 2종 생성
- Review 스레드:
  - 사용 스킬: `$round-review-control`
  - 입력: result + executor->review 릴레이
  - 완료 조건: P1/P2/P3 근거가 있는 review + review->main 릴레이 생성

## 3) 릴레이 3종 의미와 생성 타이밍
- Main -> Executor: `coordination/RELAYS/H-00N-main-to-executor.md`
  - 생성 시점: Main이 다음 라운드 handoff 확정 직후
  - 목적: Executor 시작 입력/범위/게이트 고정
- Executor -> Review: `coordination/RELAYS/H-00N-executor-to-review.md`
  - 생성 시점: Executor result 작성 직후
  - 목적: 리뷰 집중 포인트와 테스트 결과 전달
- Review -> Main: `coordination/RELAYS/H-00N-review-to-main.md`
  - 생성 시점: Review 리포트 작성 직후
  - 목적: Go/Conditional Go/No-Go 권고 전달

## 4) Automations Plan A 설정 방법 (KST)
- 원칙:
  - Automations는 점검/요약/발견사항 보고만 수행한다.
  - 자동 파일 수정/커밋/PR/웹훅 연동은 하지 않는다.
- 권장 자동화:
  1) Nightly Test Report
     - 템플릿: `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
     - 권장 스케줄: 매일 09:00 KST
  2) Doc Drift Check
     - 템플릿: `coordination/AUTOMATIONS/A-002-doc-drift-check.md`
     - 권장 스케줄: 월/수/금 14:00 KST
  3) Relay Watch (선택)
     - 템플릿: `coordination/AUTOMATIONS/A-003-relay-watch.md`
     - 권장 스케줄: 매 2시간(업무시간)

## 5) Codex App 실행 모드 가이드 (권장)
- Main/Review: Local 환경 + code-read-only 운영 권장
  - (중요) read-only = "코드 수정 금지" 의미다.
  - `coordination/`, `docs/`, `.agents/` 산출물(릴레이/리포트/운영문서)은 작성/갱신한다.
- Executor: Worktree 분리 권장(라운드 단위 작업 격리)
- 공통: 라운드 시작 시 stateless 재로딩 체크리스트를 먼저 실행

## 6) Codex App Actions 권장
- 자주 쓰는 액션으로 테스트 버튼 추가:
  - 이름: `Run tests`
  - 명령: `./gradlew clean test --no-daemon`
  - 목적: Executor 승인 게이트 실행을 일관화한다.

## 7) 참고 문서
- `AGENTS.md`
- `coordination/TASK_BOARD.md`
- `coordination/DECISIONS.md`
- `coordination/RELAYS/README.md`
- `.agents/skills/*/SKILL.md`
