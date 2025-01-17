package com.oo.srv

@Retention(AnnotationRetention.SOURCE)
annotation class Final
@Retention(AnnotationRetention.SOURCE)
annotation class Read
@Retention(AnnotationRetention.SOURCE)
annotation class Write

const val APP_DOMAIN    = "https://app.habf.com"
const val ADMIN_DOMAIN  = "https://admin.habf.com"
const val FILE_DOMAIN   = "https://res.habf.com"

const val WRITE = false
const val READ = true
const val AUTH_KEY = "AUTH"
const val ADMIN_AUTH_KEY = "X-Token"
const val DB_FILE_SEP = ";"

const val CUSTOMER_SMS_LOGIN_URI                    = "/oo-srv/api/auth/sms-login"
const val WAITRESS_SMS_LOGIN_URI                    = "/oo-srv/api/auth/sms-login"
const val LOGOUT_URI                                = "/oo-srv/api/auth/logout"
const val FIRST_PAGE_CONFIG_URI                     = "/oo-srv/api/first-1/config/list"
const val FIRST_PAGE_ACTRESS3_URI                   = "/oo-srv/api/first-2/actress3/list"
const val FIRST_PAGE_NEAR10_URI                     = "/oo-srv/api/first-3/actress10/near/list"
const val FIRST_PAGE_HOT10_URI                      = "/oo-srv/api/first-4/actress10/hot/list"
const val FIRST_PAGE_NEW10_URI                      = "/oo-srv/api/first-5/actress10/new/list"

const val ACTRESS_PAGE_LIST_BY_JOB_URI              = "/oo-srv/api/actress-1/actress/job/list"
const val ACTRESS_PAGE_LIST_STORE_URI               = "/oo-srv/api/actress-1/actress/store/list"

const val DYNAMIC_STATES_PAGE_LIST_URI              = "/oo-srv/api/dy-states-1/states/list"
const val DYNAMIC_STATES_PAGE_VISIT_URI             = "/oo-srv/api/dy-states-2/visit"
const val DYNAMIC_STATES_PAGE_FOLLOW_LIST_URI       = "/oo-srv/api/dy-states-3/follow/list"

const val STORE_PAGE_SEP3_LIST_URI                  = "/oo-srv/api/store-1/spe/list"
const val STORE_PAGE_HOT_STORE_LIST_URI             = "/oo-srv/api/store-2/host/list"
const val STORE_PAGE_HOT_ACTRESS_LIST_URI           = "/oo-srv/api/store-3/actress/list"

const val FILE_EXISTS                               = "/oo-srv/api/shared/file/exists"
const val FILE_UPLOAD_ONLY                          = "/oo-srv/api/shared/file/upload-only"
const val FILE_UPLOAD_PARAMS                        = "/oo-srv/api/shared/file/upload-params"
const val FILE_DOWNLOAD                             = "/oo-srv/api/shared/file/download"



const val ADMIN_USER_INFO_URI           = "/dev-api/vue-element-admin/user/info"
const val ADMIN_TRANSACTION_LIST_URI    = "/dev-api/vue-element-admin/transaction/list"
const val ADMIN_SEARCH_USER_URI         = "/dev-api/vue-element-admin/search/user"
const val ADMIN_AUTH_CAPTCHA_URI        = "/dev-api/vue-element-admin/auth/captcha"
const val ADMIN_USER_LOGIN_URI          = "/dev-api/vue-element-admin/user/login"
const val ADMIN_USER_LOGOUT_URI         = "/dev-api/vue-element-admin/user/logout"
const val ADMIN_ROLES_URI               = "/dev-api/vue-element-admin/roles"
const val ADMIN_ROUTERS_URI             = "/dev-api/vue-element-admin/routes"
const val ADMIN_ARTICLE_LIST            = "/dev-api/vue-element-admin/article/list"

const val ADMIN_ROLE_PERM_UPDATE        = "/dev-api/vue-element-admin/role/update"
const val ADMIN_ROLE_PERM_DELETE        = "/dev-api/vue-element-admin/role/delete"

const val TRACE_TOKEN = "trace_uuid"//see logback.xml
const val CLIENT_API_PATTERN    = "/oo-srv/api/**"
const val ADMIN_API_PATTERN     = "/dev-api/vue-element-admin/**"

const val REQ_USER_KEY = "REQ_USER_KEY"
const val AUTH_CAPTCHA_KEY = "captcha"
