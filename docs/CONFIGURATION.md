# Configuration

Open Blue Meridian from the TV launcher and use the D-pad to change a choice. Values are stored in private app preferences and read when the screensaver starts. Per-setting Random selects a new value for that setting at each start. Randomize all settings overrides the individual selections for that start.

| Key | Choices | Default | Effect |
| --- | --- | --- | --- |
| `star_density` | `sparse`, `normal`, `dense`, `random` | `normal` | Adjusts the star field from about 70 to 260 visible points. |
| `spin_speed` | `slow`, `natural`, `fast`, `random` | `natural` | Changes the planet's rotation rate. |
| `camera_motion` | `still`, `gentle`, `sweeping`, `random` | `gentle` | Controls the slow camera drift around the planet. |
| `atmosphere` | `on`, `off`, `random` | `on` | Shows the blue limb and atmospheric halo. |
| `night_lights` | `on`, `off`, `random` | `on` | Shows warm city lights on the unlit side of land. |
| `clouds` | `on`, `off`, `random` | `on` | Adds a moving procedural cloud layer. |
| `randomize_all` | `true`, `false` | `false` | Randomizes each choice when a screensaver session begins. |

## Host application interface

The exported provider authority is `com.jeremykenedy.bluemeridian.settings`.

- `content://com.jeremykenedy.bluemeridian.settings/schema` returns `key`, `title`, `type`, `default`, pipe-separated `choices` and `randomAllowed`.
- `content://com.jeremykenedy.bluemeridian.settings/settings` returns current `key` and `value` rows.

Update one setting by calling `ContentResolver.update()` on the `settings` URI with `ContentValues` named `key` and `value`. The provider rejects unknown keys or unsupported values. `randomize_all` accepts the strings `true` and `false`; other settings accept one of their schema choices. No broadcast is needed. Values take effect on the next dream start.
