import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { MeetingRepository } from './meeting.repository';
import { Meeting } from './meeting.entity';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { UpdateMeetingDto } from './dto/update-metting-dto';
import { FilterMeetingDto } from './dto/filter-meeting.dto';
import { User } from 'src/auth/user.entity';
import { UserRepository } from 'src/auth/user.repository';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';

@Injectable()
export class MeetingService {
  constructor(
    @InjectRepository(MeetingRepository)
    private readonly meetingRepository: MeetingRepository,

    private readonly userRepository: UserRepository,
  ) {}

  async getMeetingById(id: number): Promise<Meeting> {
    return this.meetingRepository.getMeetingById(id);
  }

  async getAllMeeting(): Promise<Meeting[]> {
    return this.meetingRepository.getAllMeeting();
  }

  async createMeeting(
    createMeetingDto: CreateMeetingDto,
    files: Array<Express.MulterS3.File>,
    user: User,
  ): Promise<void> {
    return this.meetingRepository.createMeeting(createMeetingDto, files, user);
  }

  async updateMeetingById(
    id: number,
    updateMeetingDto: UpdateMeetingDto,
    files: Array<Express.MulterS3.File>,
  ): Promise<void> {
    return this.meetingRepository.updateMeetingById(
      id,
      updateMeetingDto,
      files,
    );
  }

  async deleteMeetingById(id: number): Promise<void> {
    return this.meetingRepository.deleteMeetingById(id);
  }

  async searchMeeting(filterMeetingDto: FilterMeetingDto): Promise<Meeting[]> {
    return this.meetingRepository.searchMeeting(filterMeetingDto);
  }

  async searchMeetingByFilter(
    filterMeetingDTO: FilterMeetingDto,
  ): Promise<Meeting[]> {
    return this.meetingRepository.searchMeetingByFilter(filterMeetingDTO);
  }

  async applyMeeting(applyMeetingDto: ApplyMeetingDto, user: User) {
    // meeting에 user 있는지 저장
    // meeting에 user id 저장
    // meeting 상태 업데이트

    // user 상태 업데이트
    return this.meetingRepository.applyMeeting(applyMeetingDto, user);
  }
}
