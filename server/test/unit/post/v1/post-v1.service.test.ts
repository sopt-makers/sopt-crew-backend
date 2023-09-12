import { ConfigModule, ConfigService } from "@nestjs/config";
import { Test } from "@nestjs/testing";
import { TypeOrmModule } from "@nestjs/typeorm";
import { TypeOrmExModule } from "src/db/typeorm-ex.module";
import { LikeRepository } from "src/entity/like/like.repository";
import { MeetingRepository } from "src/entity/meeting/meeting.repository";
import { PostRepository } from "src/entity/post/post.repository";
import { ReportRepository } from "src/entity/report/report.repository";
import { PostV1GetPostCountQueryDto } from "src/post/v1/dto/get-meeting-post-count/post-v1-get-post-count-query.dto";
import { PostV1Service } from "src/post/v1/post-v1.service";


describe('PostV1Service', () => {
    let service: PostV1Service;
    let postRepository: PostRepository;

    beforeAll(async () => {
        const module = await Test.createTestingModule({
          imports: [
            ConfigModule.forRoot({
              isGlobal: true,
              envFilePath:'.dev.env'
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
            TypeOrmExModule.forCustomRepository([
              PostRepository,
              MeetingRepository,
              LikeRepository,
              ReportRepository,
            ]),
          ],
          providers: [PostV1Service],
        }).compile();

        service = module.get<PostV1Service>(PostV1Service);
        postRepository = module.get<PostRepository>(PostRepository);
    });

    describe('getPostCount', () => {
        it('모임에 작성된 게시글 개수를 조회한다.', async () => {
            const getPostCountQueryDto = {
                meetingId : 1
            } as unknown as PostV1GetPostCountQueryDto;
    
            jest.spyOn(postRepository, 'count').mockResolvedValue(2);
            const result = await service.getPostCount(getPostCountQueryDto);
        
            expect(result.postCount).toEqual(2);
            expect(postRepository.count).toHaveBeenCalledWith({
                where : {
                    meetingId: getPostCountQueryDto.meetingId
                }
            });
        });
    });
});