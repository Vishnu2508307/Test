class MessageUtils {
    /**
     * Read an attribute from an sns message
     *
     * @param sns the sns message
     * @param key the attribute key to find
     * @returns {Object} the sns attribute value
     * @throws Error when the key was not found in the attributes object
     */
    getMessageAttribute(sns, key) {
        const value = sns.MessageAttributes[key] ? sns.MessageAttributes[key].Value : null;
        if (!value) {
            throw new Error(`${key} not found`);
        }
        return value;
    }
}

module.exports = new MessageUtils();