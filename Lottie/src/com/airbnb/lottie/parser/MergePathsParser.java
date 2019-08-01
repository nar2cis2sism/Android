package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.model.content.MergePaths;

import java.io.IOException;

class MergePathsParser {

  private MergePathsParser() {}

  static MergePaths parse(JsonReader reader) throws IOException {
    String name = null;
    MergePaths.MergePathsMode mode = null;

    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("nm".equals(ss))
        {
            name = reader.nextString();
        }
        else if ("mm".equals(ss))
        {
            mode =  MergePaths.MergePathsMode.forId(reader.nextInt());
        }
        else
        {
            reader.skipValue();
        }
    }

    return new MergePaths(name, mode);
  }
}
