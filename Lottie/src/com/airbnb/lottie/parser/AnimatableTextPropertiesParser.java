package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;

import java.io.IOException;

public class AnimatableTextPropertiesParser {

  private AnimatableTextPropertiesParser() {}

  public static AnimatableTextProperties parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatableTextProperties anim = null;

    reader.beginObject();
    while (reader.hasNext()) {
        if ("a".equals(reader.nextName()))
        {
            anim = parseAnimatableTextProperties(reader, composition);
        }
        else
        {
            reader.skipValue();
        }
        
    }
    reader.endObject();
    if (anim == null) {
      // Not sure if this is possible.
      return new AnimatableTextProperties(null, null, null, null);
    }
    return anim;
  }

  private static AnimatableTextProperties parseAnimatableTextProperties(
      JsonReader reader, LottieComposition composition) throws IOException {
    AnimatableColorValue color = null;
    AnimatableColorValue stroke = null;
    AnimatableFloatValue strokeWidth = null;
    AnimatableFloatValue tracking = null;

    reader.beginObject();
    while (reader.hasNext()) {
        String name = reader.nextName();
        if ("fc".equals(name))
        {
            color = AnimatableValueParser.parseColor(reader, composition);
        }
        else if ("sc".equals(name))
        {
            stroke = AnimatableValueParser.parseColor(reader, composition);
        }
        else if ("sw".equals(name))
        {
            strokeWidth = AnimatableValueParser.parseFloat(reader, composition);
        }
        else if ("t".equals(name))
        {
            tracking = AnimatableValueParser.parseFloat(reader, composition);
        }
        else
        {
            reader.skipValue();
        }
    }
    reader.endObject();

    return new AnimatableTextProperties(color, stroke, strokeWidth, tracking);
  }
}
