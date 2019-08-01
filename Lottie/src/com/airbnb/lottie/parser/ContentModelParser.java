package com.airbnb.lottie.parser;

import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.ContentModel;

import java.io.IOException;

class ContentModelParser {

  private ContentModelParser() {}

  static ContentModel parse(JsonReader reader, LottieComposition composition)
      throws IOException {
    String type = null;

    reader.beginObject();
    // Unfortunately, for an ellipse, d is before "ty" which means that it will get parsed
    // before we are in the ellipse parser.
    // "d" is 2 for normal and 3 for reversed.
    int d = 2;
    typeLoop:
    while (reader.hasNext()) {
        String name = reader.nextName();
        if ("ty".equals(name))
        {
            type = reader.nextString();
            break typeLoop;
        }
        else if ("d".equals(name))
        {
            d = reader.nextInt();
        }
        else
        {
            reader.skipValue();
        }
    }

    if (type == null) {
      return null;
    }

    ContentModel model = null;
    if ("gr".equals(type))
    {
        model = ShapeGroupParser.parse(reader, composition);
    }
    else if ("st".equals(type))
    {
        model = ShapeStrokeParser.parse(reader, composition);
    }
    else if ("gs".equals(type))
    {
        model = GradientStrokeParser.parse(reader, composition);
    }
    else if ("fl".equals(type))
    {
        model = ShapeFillParser.parse(reader, composition);
    }
    else if ("gf".equals(type))
    {
        model = GradientFillParser.parse(reader, composition);
    }
    else if ("tr".equals(type))
    {
        model = AnimatableTransformParser.parse(reader, composition);
    }
    else if ("sh".equals(type))
    {
        model = ShapePathParser.parse(reader, composition);
    }
    else if ("el".equals(type))
    {
        model = CircleShapeParser.parse(reader, composition, d);
    }
    else if ("rc".equals(type))
    {
        model = RectangleShapeParser.parse(reader, composition);
    }
    else if ("tm".equals(type))
    {
        model = ShapeTrimPathParser.parse(reader, composition);
    }
    else if ("sr".equals(type))
    {
        model = PolystarShapeParser.parse(reader, composition);
    }
    else if ("mm".equals(type))
    {
        model = MergePathsParser.parse(reader);
        composition.addWarning("Animation contains merge paths. Merge paths are only " +
            "supported on KitKat+ and must be manually enabled by calling " +
            "enableMergePathsForKitKatAndAbove().");
    }
    else if ("rp".equals(type))
    {
        model = RepeaterParser.parse(reader, composition);
    }
    else
    {
        Log.w(L.TAG, "Unknown shape type " + type);
    }

    while (reader.hasNext()) {
      reader.skipValue();
    }
    reader.endObject();

    return model;
  }
}
