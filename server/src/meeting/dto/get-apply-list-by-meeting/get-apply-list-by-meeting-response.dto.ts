import { PageMetaDto } from 'src/pagination/dto/page-meta.dto';
import { GetApplyListByMeetingResponseApplyDto } from './get-apply-list-by-meeting-response-apply.dto';

export class GetApplyListByMeetingResponseDto {
  apply: GetApplyListByMeetingResponseApplyDto[];

  meta: PageMetaDto;
}
