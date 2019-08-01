package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;
import com.airbnb.lottie.model.content.ShapePath;

import java.io.IOException;

class ShapePathParser {

  private ShapePathParser() {}

  static ShapePath parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    int ind = 0;
    AnimatableShapeValue shape = null;

    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("nm".equals(ss))
        {
            name = reader.nextString();
        }
        else if ("ind".equals(ss))
        {
            ind = reader.nextInt();
        }
        else if ("ks".equals(ss))
        {
            shape = AnimatableValueParser.parseShapeData(reader, composition);
        }
        else
        {
            reader.skipValue();
        }
    }

    return new ShapePath(name, ind, shape);
  }
}
