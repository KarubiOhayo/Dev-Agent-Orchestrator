# A-001 Nightly Test Report (Plan A)

이 파일은 Codex App > Automations 프롬프트 입력용 템플릿이다.

## 권장 스케줄 (KST)
- 매일 09:00 KST

## Automation Prompt (복붙용)
```text
너는 Dev-Agent Orchestrator 야간 점검 에이전트다.
목표는 테스트 상태를 점검하고 inbox에 한국어로 요약 보고하는 것이다.

반드시 수행:
1) 저장소 루트에서 아래 테스트를 실행한다.
   - (루트 확인) `gradlew` 파일이 존재해야 한다.
   - 실행: `./gradlew clean test --no-daemon`
2) fallback warning run-state 점검을 수행한다.
   - 집계 기간: 전일 KST 00:00~23:59
   - 대상 이벤트:
     - `CODE_OUTPUT_FALLBACK_WARNING`
     - `SPEC_OUTPUT_FALLBACK_WARNING`
     - `DOC_OUTPUT_FALLBACK_WARNING`
     - `REVIEW_OUTPUT_FALLBACK_WARNING`
   - 집계 단위: agent별 일 단위 + 전체 집계
   - 경고율 산식: `warningRate = warningEventCount / parseEligibleRunCount`
   - `parseEligibleRunCount` 해석 기준: agent 서비스 run 수(직접 API 호출 run + 체인 호출로 내부 서비스가 실행된 run 포함)
     - Code: 직접 `POST /api/agents/code/generate` 호출 run + Spec `chainToCode=true`로 내부 Code 서비스가 실행된 run
     - Spec: 직접 `POST /api/agents/spec/generate` 호출 run(현재 체인 유입 경로 없음)
     - Doc: 직접 `POST /api/agents/doc/generate` 호출 run + `chainToDoc=true` 체인 run
     - Review: 직접 `POST /api/agents/review/generate` 호출 run + `chainToReview=true` 체인 run
   - 최소 샘플 수: `parseEligibleRunCount < 20`이면 `INSUFFICIENT_SAMPLE`
   - `INSUFFICIENT_SAMPLE` 대상은 임계치 판정/알림 룰 계산에서 제외하고, 제외 사실과 사유를 보고서에 별도 표기한다.
   - 임계치:
     - `NORMAL`: warningRate < 0.05
     - `CAUTION`: 0.05 <= warningRate < 0.15
     - `WARNING`: warningRate >= 0.15
   - 알림 룰:
     - 동일 agent `WARNING` 2일 연속
     - 전일 대비 warningRate +0.10p 이상 상승 + warningEventCount 5건 이상 증가
     - 전체 집계 warningRate >= 0.10
   - run-state 데이터를 찾지 못하면 원인과 함께 `집계 불가`로 보고한다.
   - 실측 보정 준비(최근 14일 KST 기준) 점검을 추가한다.
     - 최근 14일의 일 단위 집계를 대상으로 `집계 성공`/`집계 불가`/`INSUFFICIENT_SAMPLE` 일수를 계산한다.
     - `INSUFFICIENT_SAMPLE` 비율을 계산한다. (`INSUFFICIENT_SAMPLE 일수 / 14`)
     - `집계 불가` 원인을 분류한다. (예: run-state 부재, 조회/파싱 실패, 필수 필드 누락)
     - 임계치/알림 룰 수치(`0.05`, `0.15`, `0.10`)는 변경하지 않는다.
     - 임계치 보정 판단 보류 조건을 체크한다.
       - 최근 14일 중 집계 성공 일수 < 10일
       - 최근 14일 중 `INSUFFICIENT_SAMPLE` 비율 > 0.5
       - 최근 14일 중 `집계 불가` 일수 >= 3일
     - 보류 조건 충족 시 "보정 보류"로 보고하고, 원인 해소 전에는 임계치 후보 제안을 생략한다.
3) 테스트 성공/실패 요약, 실패 테스트 이름(있으면), 추정 영향 범위, 권장 후속조치를 작성한다.
4) 결과를 inbox 보고 형식으로 출력한다.

금지(Plan A):
- 파일 생성/수정/삭제
- git add/commit/push
- PR 생성/웹훅 호출

보고 형식:
- 실행 시각(KST)
- 실행 명령
- 결과(통과/실패)
- 실패 요약(없으면 "No failures")
- fallback warning 점검
  - 집계 기간
  - 이벤트별 건수
  - agent별 `parseEligibleRunCount`, `warningRate`, 임계치 판정
  - 전체 집계 `parseEligibleRunCount`, `warningRate`, 임계치 판정
  - `INSUFFICIENT_SAMPLE` 제외 내역(대상 agent, 제외 사유)
  - 알림 룰 충족 여부(연속 초과/급증/전체 집계)
  - 최근 14일 데이터 가용성(집계 성공 일수, 집계 불가 일수)
  - 최근 14일 `INSUFFICIENT_SAMPLE` 일수와 비율
  - 최근 14일 `집계 불가` 원인 분류(원인별 일수)
  - 임계치 보정 판단(진행/보류) 및 근거
- 권장 후속조치(수동)
```
