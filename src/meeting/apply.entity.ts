import { User } from 'src/users/user.entity';
import { Meeting } from 'src/meeting/meeting.entity';

import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  ManyToOne,
  PrimaryGeneratedColumn,
  RelationId,
} from 'typeorm';

import * as dayjs from 'dayjs';

export interface ImageURL {
  id: number;
  url: string;
}

export interface AppliedInfo {
  user: User;
  appliedDate: Date;
  content: string;
}

export enum ApplyType {
  APPLY = 0,
  INVITE = 1,
}

export enum ApplyStatus {
  WAITING = 0,
  APPROVE = 1,
  REJECT = 2,
}

@Entity('apply')
export class Apply extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

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

  @Column()
  content: string;

  @Column()
  appliedDate: Date;

  @Column({ default: ApplyStatus.WAITING })
  status: ApplyStatus;

  static async createApply(
    user: User,
    content: string,
    meeting: Meeting,
    type: ApplyType = ApplyType.APPLY,
  ) {
    const nowDate = dayjs().toDate();
    return await this.create({
      user,
      content,
      appliedDate: nowDate,
      meeting,
      type,
    }).save();
  }
}
