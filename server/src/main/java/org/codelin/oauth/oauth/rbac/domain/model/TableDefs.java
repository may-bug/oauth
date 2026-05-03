package org.codelin.oauth.oauth.rbac.domain.model;

import com.mybatisflex.core.query.QueryColumn;

/**
 * RBAC模块表列定义
 */
public class TableDefs {

    public static final OrganizationTable ORGANIZATION = new OrganizationTable();
    public static final RoleTable ROLE = new RoleTable();
    public static final PermissionTable PERMISSION = new PermissionTable();
    public static final UserOrganizationTable USER_ORGANIZATION = new UserOrganizationTable();

    public static class OrganizationTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn NAME = new QueryColumn("name");
        public final QueryColumn CODE = new QueryColumn("code");
        public final QueryColumn PARENT_ID = new QueryColumn("parent_id");
        public final QueryColumn STATUS = new QueryColumn("status");
        public final QueryColumn SORT_ORDER = new QueryColumn("sort_order");
        public final QueryColumn CREATED_AT = new QueryColumn("created_at");
        public final QueryColumn UPDATED_AT = new QueryColumn("updated_at");
    }

    public static class RoleTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn CODE = new QueryColumn("code");
        public final QueryColumn NAME = new QueryColumn("name");
        public final QueryColumn DESCRIPTION = new QueryColumn("description");
        public final QueryColumn ORG_ID = new QueryColumn("org_id");
        public final QueryColumn IS_SYSTEM = new QueryColumn("is_system");
        public final QueryColumn SORT_ORDER = new QueryColumn("sort_order");
        public final QueryColumn STATUS = new QueryColumn("status");
        public final QueryColumn CREATED_AT = new QueryColumn("created_at");
    }

    public static class PermissionTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn CODE = new QueryColumn("code");
        public final QueryColumn NAME = new QueryColumn("name");
        public final QueryColumn DESCRIPTION = new QueryColumn("description");
        public final QueryColumn TYPE = new QueryColumn("type");
        public final QueryColumn PARENT_ID = new QueryColumn("parent_id");
        public final QueryColumn PATH = new QueryColumn("path");
        public final QueryColumn ICON = new QueryColumn("icon");
        public final QueryColumn API_PATTERN = new QueryColumn("api_pattern");
        public final QueryColumn SORT_ORDER = new QueryColumn("sort_order");
        public final QueryColumn CREATED_AT = new QueryColumn("created_at");
    }

    public static class UserOrganizationTable {
        public final QueryColumn ID = new QueryColumn("id");
        public final QueryColumn USER_ID = new QueryColumn("user_id");
        public final QueryColumn ORG_ID = new QueryColumn("org_id");
        public final QueryColumn ROLE_ID = new QueryColumn("role_id");
        public final QueryColumn STATUS = new QueryColumn("status");
        public final QueryColumn JOINED_AT = new QueryColumn("joined_at");
    }
}
