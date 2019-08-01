package com.airbnb.lottie.parser;

import android.graphics.Color;
import android.graphics.Rect;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LayerParser {

  private LayerParser() {}

  public static Layer parse(LottieComposition composition) {
    Rect bounds = composition.getBounds();
    return new Layer(
        Collections.<ContentModel>emptyList(), composition, "__container", -1,
        Layer.LayerType.PreComp, -1, null, Collections.<Mask>emptyList(),
        new AnimatableTransform(), 0, 0, 0, 0, 0,
        bounds.width(), bounds.height(), null, null, Collections.<Keyframe<Float>>emptyList(),
        Layer.MatteType.None, null);
  }

  public static Layer parse(JsonReader reader, LottieComposition composition) throws IOException {
    // This should always be set by After Effects. However, if somebody wants to minify
    // and optimize their json, the name isn't critical for most cases so it can be removed.
    String layerName = "UNSET";
    Layer.LayerType layerType = null;
    String refId = null;
    long layerId = 0;
    int solidWidth = 0;
    int solidHeight = 0;
    int solidColor = 0;
    int preCompWidth = 0;
    int preCompHeight = 0;
    long parentId = -1;
    float timeStretch = 1f;
    float startFrame = 0f;
    float inFrame = 0f;
    float outFrame = 0f;
    String cl = null;

    Layer.MatteType matteType = Layer.MatteType.None;
    AnimatableTransform transform = null;
    AnimatableTextFrame text = null;
    AnimatableTextProperties textProperties = null;
    AnimatableFloatValue timeRemapping = null;

    List<Mask> masks = new ArrayList<Mask>();
    List<ContentModel> shapes = new ArrayList<ContentModel>();

    reader.beginObject();
    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("nm".equals(ss))
        {
            layerName = reader.nextString();
        }
        else if ("ind".equals(ss))
        {
            layerId = reader.nextInt();
        }
        else if ("refId".equals(ss))
        {
            refId = reader.nextString();
        }
        else if ("ty".equals(ss))
        {
            int layerTypeInt = reader.nextInt();
            if (layerTypeInt < Layer.LayerType.Unknown.ordinal()) {
              layerType = Layer.LayerType.values()[layerTypeInt];
            } else {
              layerType = Layer.LayerType.Unknown;
            }
        }
        else if ("parent".equals(ss))
        {
            parentId = reader.nextInt();
        }
        else if ("sw".equals(ss))
        {
            solidWidth = (int) (reader.nextInt() * Utils.dpScale());
        }
        else if ("sh".equals(ss))
        {
            solidHeight = (int) (reader.nextInt() * Utils.dpScale());
        }
        else if ("sc".equals(ss))
        {
            solidColor = Color.parseColor(reader.nextString());
        }
        else if ("ks".equals(ss))
        {
            transform = AnimatableTransformParser.parse(reader, composition);
        }
        else if ("tt".equals(ss))
        {
            matteType = Layer.MatteType.values()[reader.nextInt()];
        }
        else if ("masksProperties".equals(ss))
        {
            reader.beginArray();
            while (reader.hasNext()) {
              masks.add(MaskParser.parse(reader, composition));
            }
            reader.endArray();
        }
        else if ("shapes".equals(ss))
        {
            reader.beginArray();
            while (reader.hasNext()) {
              ContentModel shape = ContentModelParser.parse(reader, composition);
              if (shape != null) {
                shapes.add(shape);
              }
            }
            reader.endArray();
        }
        else if ("t".equals(ss))
        {
            reader.beginObject();
            while (reader.hasNext()) {
                ss = reader.nextName();
                if ("d".equals(ss))
                {
                    text = AnimatableValueParser.parseDocumentData(reader, composition);
                }
                else if ("a".equals(ss))
                {
                    reader.beginArray();
                    if (reader.hasNext()) {
                      textProperties = AnimatableTextPropertiesParser.parse(reader, composition);
                    }
                    while (reader.hasNext()) {
                      reader.skipValue();
                    }
                    reader.endArray();
                }
                else
                {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }
        else if ("ef".equals(ss))
        {
            reader.beginArray();
            List<String> effectNames = new ArrayList<String>();
            while (reader.hasNext()) {
              reader.beginObject();
              while (reader.hasNext()) {
                  ss = reader.nextName();
                  if ("nm".equals(ss))
                  {
                      effectNames.add(reader.nextString());
                  }
                  else
                  {
                      reader.skipValue();
                  }
              }
              reader.endObject();
            }
            reader.endArray();
            composition.addWarning("Lottie doesn't support layer effects. If you are using them for " +
                " fills, strokes, trim paths etc. then try adding them directly as contents " +
                " in your shape. Found: " + effectNames);
        }
        else if ("sr".equals(ss))
        {
            timeStretch = (float) reader.nextDouble();
        }
        else if ("st".equals(ss))
        {
            startFrame = (float) reader.nextDouble();
        }
        else if ("w".equals(ss))
        {
            preCompWidth = (int) (reader.nextInt() * Utils.dpScale());
        }
        else if ("h".equals(ss))
        {
            preCompHeight = (int) (reader.nextInt() * Utils.dpScale());
        }
        else if ("ip".equals(ss))
        {
            inFrame = (float) reader.nextDouble();
        }
        else if ("op".equals(ss))
        {
            outFrame = (float) reader.nextDouble();
        }
        else if ("tm".equals(ss))
        {
            timeRemapping = AnimatableValueParser.parseFloat(reader, composition, false);
        }
        else if ("cl".equals(ss))
        {
            cl = reader.nextString();
        }
        else
        {
            reader.skipValue();
        }
    }
    reader.endObject();

    // Bodymovin pre-scales the in frame and out frame by the time stretch. However, that will
    // cause the stretch to be double counted since the in out animation gets treated the same
    // as all other animations and will have stretch applied to it again.
    inFrame /= timeStretch;
    outFrame /= timeStretch;

    List<Keyframe<Float>> inOutKeyframes = new ArrayList<Keyframe<Float>>();
    // Before the in frame
    if (inFrame > 0) {
      Keyframe<Float> preKeyframe = new Keyframe<Float>(composition, 0f, 0f, null, 0f, inFrame);
      inOutKeyframes.add(preKeyframe);
    }

    // The + 1 is because the animation should be visible on the out frame itself.
    outFrame = (outFrame > 0 ? outFrame : composition.getEndFrame());
    Keyframe<Float> visibleKeyframe =
        new Keyframe<Float>(composition, 1f, 1f, null, inFrame, outFrame);
    inOutKeyframes.add(visibleKeyframe);

    Keyframe<Float> outKeyframe = new Keyframe<Float>(
        composition, 0f, 0f, null, outFrame, Float.MAX_VALUE);
    inOutKeyframes.add(outKeyframe);

    if (layerName.endsWith(".ai") || "ai".equals(cl)) {
      composition.addWarning("Convert your Illustrator layers to shape layers.");
    }

    return new Layer(shapes, composition, layerName, layerId, layerType, parentId, refId,
        masks, transform, solidWidth, solidHeight, solidColor, timeStretch, startFrame,
        preCompWidth, preCompHeight, text, textProperties, inOutKeyframes, matteType,
        timeRemapping);
  }
}
