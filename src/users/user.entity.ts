import { Apply } from 'src/meeting/apply.entity';
import { Meeting } from 'src/meeting/meeting.entity';
import {
  BaseEntity,
  Column,
  Entity,
  JoinTable,
  ManyToMany,
  OneToMany,
  PrimaryGeneratedColumn,
  Unique,
} from 'typeorm';

@Entity('user')
@Unique(['id', 'originId'])
export class User {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  name: string;

  @Column()
  originId: number;

  @OneToMany(() => Meeting, (meeting) => meeting.user)
  meetings: Meeting[];

  @OneToMany(() => Apply, (apply) => apply.user)
  @JoinTable()
  apply: Apply[];
}
