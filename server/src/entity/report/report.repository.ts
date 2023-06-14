import { Repository } from 'typeorm';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { Report } from './report.entity';

@CustomRepository(Report)
export class ReportRepository extends Repository<Report> {}
