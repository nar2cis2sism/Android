package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.Repeater;

import java.io.IOException;

class RepeaterParser {

  private RepeaterParser() {}

  static Repeater parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableFloatValue copies = null;
    AnimatableFloatValue offset = null;
    AnimatableTransform transform = null;

    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("nm".equals(ss))
        {
            name = reader.nextString();
        }
        else if ("c".equals(ss))
        {
            copies = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("o".equals(ss))
        {
            offset = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("tr".equals(ss))
        {
            transform = AnimatableTransformParser.parse(reader, composition);
        }
        else
        {
            reader.skipValue();
        }
    }

    return new Repeater(name, copies, offset, transform);
  }
}
