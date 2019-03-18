package de.schwedenmut.mobileinformationstudy.misc

import de.schwedenmut.mobileinformationstudy.BuildConfig

class Const{
    companion object {
        @JvmField val DEBUG : Boolean = BuildConfig.DEBUG;

        // Permissions
        @JvmField val NOTIFICATION_STATUS = "notification_status"
        @JvmField val SCREENCAPTURE_STATUS = "screencapture_status"

        // Statistics
        @JvmField val NOTIFICATION_ENTRIES = "notification_entries"
        @JvmField val SCREENCAPTURE_ENTRIES = "screencapture_entries"
        @JvmField val PREF_VERSION = "pref_version"

        // Preferences
        @JvmField val MEDIA_PROJECTION_ALLOWED = "media_projection_allowed"
        @JvmField val REQUEST_MEDIA_PROJECTION = 100

        @JvmField val BROADCAST: String = "de.schwedenmut.mobileinformationstudy.update"

        @JvmField val EXTRA: String = "extra"

        @JvmField val WAS_WIZARD_SHOWN = "was_wizard_shown"
    }
}
