import {
  BaseEntity,
  Column,
  Entity,
  ManyToOne,
  PrimaryGeneratedColumn,
  Unique,
} from 'typeorm';
import { User } from '../user/user.entity';
import { Comment } from '../comment/comment.entity';

@Entity('report')
@Unique(['id'])
export class Report extends BaseEntity {
  /** Primary key */
  @PrimaryGeneratedColumn()
  id: number;

  /** 작성일 */
  @Column()
  createdDate: Date;

  /** 작성자 */
  @ManyToOne(() => User, (user) => user.reports)
  reporter: User;

  /** 댓글 */
  @ManyToOne(() => Comment, (comment) => comment.likes)
  comment: Comment;
}
