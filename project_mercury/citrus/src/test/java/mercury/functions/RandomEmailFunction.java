package mercury.functions;

import java.util.List;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.Function;

import mercury.common.Utils;

public class RandomEmailFunction implements Function {

    public static final String RANDOM_EMAIL = "randomEmail";

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        return Utils.randomEmail();
    }
}
