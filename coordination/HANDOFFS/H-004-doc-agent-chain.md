# H-004 Doc Agent + Code -> Doc Chain

Owner: WT-4 (`codex/doc-chain`)
Priority: High

## 목표
- DocAgent를 구현하고 Code 결과를 입력으로 문서 생성 체이닝을 연결

## 작업 범위
- `src/main/java/me/karubidev/devagent/agents/doc/` 신규 추가
- Doc API endpoint 추가 (`/api/agents/doc/generate`)
- Code -> Doc chain service 추가(최소 1개 호출 경로)
- run-state 이벤트(`DOC_*`, chain 이벤트) 기록 추가
- 최소 테스트 추가
- 문서 업데이트 (`docs/code-agent-api.md` 또는 신규 doc guide)

## 수용 기준
1. `POST /api/agents/doc/generate`가 JSON 스키마 응답을 반환
2. Code 결과 기반 Doc 체이닝 호출이 동작
3. chain 호출 결과가 run-state event로 기록
4. `./gradlew clean test` 통과

## 비범위
- PR 자동 생성
- 외부 채널 연동(Discord/Slack)
- Web UI 추가

## 제약
- handoff 범위 밖 수정 금지
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 메인 컨트롤 사전 승인
- JSON 우선 원칙 유지, fallback 사용 시 경고 이벤트 기록
- 경로/파일 쓰기 안전성 정책 기존 수준 유지

## 보고서
- 완료 시 `coordination/REPORTS/H-004-result.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 테스트 결과(명령 + 통과/실패)
  - API 요청/응답 예시
  - 남은 리스크
  - 메인 병합 시 주의사항
