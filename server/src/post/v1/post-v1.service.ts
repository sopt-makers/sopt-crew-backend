import { InjectRepository } from '@nestjs/typeorm';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
} from '@nestjs/common';
import { PostRepository } from 'src/entity/post/post.repository';
import { PostV1CreatePostBodyDto } from './dto/create-meeting-post/post-v1-create-post-body.dto';
import { User } from 'src/entity/user/user.entity';
import { MeetingRepository } from 'src/entity/meeting/meeting.repository';
import { PostV1CreatePostResponseDto } from './dto/create-meeting-post/post-v1-create-post-response.dto';
import { ApplyStatus } from 'src/entity/apply/enum/apply-status.enum';
import dayjs from 'dayjs';
import { PostV1GetPostResponseDto } from './dto/get-meeting-post/post-v1-get-post-response.dto';
import { PostV1GetPostsResponseDto } from './dto/get-meeting-posts/post-v1-get-posts-response.dto';
import { PageOptionsDto } from 'src/common/pagination/dto/page-options.dto';
import { PostV1GetPostsQueryDto } from './dto/get-meeting-posts/post-v1-get-posts-query.dto';
import { PageMetaDto } from 'src/common/pagination/dto/page-meta.dto';
import { PostV1SwitchPostLikeParamDto } from './dto/switch-post-like/post-v1-switch-post-like-param.dto';
import { PostV1SwitchPostLikeResponseDto } from './dto/switch-post-like/post-v1-switch-post-like-response.dto';
import { LikeRepository } from 'src/entity/like/like.repository';
import { ReportRepository } from 'src/entity/report/report.repository';
import { PostV1ReportPostParamDto } from './dto/report-post/post-v1-report-post-param.dto';
import { PostV1ReportPostResponseDto } from './dto/report-post/post-v1-report-post-response.dto';
import { PostV1GetPostCountQueryDto } from './dto/get-meeting-post-count/post-v1-get-post-count-query.dto';
import { PostV1GetPostCountResponseDto } from './dto/get-meeting-post-count/post-v1-get-post-count-response.dto';

@Injectable()
export class PostV1Service {
  constructor(
    @InjectRepository(PostRepository)
    private readonly postRepository: PostRepository,

    @InjectRepository(MeetingRepository)
    private readonly meetingRepository: MeetingRepository,

    @InjectRepository(LikeRepository)
    private readonly likeRepository: LikeRepository,

    @InjectRepository(ReportRepository)
    private readonly reportRepository: ReportRepository,
  ) {}

  /**
   * 모임 게시글 목록 조회
   * - 모든 유저가 조회 가능
   */
  async getPosts(
    query: PostV1GetPostsQueryDto,
  ): Promise<PostV1GetPostsResponseDto | null> {
    const [posts, postAmount] = await this.postRepository.findAndCount({
      where: { meetingId: query.meetingId },
      relations: ['meeting', 'user', 'comments', 'comments.user'],
      order: { id: 'DESC' },
      skip: query.skip,
      take: query.take,
    });

    if (postAmount === 0) {
      return null;
    }

    const pageOptions: PageOptionsDto = {
      page: query.page,
      skip: query.skip,
      take: query.take,
    };
    const pageMeta: PageMetaDto = new PageMetaDto({
      pageOptionsDto: pageOptions,
      itemCount: postAmount,
    });

    return {
      posts: posts.map((post) => {
        /** 중복제거 후 앞 3개만 추림 */
        const commenterThumbnails = post.comments
          .filter((item, index, self) => {
            return self.findIndex((t) => t.user.id === item.user.id) === index;
          })
          .map((comment) => comment.user.profileImage)
          .slice(0, 3);

        return {
          id: post.id,
          title: post.title,
          contents: post.contents,
          updatedDate: post.updatedDate,
          images: post.images,
          user: {
            id: post.user.id,
            name: post.user.name,
            profileImage: post.user.profileImage,
          },
          likeCount: post.likeCount,
          // TODO: 좋아요 여부 조건 나중에 수정
          isLiked: false,
          viewCount: post.viewCount,
          commentCount: post.commentCount,
          commenterThumbnails,
        };
      }),
      meta: pageMeta,
    };
  }

