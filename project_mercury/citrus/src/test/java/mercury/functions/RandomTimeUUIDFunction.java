package mercury.functions;

import java.util.List;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.Function;
import com.datastax.driver.core.utils.UUIDs;

/**
 * Create a random timeuuid.
 */
public class RandomTimeUUIDFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        return UUIDs.timeBased().toString();
    }
}
