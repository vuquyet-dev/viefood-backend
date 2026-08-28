package com.viefood.base.event;

public final class EventTypes {

    public static final String IDENTITY_USER_REGISTERED = "identity.user.registered.v1";
    public static final String IDENTITY_USER_PROFILE_CHANGED = "identity.user.profile_changed.v1";

    public static final String CATALOG_DISH_PUBLISHED = "catalog.dish.published.v1";

    public static final String CONTRIBUTION_SUBMITTED = "contribution.submitted.v1";
    public static final String CONTRIBUTION_APPROVED = "contribution.approved.v1";
    public static final String CONTRIBUTION_REJECTED = "contribution.rejected.v1";

    public static final String COMMENT_REPORTED = "comment.reported.v1";

    public static final String MEDIA_ATTACHED = "media.attached.v1";
    public static final String MEDIA_RELEASED = "media.released.v1";

    private EventTypes() {
    }
}
