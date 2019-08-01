package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.model.DocumentData;

import java.io.IOException;

public class DocumentDataParser implements ValueParser<DocumentData> {
  public static final DocumentDataParser INSTANCE = new DocumentDataParser();

  private DocumentDataParser() {}

  @Override public DocumentData parse(JsonReader reader, float scale) throws IOException {
    String text = null;
    String fontName = null;
    double size = 0;
    int justification = 0;
    int tracking = 0;
    double lineHeight = 0;
    double baselineShift = 0;
    int fillColor = 0;
    int strokeColor = 0;
    double strokeWidth = 0;
    boolean strokeOverFill = true;

    reader.beginObject();
    while (reader.hasNext()) {
        String name = reader.nextName();
        if ("t".equals(name))
        {
            text = reader.nextString();
        }
        else if ("f".equals(name))
        {
            fontName = reader.nextString();
        }
        else if ("s".equals(name))
        {
            size = reader.nextDouble();
        }
        else if ("j".equals(name))
        {
            justification = reader.nextInt();
        }
        else if ("tr".equals(name))
        {
            tracking = reader.nextInt();
        }
        else if ("lh".equals(name))
        {
            lineHeight = reader.nextDouble();
        }
        else if ("ls".equals(name))
        {
            baselineShift = reader.nextDouble();
        }
        else if ("fc".equals(name))
        {
            fillColor = JsonUtils.jsonToColor(reader);
        }
        else if ("sc".equals(name))
        {
            strokeColor = JsonUtils.jsonToColor(reader);
        }
        else if ("sw".equals(name))
        {
            strokeWidth = reader.nextDouble();
        }
        else if ("of".equals(name))
        {
            strokeOverFill = reader.nextBoolean();
        }
        else
        {
            reader.skipValue();
        }
    }
    reader.endObject();

    return new DocumentData(text, fontName, size, justification, tracking, lineHeight,
        baselineShift, fillColor, strokeColor, strokeWidth, strokeOverFill);
  }
}
