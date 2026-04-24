package app.pedallog.android.ui.navigation

object NavRoutes {

    const val SPLASH = "splash"

    const val HOME = "home"
    const val DASHBOARD = "dashboard"
    const val TEMPLATE = "template"
    const val SETTINGS = "settings"

    const val HISTORY_DETAIL = "history_detail/{sessionId}"
    fun historyDetail(sessionId: Long) = "history_detail/$sessionId"

    const val RECEIVE = "receive"
    const val CONFIRM = "confirm/{sessionId}"
    fun confirm(sessionId: Long) = "confirm/$sessionId"

    const val TEMPLATE_EDIT = "template_edit/{id}"
    fun templateEdit(id: Long? = null) =
        "template_edit/${id ?: -1L}"

    const val DASHBOARD_ANNUAL = "dashboard/annual"
    const val DASHBOARD_MONTHLY = "dashboard/monthly"
    const val DASHBOARD_COURSE = "dashboard/course"
    const val DASHBOARD_CALORIE = "dashboard/calorie"
    const val DASHBOARD_TIME = "dashboard/time"
    const val DASHBOARD_INTENSITY = "dashboard/intensity"

    /** 대시보드 내부 NavHost용 상대 경로 */
    object DashboardNested {
        const val ANNUAL = "annual"
        const val MONTHLY = "monthly"
        const val COURSE = "course"
        const val CALORIE = "calorie"
        const val TIME = "time"
        const val INTENSITY = "intensity"
    }
}
