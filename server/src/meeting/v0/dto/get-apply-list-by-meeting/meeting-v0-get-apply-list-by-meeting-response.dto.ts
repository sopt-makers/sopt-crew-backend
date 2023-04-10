import { PageMetaDto } from 'src/common/pagination/dto/page-meta.dto';
import { MeetingV0GetApplyListByMeetingResponseApplyDto } from './meeting-v0-get-apply-list-by-meeting-response-apply.dto';

export class MeetingV0GetApplyListByMeetingResponseDto {
  apply: MeetingV0GetApplyListByMeetingResponseApplyDto[];

  meta: PageMetaDto;
}
