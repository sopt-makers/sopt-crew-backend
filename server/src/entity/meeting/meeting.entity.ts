import { User } from 'src/entity/user/user.entity';
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
import { Apply } from '../apply/apply.entity';
import { MeetingJoinablePart } from './enum/meeting-joinable-part.enum';
import { MeetingCategory } from './enum/meeting-category.enum';
import { ImageURL } from './interface/image-url.interface';

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
  note: string | null;

  /** 멘토 필요 여부 */
  @Column()
  isMentorNeeded: boolean;

  /**
   * 활동 기수만 참여 가능한지 여부
   * */
  @Column()
  canJoinOnlyActiveGeneration: boolean;

  /**
   * 대상 활동 기수
   * null인 경우 모든 기수 허용
   * */
  @Column({
    nullable: true,
    default: null,
  })
  targetActiveGeneration: number | null;

  @Column({
    type: 'enum',
    enum: MeetingJoinablePart,
    array: true,
    nullable: true,
  })
  joinableParts: MeetingJoinablePart[];
}
