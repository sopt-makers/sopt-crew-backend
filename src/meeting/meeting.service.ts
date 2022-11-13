import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { MeetingRepository } from './meeting.repository';
import { Meeting } from './meeting.entity';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { UpdateMeetingDto } from './dto/update-metting-dto';
import { FilterMeetingDto } from './dto/filter-meeting.dto';
import { User } from 'src/users/user.entity';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';
import { UserRepository } from 'src/users/user.repository';
import { GetMeetingDto } from './dto/get-meeting.dto';
import { GetListDto } from './dto/get-list.dto';

@Injectable()
export class MeetingService {
  constructor(
    @InjectRepository(MeetingRepository)
    private readonly meetingRepository: MeetingRepository,

    private readonly userRepository: UserRepository,
  ) {}

  async getListByMeeting(id: number, user: User, getListDto: GetListDto) {
    return this.meetingRepository.getListByMeeting(id, user, getListDto);
  }

  async getMeetingById(id: number): Promise<Meeting> {
    return this.meetingRepository.getMeetingById(id);
  }

  async getAllMeeting(getMeetingDto: GetMeetingDto): Promise<Meeting[]> {
    return this.meetingRepository.getAllMeeting(getMeetingDto);
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
