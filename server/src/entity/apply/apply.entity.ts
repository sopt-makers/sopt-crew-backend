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
  // primary key
  @PrimaryGeneratedColumn()
  id: number;

  // 지원 or 초대
  @Column({ default: ApplyType.APPLY })
  type: ApplyType;

  @RelationId((apply: Apply) => apply.meeting) // you need to specify target relation
  @Column()
  meetingId: number;

  @ManyToOne(() => Meeting, (meeting) => meeting.id, {
    onDelete: 'CASCADE',
  })
  meeting: Meeting;

  @RelationId((apply: Apply) => apply.user) // you need to specify target relation
  @Column()
  userId: number;

  @ManyToOne(() => User, (user) => user.id, {
    onDelete: 'CASCADE',
    eager: true,
  })
  @JoinTable()
  user: User;

  // 지원 동기
  @Column()
  content: string;

  // 지원한 날짜
  @Column()
  appliedDate: Date;

  // 지원 상태
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
