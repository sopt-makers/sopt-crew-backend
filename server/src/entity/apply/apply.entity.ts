import { User } from 'src/entity/user/user.entity';

import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  ManyToOne,
  PrimaryGeneratedColumn,
  RelationId,
} from 'typeorm';
import { Meeting } from '../meeting/meeting.entity';
import { ApplyType } from './enum/apply-type.enum';
import { ApplyStatus } from './enum/apply-status.enum';

@Entity('apply')
export class Apply extends BaseEntity {
  /** Primary Key */
  @PrimaryGeneratedColumn()
  id: number;

  /** 지원 타입 */
  @Column({ default: ApplyType.APPLY })
  type: ApplyType;

  /** 지원한 모임 ID */
  @RelationId((apply: Apply) => apply.meeting) // you need to specify target relation
  @Column()
  meetingId: number;

  /** 지원한 모임 */
  @ManyToOne(() => Meeting, (meeting) => meeting.id, {
    onDelete: 'CASCADE',
  })
  meeting: Meeting;

  /** 지원자 ID */
  @RelationId((apply: Apply) => apply.user) // you need to specify target relation
  @Column()
  userId: number;

  /** 지원자 */
  @ManyToOne(() => User, (user) => user.id, {
    onDelete: 'CASCADE',
    eager: true,
  })
  @JoinTable()
  user: User;

  /** 지원 동기 */
  @Column()
  content: string;

  /** 지원한 날짜 */
  @Column()
  appliedDate: Date;

  /** 지원 상태 */
  @Column({ default: ApplyStatus.WAITING })
  status: ApplyStatus;

  static async createApply(
    user: User,
    content: string,
    meeting: Meeting,
    type: ApplyType = ApplyType.APPLY,
    appliedDate: Date,
  ) {
    return await this.create({
      user,
      content,
      appliedDate: appliedDate,
      meeting,
      type,
    }).save();
  }
}
