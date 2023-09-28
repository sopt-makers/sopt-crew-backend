import { Type, applyDecorators } from '@nestjs/common';
import { ApiExtraModels, ApiOkResponse, getSchemaPath } from '@nestjs/swagger';
import { CommonResponseDto } from '../dto/common-response.dto';

/**
 * Swagger Ok Response에 공통 응답 DTO를 추가하는 Decorator
 * @param dataDto 응답 데이터 DTO
 */
export const ApiOkResponseCommon = <DataDto extends Type<unknown>>(
  dataDto: DataDto,
) =>
  applyDecorators(
    ApiExtraModels(CommonResponseDto, dataDto),
    ApiOkResponse({
      description: '성공',
      schema: {
        allOf: [
          { $ref: getSchemaPath(CommonResponseDto) },
          {
            properties: {
              data: { $ref: getSchemaPath(dataDto) },
            },
          },
        ],
      },
    }),
  );
