package com.airbnb.lottie.parser;

import android.graphics.Path;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeFill;

import java.io.IOException;

class ShapeFillParser {

  private ShapeFillParser() {}

  static ShapeFill parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatableColorValue color = null;
    boolean fillEnabled = false;
    AnimatableIntegerValue opacity = null;
    String name = null;
    int fillTypeInt = 1;

    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("nm".equals(ss))
        {
            name = reader.nextString();
        }
        else if ("c".equals(ss))
        {
            color = AnimatableValueParser.parseColor(reader, composition);
        }
        else if ("o".equals(ss))
        {
            opacity = AnimatableValueParser.parseInteger(reader, composition);
        }
        else if ("fillEnabled".equals(ss))
        {
            fillEnabled = reader.nextBoolean();
        }
        else if ("r".equals(ss))
        {
            fillTypeInt = reader.nextInt();
        }
        else
        {
            reader.skipValue();
        }
    }

    Path.FillType fillType = fillTypeInt == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
    return new ShapeFill(name, fillEnabled, fillType, color, opacity);
  }
}
