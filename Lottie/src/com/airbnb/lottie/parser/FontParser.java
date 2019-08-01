package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.model.Font;

import java.io.IOException;

class FontParser {

  private FontParser() {}

  static Font parse(JsonReader reader) throws IOException {
    String family = null;
    String name = null;
    String style = null;
    float ascent = 0;

    reader.beginObject();
    while (reader.hasNext()) {
        String n = reader.nextName();
        if ("fFamily".equals(n))
        {
            family = reader.nextString();
        }
        else if ("fName".equals(n))
        {
            name = reader.nextString();
        }
        else if ("fStyle".equals(n))
        {
            style = reader.nextString();
        }
        else if ("ascent".equals(n))
        {
            ascent = (float) reader.nextDouble();
        }
        else
        {
            reader.skipValue();
        }
    }
    reader.endObject();

    return new Font(family, name, style, ascent);
  }
}
