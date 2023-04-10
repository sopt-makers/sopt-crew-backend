export declare global {
  type AnyObject = Record<string, unknown>;

  namespace NodeJS {
    interface ProcessEnv {
      DB_HOST: string;
      DB_PORT: string;
      DB_USERNAME: string;
      DB_PASSWORD: string;
      DB_USER: string;
      DB_SCHEMA: string;

      AWS_S3_BUCKET_NAME: string;
      AWS_ACCESS_KEY_ID: string;
      AWS_SECRET_ACCESS_KEY: string;
      AWS_REGION: string;

      JWT_SECRET: string;
    }
  }

  namespace Express {
    // interface Request {
    //   id: string;
    // }
    // // eslint-disable-next-line @typescript-eslint/no-empty-interface
    // interface User extends Payload {}
  }
}
