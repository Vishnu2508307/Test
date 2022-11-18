const ActivityParser = require('../parsers/activity.parser');
const InteractiveParser = require('../parsers/interactive.parser');
const PathwayParser = require('../parsers/pathway.parser');
const ComponentParser = require('../parsers/component.parser');

class NotificationHandler {

    /**
     * Handles the incoming sns notification. Validates that the notification is valid then invokes
     * the appropriate ambrosia parser
     */
    async handle(message) {

        // validate that the message has all the required properties
        validateFields(message);

        // invoke the appropriate parser based on the element type
        switch (message.elementType) {
            case 'ACTIVITY':
                return new ActivityParser().parse(message);
            case 'PATHWAY':
                return new PathwayParser().parse(message);
            case 'INTERACTIVE':
                return new InteractiveParser().parse(message);
            case 'COMPONENT':
                return new ComponentParser().parse(message);
            default:
                throw new Error(`[${message.elementType}] parser not supported`);
        }
    }
}

/**
 * Validate the field of the incoming sns message. Invokes the promise reject function when a
 * required field is missing.
 *
 * @param { Sns.Message } message the message to validate the fields for
 */
function validateFields(message) {
    if (!message.notificationId) {
        throw new Error('notificationId is required');
    }

    if (!message.elementId) {
        throw new Error('elementId is required');
    }

    if (!message.elementType) {
        throw new Error('elementType is required');
    }

    if (!message.accountId) {
        throw new Error('accountId is required');
    }

    if (!message.projectId) {
        throw new Error('projectId is required');
    }

    if (!message.workspaceId) {
        throw new Error('workspaceId is required');
    }

    if (!message.exportId) {
        throw new Error('exportId is required');
    }
}

module.exports = new NotificationHandler();
