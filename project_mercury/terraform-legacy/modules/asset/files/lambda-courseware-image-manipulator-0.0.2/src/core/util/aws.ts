import { IImageResizeMessageBody } from '../types';

/**
 * This utility function would take an incoming lambda Sns event
 * and return an array of records parsed out as objects
 * @param event
 */
export const parseLambdaSnsResizeEvent = (event): IImageResizeMessageBody[] => {
	if (event.Records?.[0]?.Sns?.Message) {
		return event.Records.map((record) => JSON.parse(record.Sns.Message));
	}
	return [];
};
