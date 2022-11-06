import { MulterOptions } from '@nestjs/platform-express/multer/interfaces/multer-options.interface';
import * as multerS3 from 'multer-s3';
import { S3Client } from '@aws-sdk/client-s3';

export const multerOptionsFactory = (): MulterOptions => {
  const s3 = new S3Client({
    region: process.env.AWS_REGION,
    credentials: {
      accessKeyId: process.env.AWS_ACCESS_KEY_ID,
      secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
    },
  });
  return {
    storage: multerS3({
      s3,
      bucket: process.env.AWS_S3_BUCKET_NAME,
      key(_req, file, done) {
        // const ext = path.extname(file.originalname);
        // const basename = path.basename(file.originalname, ext);
        done(null, `${Date.now()}-${file.originalname}`);
      },
    }),
    limits: { fileSize: 10 * 1024 * 1024 }, // 10MB
  };
};
