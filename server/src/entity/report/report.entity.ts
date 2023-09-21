import {
  BaseEntity,
  Column,
  Entity,
  ManyToOne,
  PrimaryGeneratedColumn,
  RelationId,
  Unique,
} from 'typeorm';
import { User } from '../user/user.entity';
import { Comment } from '../comment/comment.entity';
import { Post } from '../post/post.entity';

@Entity('report')
@Unique(['id'])
export class Report extends BaseEntity {
  /** Primary key */
  @PrimaryGeneratedColumn()
  id: number;

  /** 작성일 */
  @Column()
  createdDate: Date;

  /** 신고자 */
  @ManyToOne(() => User, (user) => user.reports)
  user: User;

  /** 신고자 id */
  @RelationId((report: Report) => report.user)
  @Column()
  userId: number;

  /** 게시글 */
  @ManyToOne(() => Post, (post) => post.reports)
  post: Post;

  /**
   * 게시글 id
   * @description 게시글 신고가 아닌 경우 null
   * */
  @RelationId((report: Report) => report.post)
  @Column({ default: null, nullable: true })
  postId: number | null;

  /** 댓글 */
  @ManyToOne(() => Comment, (comment) => comment.reports)
  comment: Comment;

  /**
   * 댓글 id
   * @description 댓글 신고가 아닌 경우 null
   * */
  @RelationId((report: Report) => report.comment)
  @Column({ default: null, nullable: true })
  commentId: number | null;
}
