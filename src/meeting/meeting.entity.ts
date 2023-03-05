import { User } from 'src/users/user.entity';
import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
  RelationId,
  Unique,
} from 'typeorm';
import { Apply, ApplyType } from './apply.entity';

export enum MeetingStatus {
  PRE = 0,
  POSSIBLE = 1,
  END = 2,
}

export enum MeetingCategory {
  STUDY = '스터디',
  LECTURE = '강연',
  LIGHTNING = '번개',
}

export interface ImageURL {
  id: number;
  url: string;
}

export interface AppliedInfo {
  user: User;
  appliedDate: Date;
  content: string;
  type: ApplyType;
}

@Entity('meeting')
@Unique(['id'])
export class Meeting extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @ManyToOne(() => User, (user) => user.meetings, {
    onDelete: 'CASCADE',
  })
  user: User;

  @RelationId((meeting: Meeting) => meeting.user) // you need to specify target relation
  @Column()
  userId: number;

  @OneToMany(() => Apply, (apply) => apply.meeting, {
    onDelete: 'CASCADE',
    cascade: ['remove'],
  })
  @JoinTable()
  appliedInfo: Apply[];

  @Column()
  title: string;

  @Column()
  category: MeetingCategory;

  @Column('jsonb')
  imageURL: ImageURL[];

  @Column()
  startDate: Date;

  @Column()
  endDate: Date;

  @Column()
  capacity: number;

  @Column()
  desc: string;

  @Column()
  processDesc: string;

  @Column()
  mStartDate: Date;

  @Column()
  mEndDate: Date;

  @Column()
  leaderDesc: string;

  @Column()
  targetDesc: string;

  @Column({ nullable: true })
  note: string;

  status: MeetingStatus;
  confirmedApply: Apply[];
  host: boolean;
  apply: boolean;
  invite: boolean;
  approved: boolean;
}
