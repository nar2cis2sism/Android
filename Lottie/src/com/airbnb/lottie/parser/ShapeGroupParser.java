package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.ShapeGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ShapeGroupParser {

  private ShapeGroupParser() {}

  static ShapeGroup parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    List<ContentModel> items = new ArrayList<ContentModel>();

    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("nm".equals(ss))
        {
            name = reader.nextString();
        }
        else if ("it".equals(ss))
        {
            reader.beginArray();
            while (reader.hasNext()) {
              ContentModel newItem = ContentModelParser.parse(reader, composition);
              if (newItem != null) {
                items.add(newItem);
              }
            }
            reader.endArray();
        }
        else
        {
            reader.skipValue();
        }
    }

    return new ShapeGroup(name, items);
  }
}
