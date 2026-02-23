# Fallback-warning (Output Parsing) Observability

## 목적
- `fallback-warning`를 모델 라우팅 fallback과 명확히 분리해 운영 해석 혼선을 줄인다.
- 경고율을 "출력 형식 안정성" 지표로 고정해 라운드 판단의 재현성을 높인다.

## 정의
- `fallback-warning`:
  - LLM 호출 자체는 성공했지만 기대한 JSON 스키마 파싱이 실패해, 파서 fallback 경로로 결과를 복구한 경우의 운영 경고다.
- `routing fallback`:
  - 후보 모델/벤더 재시도로 호출을 이어가는 라우팅 계층 동작이다.
- 두 개념은 서로 다른 계층이며, 같은 run에서 동시에 발생할 수 있다.

## 왜 추적하는가
- 지표의 목적은 콘텐츠 품질 점수가 아니라 파싱 안정성(형식 준수율) 점검이다.
- `warningRate` 상승은 체인 누락/자동 적용 불안정의 조기 신호로 해석한다.

## 운영 지표 계약
- `parseEligibleRunCount`: 파싱 대상 run 수(에이전트별 직접 호출 + 해당 체인 호출 포함)
- `warningEventCount`: `*_OUTPUT_FALLBACK_WARNING` 이벤트 건수
- `warningRate`: `warningEventCount / parseEligibleRunCount`
- `INSUFFICIENT_SAMPLE`: `parseEligibleRunCount < 20`

## 관찰 윈도우
- 최근 14일: 재보정/재개 판단용 기본 윈도우
- 최근 7일: 추세 보조 지표(급격한 변동 탐지)

## 운영 해석 원칙
- `warningRate`는 `INSUFFICIENT_SAMPLE` 비율과 함께 해석한다.
- `parseEligibleRunCount < 20`인 날/구간만으로 정책 변경을 결정하지 않는다.
- fallback-warning 정책 변경은 `coordination/DECISIONS.md`를 SoT로 확정한다.

## 현재 트랙과의 연결
- H-041 우선순위는 parser safety(`LOOSE_JSON_FALLBACK` 안전화)와 apply 실증 증빙 확보다.
- H-039/H-024 fallback-warning 트랙은 H-041 종료 후 재개한다.
