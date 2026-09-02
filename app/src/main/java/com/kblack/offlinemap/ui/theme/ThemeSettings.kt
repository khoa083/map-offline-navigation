package com.kblack.offlinemap.ui.theme

/**
 * User-facing theme mode. Replaces the previous hardcoded val darkTheme: Boolean = true in
 * OfflinemapTheme (tracked in the spec as "carried over from the current build" — the app
 * ignored both the system setting and the caller-supplied parameter and was always dark).
 *
 * There is no System "auto" option paired with a conventional dim dark theme here: the two
 * real palettes for this app are Light (outdoor/daytime) and Amoled (true-black night). A later
 * "follow system" mode can be added by mapping the platform's dark-mode signal to Amoled, but
 * that mapping is a product decision, not a default — until made explicitly, OfflinemapTheme
 * takes the caller-supplied darkTheme boolean at face value again instead of overriding it.
 */
enum class AppThemeMode {
    Light,
    Amoled,
}
