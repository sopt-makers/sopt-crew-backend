import { Controller, Get } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import {
  HealthCheck,
  HealthCheckService,
  HttpHealthIndicator,
  TypeOrmHealthIndicator,
} from '@nestjs/terminus';

@ApiTags('health-check-controller')
@Controller('/')
export class HealthController {
  constructor(
    private health: HealthCheckService,
    private http: HttpHealthIndicator,
    private db: TypeOrmHealthIndicator,
  ) {}

  // healthcheck를 위한 Router
  @Get()
  @HealthCheck()
  async healthCheck() {
    const webKey =
      process.env.NODE_ENV === 'prod' ? 'crew-web' : 'crew-web-dev';
    const webUrl =
      process.env.NODE_ENV === 'prod'
        ? 'https://playground.sopt.org/group'
        : 'https://sopt-internal-dev.pages.dev/group';

    return this.health.check([
      () => this.http.pingCheck(webKey, webUrl),
      () => this.db.pingCheck('database'),
    ]);
  }
}
