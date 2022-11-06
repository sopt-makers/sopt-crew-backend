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
@Unique(['id'])
export class User {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  name: string;

  @Column()
  originId: string;

  @OneToMany(() => Meeting, (meeting) => meeting.user, { eager: true })
  meetings: Meeting[];

  @OneToMany(() => Apply, (apply) => apply.user)
  @JoinTable()
  apply: Apply[];
}
