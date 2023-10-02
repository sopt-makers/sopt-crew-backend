import { EntityManager, Repository } from 'typeorm';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { Post } from './post.entity';

@CustomRepository(Post)
export class PostRepository extends Repository<Post> {
  async getTransaction(): Promise<EntityManager> {
    return this.manager;
  }
}
