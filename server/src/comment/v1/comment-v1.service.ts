import { InjectRepository } from '@nestjs/typeorm';
import { Injectable } from '@nestjs/common';
import { CommentRepository } from 'src/entity/comment/comment.repository';

@Injectable()
export class CommentV1Service {
  constructor(
    @InjectRepository(CommentRepository)
    private readonly commentRepository: CommentRepository,
  ) {}
}
