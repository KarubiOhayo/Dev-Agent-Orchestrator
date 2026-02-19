# Dev-Agent Orchestrator AGENTS Guide

이 문서는 이 저장소에서 Codex가 따라야 할 공통 운영 규칙이다.

## 1) 3-스레드 역할 정의

- Main-Control (code-read-only)
  - 라운드 계획 수립, handoff 확정, 승인/보류 판단, 문서 동기화 담당
- Review-Control (code-read-only)
  - 결과물 리뷰 전담(P1 > P2 > P3, 파일/라인 근거 필수)
- Executor (implementation)
  - handoff 범위 내 구현/테스트/커밋/결과 보고 담당

## 1.1) "읽기 전용(read-only)" 정의 (중요)

이 레포에서 "읽기 전용"은 **코드 수정 금지**를 의미한다.

- Main/Review 금지:
  - 구현 코드/테스트 코드 수정 (`src/`, `src/test/` 등)
  - 빌드/런타임 설정 변경 (`build.gradle`, `settings.gradle`, `gradle/wrapper/**`, `src/main/resources/application.yml`)
  - 코드/설정 파일에 대한 git add/commit/push/merge
  - 테스트 실행(Executor 승인 게이트 전용)
- Main/Review 허용/필수:
  - `coordination/` 산출물 작성/갱신(RELAYS/REPORTS/HANDOFFS 포함)
  - `docs/`, `.agents/` 운영 문서 작성/갱신
  - 라운드 종료 시 본인이 생성/수정한 운영 문서(`coordination/`, `docs/`, `.agents/`) 커밋/푸시

## 2) Stateless 라운드 원칙

- 모든 라운드는 시작 시 핵심 문서를 재로딩한다.
- 단일 소스 오브 트루스(SoT)는 `coordination/` 문서다.
- 최소 재로딩 기준:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - 대상 라운드의 `coordination/REPORTS/*` 및 `coordination/RELAYS/*`

## 3) H-XXX Placeholder 해석 규칙

- `H-XXX`는 템플릿 placeholder이며 literal 파일명이 아니다.
- 실제 실행 시 대상 라운드의 실제 번호(`H-00N`)로 치환해 경로를 확정한다.
- 예: `H-XXX-result.md` -> `H-010-result.md`

## 4) 승인 게이트

- Executor는 라운드 완료 전에 반드시 `./gradlew clean test --no-daemon`를 실행한다.
- Main/Review는 구현 실행 스레드가 아니므로 **코드 수정/코드 테스트 실행**을 하지 않는다.
  - 단, `coordination/` 및 운영 문서 산출물(릴레이/리포트)은 작성/갱신하고 라운드 단위 커밋/푸시를 수행한다.
- Main 최종 판단은 `테스트 통과 + result + review` 3종 근거로 확정한다.

## 5) 공통 파일 변경 승인 절차

- 아래 파일군은 Main 사전 승인 없이는 변경 금지:
  - `src/main/resources/application.yml`
  - 공용 모델/계약 파일(`src/main/java/**/model/**` 등 팀 공통 계약 영향 파일)
  - 빌드 설정(`build.gradle`, `settings.gradle`, `gradle/wrapper/**`)
- Executor가 필요성을 발견하면 즉시 중단하고 승인 요청만 남긴다.

## 6) 컨텍스트/검색 절약 규칙

- 기본적으로 아래 경로는 읽지 않는다:
  - `build/`, `.gradle/`, `.idea/`, `storage/`
- 예외는 디버깅/근거 확인이 반드시 필요한 경우에 한정한다.
- 검색은 범위를 좁혀 `coordination/`, `docs/`, 지정 handoff 대상 파일 중심으로 수행한다.

## 7) 결과물 경로 규칙

- 결과 보고: `coordination/REPORTS/`
- 스레드 릴레이: `coordination/RELAYS/`
- 구현 지시: `coordination/HANDOFFS/`
- 신규 운영 템플릿/자동화 프롬프트는 해당 전용 폴더(`coordination/AUTOMATIONS/`, `.agents/skills/`)에 저장한다.

## 8) 스레드별 커밋/푸시 규칙

- 공통:
  - 각 스레드는 라운드 산출물 작성 완료 직후 `git add -> git commit -> git push`를 수행한다.
  - 커밋 메시지는 라운드/역할이 드러나게 작성한다(권장: `[H-00N][main|review|executor] ...`).
- Main/Review:
  - 커밋 가능 범위는 운영 문서(`coordination/`, `docs/`, `.agents/`)로 한정한다.
  - 구현 코드/테스트 코드/빌드·런타임 설정 파일 커밋은 금지한다.
- Executor:
  - handoff 범위 내 구현 변경과 라운드 산출물(`REPORTS/RELAYS`)을 함께 커밋/푸시한다.
