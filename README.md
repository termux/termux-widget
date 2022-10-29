# Termux:Widget

[![Build status](https://github.com/termux/termux-widget/workflows/Build/badge.svg)](https://github.com/termux/termux-widget/actions)
[![Join the chat at https://gitter.im/termux/termux](https://badges.gitter.im/termux/termux.svg)](https://gitter.im/termux/termux)

A [Termux] plugin app to run scripts in Termux with launcher shortcuts and widgets.



### Contents
- [Installation](#Installation)
- [Setup Instructions](#Setup-Instructions)
- [Creating And Modifying Scripts](#Creating-And-Modifying-Scripts)
- [Debugging](#Debugging)
- [Worthy Of Note](#Worthy-Of-Note)
- [For Maintainers and Contributors](#For-Maintainers-and-Contributors)
- [Forking](#Forking)
##



### Installation

Latest version is `v0.13.0`.

Check [`termux-app` Installation](https://github.com/termux/termux-app#Installation) for details before reading forward.

### F-Droid

`Termux:Widget` application can be obtained from `F-Droid` from [here](https://f-droid.org/en/packages/com.termux.widget).

You **do not** need to download the `F-Droid` app (via the `Download F-Droid` link) to install `Termux:Widget`. You can download the `Termux:Widget` APK directly from the site by clicking the `Download APK` link at the bottom of each version section.

It usually takes a few days (or even a week or more) for updates to be available on `F-Droid` once an update has been released on `Github`. The `F-Droid` releases are built and published by `F-Droid` once they [detect](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.termux.widget.yml) a new `Github` release. The Termux maintainers **do not** have any control over the building and publishing of the Termux apps on `F-Droid`. Moreover, the Termux maintainers also do not have access to the APK signing keys of `F-Droid` releases, so we cannot release an APK ourselves on `Github` that would be compatible with `F-Droid` releases.

The `F-Droid` app often may not notify you of updates and you will manually have to do a pull down swipe action in the `Updates` tab of the app for it to check updates. Make sure battery optimizations are disabled for the app, check https://dontkillmyapp.com/ for details on how to do that.

### Github

`Termux:Widget` application can be obtained on `Github` either from [`Github Releases`](https://github.com/termux/termux-widget/releases) for version `>= 0.13.0` or from [`Github Build`](https://github.com/termux/termux-widget/actions/workflows/debug_build.yml) action workflows.

The APKs for `Github Releases` will be listed under `Assets` drop-down of a release. These are automatically attached when a new version is released.

The APKs for `Github Build` action workflows will be listed under `Artifacts` section of a workflow run. These are created for each commit/push done to the repository and can be used by users who don't want to wait for releases and want to try out the latest features immediately or want to test their pull requests. Note that for action workflows, you need to be [**logged into a `Github` account**](https://github.com/login) for the `Artifacts` links to be enabled/clickable. If you are using the [`Github` app](https://github.com/mobile), then make sure to open workflow link in a browser like Chrome or Firefox that has your Github account logged in since the in-app browser may not be logged in.

The APKs for both of these are [`debuggable`](https://developer.android.com/studio/debug) and are compatible with each other but they are not compatible with other sources.

### Google Play Store **(Deprecated)**

**Termux and its plugins are no longer updated on [Google Play Store](https://play.google.com/store/apps/details?id=com.termux.widget) due to [android 10 issues](https://github.com/termux/termux-packages/wiki/Termux-and-Android-10) and have been deprecated. It is highly recommended to not install Termux apps from Play Store any more.** Check https://github.com/termux/termux-app#google-play-store-deprecated for details.
##



### Setup Instructions

#### Install `Termux` app (Mandatory)
The `Termux:Widget` plugin requires [Termux] app to run the actual commands. You need to install it and start it at least once and have it install the bootstrap files for the plugin to start working. The Termux prefix directory `/data/data/com.termux/files/usr/` and Termux home directory `/data/data/com.termux/files/home/` must also exist and must have read, write and execute permissions `(0700)` for the plugin to work. The `$PREFIX/` is shortcut for the Termux [prefix directory](https://github.com/termux/termux-packages/wiki/Termux-file-system-layout) and can also be referred by the `$PREFIX` shell environment variable. The `~/` is a shortcut for the Termux home directory and can also be referred by the `$HOME` shell environment variable. Note that `~/` will not expand inside single or double quotes when running commands. Permissions and ownerships can be checked with the `stat <path>` command.


#### Script Directories (Mandatory)

The `~/.shortcuts/` directory stores the scripts that can be run with the plugin in foreground terminal sessions in the `Termux` app. The `~/.shortcuts/tasks` directory stores the scripts that can be run with the plugin in background with the `Termux` app and will show as running tasks in `Termux` app notification.

The parent directory of the scripts must have read permission, otherwise the plugin will not be able to read the script files and will not show any scripts in the launcher widget and will give errors like `No regular file found at path` when executing launcher shortcuts. The parent directory of the script must also have executable permissions for the script to be allowed to execute.

Files under hidden directories whose name starts with a dot `.`, broken symlinks or files whose canonical path is not under the `~/.shortcuts` or `~/.termux` directory are not shown in the widget and execution is not allowed for the later either.

Open a non-root termux session and run the below commands to create the directories and give them read, write and executable permissions `(0700)`.

- Create `~/.shortcuts/` directory.

```
mkdir -p /data/data/com.termux/files/home/.shortcuts
chmod 700 -R /data/data/com.termux/files/home/.shortcuts
```

- Create `~/.shortcuts/tasks` directory.

```
mkdir -p /data/data/com.termux/files/home/.shortcuts/tasks
chmod 700 -R /data/data/com.termux/files/home/.shortcuts/tasks
```

Once you have created the directories, you can then create scripts files as per instructions in [Creating And Modifying Scripts](#Creating-And-Modifying-Scripts).

Once you have created script files, you can add a launcher widget for the `Termux:Widget` app that will show the list of the script files, which you can execute by clicking them. If you create/modify shortcuts files, you will have to press the refresh button on the widget for the updated list to be shown. You can also update all widgets from inside the app with the `REFRESH` button in the refresh widgets section. You can also refresh a specific widget by running `am broadcast -n com.termux.widget/.TermuxWidgetProvider -a com.termux.widget.ACTION_REFRESH_WIDGET --ei appWidgetId <id>` from Termux terminal/scripts for version `>= 0.13.0`, where `id` is the number in the `Termux widgets reloaded: <id>)` flash shown when you press the refresh button. You can pass `0` to update all widgets for version `>= 0.114.0`. Refreshing widgets with the in-app `REFRESH` button or running command with id `0` may also be needed in some cases after app updates where widgets become non-responsive and do not show any shortcuts and refresh buttons of the widgets itself do not work either.

You can also add a launcher shortcut or dynamic shortcut for any script file with an optional custom icon as detailed in [Script Icon Directory](#script-icon-directory-optional).

<img src="termux-widget.png" alt="" width="50%"/>


#### Script Icon Directory (Optional)

The `~/.shortcuts/icons` directory stores the icon that will be used for a script when a launcher shortcut is created for it for version `>= 0.12`. The icon file name must be equal to `<script_name>.png`, like `script.sh.png`. For a `1080p` `~6in` screen, something like `96x96px` `png` file should probably be fine, otherwise try `144px` or `196px` for higher resolution screens.

The parent directory of the icons must have read permission, otherwise the plugin will not be able to read them.

The icon file must be a regular file and its canonical path must exist under `~/.shortcuts/icons` or `~/.termux` directory.

Open a non-root termux session and run the below commands to create the directory and give it appropriate permissions.

- Create `~/.shortcuts/icons` directory.

```
mkdir -p /data/data/com.termux/files/home/.shortcuts/icons
chmod -R a-x,u=rwX,go-rwx /data/data/com.termux/files/home/.shortcuts/icons
```
The `chmod` command will set the `icons` directory permissions to `0700`, but any files already in the directory will be set to `0600` which is recommended.


#### Dynamic Shortcuts (Optional)

Dynamic shortcuts will normally show when long holding the `Termux:Widget` app launcher icon and in launcher searches results.

To create dynamic shortcuts, put desired scripts/binaries in `~/.termux/widget/dynamic_shortcuts` with the Termux app and then click the `CREATE SHORTCUTS` button in the `Termux:Widget` app in the dynamic shortcuts section. To remove published dynamic shortcuts, click the `REMOVE SHORTCUTS` button but this won't remove dynamic shortcuts already converted to launcher shortcuts.

For some launchers it might be necessary to regenerate the app shortcuts to display them correctly. Lookup the settings of your launcher to find such actions.


#### Max Shortcuts Limit (Optional)

Android has a limit on how many static and dynamic shortcuts can be created per app/activity, which is controlled by the [`max_shortcuts`](https://cs.android.com/android/platform/superproject/+/android-13.0.0_r8:frameworks/base/services/core/java/com/android/server/pm/ShortcutService.java;l=254) sub key of the [`shortcut_manager_constants`](https://cs.android.com/android/platform/superproject/+/android-13.0.0_r8:frameworks/base/core/java/android/provider/Settings.java;l=13799) key in `global` settings namespace. The default value is [`5` on Android `>= 7.0`](https://cs.android.com/android/platform/superproject/+/android-7.0.0_r36:frameworks/base/services/core/java/com/android/server/pm/ShortcutService.java;l=141), [`10` on Android `>= 10`](https://cs.android.com/android/platform/superproject/+/android-10.0.0_r1:frameworks/base/services/core/java/com/android/server/pm/ShortcutService.java;l=165) and [`15` on Android `>= 11`](https://cs.android.com/android/platform/superproject/+/android-11.0.0_r9:frameworks/base/services/core/java/com/android/server/pm/ShortcutService.java;l=172).

To check `max_shortcuts` value currently being used by android `ShortcutService`, run:

- `adb`: `adb shell "dumpsys shortcut | grep -E 'maxShortcutsPerActivity|mMaxDynamicShortcuts'"`

- `root`: `su -c "dumpsys shortcut | grep -E 'maxShortcutsPerActivity|mMaxDynamicShortcuts'`

To change the limit, check below.

**Till Next Reboot**

You can change the limit till next reboot with the [`cmd shortcut override-config`](https://cs.android.com/android/platform/superproject/+/android-13.0.0_r8:frameworks/base/services/core/java/com/android/server/pm/ShortcutService.java;l=4943) command from an [`adb`] or [`root`] shell. For example to increase the limit to `25`, run:

- `adb`: `adb shell "cmd shortcut override-config max_shortcuts=25"`

- `root`: `su -c "cmd shortcut override-config max_shortcuts=25"`

To reset to default, run `cmd shortcut override-config max_shortcuts=`

**Permanently**

You can change the limit permanently with the [`settings put global`](https://cs.android.com/android/platform/superproject/+/android-12.0.0_r34:frameworks/base/packages/SettingsProvider/src/com/android/providers/settings/SettingsService.java;l=465) command from an [`adb`] or [`root`] shell.

The `max_shortcuts` sub key is stored in `settings` `global` namespace under a single `shortcut_manager_constants` key as a comma separated list of `key=value` pairs. You can check the current/default values set with:

- `adb`: `adb shell "settings get global shortcut_manager_constants"`

- `root`: `su -c "settings get global shortcut_manager_constants"`

Now, since this is single key storing all the other sub keys, you can't just run `settings put` command to set a sub key value if the key value is already set, since it will overwrite all the existing values.

You should first get the default/current, then update or append `,max_shortcuts=25` to it and then put the joint value back with `settings get global shortcut_manager_constants '<joint_value>'`.

If the `shortcut_manager_constants` value is not set (by default it should be unset), then to increase the limit to `25` run:

- `adb`: `adb shell "settings put global shortcut_manager_constants 'max_shortcuts=25'"`

- `root`: `su -c "settings put global shortcut_manager_constants 'max_shortcuts=25'"`

To reset to default if no other sub keys set, run `settings delete global shortcut_manager_constants`


#### Draw Over Apps permission (Optional)

For android `>= 10` there are new [restrictions](https://developer.android.com/guide/components/activities/background-starts) that prevent activities from starting from the background. This prevents the background `TermuxService` from starting a terminal session in the foreground and running the commands until the user manually clicks `Termux` notification in the status bar dropdown notifications list. This only affects plugin commands that are to be executed in a terminal session and not the background ones. `Termux` version `>= 0.100` requests the `Draw Over Apps` permission so that users can bypass this restriction so that commands can automatically start running without user intervention. You can grant `Termux` the `Draw Over Apps` permission from its `App Info` activity `Android Settings` -> `Apps` -> `Termux` -> `Advanced` -> `Draw over other apps`.
##



### Creating And Modifying Scripts

You can create scripts in `~/.shortcuts/` and `~/.shortcuts/tasks` directories after following their [Setup Instructions](#Setup-Instructions).

You can use `shell` based text editors like `nano`, `vim` or `emacs` to create and modify scripts.

`nano ~/.shortcuts/some_script`

You can also use `GUI` based text editor android apps that support `SAF`. Termux provides a [Storage Access Framework (SAF)](https://wiki.termux.com/wiki/Internal_and_external_storage) file provider to allow other apps to access its `~/` home directory. However, the `$PREFIX/` directory is not accessible to other apps. The [QuickEdit] or [QuickEdit Pro] app does support `SAF` and can handle large files without crashing, however, it is closed source and its pro version without ads is paid. You can also use [Acode editor] or [Turbo Editor] if you want an open source app.

Note that the android default `SAF` `Document` file picker may not support hidden file or directories like `~/.shortcuts` which start with a dot `.`, so if you try to use it to open files for a text editor app, then that directory will not show. You can instead create a symlink for  `~/.shortcuts` at `~/shortcuts_sym` so that it is shown. Use `ln -s "/data/data/com.termux/files/home/.shortcuts" "/data/data/com.termux/files/home/shortcuts_sym"` to create it.
##



### Debugging

You can help debug problems like how plugin shortcuts and scripts are being parsed by the plugin or if the plugin is even firing etc by setting appropriate `logcat` `Log Level` in `Termux` app settings -> `Termux:Widget` -> `Debugging` -> `Log Level` (Requires `Termux` app version `>= 0.118.0`). The `Log Level` defaults to `Normal` and log level `Verbose` currently logs additional information. Its best to revert log level to `Normal` after you have finished debugging since private data may otherwise be passed to `logcat` during normal operation and moreover, additional logging increases execution time.

The plugin **does not execute the commands itself** but sends an execution intent to `Termux` app, which has its own log level which can be set in `Termux` app settings -> `Termux` -> `Debugging` -> `Log Level`. So you must set log level for both `Termux` and `Termux:Widget` app settings to get all the info.

Once log levels have been set, you can run the `logcat` command in `Termux` app terminal to view the logs in realtime (`Ctrl+c` to stop) or use `logcat -d > logcat.txt` to take a dump of the log. You can also view the logs from a PC over `ADB`. For more information, check official android `logcat` guide [here](https://developer.android.com/studio/command-line/logcat).

##### Log Levels

- `Off` - Log nothing.
- `Normal` - Start logging error, warn and info messages and stacktraces.
- `Debug` - Start logging debug messages.
- `Verbose` - Start logging verbose messages.
##



### Worthy Of Note

##### Termux Environment

Termux does not load the environment fully for external plugins or [RUN_COMMAND Intent] commands, like setting `LD_PRELOAD`, so any *external* scripts which do not have shebangs to full path to termux bin directory will not work if called from inside your *plugin* scripts, since `libtermux-exec.so` is not called since `LD_PRELOAD` isn't set and you will get `bad interpreter: No such file or directory` errors. Simply setting `LD_PRELOAD` will not work either without starting a new shell. So make sure to set the shebangs correctly for any *external* scripts you want to run from inside your *plugin* script. The correct shebangs for termux scripts are like `#!/data/data/com.termux/files/usr/bin/bash` for bash scripts instead of `#!/usr/bin/bash` used in common linux distros. You can also use [termux-fix-shebang](https://wiki.termux.com/wiki/Termux-fix-shebang) command on the *external* scripts before running them with the plugin to fix the shebangs automatically or use `tudo`/`sudo`.

The [`tudo`](https://github.com/agnostic-apollo/tudo) script can be used for running commands in termux user context and the [`sudo`](https://github.com/agnostic-apollo/sudo) script for running commands with super user (root) context. You can call the *external* scripts in your scripts with the `path` command type of `tudo`/`sudo`. These scripts will load the termux environment properly like setting `LD_PRELOAD` etc before running the commands.
##



## For Maintainers and Contributors

Check [For Maintainers and Contributors](https://github.com/termux/termux-app#For-Maintainers-and-Contributors) section of `termux/termux-app` `README` for details.
##



## Forking

Check [Forking](https://github.com/termux/termux-app#Forking) section of `termux/termux-app` `README` for details.
##



[`adb`]: https://developer.android.com/studio/command-line/adb
[Termux]: https://termux.com
[QuickEdit]: https://play.google.com/store/apps/details?id=com.rhmsoft.edit
[QuickEdit Pro]: https://play.google.com/store/apps/details?id=com.rhmsoft.edit.pro
[Acode editor]: https://github.com/deadlyjack/code-editor
[Turbo Editor]: https://github.com/vmihalachi/turbo-editor
[`root`]: https://topjohnwu.github.io/Magisk/tools.html#su
[RUN_COMMAND Intent]: https://github.com/termux/termux-app/blob/master/app/src/main/java/com/termux/app/RunCommandService.java
