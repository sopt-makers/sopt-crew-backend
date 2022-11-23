import { User } from 'src/users/user.entity';
import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  ManyToMany,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
  RelationId,
} from 'typeorm';
import { Apply } from './apply.entity';

export interface ImageURL {
  id: number;
  url: string;
}

export interface AppliedInfo {
  user: User;
  appliedDate: Date;
  content: string;
}

@Entity('meeting')
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
  category: string;

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

  status: number;

  confirmedApply: Apply[];
}
