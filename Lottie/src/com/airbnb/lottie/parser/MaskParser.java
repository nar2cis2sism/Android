package com.airbnb.lottie.parser;

import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;
import com.airbnb.lottie.model.content.Mask;

import java.io.IOException;

class MaskParser {

  private MaskParser() {}

  static Mask parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    Mask.MaskMode maskMode = null;
    AnimatableShapeValue maskPath = null;
    AnimatableIntegerValue opacity = null;

    reader.beginObject();
    while (reader.hasNext()) {
        String mode = reader.nextName();
        if ("mode".equals(mode))
        {
            String ss = reader.nextName();
            if ("a".equals(ss))
            {
                maskMode = Mask.MaskMode.MaskModeAdd;
            }
            else if ("s".equals(ss))
            {
                maskMode = Mask.MaskMode.MaskModeSubtract;
            }
            else if ("i".equals(ss))
            {
                composition.addWarning(
                        "Animation contains intersect masks. They are not supported but will be treated like add masks.");
                    maskMode = Mask.MaskMode.MaskModeIntersect;
            }
            else
            {
                Log.w(L.TAG, "Unknown mask mode " + mode + ". Defaulting to Add.");
                maskMode = Mask.MaskMode.MaskModeAdd;
            }
        }
        else if ("pt".equals(mode))
        {
            maskPath = AnimatableValueParser.parseShapeData(reader, composition);
        }
        else if ("o".equals(mode))
        {
            opacity = AnimatableValueParser.parseInteger(reader, composition);
        }
        else
        {
            reader.skipValue();
        }
    }
    reader.endObject();

    return new Mask(maskMode, maskPath, opacity);
  }

}
