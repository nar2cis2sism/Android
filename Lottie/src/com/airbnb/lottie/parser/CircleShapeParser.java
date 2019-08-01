package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.content.CircleShape;

import java.io.IOException;

class CircleShapeParser {

  private CircleShapeParser() {}

  static CircleShape parse(
      JsonReader reader, LottieComposition composition, int d) throws IOException {
    String name = null;
    AnimatableValue<PointF, PointF> position = null;
    AnimatablePointValue size = null;
    boolean reversed = d == 3;

    while (reader.hasNext()) {
        String n = reader.nextName();
        if ("nm".equals(n))
        {
            name = reader.nextString();
        }
        else if ("p".equals(n))
        {
            position = AnimatablePathValueParser.parseSplitPath(reader, composition);
        }
        else if ("s".equals(n))
        {
            size = AnimatableValueParser.parsePoint(reader, composition);
        }
        else if ("d".equals(n))
        {
            // "d" is 2 for normal and 3 for reversed.
            reversed = reader.nextInt() == 3;
        }
        else
        {
            reader.skipValue();
        }
    }

    return new CircleShape(name, position, size, reversed);
  }
}
