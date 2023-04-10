import { UserPart } from 'src/entity/user/enum/user-part.enum';

export interface UserActivity {
  generation: number;
  part: UserPart;
}
