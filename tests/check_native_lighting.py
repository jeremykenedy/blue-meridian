#!/usr/bin/env python3
"""Check visible daylight during real native rotation on an Android TV emulator."""

import argparse
import json
from pathlib import Path
import re
import subprocess
import time

PACKAGE = 'com.jeremykenedy.bluemeridian'
URI = 'content://' + PACKAGE + '.settings/settings'


def check(serial, output):
    def adb(*args):
        return subprocess.check_output(['adb', '-s', serial, *args], timeout=35)

    def shell(*args):
        return adb('shell', *args).decode().strip()

    def preferences():
        return dict(re.findall(r'key=([^,]+), value=([^\r\n]+)',
                               shell('content', 'query', '--uri', URI)))

    def update(key, value):
        result = shell('content', 'update', '--uri', URI,
                       '--bind', 'key:s:' + key, '--bind', 'value:s:' + value)
        if 'Exception' in result or 'Error' in result:
            raise ValueError(result)

    if shell('getprop', 'ro.kernel.qemu') != '1':
        raise ValueError('Use an emulator for this repeatable lighting check.')
    before = preferences()
    output.mkdir(parents=True, exist_ok=True)
    results = []
    try:
        for key, value in {'spin_speed': 'fast', 'camera_motion': 'still',
                           'atmosphere': 'on', 'night_lights': 'on',
                           'clouds': 'on', 'randomize_all': 'false'}.items():
            update(key, value)
        shell('am', 'force-stop', PACKAGE)
        shell('am', 'start', '-W', '-n', PACKAGE + '/.PreviewActivity')
        started = time.monotonic()
        for seconds in (8, 24, 40):
            time.sleep(max(0, seconds - (time.monotonic() - started)))
            raw = output / ('rotation-' + str(seconds) + '.png')
            raw.write_bytes(adb('exec-out', 'screencap', '-p'))
            pixels = subprocess.check_output(
                ['magick', str(raw), '-resize', '240x135!', '-depth', '8', 'RGB:-'])
            luminance = []
            for y in range(135):
                for x in range(240):
                    if (x - 122) ** 2 + (y - 70) ** 2 > 37 ** 2:
                        continue
                    index = (y * 240 + x) * 3
                    red, green, blue = pixels[index:index + 3]
                    luminance.append(0.2126 * red + 0.7152 * green + 0.0722 * blue)
            mean = sum(luminance) / len(luminance)
            daylight = sum(value > 25 for value in luminance) / len(luminance)
            result = {'seconds': seconds, 'mean_luminance': mean,
                      'visible_daylight_fraction': daylight}
            results.append(result)
            (output / 'lighting.json').write_text(json.dumps(results, indent=2) + '\n')
            print(result, flush=True)
            if mean < 15 or daylight < 0.08:
                raise AssertionError('The rotating globe lost its visible daylight hemisphere.')
    finally:
        shell('am', 'force-stop', PACKAGE)
        for key, value in before.items():
            update(key, value)
        if preferences() != before:
            raise AssertionError('The native lighting check changed app preferences.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--serial', required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    check(args.serial, args.output)
