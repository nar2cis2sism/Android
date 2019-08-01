package com.airbnb.lottie.parser;

import android.graphics.Path;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.content.GradientFill;
import com.airbnb.lottie.model.content.GradientType;

import java.io.IOException;

class GradientFillParser {

  private GradientFillParser() {}

  static GradientFill parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableGradientColorValue color = null;
    AnimatableIntegerValue opacity = null;
    GradientType gradientType = null;
    AnimatablePointValue startPoint = null;
    AnimatablePointValue endPoint = null;
    Path.FillType fillType = null;

    while (reader.hasNext()) {
        String n = reader.nextName();
        if ("nm".equals(n))
        {
            name = reader.nextString();
        }
        else if ("g".equals(n))
        {
            int points = -1;
            reader.beginObject();
            while (reader.hasNext()) {
                String nn = reader.nextName();
                if ("p".equals(nn))
                {
                    points = reader.nextInt();
                }
                else if ("k".equals(nn))
                {
                    color = AnimatableValueParser.parseGradientColor(reader, composition, points);
                }
                else
                {
                    reader.skipValue();
                }
            
            }
            reader.endObject();
        }
        else if ("o".equals(n))
        {
            opacity = AnimatableValueParser.parseInteger(reader, composition);
        }
        else if ("t".equals(n))
        {
            gradientType = reader.nextInt() == 1 ? GradientType.Linear : GradientType.Radial;
        }
        else if ("s".equals(n))
        {
            startPoint = AnimatableValueParser.parsePoint(reader, composition);
        }
        else if ("e".equals(n))
        {
            endPoint = AnimatableValueParser.parsePoint(reader, composition);
        }
        else if ("r".equals(n))
        {
            fillType = reader.nextInt() == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
        }
        else
        {
            reader.skipValue();
        }
    }

    return new GradientFill(
        name, gradientType, fillType, color, opacity, startPoint, endPoint, null, null);
  }
}
