class InteractiveParser {
	/**
	 * Parses an interactive into an ambrosia snippet, then save that snippet to Bronte via rtm.
	 * Then returns the original message with a status of completed.
	 *
	 * @param {ExportRequestNotification} message the notification message coming from Sns, sent by the Bronte backend
	 * @throws {Error} when anything fails. It is important to catch this error above
	 */
	async parse(message) {
		// FIXME this is a workaround to immediately fix https://one-jira.pearson.com/browse/BRNT-2616
		const rootElement = message.rootElement || { activityTheme: null };
		const interactive = message.interactive;
		const assets = message.resolvedAssets || [];
		const annotations = message.annotations || [];

		// generate the snippet
		const snippet = generateAmbrosiaSnippet(interactive, assets, message, annotations, rootElement);

		console.log('Interactive snippet >>> ', snippet);

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

function generateAmbrosiaSnippet(interactive, assets, message, annotations, rootElement) {
	// generate an empty snippet for now
	let config = interactive.config;

	for (let i = 0; i < assets.length; i++) {
		const asset = assets[i];

		let assetWrapper = {
			"$ambrosia": "aero:asset:upload",
			"urn": asset.urn,
			"asset": asset
		};
		config = config.replace("\"" + asset.urn + "\"", JSON.stringify(assetWrapper));
	}

	const selectedThemePayload = rootElement.themePayload;

	const scenarios = (message.scenarios ? message.scenarios : []);

	const activityThemeIconLibraries = (rootElement.activityThemeIconLibraries ? rootElement.activityThemeIconLibraries : []);

	return {
		$ambrosia: `aero:interactive:${interactive.plugin.pluginId}:${interactive.plugin.version}`,
		$id: interactive.interactiveId,
		$workspaceId: message.workspaceId,
		$projectId: message.projectId,
		config: config ? JSON.parse(config) : null,
		activityTheme: (rootElement.activityTheme ? JSON.parse(rootElement.activityTheme) : null),
		annotations: annotations.map((annotation) => ({
			$ambrosia: `aero:annotation:${annotation.motivation.toLowerCase()}`,
			$id: annotation.id,
			subTarget: annotation.targetJson[0].subTarget,
			target: annotation.targetJson[0],
			body: annotation.bodyJson,
		})),
		selectedThemePayload: selectedThemePayload,
		scenarios: scenarios.map((scenario) => ({
			$ambrosia: `aero:scenario:system:${scenario.scenarioId}`,
			$id: scenario.scenarioId,
			$condition: scenario.condition,
			$actions: scenario.actions,
			$name: scenario.name,
			$description: scenario.description,
			lifecycle: scenario.lifecycle,
			correctness: scenario.correctness
		})),
		pluginPayload: message.pluginPayload,
		activityThemeIconLibraries: activityThemeIconLibraries
	};
}

module.exports = InteractiveParser;
