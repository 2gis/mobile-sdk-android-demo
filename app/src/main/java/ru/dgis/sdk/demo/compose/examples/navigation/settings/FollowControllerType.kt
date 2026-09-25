package ru.dgis.sdk.demo.compose.examples.navigation.settings

/**
 * Defines which follow controller implementation is used by the navigation UI.
 *
 * This enum is stored in settings (see [ComposeNavigationSettingsState.followControllerTypeOrdinal])
 * and later mapped to a concrete follow controller when starting navigation.
 */
internal enum class FollowControllerType {
    Default,
    Custom
}
