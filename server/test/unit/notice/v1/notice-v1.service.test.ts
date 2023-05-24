import { ConfigModule, ConfigService } from '@nestjs/config';
import { Test } from '@nestjs/testing';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { NoticeRepository } from 'src/entity/notice/notice.repository';
import { EnGetNoticesStatus } from 'src/notice/v1/enum/get-notices-status.enum';
import { NoticeV1Service } from 'src/notice/v1/notice-v1.service';

describe('NoticeV1Service', () => {
  let service: NoticeV1Service;
  let noticeRepository: NoticeRepository;

  beforeAll(async () => {
    const module = await Test.createTestingModule({
      imports: [
        ConfigModule.forRoot({
          isGlobal: true,
          envFilePath: '.dev.env',
        }),
        TypeOrmModule.forRootAsync({
          useFactory: (config: ConfigService) => ({
            type: 'postgres',
            host: config.get('DB_HOST'),
            port: parseInt(config.get('DB_PORT')),
            username: config.get('DB_USERNAME'),
            password: config.get('DB_PASSWORD'),
            database: config.get('DB_NAME'),
            entities: [__dirname + '/**/*.entity.{js,ts}'],
            schema: config.get('DB_SCHEMA'),
            synchronize: false,
          }),
          inject: [ConfigService],
        }),
        TypeOrmExModule.forCustomRepository([NoticeRepository]),
      ],
      providers: [NoticeV1Service],
    }).compile();

    service = module.get<NoticeV1Service>(NoticeV1Service);
    noticeRepository = module.get<NoticeRepository>(NoticeRepository);
  });

  describe('getNotices', () => {
    it('조회에 성공한다.', async () => {
      const mockResolvedValue = [
        {
          id: 1,
          title: 'test',
          subTitle: 'test',
          contents: 'test',
          createdDate: new Date(),
          exposeEndDate: new Date(),
          exposeStartDate: new Date(),
        },
      ] as any;
      jest
        .spyOn(noticeRepository, 'getExposingNotices')
        .mockResolvedValue(mockResolvedValue);

      const notices = await service.getNotices({
        status: EnGetNoticesStatus.EXPOSING,
      });

      expect(notices).toBeDefined();
      expect(notices[0]).toEqual({
        id: 1,
        title: 'test',
        subTitle: 'test',
        contents: 'test',
        createdDate: mockResolvedValue[0].createdDate,
      });
    });

    it('의도하지 않은 status값이 들어오면 null을 반환한다.', async () => {
      const notices = await service.getNotices({
        status: 'test' as any,
      });

      expect(notices).toBeNull();
    });
  });

  describe('createNotice', () => {
    it('공지사항을 생성한다.', async () => {
      const mockResolvedValue = {
        id: 1,
        title: 'test',
        subTitle: 'test',
        contents: 'test',
        createdDate: new Date(),
        exposeEndDate: new Date(),
        exposeStartDate: new Date(),
      } as any;
      jest.spyOn(noticeRepository, 'save').mockResolvedValue(mockResolvedValue);

      const notice = await service.createNotice({
        title: 'test',
        subTitle: 'test',
        contents: 'test',
        exposeEndDate: new Date(),
        exposeStartDate: new Date(),
      });

      expect(notice).toBeDefined();
      expect(notice).toEqual(1);
    });
  });
});
