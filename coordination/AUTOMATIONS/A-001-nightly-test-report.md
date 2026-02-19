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
   - H-023 실행량 회복 액션 이행률 추적/검증 + 재보정 readiness 점검(최근 14일 KST 기준)을 추가한다.
     - 기준선(H-016)을 고정해 함께 보고한다.
       - 집계 성공 `14일`, `INSUFFICIENT_SAMPLE` `14일/1.00`, `집계 불가` `0일`
       - `parseEligibleRunCount(14d)`: `CODE 4`, `SPEC 1`, `DOC 0`, `REVIEW 0`, `전체 5`
     - 최근 14일의 일 단위 집계를 대상으로 `집계 성공`/`집계 불가`/`INSUFFICIENT_SAMPLE`/`샘플 충분(>=20)` 일수를 계산한다.
     - 최근 7일(KST, `today-6 ~ today`) 실행률/호출 믹스 추세를 계산한다.
       - agent별 `executionMix` 항목을 고정 출력한다. (`directRuns`, `chainRuns`, `totalActualRuns`, `chainShare`)
       - `totalActualRuns = directRuns + chainRuns` 산식을 사용한다.
       - `chainShare = chainRuns / totalActualRuns` 산식을 사용한다. (단, `totalActualRuns = 0`이면 `0` 처리)
       - agent별 `agentExecution` 항목을 고정 출력한다. (`targetRuns`, `totalActualRuns`, `achievementRate`)
       - `achievementRate = min(1, totalActualRuns / targetRuns)` 산식을 사용한다.
       - `overallExecutionRate = min(1, totalActualRunsAllAgents / 32)` 산식을 사용한다.
       - agent별 `executionRecoveryPlan` 항목을 고정 출력한다. (`targetDirectRuns`, `targetChainRuns`, `targetChainShare`)
       - `targetTotalRuns = targetDirectRuns + targetChainRuns` 산식을 사용한다.
       - agent별 `executionRecoveryProgress` 항목을 고정 출력한다. (`actualDirectRuns`, `actualChainRuns`, `executionGap`, `chainShareGap`)
       - `actualTotalRuns = actualDirectRuns + actualChainRuns` 산식을 사용한다.
       - `actualChainShare = actualChainRuns / actualTotalRuns` 산식을 사용한다. (단, `actualTotalRuns = 0`이면 `0` 처리)
       - `executionGap = targetTotalRuns - actualTotalRuns` 산식을 사용한다.
       - `chainShareGap = targetChainShare - actualChainShare` 산식을 사용한다.
       - agent별 `executionRecoveryTrend` 항목을 고정 출력한다. (`executionGap`, `executionGapDelta`, `chainShareGap`, `chainShareGapDelta`)
       - `executionGapDelta = executionGap(최근7일) - executionGap(직전7일)` 산식을 사용한다.
       - `chainShareGapDelta = chainShareGap(최근7일) - chainShareGap(직전7일)` 산식을 사용한다.
       - `executionGapDelta < 0` 또는 `chainShareGapDelta < 0`이면 개선, 둘 다 `>= 0`이면 미개선으로 해석한다.
     - `INSUFFICIENT_SAMPLE` 비율을 계산한다. (`INSUFFICIENT_SAMPLE 일수 / 14`)
     - `집계 불가` 원인을 분류한다. (예: run-state 부재, 조회/파싱 실패, 필수 필드 누락)
     - 최근 14일 `parseEligibleRunCount` 추세를 전체 + agent별로 계산한다.
       - 일별 값 표(14행) + 7일 이동평균
       - 일일 최소 모수 목표 대비 진행률:
         - `CODE >= 16`, `SPEC >= 4`, `DOC >= 6`, `REVIEW >= 6`, `전체 >= 32`
     - H-017 목표 대비 진행률/미달률을 계산한다.
       - 집계 성공 일수 `>= 10`
       - `INSUFFICIENT_SAMPLE` 비율 `<= 0.50`
       - `집계 불가` 일수 `< 3`
       - 샘플 충분 일수(`parseEligibleRunCount >= 20`) `>= 7`
       - `집계 성공 달성률 = min(1, 집계 성공 일수 / 10)`으로 계산하고 `0~100%` 범위로 표기한다.
       - 목표 초과 정보는 `목표 초과 일수 = max(0, 집계 성공 일수 - 10)`로 별도 표기한다.
     - Projection 대비 실측 오차를 계산한다.
       - `deltaSufficientDays = actualSufficientDays - 7`
       - `deltaInsufficientRatio = actualInsufficientRatio - 0.50`
       - `deltaStartDate`: H-017 예상 착수일(산정 가능 시)과 실제 판정 시점 차이
     - 오차 허용 기준을 판정한다.
       - `abs(deltaSufficientDays) > 2` 또는 `abs(deltaInsufficientRatio) > 0.10`이면 오차 초과
       - `deltaStartDate` 미산정이면 전제조건 미충족으로 간주하고 보류 근거에 포함
     - 재보정 착수 가능/보류를 판정하고 다음 액션을 제시한다.
       - 재보정 착수 게이트(4개): `집계 성공 >= 10`, `INSUFFICIENT_SAMPLE <= 0.50`, `집계 불가 < 3`, `샘플 충분 일수(parseEligibleRunCount >= 20) >= 7`
       - 최종 판정은 `recalibrationReadiness` 필드에 `READY` 또는 `HOLD`로 표기한다.
       - 미충족 게이트는 `unmetGates` 목록으로 명시한다. (예: `INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`)
       - 오차 초과 또는 게이트 4개 중 1개라도 미충족: `HOLD` + 원인 분류(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`, `COLLECTION_FAILURE`) + 우선순위 액션
       - `HOLD`일 때 원인 우선순위는 목표-실적 gap + 실행률 지표 기반으로 정한다.
         - `LOW_TRAFFIC`: 최근 7일 `executionRecoveryTrend.executionGap` + `executionGapDelta` + 최근 3일 평균 `overallExecutionRate` 기준
         - `CHAIN_COVERAGE_GAP`: 최근 7일 `DOC`/`REVIEW` `executionRecoveryTrend.chainShareGap` + `chainShareGapDelta` + `actualChainRuns` 기준
         - `COLLECTION_FAILURE`: 최근 14일 `집계 불가` 원인 분류 기준
       - `HOLD`일 때 원인별 `recoveryActionStatus`를 고정 출력한다. (`cause`, `priority`, `status`, `evidence`)
         - `status`는 `IN_PROGRESS|BLOCKED|DONE` 중 하나로 표기한다.
         - `evidence`에는 runId 또는 집계표 근거를 반드시 포함한다.
       - 게이트 4개 모두 충족 + 오차 허용 범위 내: `READY` + 다음 라운드 문구를 `임계치 후보 산정 라운드 착수 제안`으로 고정한다.
     - `HOLD`일 때 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 유지한다.
     - `HOLD`일 때 일일 우선 액션(직접 호출 증량, 체인 호출 증량, 점검 시각/담당)을 목표-실적 gap 근거와 함께 제시한다.
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
  - 최근 14일 `parseEligibleRunCount` 추세(전체 + agent별 일별값, 7일 이동평균)
  - 최근 7일 실행률/호출 믹스 추세(일자별 `agentExecution` + `executionMix` + `overallExecutionRate`)
  - `executionMix`(agent별 `directRuns`, `chainRuns`, `totalActualRuns`, `chainShare`)
  - `agentExecution`(agent별 `targetRuns`, `totalActualRuns`, `achievementRate`)
  - `executionRecoveryPlan`(agent별 `targetDirectRuns`, `targetChainRuns`, `targetTotalRuns`, `targetChainShare`)
  - `executionRecoveryProgress`(agent별 `actualDirectRuns`, `actualChainRuns`, `actualTotalRuns`, `executionGap`, `chainShareGap`)
  - `executionRecoveryTrend`(agent별 `executionGap`, `executionGapDelta`, `chainShareGap`, `chainShareGapDelta`)
  - `recoveryActionStatus`(원인별 `cause`, `priority`, `status`, `evidence`)
  - `overallExecutionRate`(일자별 + 최근 3일 평균)
  - H-017 게이트 목표 대비 진행률/미달률(집계 성공 달성률 `min(1, 집계 성공 일수 / 10)` + 목표 초과 일수 포함, 샘플 부족/집계 불가/샘플 충분 일수)
  - 최근 7일 목표-실적 gap + delta 요약(`executionGap`, `executionGapDelta`, `chainShareGap`, `chainShareGapDelta`, `DOC/REVIEW actualChainRuns`)
  - Projection 대비 실측 오차(`deltaSufficientDays`, `deltaInsufficientRatio`, `deltaStartDate`)
  - `recalibrationReadiness` (`READY`/`HOLD`)
  - 미충족 게이트 목록(`unmetGates`)
  - 임계치 보정 판단 근거(4개 게이트 + 오차 판정)
  - 다음 액션(재보정 착수 준비 또는 샘플 확보 지속, `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 우선순위 포함, 목표-실적 gap + 실행률/호출 믹스 근거 명시)
  - `READY` 시: `임계치 후보 산정 라운드 착수 제안` 문구 고정
  - `HOLD` 시: 기존 수치 유지 확인 + 보류 사유(4개 게이트 기준 미충족 항목/오차 초과 항목) + 일일 우선 액션(점검 시각/담당 포함)
- 권장 후속조치(수동)
```
