package mercury.functions;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.util.CollectionUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;

/**
 * Escapes Json fragment with Java String rules.
 * <p>Example:
 * <pre>
 * input string: He didn't say, "Stop!"
 * output string: He didn't say, \"Stop!\"
 * </pre>
 * </p>
 */
public class EscapeJsonFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (CollectionUtils.isEmpty(parameterList) || parameterList.size() != 1) {
            throw new InvalidFunctionUsageException("Invalid function parameter usage! Expected single parameter but found: " + parameterList.size());
        }

        return StringEscapeUtils.escapeJava(parameterList.get(0));
    }
}
