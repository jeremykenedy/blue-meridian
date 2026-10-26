package com.jeremykenedy.bluemeridian;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class CreditsDialog {
  private CreditsDialog() {}

  static void show(Context context) {
    TextView text = new TextView(context);
    text.setTextSize(18);
    int padding = (int) (24 * context.getResources().getDisplayMetrics().density);
    text.setPadding(padding, padding, padding, padding);
    try {
      text.setText(
          read(context, "NOTICE") + "\n\nApplication license\n\n" + read(context, "LICENSE"));
    } catch (IOException error) {
      text.setText("Credits could not be loaded from this build.");
    }
    ScrollView scroll = new ScrollView(context);
    scroll.addView(text);
    scroll.setFocusable(true);
    new AlertDialog.Builder(context)
        .setTitle("Credits and licenses")
        .setView(scroll)
        .setPositiveButton("Close", null)
        .show();
  }

  private static String read(Context context, String name) throws IOException {
    try (InputStream input = context.getAssets().open(name)) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int count;
      while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
      return output.toString("UTF-8");
    }
  }
}
