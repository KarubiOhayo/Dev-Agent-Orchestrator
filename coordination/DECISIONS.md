# Architecture Decisions

## D-001 라우팅 기본 모드
- Date: 2026-02-11
- Decision: 기본 모드는 `BALANCED`
- Rationale: 비용/품질 균형을 유지하면서 운영 리스크 최소화
- Consequence: 특수 요구는 요청 단위 모드 전환으로 처리

## D-002 CODE/REFACTOR 기본 모델
- Date: 2026-02-11
- Decision: `BALANCED`에서 `openai:gpt-5.2-codex` 사용
- Rationale: 코드 생성/수정 작업 품질 향상
- Consequence: strict-json escalation이 켜진 요청은 openai:gpt-5.2가 우선될 수 있음

## D-003 Gemini 3 Preview 운영 정책
- Date: 2026-02-11
- Decision: `GEMINI3_CANARY`는 일부 에이전트(CODE/DOC)만 허용
- Rationale: Preview 모델 리스크를 제한된 범위에서 검증
- Consequence: 미지원 agent의 canary 요청은 default mode fallback

## D-004 상태 저장소
- Date: 2026-02-11
- Decision: RunState는 SQLite 우선, 실패 시 파일 fallback
- Rationale: 운영 시 조회 가능성과 내구성 확보 + 로컬 실행 안정성
- Consequence: `storage/`는 형상관리 제외

## D-005 파일 적용 정책
- Date: 2026-02-11
- Decision: `apply=false` 기본, `apply=true`일 때만 파일 쓰기
- Rationale: 안전한 검토 우선 워크플로우
- Consequence: 사용자 액션으로만 실제 파일 변경 발생

## D-006 프롬프트 계층
- Date: 2026-02-11
- Decision: `global -> agent -> project-global -> project-agent`
- Rationale: 오케스트레이터 공통 규칙과 프로젝트 커스터마이징 분리
- Consequence: 멀티 프로젝트 운영 시 재사용성 향상
