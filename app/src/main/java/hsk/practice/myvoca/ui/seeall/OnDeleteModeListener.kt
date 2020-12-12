package hsk.practice.myvoca.ui.seeall

/**
 * Custom listener which defines a behavior when the delete mode is enabled/disabled.
 * A user can select items to remove from the database when the delete mode is enabled.
 * To enter the delete mode, long-click the item in the SeeAllFragment and select the '삭제' option.
 */
interface OnDeleteModeListener {
    fun enableDeleteMode()
    fun disableDeleteMode()
}