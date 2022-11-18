import * as AWSMock from 'aws-sdk-mock';

import { SNS, SnsTopics } from '../sns';

const mockSNSMessage = {
	bucket: 'test-bucket',
	prefix: 'imgs',
	key: 'kitty-img.jpg',
};

afterAll(() => {
	AWSMock.restore();
});

describe('SNS', () => {
	const mockSNSPublish = jest.fn().mockResolvedValue({ MessageId: 'message-1' });
	AWSMock.mock('SNS', 'publish', mockSNSPublish);

	const sns = new SNS();

	describe('publish', () => {
		test('should publish message', async () => {
			const result = await sns.publish(JSON.stringify(mockSNSMessage), SnsTopics.SUCCESS);
			expect(result.MessageId).toBe('message-1');
			expect(mockSNSPublish).toBeCalledTimes(1);
		});
	});
});
