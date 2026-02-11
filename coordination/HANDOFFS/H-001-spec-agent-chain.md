# H-001 Spec Agent + Chain

Owner: WT-1 (`codex/spec-chain`)
Priority: High

## 목표
- SpecAgent를 구현하고 Spec JSON을 CodeAgent 입력으로 연결

## 작업 범위
- `src/main/java/me/karubidev/devagent/agents/spec/` 신규 추가
- Spec API endpoint 추가
- Spec -> Code chain service 추가
- 최소 테스트 추가

## 수용 기준
1. `POST /api/agents/spec/generate`가 JSON 스키마를 반환
2. `POST /api/agents/code/generate`가 spec 입력 경로를 받을 수 있음
3. chain 호출 결과가 run-state event로 기록됨

## 비범위
- PR 자동 생성
- 디스코드/슬랙 연동

## 보고서
- 완료 시 `coordination/REPORTS/H-001-result.md` 생성
- 필수 항목: 변경 파일, 테스트 결과, 리스크
