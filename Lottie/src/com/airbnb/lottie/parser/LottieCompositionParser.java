package com.airbnb.lottie.parser;

import android.graphics.Rect;
import android.support.v4.util.SparseArrayCompat;
import android.util.JsonReader;
import android.util.LongSparseArray;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LottieCompositionParser {

  private LottieCompositionParser() {}

  public static LottieComposition parse(JsonReader reader) throws IOException {
    float scale = Utils.dpScale();
    float startFrame = 0f;
    float endFrame = 0f;
    float frameRate = 0f;
    final LongSparseArray<Layer> layerMap = new LongSparseArray<Layer>();
    final List<Layer> layers = new ArrayList<Layer>();
    int width = 0;
    int height = 0;
    Map<String, List<Layer>> precomps = new HashMap<String, List<Layer>>();
    Map<String, LottieImageAsset> images = new HashMap<String, LottieImageAsset>();
    Map<String, Font> fonts = new HashMap<String, Font>();
    SparseArrayCompat<FontCharacter> characters = new SparseArrayCompat<FontCharacter>();

    LottieComposition composition = new LottieComposition();

    reader.beginObject();
    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("w".equals(ss))
        {
            width = reader.nextInt();
        }
        else if ("h".equals(ss))
        {
            height = reader.nextInt();
        }
        else if ("ip".equals(ss))
        {
            startFrame = (float) reader.nextDouble();
        }
        else if ("op".equals(ss))
        {
            endFrame = (float) reader.nextDouble() - 0.01f;
        }
        else if ("fr".equals(ss))
        {
            frameRate = (float) reader.nextDouble();
        }
        else if ("v".equals(ss))
        {
            String version = reader.nextString();
            String[] versions = version.split("\\.");
            int majorVersion = Integer.parseInt(versions[0]);
            int minorVersion = Integer.parseInt(versions[1]);
            int patchVersion = Integer.parseInt(versions[2]);
            if (!Utils.isAtLeastVersion(majorVersion, minorVersion, patchVersion,
                4, 4, 0)) {
              composition.addWarning("Lottie only supports bodymovin >= 4.4.0");
            }
        }
        else if ("layers".equals(ss))
        {
            parseLayers(reader, composition, layers, layerMap);
        }
        else if ("assets".equals(ss))
        {
            parseAssets(reader, composition, precomps, images);
        }
        else if ("fonts".equals(ss))
        {
            parseFonts(reader, fonts);
        }
        else if ("chars".equals(ss))
        {
            parseChars(reader, composition, characters);
        }
        else
        {
            reader.skipValue();
        }
    }
    reader.endObject();

    int scaledWidth = (int) (width * scale);
    int scaledHeight = (int) (height * scale);
    Rect bounds = new Rect(0, 0, scaledWidth, scaledHeight);

    composition.init(bounds, startFrame, endFrame, frameRate, layers, layerMap, precomps,
        images, characters, fonts);

    return composition;
  }

  private static void parseLayers(JsonReader reader, LottieComposition composition,
      List<Layer> layers, LongSparseArray<Layer> layerMap) throws IOException {
    int imageCount = 0;
    reader.beginArray();
    while (reader.hasNext()) {
      Layer layer = LayerParser.parse(reader, composition);
      if (layer.getLayerType() == Layer.LayerType.Image) {
        imageCount++;
      }
      layers.add(layer);
      layerMap.put(layer.getId(), layer);

      if (imageCount > 4) {
        L.warn("You have " + imageCount + " images. Lottie should primarily be " +
            "used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers" +
            " to shape layers.");
      }
    }
    reader.endArray();
  }

  private static void parseAssets(JsonReader reader, LottieComposition composition,
      Map<String, List<Layer>> precomps, Map<String, LottieImageAsset> images) throws IOException {
    reader.beginArray();
    while (reader.hasNext()) {
      String id = null;
      // For precomps
      List<Layer> layers = new ArrayList<Layer>();
      LongSparseArray<Layer> layerMap = new LongSparseArray<Layer>();
      // For images
      int width = 0;
      int height = 0;
      String imageFileName = null;
      String relativeFolder = null;
      reader.beginObject();
      while (reader.hasNext()) {
          String ss = reader.nextName();
          if ("id".equals(ss))
          {
              id = reader.nextString();
          }
          else if ("layers".equals(ss))
          {
              reader.beginArray();
              while (reader.hasNext()) {
                Layer layer = LayerParser.parse(reader, composition);
                layerMap.put(layer.getId(), layer);
                layers.add(layer);
              }
              reader.endArray();
          }
          else if ("w".equals(ss))
          {
              width = reader.nextInt();
          }
          else if ("h".equals(ss))
          {
              height = reader.nextInt();
          }
          else if ("p".equals(ss))
          {
              imageFileName = reader.nextString();
          }
          else if ("u".equals(ss))
          {
              relativeFolder = reader.nextString();
          }
          else
          {
              reader.skipValue();
          }
      }
      reader.endObject();
      if (imageFileName != null) {
        LottieImageAsset image =
            new LottieImageAsset(width, height, id, imageFileName, relativeFolder);
        images.put(image.getId(), image);
      } else {
        precomps.put(id, layers);
      }
    }
    reader.endArray();
  }

  private static void parseFonts(JsonReader reader, Map<String, Font> fonts) throws IOException {

    reader.beginObject();
    while (reader.hasNext()) {
        String ss = reader.nextName();
        if ("list".equals(ss))
        {
            reader.beginArray();
            while (reader.hasNext()) {
              Font font = FontParser.parse(reader);
              fonts.put(font.getName(), font);
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

  private static void parseChars(
      JsonReader reader, LottieComposition composition,
      SparseArrayCompat<FontCharacter> characters) throws IOException {
    reader.beginArray();
    while (reader.hasNext()) {
      FontCharacter character = FontCharacterParser.parse(reader, composition);
      characters.put(character.hashCode(), character);
    }
    reader.endArray();
  }
}
