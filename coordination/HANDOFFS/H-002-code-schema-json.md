# H-002 Code Output JSON Schema

Owner: WT-2 (`codex/code-json-schema`)
Priority: High

## 목표
- CodeAgent 출력의 1순위를 JSON(files[]) 스키마로 고정

## 작업 범위
- CodeAgent 프롬프트를 JSON 우선 출력으로 변경
- 파서: JSON parse 우선, 실패 시 markdown fallback
- apply 계층은 JSON files[]를 기본으로 처리

## 수용 기준
1. 응답 payload에 `files[]`가 안정적으로 포함
2. 최소 3개 시나리오에서 parse 성공 테스트
3. fallback이 동작하되 경고 이벤트 기록

## 비범위
- 대규모 프롬프트 리라이팅
- UI 작업

## 보고서
- 완료 시 `coordination/REPORTS/H-002-result.md` 생성
- 필수 항목: 스키마 예시, 실패 케이스, 후속 제안
