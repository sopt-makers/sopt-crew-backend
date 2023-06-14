import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  OneToMany,
  PrimaryGeneratedColumn,
  Unique,
} from 'typeorm';
import { UserActivity } from './interface/user-activity.interface';
import { Meeting } from 'src/entity/meeting/meeting.entity';
import { Apply } from 'src/entity/apply/apply.entity';
import { Post } from '../post/post.entity';
import { Like } from '../like/like.entity';
import { Report } from '../report/report.entity';

@Entity('user')
@Unique(['id'])
export class User extends BaseEntity {
  /** Primary Key */
  @PrimaryGeneratedColumn()
  id: number;

  /** 사용자 이름 */
  @Column()
  name: string;

  /** sopt org unique id */
  @Column()
  orgId: number;

  /** 활동 목록 */
  @Column({ type: 'jsonb', nullable: true, default: null })
  activities: UserActivity[] | null;

  /** 프로필 이미지 */
  @Column({
    nullable: true,
    default: null,
  })
  profileImage: string | null;

  /** 핸드폰 번호 */
  @Column({ nullable: true, default: null })
  phone: string;

  /** 내가 생성한 모임 */
  @OneToMany(() => Meeting, (meeting) => meeting.user)
  meetings: Meeting[];

  /** 내가 지원한 내역 */
  @OneToMany(() => Apply, (apply) => apply.user)
  @JoinTable()
  apply: Apply[];

  /** 작성한 게시글 */
  @OneToMany(() => Post, (post) => post.user)
  posts: Post[];

  /** 좋아요 */
  @OneToMany(() => Like, (like) => like.user)
  likes: Like[];

  /** 신고 내역 */
  @OneToMany(() => Report, (report) => report.reporter)
  reports: Report[];
}
