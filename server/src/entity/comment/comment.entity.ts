import {
  BaseEntity,
  Column,
  Entity,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
  Unique,
} from 'typeorm';
import { User } from '../user/user.entity';
import { Meeting } from '../meeting/meeting.entity';
import { Like } from '../like/like.entity';
import { Post } from '../post/post.entity';

@Entity('comment')
@Unique(['id'])
export class Comment extends BaseEntity {
  /** Primary key */
  @PrimaryGeneratedColumn()
  id: number;

  /** 내용 */
  @Column()
  contents: string;

  /** 댓글 깊이 */
  @Column({ default: 0 })
  depth: number;

  /** 댓글 순서 */
  @Column({ default: 0 })
  order: number;

  /** 작성일 */
  @Column()
  createdDate: Date;

  /** 수정일 */
  @Column()
  updatedDate: Date;

  /** 작성자 */
  @ManyToOne(() => User, (user) => user.posts)
  user: User;

  /** 미팅 */
  @ManyToOne(() => Meeting, (meeting) => meeting.posts)
  meeting: Meeting;

  /** 미팅 */
  @ManyToOne(() => Post, (post) => post.comments)
  post: Post;

  /** 좋아요 */
  @OneToMany(() => Like, (like) => like.post)
  likes: Like[];

  /** 좋아요 수 */
  @Column({ default: 0 })
  likeCount: number;

  /** 부모 댓글 */
  @ManyToOne(() => Comment, (comment) => comment.children)
  parent: Comment;

  /** 자식 댓글 */
  @OneToMany(() => Comment, (comment) => comment.parent)
  children: Comment[];
}
