class PathwayParser {
	/**
	 * Parses a pathway into an ambrosia snippet, then save that snippet to Bronte via rtm.
	 * Then returns the original message with a status of completed.
	 *
	 * @param {ExportRequestNotification} message the notification message coming from Sns, sent by the Bronte backend
	 * @throws {Error} when anything fails. It is important to catch this error above
	 */
	async parse(message) {
		const pathway = message.pathway;
		const assets = message.resolvedAssets || [];
		const annotations = message.annotations || [];

		// generate the snippet
		const snippet = generateAmbrosiaSnippet(pathway, assets, annotations);

		// return the completed notification
		return {
			notificationId: message.notificationId,
			elementId: message.elementId,
			accountId: message.accountId,
			projectId: message.projectId,
			workspaceId: message.workspaceId,
			exportId: message.exportId,
			elementType: message.elementType,
			status: 'COMPLETED',
			ambrosiaSnippet: JSON.stringify(snippet),
		};
	}
}

function generateAmbrosiaSnippet(pathway, assets, annotations) {
	// generate an empty snippet for now
	let config = pathway.config;

	for (let i = 0; i < assets.length; i++) {
		const asset = assets[i];
		// support bronte and external asset providers
		const url = asset.data.url ? asset.data.url : asset.data.original.url;
		config = config.replace(asset.urn, url);
	}

	const children = (pathway.children ? pathway.children : []);

	return {
		$ambrosia: `aero:pathway:${pathway.pathwayType}`,
		$id: pathway.pathwayId,
		config: (config ? JSON.parse(config) : null),
		annotations: annotations.map((annotation) => ({
			$ambrosia: `aero:annotation:${annotation.motivation.toLowerCase()}`,
			$id: annotation.id,
			subTarget: annotation.targetJson[0].subTarget,
			target: annotation.targetJson[0],
			body: annotation.bodyJson,
		})),
		children: children.map((child) => child.elementId
		)
	};
}

module.exports = PathwayParser;
