package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeStroke;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ShapeStrokeParser {

  private ShapeStrokeParser() {}

  static ShapeStroke parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableColorValue color = null;
    AnimatableFloatValue width = null;
    AnimatableIntegerValue opacity = null;
    ShapeStroke.LineCapType capType = null;
    ShapeStroke.LineJoinType joinType = null;
    AnimatableFloatValue offset = null;
    float miterLimit = 0f;

    List<AnimatableFloatValue> lineDashPattern = new ArrayList<AnimatableFloatValue>();

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
        else if ("w".equals(ss))
        {
            width = AnimatableValueParser.parseFloat(reader, composition);
        }
        else if ("o".equals(ss))
        {
            opacity = AnimatableValueParser.parseInteger(reader, composition);
        }
        else if ("lc".equals(ss))
        {
            capType = ShapeStroke.LineCapType.values()[reader.nextInt() - 1];
        }
        else if ("lj".equals(ss))
        {
            joinType = ShapeStroke.LineJoinType.values()[reader.nextInt() - 1];
        }
        else if ("ml".equals(ss))
        {
            miterLimit =  (float) reader.nextDouble();
        }
        else if ("d".equals(ss))
        {
            reader.beginArray();
            while (reader.hasNext()) {
              String n = null;
              AnimatableFloatValue val = null;

              reader.beginObject();
              while (reader.hasNext()) {
                  ss = reader.nextName();
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

              if ("o".equals(n))
              {
                  offset = val;
              }
              else if ("d".equals(n) || "g".equals(n))
              {
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

    return new ShapeStroke(
        name, offset, lineDashPattern, color, opacity, width, capType, joinType, miterLimit);
  }
}
