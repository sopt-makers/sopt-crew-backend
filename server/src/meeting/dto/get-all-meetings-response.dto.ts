import { PageMetaDto } from 'src/pagination/dto/page-meta.dto';
import { GetMeetingByIdResponseDto } from './get-meeting-by-id-response.dto';

export class GetAllMeetingsResponseDto {
  meetings: GetMeetingByIdResponseDto[];

  meta: PageMetaDto;
}
