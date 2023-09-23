import { User } from 'src/entity/user/user.entity';
import { Apply } from '../../../entity/apply/apply.entity';
import { MeetingJoinablePart } from '../../../entity/meeting/enum/meeting-joinable-part.enum';
import { MeetingCategory } from '../../../entity/meeting/enum/meeting-category.enum';
import { MeetingV0MeetingStatus } from '../enum/meeting-v0-meeting-status.enum';
import { ImageURL } from '../../../entity/meeting/interface/image-url.interface';

export class MeetingV0GetMeetingByIdResponseDto {
  id: number;

  // 개설한 유저
  user: User;

  // 유저 id
  userId: number;

  // 지원or초대 정보
  appliedInfo: Apply[];

  // 모임 제목
  title: string;

  // 모임 카테고리
  category: MeetingCategory;

  // 이미지
  imageURL: ImageURL[];

  // 모집 시작 기간
  startDate: Date;

  // 모집 마감 기간
  endDate: Date;

  // 모집 인원
  capacity: number;

  // 모임 소개
  desc: string;

  // 진행방식 소개
  processDesc: string;

  // 모임 시작 기간
  mStartDate: Date;

  // 모임 마감 기간
  mEndDate: Date;

  // 개절자 소개
  leaderDesc: string;

  // 모집 대상
  targetDesc: string;

  // 유의 사항
  note: string | null;

  /** 멘토 필요 여부 */
  isMentorNeeded: boolean;

  /**
   * 활동 기수만 참여 가능한지 여부
   * */
  canJoinOnlyActiveGeneration: boolean;

  /**
   * 대상 활동 기수
   * null인 경우 모든 기수 허용
   * */
  targetActiveGeneration: number | null;

  approvedApplyCount: number;

  createdGeneration: number;

  joinableParts: MeetingJoinablePart[];
  // 칼럼이 아닌 response할 때 meeting 객체에 넣어줄 값
  status?: MeetingV0MeetingStatus; // 모임 상태
  // confirmedApply?: Apply[]; // 승인된 모임
  host?: boolean; // 해당 모임의 호스트인지
  apply?: boolean; // 해당 모임을 지원 했는지
  invite?: boolean; // 해당 모임에 초대 받았는지
  approved?: boolean; // 초대를 받았다면 승인을 했는지
}
