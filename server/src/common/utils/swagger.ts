import { INestApplication } from '@nestjs/common';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';
import { BaseExceptionDto } from '../dto/base-exception.dto';
import { CommonResponseDto } from '../dto/common-response.dto';

/**
 * Swagger 세팅
 *
 * @param {INestApplication} app
 */
export function setupSwagger(app: INestApplication): void {
  const options = new DocumentBuilder()
    .setTitle('Crew API 문서')
    .setDescription('Crew API 문서')
    .setVersion('1.0.0')
    .addBearerAuth()
    .build();

  const document = SwaggerModule.createDocument(app, options, {
    extraModels: [BaseExceptionDto, CommonResponseDto],
  });
  SwaggerModule.setup('api-docs', app, document, {
    swaggerOptions: {
      tagsSorter: 'alpha',
      operationsSorter: 'alpha',
    },
  });
}
