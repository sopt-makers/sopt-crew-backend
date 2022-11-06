import { HttpException, HttpStatus } from '@nestjs/common';
import { User } from 'src/auth/user.entity';
import { UserRepository } from 'src/auth/user.repository';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { LessThan, Like, MoreThan, Repository } from 'typeorm';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { FilterMeetingDto } from './dto/filter-meeting.dto';
import { UpdateMeetingDto } from './dto/update-metting-dto';
import { Meeting, ImageURL } from './meeting.entity';
import { Apply } from './apply.entity';

@CustomRepository(Meeting)
export class MeetingRepository extends Repository<Meeting> {
  async getMeetingById(id: number): Promise<Meeting> {
    const meeting = await this.findOne({
      where: { id: id },
      relations: ['user', 'appliedInfo'],
    });
    return meeting;
  }

  async getAllMeeting() {
    const meetings = await this.find({ relations: ['user', 'appliedInfo'] });
    if (!meetings) {
      throw new HttpException(
        { message: '배열이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }
    return meetings;
  }

  async createMeeting(
    createMeetingDto: CreateMeetingDto,
    files: Array<Express.MulterS3.File>,
    user: User,
  ): Promise<Meeting> {
    const imageURL: Array<ImageURL> = files.map((file, index) => ({
      id: index,
      url: file.location,
    }));

    const meeting = await this.create({
      ...createMeetingDto,
      imageURL,
      user,
      appliedInfo: [],
    });
    const savedMeeting = await this.save(meeting);
    return savedMeeting;
  }

  async updateMeetingById(
    id: number,
    updateMeetingDto: UpdateMeetingDto,
    files: Array<Express.MulterS3.File>,
  ): Promise<void> {
    const imageURL = files.map((file) => file.location);

    const meeting = await this.update(id, updateMeetingDto);

    return null;
  }

  async deleteMeetingById(id: number): Promise<void> {
    await this.delete(id);
    return null;
  }

  async searchMeeting(
    query: string,
    filterMeetingDto: FilterMeetingDto,
  ): Promise<Meeting[]> {
    const { category, status } = filterMeetingDto;
    const nowDate = new Date('2022-09-20');

    const statusDate =
      status === 0
        ? null
        : status === 1
        ? {
            startDate: LessThan(nowDate),
            endDate: MoreThan(nowDate),
          }
        : {
            startDate: MoreThan(nowDate),
            endDate: LessThan(nowDate),
          };

    const querys = category.map((item) => ({
      category: item,
      title: Like(`%${query}%`),
      ...statusDate,
    }));

    const result = await this.find({
      where: querys,
    });
    return result;
  }

  async searchMeetingByFilter(
    filterMeetingDTO: FilterMeetingDto,
  ): Promise<Meeting[]> {
    const { category, status } = filterMeetingDTO;
    const nowDate = new Date('2022-09-20');

    const statusDate =
      status === 0
        ? null
        : status === 1
        ? {
            startDate: LessThan(nowDate),
            endDate: MoreThan(nowDate),
          }
        : {
            startDate: MoreThan(nowDate),
            endDate: LessThan(nowDate),
          };

    const query = category.map((item) => ({
      category: item,
      ...statusDate,
    }));

    const result = await this.find({
      where: query,
    });

    return result;
  }

  async applyMeeting(id: number, content: string, user: User) {
    // meeting에 user 있는지 저장
    // meeting에 user id 저장
    // meeting 상태 업데이트
    const meeting = await this.findOne({
      where: { id },
      relations: ['user', 'appliedInfo'],
    });

    console.log(meeting);

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const result = meeting.appliedInfo.findIndex(
      (target) => target.user.id === user.id,
    );

    if (result === -1) {
      const apply = await Apply.createApply(user, content, meeting);
      // meeting.appliedUser.push({
      //   content,
      //   appliedDate: nowDate,
      //   user,
      //   status: false,
      // });
      meeting.appliedInfo.push(apply);
    } else {
      console.log('?!?!?!');
      const targetApply = meeting.appliedInfo[result];
      meeting.appliedInfo.splice(result, 1);
      await Apply.delete({ id: targetApply.id });
    }
    await this.save(meeting);

    return null;
  }
}
