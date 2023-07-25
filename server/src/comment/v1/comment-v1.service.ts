import { InjectRepository } from '@nestjs/typeorm';
import { BadRequestException, Injectable } from '@nestjs/common';
import { CommentRepository } from 'src/entity/comment/comment.repository';
import dayjs from 'dayjs';
import { CommentV1CreateCommentResponseDto } from './dto/create-comment/comment-v1-create-comment-response.dto';
import { CommentV1CreateCommentBodyDto } from './dto/create-comment/comment-v1-create-comment-body.dto';
import { User } from 'src/entity/user/user.entity';
import { PostRepository } from 'src/entity/post/post.repository';
import { CommentV1GetCommentsQueryDto } from './dto/get-comments/comment-v1-get-comments-query.dto';
import { CommentV1GetCommentsResponseDto } from './dto/get-comments/comment-v1-get-comments-response.dto';
import { PageOptionsDto } from 'src/common/pagination/dto/page-options.dto';
import { PageMetaDto } from 'src/common/pagination/dto/page-meta.dto';
import { LikeRepository } from 'src/entity/like/like.repository';
import { CommentV1SwitchCommentLikeParamDto } from './dto/switch-comment-like/comment-v1-switch-comment-like-param.dto';
import { CommentV1SwitchCommentLikeResponseDto } from './dto/switch-comment-like/comment-v1-switch-comment-like-response.dto';
import { CommentV1ReportCommentParamDto } from './dto/report-comment/comment-v1-report-comment-param.dto';
import { CommentV1ReportCommentResponseDto } from './dto/report-comment/comment-v1-report-comment-response.dto';
import { ReportRepository } from 'src/entity/report/report.repository';

@Injectable()
export class CommentV1Service {
  constructor(
    @InjectRepository(CommentRepository)
    private readonly commentRepository: CommentRepository,

    @InjectRepository(PostRepository)
    private readonly postRepository: PostRepository,

    @InjectRepository(LikeRepository)
    private readonly likeRepository: LikeRepository,

    @InjectRepository(ReportRepository)
    private readonly reportRepository: ReportRepository,
  ) {}

  /**
   * 모임 게시글 댓글 목록 조회
   * - 모든 유저가 조회 가능
   */
  async getComments({
    postId,
    page,
    take,
    skip,
  }: CommentV1GetCommentsQueryDto): Promise<CommentV1GetCommentsResponseDto | null> {
    const [comments, commentAmount] = await this.commentRepository.findAndCount(
      {
        where: { postId },
        relations: ['user'],
        order: { id: 'ASC' },
        skip,
        take,
      },
    );

    if (commentAmount === 0) {
      return null;
    }

    const pageOptions: PageOptionsDto = {
      page,
      skip,
      take,
    };
    const pageMeta: PageMetaDto = new PageMetaDto({
      pageOptionsDto: pageOptions,
      itemCount: commentAmount,
    });

    return {
      meta: pageMeta,
      comments: comments.map((comment) => {
        return {
          id: comment.id,
          contents: comment.contents,
          updatedDate: comment.updatedDate,
          likeCount: comment.likeCount,
          // TODO: 좋아요 여부 확인
          isLiked: false,
          user: {
            id: comment.user.id,
            name: comment.user.name,
            profileImage: comment.user.profileImage,
          },
        };
      }),
    };
  }

  /**
   * 댓글 좋아요 토글
   * - 회원만 할 수 있음
   * - 좋아요 버튼 누르면 삭제했다가 다시 생김
   */
  async switchCommentLike({
    param,
    user,
  }: {
    param: CommentV1SwitchCommentLikeParamDto;
    user: User;
  }): Promise<CommentV1SwitchCommentLikeResponseDto> {
    const deletedLike = await this.likeRepository.delete({
      userId: user.id,
      commentId: param.commentId,
    });

    /** 삭제된 좋아요 정보가 없을 경우 */
    if (!deletedLike.affected) {
      const nowDate = dayjs().toDate();
      const createLikeQuery = this.likeRepository
        .create({
          createdDate: nowDate,
          userId: user.id,
          commentId: param.commentId,
        })
        .save();
      const increaseLikeCountQuery = this.commentRepository.increment(
        {
          id: param.commentId,
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

    /** 삭제된 경우 댓글의 좋아요 개수를 감소시킴 */
    await this.commentRepository.decrement(
      {
        id: param.commentId,
      },
      'likeCount',
      1,
    );

    return {
      isLiked: false,
    };
  }

  /**
   * 댓글 신고
   * - 회원만 할 수 있음
   * - 중복 신고는 안 됨
   */
  async reportComment({
    param,
    user,
  }: {
    param: CommentV1ReportCommentParamDto;
    user: User;
  }): Promise<CommentV1ReportCommentResponseDto> {
    const isExistedReport = await this.reportRepository.findOne({
      where: { userId: user.id, commentId: param.commentId },
    });

    // 이미 해당 댓글에 신고를 했을 경우
    if (isExistedReport) {
      throw new BadRequestException('이미 신고된 댓글입니다.');
    }

    const report = await this.reportRepository
      .create({
        createdDate: dayjs().toDate(),
        userId: user.id,
        commentId: param.commentId,
      })
      .save();

    return {
      reportId: report.id,
    };
  }

  /**
   * 모임 게시글 댓글 작성
   * - 회원만 달 수 있음
   */
  async createPostComment({
    body,
    user,
  }: {
    body: CommentV1CreateCommentBodyDto;
    user: User;
  }): Promise<CommentV1CreateCommentResponseDto> {
    const post = await this.postRepository.findOne({
      where: { id: body.postId },
    });

    if (!post) {
      throw new BadRequestException('존재하지 않는 게시글입니다.');
    }

    const nowDate = dayjs().toDate();
    const input = {
      contents: body.contents,
      createdDate: nowDate,
      updatedDate: nowDate,
      userId: user.id,
      postId: body.postId,
    };

    const comment = await this.commentRepository.create(input).save();

    this.postRepository.increment({ id: body.postId }, 'commentCount', 1);

    return {
      commentId: comment.id,
    };
  }
}
