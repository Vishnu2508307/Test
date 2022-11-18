class ComponentParser {

    /**
     * Parses a component into an ambrosia snippet, then save that snippet to Bronte via rtm.
     * Then returns the original message with a status of completed.
     *
     * @param {ExportRequestNotification} message the notification message coming from Sns, sent by the Bronte backend
     * @throws {Error} when anything fails. It is important to catch this error above
     */
    async parse(message) {
        const component = message.component;
        const assets = message.resolvedAssets || [];
        const annotations = message.annotations || [];
        const pluginPayload = message.pluginPayload;

        // generate the snippet
        const snippet = generateAmbrosiaSnippet(component, assets, annotations, pluginPayload);

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

function generateAmbrosiaSnippet(component, assets, annotations, pluginPayload) {
    // generate an empty snippet for now
    let config = component.config;

    for (let i = 0; i < assets.length; i++) {
        const asset = assets[i];

        let assetWrapper = {
            "$ambrosia": "aero:asset:upload",
            "urn": asset.urn,
            "asset": asset
        };
        config = config.replace("\"" + asset.urn + "\"", () => JSON.stringify(assetWrapper));
    }

    return {
        $ambrosia: `aero:component:${component.plugin.pluginId}:${component.plugin.version}`,
        $id: component.componentId,
        config: (config ? JSON.parse(config) : null),
        annotations: annotations.map((annotation) => ({
			$ambrosia: `aero:annotation:${annotation.motivation.toLowerCase()}`,
			$id: annotation.id,
			subTarget: annotation.targetJson[0].subTarget,
			target: annotation.targetJson[0],
			body: annotation.bodyJson,
		})),
        pluginPayload: pluginPayload
    };
}

module.exports = ComponentParser;
