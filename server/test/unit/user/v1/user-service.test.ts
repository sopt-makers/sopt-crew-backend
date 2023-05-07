import { User } from 'src/entity/user/user.entity';
import { UserV1Service } from 'src/user/v1/user-v1.service';

describe('UserV1Service', () => {
  let service: UserV1Service;

  beforeAll(() => {
    service = new UserV1Service();
  });

  describe('getUserOwnProfile', () => {
    it('유저 정보가 들어오면 성공한다.', () => {
      const mockInput = {
        id: 1,
        name: 1,
        orgId: 1,
        profileImage: 1,
        activities: null,
        phone: null,
      } as unknown as User;

      const result = service.getUserOwnProfile(mockInput);

      expect(result).toEqual({
        id: mockInput.id,
        name: mockInput.name,
        orgId: mockInput.orgId,
        profileImage: mockInput.profileImage,
      });
    });
  });
});
