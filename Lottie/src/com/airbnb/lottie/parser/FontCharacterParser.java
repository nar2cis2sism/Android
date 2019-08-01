package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.content.ShapeGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FontCharacterParser {

  private FontCharacterParser() {}

  static FontCharacter parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    char character = '\0';
    double size = 0;
    double width = 0;
    String style = null;
    String fontFamily = null;
    List<ShapeGroup> shapes = new ArrayList<ShapeGroup>();

    reader.beginObject();
    while (reader.hasNext()) {
        String name = reader.nextName();
        if ("ch".equals(name))
        {
            character = reader.nextString().charAt(0);
        }
        else if ("size".equals(name))
        {
            size = reader.nextDouble();
        }
        else if ("w".equals(name))
        {
            width = reader.nextDouble();
        }
        else if ("style".equals(name))
        {
            style = reader.nextString();
        }
        else if ("fFamily".equals(name))
        {
            fontFamily = reader.nextString();
        }
        else if ("data".equals(name))
        {
            reader.beginObject();
            while (reader.hasNext()) {
              if ("shapes".equals(reader.nextName())) {
                reader.beginArray();
                while (reader.hasNext()) {
                  shapes.add((ShapeGroup) ContentModelParser.parse(reader, composition));
                }
                reader.endArray();
              } else {
                reader.skipValue();
              }
            }
            reader.endObject();
        }
        else
        {
            reader.skipValue();
        }
    }
    reader.endObject();

    return new FontCharacter(shapes, character, size, width, style, fontFamily);
  }
}
