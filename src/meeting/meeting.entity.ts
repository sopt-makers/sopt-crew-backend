import { User } from 'src/auth/user.entity';
import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  ManyToMany,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
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

  @Column('text', { array: true })
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
}
