const awsUtils = require('../aws');

const snsMockMessage = require('./testData/snsMockMessage.json');

describe('utils aws', () => {
	describe('parseLambdaSnsResizeEvent', () => {
		test('should return all messages in sns record as array', () => {
			const result = awsUtils.parseLambdaSnsResizeEvent(snsMockMessage);
			expect(result[0]).toEqual(JSON.parse(snsMockMessage.Records[0].Sns.Message));
		}),
			test('should return empty array if no messages in event', () => {
				const result = awsUtils.parseLambdaSnsResizeEvent({ records: [] });
				expect(result).toEqual([]);
				expect(result[0]).toBe(undefined);
			});
	});
});
