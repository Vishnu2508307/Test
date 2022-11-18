package com.smartsparrow.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.smartsparrow.exception.InvalidJWTException;

class JWTTest {

    private static final String validToken = "eyJraWQiOiJrMzI4LjE1NjM5MTM0ODEiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJmZmZmZmZmZjVmMjBlMDlkMWQ0Y" +
            "jc0MDFkZmNjYmZiMiIsImhjYyI6IkFVIiwidHlwZSI6ImF0IiwiZXhwIjoxNTk3MTI0NjQxLCJpYXQiOjE1OTcxMjI4NDEsImNsaWVud" +
            "F9pZCI6Im9YNVZtNlNFRWVTaTVRQkVBVTAwODF0eDIwVUhFNDY5Iiwic2Vzc2lkIjoiNzY1ZmVjZDAtMWU5ZC00ZDkxLWEzNDktMDI2N" +
            "zRkNzRkNGI4In0.Bd89NMcbydGhzv_QkP-rCXUqNNrPhl9qwXQ0czz_cKgsI66Bqi9aAcaTGeSz2awbFlOzDfrYm9fkwrlbq0yaeowjo" +
            "SVw6BAhXct_vqv83_agcY3w5fhJmpl-gUL4wj3uZIg8uKHXBF8fhjaNLdIO9HmahwAocSpH71EtLOD62nnGv3EmsF9Hzw0abpPGMSF9g" +
            "DUqRS3rjXzrAkjRzX9CX1A_odYPkP65UYSgHNfVKP7jjJHS1x-v2um6GpX435RO-F38LPRy336mEeoGoZv6X9q6i5JA5H2dauhLna728" +
            "q3Fmg2kKsLlvGi148JM4wNb6-DzxBnq_LyES0e7Iwkg1w";

    private static final String invalidToken = "invalid token";

    @Test
    void getExpTime_invalidToken() {
        InvalidJWTException e = assertThrows(InvalidJWTException.class, () -> JWT.getExpDateTime(invalidToken));

        assertNotNull(e);
        assertEquals("invalid jwt", e.getMessage());
    }

    @Test
    void getExpTime() {
        assertDoesNotThrow(() -> JWT.getExpDateTime(validToken));
    }

    @Test
    void getUserId_invalid() {
        InvalidJWTException e = assertThrows(InvalidJWTException.class, () -> JWT.getUserId(invalidToken));

        assertNotNull(e);
        assertEquals("invalid jwt", e.getMessage());
    }

    @Test
    void getUserId() {
        assertDoesNotThrow(() -> JWT.getUserId(validToken));
    }

    @Test
    void getSecondsExp() {
        String token = "eyJraWQiOiJrMzI4LjE1NjM5MTM0ODEiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJmZmZmZmZmZjVmMGU0YTA3ZDEyMDMyMD" +
                "FkZjc2MDhiZCIsImhjYyI6IkFVIiwidHlwZSI6ImF0IiwiZXhwIjoxNTk3Nzk4MjUwLCJpYXQiOjE1OTc3OTY0NTAsImNsaWVudF9pZ" +
                "CI6Im9YNVZtNlNFRWVTaTVRQkVBVTAwODF0eDIwVUhFNDY5Iiwic2Vzc2lkIjoiNmNmZTU2MjQtMjFiOS00NmIxLWI1YWMtN2RjNDZh" +
                "ZWVkOGMzIn0.MHRhei6pDqZoJ3pHEUzImWw1o7v-orzMCwrHS8dn5ZzaNgK37Wx_1OcJkSBd2-uWGt7D8BR0VsxJJRq_juXg1Uv05i-" +
                "ZUMsMGAmm-qgoTJ_ITrkT9D0OAvAxQpGpQEIaOfOkUelX4vTNhzVuTTpN7SqNEOMEvLXM-F9rMGLOlfIYB4zchpVaZvmCo1sTdd1dUe" +
                "icklavRmQN3UqflH_egq5ZYJ0De1Yo624ebKk-OXsfyxRXKVC-SzGB3YZuhn2WuqV0VXdRXoTECSPNeDMW5W2kfaqjXLmj8Taz-M3qH" +
                "geLLFsZY0IyMJm2_nDnzJqj9CV13CyH8OpxdEonTLrQDg";
        assertDoesNotThrow(() -> JWT.getSecondsExp(token));
    }

}