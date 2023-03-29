import { PageMetaDto } from 'src/pagination/dto/page-meta.dto';
import { GetAllMeetingResponseMeetingDto } from './get-all-meetings-response-meeting.dto';

export class GetAllMeetingsResponseDto {
  meetings: GetAllMeetingResponseMeetingDto[];

  meta: PageMetaDto;
}
