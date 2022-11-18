package com.smartsparrow.rtm.message.authorization;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.user_content.FavoriteMessage;
import com.smartsparrow.rtm.message.recv.user_content.RecentlyViewedMessage;

import graphql.Assert;

class AllowFavoriteViewedProjectOwnerTest {

    @InjectMocks
    private AllowFavoriteViewedProjectOwner allowFavoriteViewedProjectOwner;

    @Mock
    private CoursewareElementAuthorizerService coursewareElementAuthorizerService;

    private AuthenticationContext authenticationContext;
    private FavoriteMessage favoriteMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        favoriteMessage = mock(FavoriteMessage.class);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void test_contributorPermission() {
        when(coursewareElementAuthorizerService.authorize(authenticationContext, favoriteMessage.getRootElementId(), CoursewareElementType.ACTIVITY, PermissionLevel.OWNER))
                .thenReturn(Boolean.TRUE);

        Assert.assertTrue(allowFavoriteViewedProjectOwner.test(authenticationContext, favoriteMessage));
    }

    @Test
    void test_reviewerPermission() {
        when(coursewareElementAuthorizerService.authorize(authenticationContext, favoriteMessage.getRootElementId(), CoursewareElementType.ACTIVITY, PermissionLevel.REVIEWER))
                .thenReturn(Boolean.FALSE);

        Assert.assertFalse(allowFavoriteViewedProjectOwner.test(authenticationContext, favoriteMessage));
    }

}