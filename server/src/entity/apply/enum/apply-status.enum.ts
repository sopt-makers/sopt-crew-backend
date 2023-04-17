/** 신청 상태 */
export enum ApplyStatus {
  /** 대기 */
  WAITING = 0,

  /** 승인된 신청자 */
  APPROVE = 1,

  /** 거절된 신청자 */
  REJECT = 2,
}
