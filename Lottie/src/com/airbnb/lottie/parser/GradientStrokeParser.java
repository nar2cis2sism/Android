package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.content.GradientStroke;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.model.content.ShapeStroke;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class GradientStrokeParser {

  private GradientStrokeParser() {}

  static GradientStroke parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableGradientColorValue color = null;
    AnimatableIntegerValue opacity = null;
    GradientType gradientType = null;
    AnimatablePointValue startPoint = null;
    AnimatablePointValue endPoint = null;
    AnimatableFloatValue width = null;
    ShapeStroke.LineCapType capType = null;
    ShapeStroke.LineJoinType joinType = null;
    AnimatableFloatValue offset = null;
    float miterLimit = 0f;


    List<AnimatableFloatValue> lineDashPattern = new ArrayList<AnimatableFloatValue>();

    while (reader.hasNext()) {
        String s = reader.nextName();
        if ("nm".equals(s))
        {
            name = reader.nextString();
        }
        else if ("g".equals(s))
        {
            int points = -1;
            reader.beginObject();
            while (reader.hasNext()) {
                String ss = reader.nextName();
                if ("p".equals(ss))
                {
                    points = reader.nextInt();
                }
                else if ("k".equals(ss))
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
        else if ("o".equals(s))
        {
            opacity = AnimatableValueParser.parseInteger(reader, composition);
        }
        else if ("t".equals(s))
        {
            gradientType = reader.nextInt() == 1 ? GradientType.Linear : GradientType.Radial;
        }
        else if ("s".equals(s))
        {
            startPoint = AnimatableValueParser.parsePoint(reader, composition);
        }
        else if ("e".equals(s))
        {
            endPoint = AnimatableValueParser.parsePoint(reader, composition);
        }
        else if ("w".equals(s))
        {
            width = AnimatableValueParser.parseFloat(reader, composition);
        }
        else if ("lc".equals(s))
        {
            capType = ShapeStroke.LineCapType.values()[reader.nextInt() - 1];
        }
        else if ("lj".equals(s))
        {
            joinType = ShapeStroke.LineJoinType.values()[reader.nextInt() - 1];
        }
        else if ("ml".equals(s))
        {
            miterLimit = (float) reader.nextDouble();
        }
        else if ("d".equals(s))
        {
            reader.beginArray();
            while (reader.hasNext()) {
              String n = null;
              AnimatableFloatValue val = null;
              reader.beginObject();
              while (reader.hasNext()) {
                  String ss = reader.nextName();
                  if ("n".equals(ss))
                  {
                      n = reader.nextString();
                  }
                  else if ("v".equals(ss))
                  {
                      val = AnimatableValueParser.parseFloat(reader, composition);
                  }
                  else
                  {
                      reader.skipValue();
                  }
              }
              reader.endObject();

              if (n.equals("o")) {
                offset = val;
              } else if (n.equals("d") || n.equals("g")) {
                lineDashPattern.add(val);
              }
            }
            reader.endArray();
            if (lineDashPattern.size() == 1) {
              // If there is only 1 value then it is assumed to be equal parts on and off.
              lineDashPattern.add(lineDashPattern.get(0));
            }
        }
        else
        {
            reader.skipValue();
        }
    }

    return new GradientStroke(
        name, gradientType, color, opacity, startPoint, endPoint, width, capType, joinType,
        miterLimit, lineDashPattern, offset);
  }
}
