import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  OneToMany,
  PrimaryGeneratedColumn,
  Unique,
} from 'typeorm';
import { UserActivity } from './interface/user-activity.interface';
import { Meeting } from 'src/entity/meeting/meeting.entity';
import { Apply } from 'src/entity/apply/apply.entity';

@Entity('user')
@Unique(['id'])
export class User extends BaseEntity {
  // Primary Key
  @PrimaryGeneratedColumn()
  id: number;

  // 사용자 이름
  @Column()
  name: string;

  // sopt org unique id
  @Column()
  orgId: number;

  @Column({ type: 'jsonb', nullable: true, default: null })
  activities: UserActivity[];

  // 프로필 이미지
  @Column({
    nullable: true,
    default: null,
  })
  profileImage: string;

  // 내가 생성한 모임
  @OneToMany(() => Meeting, (meeting) => meeting.user)
  meetings: Meeting[];

  // 내가 지원한 내역
  @OneToMany(() => Apply, (apply) => apply.user)
  @JoinTable()
  apply: Apply[];
}
