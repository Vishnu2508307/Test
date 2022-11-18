class ActivityParser {

    /**
     * Parses an activity into an ambrosia snippet, then save that snippet to Bronte via rtm.
     * Then returns the original message with a status of completed.
     *
     * @param {ExportRequestNotification} message the notification message coming from Sns, sent by the Bronte backend
     * @throws {Error} when anything fails. It is important to catch this error above
     */
    async parse(message) {
        // rootElement may not be present for sub activities for root activity export
        const rootElement = message.rootElement || {};
        console.log("rootElement: ", rootElement);
        
        const activity = message.activity;
        const assets = message.resolvedAssets || [];
        const annotations = message.annotations || [];

        // generate the snippet
        const snippet = generateAmbrosiaSnippet(activity, assets, message, rootElement, annotations);

        console.log('Activity snippet >>> ', snippet);

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
        }
    }
}

function generateAmbrosiaSnippet(activity, assets, message, rootElement, annotations) {
    // generate an empty snippet for now
    let config = activity.config;

    for (let i = 0; i < assets.length; i++) {
        const asset = assets[i];

        let assetWrapper = {
            "$ambrosia": "aero:asset:upload",
            "urn": asset.urn,
            "asset": asset
        };
        config = config.replace("\"" + asset.urn + "\"", JSON.stringify(assetWrapper));
    }

    const activityTheme = (activity.activityTheme ? activity.activityTheme : rootElement.activityTheme);

    const selectedThemePayload = rootElement.themePayload || activity.themePayload;

    const scenarios = (message.scenarios ? message.scenarios : []);

    const activityThemeIconLibraries = (rootElement.activityThemeIconLibraries ? rootElement.activityThemeIconLibraries : (activity.activityThemeIconLibraries || []));

    return {
        $ambrosia: `aero:activity:${activity.plugin.pluginId}:${activity.plugin.version}`,
        $id: activity.activityId,
        $workspaceId: message.workspaceId,
        $projectId: message.projectId,
        config: (config ? JSON.parse(config) : null),
        // only parse the activityTheme when not null
        activityTheme: (activityTheme ? JSON.parse(activityTheme) : null),
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

module.exports = ActivityParser;
