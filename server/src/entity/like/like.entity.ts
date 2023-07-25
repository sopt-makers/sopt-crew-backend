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
import { Post } from '../post/post.entity';
import { Comment } from '../comment/comment.entity';

@Entity('like')
@Unique(['id'])
export class Like extends BaseEntity {
  /** Primary key */
  @PrimaryGeneratedColumn()
  id: number;

  /** 좋아요 누른 날짜 */
  @Column()
  createdDate: Date;

  /** 좋아요 누른사람 */
  @ManyToOne(() => User, (user) => user.likes)
  user: User;

  /** 좋아요 누른사람 id */
  @RelationId((like: Like) => like.user)
  @Column()
  userId: number;

  /** 게시글 */
  @ManyToOne(() => Post, (post) => post.likes)
  post: Post;

  /**
   * 게시글 id
   * - 게시글 좋아요가 아닐 경우 null
   * */
  @RelationId((like: Like) => like.post)
  @Column({ nullable: true, default: null })
  postId: number | null;

  /** 댓글 */
  @ManyToOne(() => Comment, (comment) => comment.likes)
  comment: Comment;

  /**
   * 댓글 id
   * - 댓글 좋아요가 아닐 경우 null
   * */
  @RelationId((like: Like) => like.comment)
  @Column({ nullable: true, default: null })
  commentId: number | null;
}
