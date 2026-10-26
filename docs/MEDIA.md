# Earth imagery

Blue Meridian uses archival satellite composites for its surface, cloud, and night-light layers. They rotate continuously on a shaded globe; the cloud layer drifts relative to the surface. This is an artistic visualization of Earth, not current weather.

| Layer | Source | Shipped size |
| --- | --- | --- |
| Surface | [NASA Blue Marble Next Generation, July 2004 cloud-free base map](https://science.nasa.gov/earth/earth-observatory/blue-marble-next-generation/base-map/) by Reto Stockli, NASA Earth Observatory / NASA GSFC | 4096 x 2048 |
| Clouds | [NASA Blue Marble Clouds, 2002](https://visibleearth.nasa.gov/images/57747/blue-marble-clouds/77558l), NASA GSFC / MODIS Atmosphere Group | 2048 x 1024 |
| Night lights | [NASA Black Marble, 2012 annual composite](https://science.nasa.gov/earth/earth-observatory/earth-at-night/maps/), NASA Earth Observatory / NOAA / Suomi NPP VIIRS | 2048 x 1024 |

The images are resized to power-of-two dimensions, converted to JPEG, and stripped of metadata. The renderer reduces texture size further when the device's maximum texture dimension requires it. Mipmaps reduce distant texture shimmer.

[The source manifest](../assets/SOURCES.json) records every download URL and SHA-256, along with the shipped file hashes. The original downloads are held outside the repository as build inputs. No runtime download occurs.

NASA imagery is used under its [Images and Media Usage Guidelines](https://www.nasa.gov/nasa-brand-center/images-and-media/). NASA is acknowledged as the source and does not endorse this app. See [NOTICE](../NOTICE).
