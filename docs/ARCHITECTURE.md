# Architecture

Blue Meridian is a dependency-free Android application with one `DreamService`, a native remote-friendly settings activity and an exported, versioned settings provider. The service presents a continuously animated OpenGL ES 2 scene. A fragment shader projects a bundled equirectangular Earth surface onto a rotating sphere, applies day and night shading, and draws an atmosphere rim, optional satellite cloud and city-light layers, and stars.

The renderer uses one full-screen quad and three bundled textures. It requests frames at about 30 frames per second and pauses its GL surface when the dream or preview stops. The app has no network permission and no runtime network client. The only network feature is the separate, user-invoked Python installer, which fetches release assets from this GitHub repository and verifies the checksum before installation.

## Android components

- `BlueMeridianDreamService` hosts the renderer while the system screensaver is active.
- `BlueMeridianSceneView` owns the OpenGL surface, texture and shader.
- `SettingsActivity` presents supported choices with the D-pad.
- `PreviewActivity` shows the animation without activating the system dream.
- `SettingsProvider` publishes a schema and validated read/write preference interface for host apps.

The globe uses NASA archival surface, cloud, and night-light composites, documented in [the media guide](MEDIA.md). It includes no runtime network client, analytics libraries, or external fonts. Surface texture rotation is separate from view-space lighting so visible daylight remains stable through each revolution.

Longitude wraps at the edge of each repeating map. On devices exposing both standard derivatives and explicit texture gradients, the shader unwraps those gradients before mipmap selection. This prevents a blurred vertical line at the longitude boundary. Other GLES 2 devices use bilinear sampling without mipmaps; they do not require either extension. Camera choices apply slow screen-space drift rather than an orbital camera.
