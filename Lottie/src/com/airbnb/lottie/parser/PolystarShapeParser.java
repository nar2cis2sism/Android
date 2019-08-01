package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.content.PolystarShape;

import java.io.IOException;

class PolystarShapeParser {

  private PolystarShapeParser() {}

  static PolystarShape parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    PolystarShape.Type type = null;
    AnimatableFloatValue points = null;
    AnimatableValue<PointF, PointF> position = null;
    AnimatableFloatValue rotation = null;
    AnimatableFloatValue outerRadius = null;
    AnimatableFloatValue outerRoundedness = null;
    AnimatableFloatValue innerRadius = null;
    AnimatableFloatValue innerRoundedness = null;

    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("nm".equals(ss))
        {
            name = reader.nextString();
        }
        else if ("sy".equals(ss))
        {
            type = PolystarShape.Type.forValue(reader.nextInt());
        }
        else if ("pt".equals(ss))
        {
            points = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("p".equals(ss))
        {
            position = AnimatablePathValueParser.parseSplitPath(reader, composition);
        }
        else if ("r".equals(ss))
        {
            rotation = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("or".equals(ss))
        {
            outerRadius = AnimatableValueParser.parseFloat(reader, composition);
        }
        else if ("os".equals(ss))
        {
            outerRoundedness = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("ir".equals(ss))
        {
            innerRadius = AnimatableValueParser.parseFloat(reader, composition);
        }
        else if ("is".equals(ss))
        {
            innerRoundedness = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else
        {
            reader.skipValue();
        }
    }

    return new PolystarShape(
        name, type, points, position, rotation, innerRadius, outerRadius, innerRoundedness, outerRoundedness);
  }
}