  /**
   * 모임 게시글 단일 조회
   * - 모든 유저가 조회 가능
   * @param postId 게시글 ID
   */
  async getPost({
    postId,
  }: {
    postId: number;
  }): Promise<PostV1GetPostResponseDto> {
    const post = await this.postRepository.findOne({
      where: { id: postId },
      relations: ['meeting', 'user'],
    });

    if (post === null) {
      throw new BadRequestException('게시글이 없습니다.');
    }

    await this.postRepository.increment({ id: postId }, 'viewCount', 1);

    return {
      id: post.id,
      title: post.title,
      contents: post.contents,
      updatedDate: post.updatedDate,
      images: post.images,
      user: {
        id: post.user.id,
        name: post.user.name,
        profileImage: post.user.profileImage,
      },
      viewCount: post.viewCount,
      likeCount: post.likeCount,
      // TODO: 좋아요 여부 어떻게 처리할건지 확인하기
      isLiked: false,
    };
  }

  /**
   * 모임 게시글 작성
   * - 모임에 속한 유저만 작성 가능
   * - 모임에 속한 유저가 아니면 403 에러
   * @param user 유저 정보
   * @param body 게시글 생성 body
   * @returns PostV1CreatePostResponseDto 게시글 생성 response dto
   */
  async createPost({
    user,
    body,
  }: {
    user: User;
    body: PostV1CreatePostBodyDto;
  }): Promise<PostV1CreatePostResponseDto> {
    const meeting = await this.meetingRepository.findOne({
      where: { id: body.meetingId },
      relations: ['appliedInfo'],
    });

    if (meeting === null) {
      throw new BadRequestException('모임이 없습니다.');
    }

    const isInMeeting = meeting.appliedInfo.some((apply) => {
      return apply.userId === user.id && apply.status === ApplyStatus.APPROVE;
    });

    if (isInMeeting === false) {
      throw new ForbiddenException('권한이 없습니다.');
    }

    const nowDate = dayjs().toDate();
    const post = await this.postRepository
      .create({
        userId: user.id,
        meetingId: body.meetingId,
        title: body.title,
        contents: body.contents,
        createdDate: nowDate,
        updatedDate: nowDate,
        images: body.images,
      })
      .save();

    return { postId: post.id };
  }

  /**
   * 모임 게시글 좋아요 토글
   * - 회원만 할 수 있음
   * - 좋아요 버튼 누르면 삭제했다가 다시 생김
   */
  async switchPostLike({
    param,
    user,
  }: {
    param: PostV1SwitchPostLikeParamDto;
    user: User;
  }): Promise<PostV1SwitchPostLikeResponseDto> {
    const deletedLike = await this.likeRepository.delete({
      userId: user.id,
      postId: param.postId,
    });

    /** 삭제된 좋아요 정보가 없을 경우 */
    if (!deletedLike.affected) {
      const nowDate = dayjs().toDate();
      const createLikeQuery = this.likeRepository
        .create({
          createdDate: nowDate,
          userId: user.id,
          postId: param.postId,
        })
        .save();
      const increaseLikeCountQuery = this.postRepository.increment(
        {
          id: param.postId,
        },
        'likeCount',
        1,
      );

      // 좋아요 정보를 생성하고 좋아요 개수를 증가시킨다.
      await Promise.all([createLikeQuery, increaseLikeCountQuery]);

      return {
        isLiked: true,
      };
    }

    /** 삭제된 경우 게시글 좋아요 개수를 감소시킴 */
    await this.postRepository.decrement(
      {
        id: param.postId,
      },
      'likeCount',
      1,
    );

    return {
      isLiked: false,
    };
  }

  /**
   * 게시글 신고
   * - 회원만 할 수 있음
   * - 중복 신고는 안 됨
   */
  async reportPost({
    param,
    user,
  }: {
    param: PostV1ReportPostParamDto;
    user: User;
  }): Promise<PostV1ReportPostResponseDto> {
    const isExistedReport = await this.reportRepository.findOne({
      where: { userId: user.id, postId: param.postId },
    });

    // 이미 해당 게시글에 신고를 했을 경우
    if (isExistedReport) {
      throw new BadRequestException('이미 신고된 게시글입니다.');
    }

    const report = await this.reportRepository
      .create({
        createdDate: dayjs().toDate(),
        userId: user.id,
        postId: param.postId,
      })
      .save();

    return {
      reportId: report.id,
    };
  }

  /**
   * 모임에 생성된 게시글 개수 조회
   * - 모든 유저가 조회 가능
   */
  async getPostCount(
    query : PostV1GetPostCountQueryDto 
  ) : Promise<PostV1GetPostCountResponseDto> {
    const postCount = await this.postRepository.count({
      where : {
        meetingId : query.meetingId
      }
    });

    return {
      postCount
    };
  }
}
