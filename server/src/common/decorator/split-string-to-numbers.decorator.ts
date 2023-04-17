import { Transform } from 'class-transformer';

/**
 * ,로 구분된 string을 number[]로 변환
 * @rdd9223
 * @returns ,로 구분된 string을 number[]로 변환
 */
export const SplitStringToNumbers = () =>
  Transform(({ value }) => {
    const result = value.split(',').map((v: string) => parseInt(v, 10));

    return result;
  });
