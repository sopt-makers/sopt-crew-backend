import {
  BaseEntity,
  Column,
  Entity,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
  RelationId,
  Unique,
} from 'typeorm';
import { User } from '../user/user.entity';
import { Meeting } from '../meeting/meeting.entity';
import { Like } from '../like/like.entity';
import { Comment } from '../comment/comment.entity';
import { Report } from '../report/report.entity';

@Entity('post')
@Unique(['id'])
export class Post extends BaseEntity {
  /** Primary key */
  @PrimaryGeneratedColumn()
  id: number;

  /** 제목 */
  @Column()
  title: string;

  /** 내용 */
  @Column()
  contents: string;

  /** 작성일 */
  @Column()
  createdDate: Date;

  /** 수정일 */
  @Column()
  updatedDate: Date;

  /** 조회수 */
  @Column({ default: 0 })
  viewCount: number;

  /** 이미지 리스트 */
  @Column('text', { array: true, nullable: true })
  images: string[] | null;

  /** 작성자 */
  @ManyToOne(() => User, (user) => user.posts)
  user: User;

  /** 유저 id */
  @RelationId((post: Post) => post.user)
  @Column()
  userId: number;

  /** 미팅 */
  @ManyToOne(() => Meeting, (meeting) => meeting.posts)
  meeting: Meeting;

  /** 미팅 id */
  @RelationId((post: Post) => post.meeting)
  @Column()
  meetingId: number;

  /** 좋아요 */
  @OneToMany(() => Comment, (comment) => comment.post)
  comments: Comment[];

  /** 좋아요 수 */
  @Column({ default: 0 })
  commentCount: number;

  /** 좋아요 */
  @OneToMany(() => Like, (like) => like.post)
  likes: Like[];

  /** 좋아요 수 */
  @Column({ default: 0 })
  likeCount: number;

  /** 신고 */
  @OneToMany(() => Report, (report) => report.post)
  reports: Report[];
}
