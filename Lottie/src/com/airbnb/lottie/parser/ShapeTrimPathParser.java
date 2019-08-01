package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.content.ShapeTrimPath;

import java.io.IOException;

class ShapeTrimPathParser {

  private ShapeTrimPathParser() {}

  static ShapeTrimPath parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    ShapeTrimPath.Type type = null;
    AnimatableFloatValue start = null;
    AnimatableFloatValue end = null;
    AnimatableFloatValue offset = null;

    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("s".equals(ss))
        {
            start = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("e".equals(ss))
        {
            end = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("o".equals(ss))
        {
            offset = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("nm".equals(ss))
        {
            name = reader.nextString();
        }
        else if ("m".equals(ss))
        {
            type = ShapeTrimPath.Type.forId(reader.nextInt());
        }
        else
        {
            reader.skipValue();
        }
    }

    return new ShapeTrimPath(name, type, start, end, offset);
  }
}
