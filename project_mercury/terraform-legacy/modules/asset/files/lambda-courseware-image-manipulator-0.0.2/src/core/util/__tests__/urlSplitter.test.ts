import * as urlSplitter from '../urlSplitter';

// Tried pulling in from urlSplitter but was throwing undefined
const FIXEDPREFIX = 'assets/';

describe('utils urlSplitter', () => {
	const mockPrefix = 'prefix/size';
	const mockKey = 'key.jpg';
	const mockUrl = `${mockPrefix}/${mockKey}`;
	const mockBucket = 'bronteBucket';
	describe('getS3PrefixKeyFromUrl', () => {
		test('should return prefix & key from a given S3 object url', () => {
			const result = urlSplitter.getS3PrefixKeyFromUrl(mockUrl);

			expect(result.key).toBe(mockKey);
			expect(result.prefix).toBe(`${FIXEDPREFIX}${mockPrefix}`);
		});
	});

	describe('generateGetParams', () => {
		test('should return get params from a url & bucketName', () => {
			const result = urlSplitter.generateGetParams(mockUrl, mockBucket);
			expect(result.key).toBe(mockKey);
			expect(result.prefix).toBe(`${FIXEDPREFIX}${mockPrefix}`);
			expect(result.bucket).toBe(mockBucket);
		});
	});

	describe('convertPrefixSize', () => {
		test('should return get params from a url & bucketName', () => {
			const mockPrefix = `${FIXEDPREFIX}prefix/normal-size`;
			const result = urlSplitter.convertPrefixSize(mockPrefix, 'large');
			expect(result).toBe(`${FIXEDPREFIX}prefix/large`);
		});
	});

	describe('generateS3Params', () => {
		test('should return get params and upload params from url & bucketName', () => {
			// @ts-ignore
			const result = urlSplitter.generateS3Params({ url: mockUrl, size: 'large' }, mockBucket);
			expect(result.getParams.bucket).toBe(mockBucket);
			expect(result.getParams.prefix).toBe(`${FIXEDPREFIX}${mockPrefix}`);
			expect(result.uploadParams.key).toBe(mockKey);
			expect(result.uploadParams.prefix).toBe(`${FIXEDPREFIX}prefix/large`);
		});
	});
});
