import { Apply } from 'src/meeting/apply.entity';
import { Meeting } from 'src/meeting/meeting.entity';
import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  OneToMany,
  PrimaryGeneratedColumn,
  Unique,
} from 'typeorm';

@Entity('user')
@Unique(['id'])
// 주석 추가해놓기
export class User extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  name: string;

  @Column()
  orgId: number;

  @Column({
    nullable: true,
    default: '',
  })
  profileImage: string;

  @OneToMany(() => Meeting, (meeting) => meeting.user)
  meetings: Meeting[];

  @OneToMany(() => Apply, (apply) => apply.user)
  @JoinTable()
  apply: Apply[];
}
