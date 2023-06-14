import {
  BaseEntity,
  Column,
  Entity,
  ManyToOne,
  PrimaryGeneratedColumn,
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

  /** 작성일 */
  @Column()
  createdDate: Date;

  /** 수정일 */
  @Column()
  updatedDate: Date;

  /** 작성자 */
  @ManyToOne(() => User, (user) => user.likes)
  user: User;

  /** 게시글 */
  @ManyToOne(() => Post, (post) => post.likes)
  post: Post;

  /** 댓글 */
  @ManyToOne(() => Comment, (comment) => comment.likes)
  comment: Comment;
}
