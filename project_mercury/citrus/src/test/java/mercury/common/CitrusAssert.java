package mercury.common;

import java.util.Map;

import org.junit.Assert;
import org.springframework.http.HttpStatus;

import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.http.message.HttpMessageHeaders;

public class CitrusAssert {


    /**
     * Fails a Citrus test case
     *
     * @param reason for failing the test case
     */
    public static void fail(String reason) {
        throw new TestCaseFailedException(new ValidationException(reason));
    }

    /**
     * Fails a Citrus test case if the expected string is not contained by the actual string
     *
     * @param actual the actual value
     * @param expected the value expected to be contained by the actual string
     */
    public static void assertContains(String expected, String actual) {
        if (!actual.contains(expected)) {
            fail(String.format("Assertion error: actual value '%s' does not contain expected value '%s'", actual, expected));
        }
    }

    /**
     * Fails a Citrus test case if HTTP Headers contain the expected status code.
     * @param expectedStatusCode the expected Http Status Code {@link HttpStatus}
     * @param actualHeaders headers of the actual message
     */
    public static void assertHttpStatusCode(int expectedStatusCode, Map<String, Object> actualHeaders) {
        Assert.assertNotNull(actualHeaders);
        Assert.assertEquals(expectedStatusCode, actualHeaders.get(HttpMessageHeaders.HTTP_STATUS_CODE));
    }
}
