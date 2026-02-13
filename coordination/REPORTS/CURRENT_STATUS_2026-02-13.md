# Current Status Report (2026-02-13)

## 요약
Spec -> Code -> Doc/Review 체이닝(1차), Code 출력 JSON files[] 우선화, CLI 고도화(H-006: `--json`, alias, 반복 실행 성능)까지 완료됨.
- 라우팅 -> LLM 호출 -> JSON 우선 파싱 -> dry-run/apply -> run-state 기록
- 운영 체계는 3스레드(Main/Review/Executor) + stateless 라운드 방식으로 전환

## 구현 완료 항목
- ModelRouter + Routing API
- Provider adapters(OpenAI/Anthropic/Google)
- CodeAgent API
- SpecAgent API + Spec -> Code chain
- PromptRegistry + ContextPolicy
- RunState(SQLite/fallback)
- apply/dry-run + path safety(spec in/out)
- Code output JSON files[] 우선 파싱 + markdown fallback warning 이벤트
- DocAgent API + 문서 JSON 스키마 파싱/보정 + fallback warning 이벤트
- Code -> Doc chain (`chainToDoc`, `docUserRequest`) + chain 이벤트 기록
- ReviewAgent API + 리뷰 JSON 스키마 파싱/보정 + fallback warning 이벤트
- Code -> Review chain (`chainToReview`, `reviewUserRequest`) + chain 이벤트 기록
- CLI 엔트리포인트(`devagent generate/spec/help`) + 결과 표/리스트 formatter
- CLI 오류 메시지 간소화(스택트레이스 기본 노출 차단) + API docs `files` 계약 반영
- CLI 실행 래퍼 `bootJar -> java -jar` 전환으로 종료코드 정확 전달(`help=0`, unknown option=`2`)
- CLI `--json` 출력(성공/실패 envelope), 대표 alias, 반복 실행 성능 개선(H-006)
- 테스트 추가 및 통과

## 잔여 과제
1. Code 체인(Doc/Review) 실패 시 부분 성공 허용 여부 정책 결정(H-007)
2. files[]/document/review 스키마 의미 검증(path/content/section quality) 및 관측 지표 고도화(H-008)
3. ReviewOutputSchemaParser 경계 입력(fenced JSON 변형) 테스트 보강
4. 릴레이 자동화 규칙의 라운드 선택/최신 파일 탐색 로직 고도화

## 리스크
- 모델 출력이 비정형일 때 markdown fallback 빈도가 높아질 수 있음
- JSON 구조는 맞아도 files[]/document/review 항목 의미 검증은 아직 제한적
- `devagent` 스크립트의 빌드 산출물 신선도 판단이 mtime 기반이므로 특수 케이스에서 오판 가능성 존재
- 현재 Code -> Doc/Review 체인 실패는 Code 요청 실패로 전파되어 운영에서 실패 민감도가 높을 수 있음
- strict-json escalation 동작에 따라 codex 모델 우선순위가 변동 가능

## 메인 제안
다음 라운드는 H-007(체인 실패 부분성공 정책)을 handoff로 고정해 진행
