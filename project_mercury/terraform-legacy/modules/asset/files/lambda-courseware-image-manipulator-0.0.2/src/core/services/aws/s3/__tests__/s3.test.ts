import * as AWSMock from 'aws-sdk-mock';

import { S3 } from '../s3';

const mockStorageInfo = {
	bucket: 'test-bucket',
	prefix: 'imgs',
	key: 'kitty-img.jpg',
};

afterAll(() => {
	AWSMock.restore();
});

describe('S3', () => {
	const mockS3GetObject = jest.fn().mockResolvedValue({ Body: mockStorageInfo.key});
	const mockS3UploadObject = jest.fn().mockResolvedValue({ Key: mockStorageInfo.key});
	AWSMock.mock('S3', 'upload', mockS3UploadObject);
	AWSMock.mock('S3', 'getObject', mockS3GetObject);

	const s3 = new S3();
	
	describe('get', () => {
    test('should get file', async () => {
			const result = await s3.get(mockStorageInfo);
			expect(result.Body).toBe(mockStorageInfo.key);
			expect(mockS3GetObject).toBeCalledTimes(1)
		});
	});

	describe('put', () => {
		test('should upload file', async () => {
			const result = await s3.put({ ...mockStorageInfo, body: 'message' });
			expect(result.Key).toBe(mockStorageInfo.key);
		});
	});
});
