import { Repository } from 'typeorm';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { Like } from './like.entity';

@CustomRepository(Like)
export class LikeRepository extends Repository<Like> {}
