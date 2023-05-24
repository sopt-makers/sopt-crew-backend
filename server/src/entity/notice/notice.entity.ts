import {
  BaseEntity,
  Column,
  Entity,
  PrimaryGeneratedColumn,
  Unique,
} from 'typeorm';

@Entity('notice')
@Unique(['id'])
export class Notice extends BaseEntity {
  /** primary key */
  @PrimaryGeneratedColumn()
  id: number;

  /** 공지사항 제목 */
  @Column()
  title: string;

  /** 공지사항 부제목 */
  @Column()
  subTitle: string;

  /** 공지사항 내용 */
  @Column()
  contents: string;

  /** 공지사항 작성일 */
  @Column()
  createdDate: Date;

  /** 공지사항 노출 시작일 */
  @Column()
  exposeStartDate: Date;

  /** 공지사항 노출 종료일 */
  @Column()
  exposeEndDate: Date;
}
