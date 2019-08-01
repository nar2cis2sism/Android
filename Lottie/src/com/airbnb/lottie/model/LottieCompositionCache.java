package com.airbnb.lottie.model;

import android.util.LruCache;

import com.airbnb.lottie.LottieComposition;

public class LottieCompositionCache {

  private static final int CACHE_SIZE_MB = 10;
  private static final LottieCompositionCache INSTANCE = new LottieCompositionCache();

  public static LottieCompositionCache getInstance() {
    return INSTANCE;
  }

  private final LruCache<String, LottieComposition> cache = new LruCache<String, LottieComposition>(1024 * 1024 * CACHE_SIZE_MB);

  LottieCompositionCache() {
  }

  public LottieComposition get(String cacheKey) {
    if (cacheKey == null) {
      return null;
    }
    return cache.get(cacheKey);
  }

  public void put(String cacheKey, LottieComposition composition) {
    if (cacheKey == null) {
      return;
    }
    cache.put(cacheKey, composition);
  }
}
