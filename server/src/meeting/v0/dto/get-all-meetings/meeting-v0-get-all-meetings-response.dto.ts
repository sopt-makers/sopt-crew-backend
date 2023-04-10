import { PageMetaDto } from 'src/common/pagination/dto/page-meta.dto';
import { MeetingV0GetAllMeetingResponseMeetingDto } from './meeting-v0-get-all-meetings-response-meeting.dto';

export class MeetingV0GetAllMeetingsResponseDto {
  meetings: MeetingV0GetAllMeetingResponseMeetingDto[];

  meta: PageMetaDto;
}
