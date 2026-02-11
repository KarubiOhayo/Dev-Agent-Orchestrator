# H-003 CLI + Output UX

Owner: WT-3 (`codex/cli-ux`)
Priority: Medium

## 목표
- 반복 curl 호출을 줄이고 사람이 읽기 쉬운 출력 제공

## 작업 범위
- CLI 엔트리포인트 초안 (`devagent generate/spec`)
- 결과 요약 formatter 추가(runId, 모델, 파일수, 적용결과)
- docs 사용 예시 보강

## 수용 기준
1. CLI 한 줄 명령으로 code generate 호출 가능
2. dry-run 결과가 표/리스트 형태로 요약 출력
3. 문서에 설치/실행/예시 포함

## 비범위
- 디스코드 봇 연동
- 웹 UI

## 보고서
- 완료 시 `coordination/REPORTS/H-003-result.md` 생성
- 필수 항목: 사용 예시, UX 개선 포인트, 제약사항
