import { IsNotEmpty, IsNumber } from 'class-validator';

export class MeetingV1CreateMeetingResponseDto {
  /** 생성된 미팅 id */
  @IsNotEmpty()
  @IsNumber()
  meetingId: number;
}
