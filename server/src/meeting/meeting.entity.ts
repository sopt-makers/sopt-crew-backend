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

  // 개설한 유저
  @ManyToOne(() => User, (user) => user.meetings, {
    onDelete: 'CASCADE',
  })
  user: User;

  // 유저 id
  @RelationId((meeting: Meeting) => meeting.user) // you need to specify target relation
  @Column()
  userId: number;

  // 지원or초대 정보
  @OneToMany(() => Apply, (apply) => apply.meeting, {
    onDelete: 'CASCADE',
    cascade: ['remove'],
  })
  @JoinTable()
  appliedInfo: Apply[];

  // 모임 제목
  @Column()
  title: string;

  // 모임 카테고리
  @Column()
  category: MeetingCategory;

  // 이미지
  @Column('jsonb')
  imageURL: ImageURL[];

  // 모집 시작 기간
  @Column()
  startDate: Date;

  // 모집 마감 기간
  @Column()
  endDate: Date;

  // 모집 인원
  @Column()
  capacity: number;

  // 모임 소개
  @Column()
  desc: string;

  // 진행방식 소개
  @Column()
  processDesc: string;

  // 모임 시작 기간
  @Column()
  mStartDate: Date;

  // 모임 마감 기간
  @Column()
  mEndDate: Date;

  // 개절자 소개
  @Column()
  leaderDesc: string;

  // 모집 대상
  @Column()
  targetDesc: string;

  // 유의 사항
  @Column({ nullable: true })
  note: string;

  // 칼럼이 아닌 response할 때 meeting 객체에 넣어줄 값
  status: MeetingStatus; // 모임 상태
  confirmedApply: Apply[]; // 승인된 모임
  host: boolean; // 해당 모임의 호스트인지
  apply: boolean; // 해당 모임을 지원 했는지
  invite: boolean; // 해당 모임에 초대 받았는지
  approved: boolean; // 초대를 받았다면 승인을 했는지
}
