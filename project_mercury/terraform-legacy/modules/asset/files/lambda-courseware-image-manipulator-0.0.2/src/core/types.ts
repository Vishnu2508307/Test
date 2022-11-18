export interface IImageResizeMessageBody {
	notificationId?: string;
	assetId?: string;
	url: string;
	size: string;
	threshold: number;
	originalWidth: number;
	originalHeight: number;
}
