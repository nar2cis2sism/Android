package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.content.RectangleShape;

import java.io.IOException;

class RectangleShapeParser {

  private RectangleShapeParser() {}

  static RectangleShape parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableValue<PointF, PointF> position = null;
    AnimatablePointValue size = null;
    AnimatableFloatValue roundedness = null;

    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("nm".equals(ss))
        {
            name = reader.nextString();
        }
        else if ("p".equals(ss))
        {
            position =
                    AnimatablePathValueParser.parseSplitPath(reader, composition);
        }
        else if ("s".equals(ss))
        {
            size = AnimatableValueParser.parsePoint(reader, composition);
        }
        else if ("r".equals(ss))
        {
            roundedness = AnimatableValueParser.parseFloat(reader, composition);
        }
        else
        {
            reader.skipValue();
        }
    }

    return new RectangleShape(name, position, size, roundedness);
  }
}
