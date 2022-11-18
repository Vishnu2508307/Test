package com.smartsparrow.graphql.type;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.Optional;

import com.smartsparrow.util.RandomStrings;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;

/*
 * This is a contrived example in order to test error generation.
 */
public class Generator {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String DEFAULT_STRING_POOL = "abcdefghijklmnpqrstuvwxyz123456789";
    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 1024;

    @GraphQLQuery(name = "randomString")
    public String randomString(@GraphQLArgument(name = "length") @GraphQLNonNull Integer paramLength,
            @GraphQLArgument(name = "pool") Optional<String> paramPool) {
        //
        String pool = DEFAULT_STRING_POOL;
        if (paramPool != null && paramPool.isPresent()) {
            pool = paramPool.get();
        }

        affirmArgumentNotNullOrEmpty(pool, "pool can not be empty if supplied");
        affirmArgument(paramLength >= MIN_LENGTH, "length must be at least " + MIN_LENGTH);
        affirmArgument(paramLength <= MAX_LENGTH, "length must be less than " + MAX_LENGTH);

        return RandomStrings.random(paramLength, pool);
    }
}
