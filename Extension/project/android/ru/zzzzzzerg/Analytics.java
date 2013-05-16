package ru.zzzzzzerg;

import android.content.Context;
import android.util.Log;

import java.util.Map;
import java.util.HashMap;

import com.flurry.android.FlurryAgent;
import com.localytics.android.LocalyticsSession;

public class Analytics
{
  public static LocalyticsSession localytics;

  private static String tag = "Analytics:ru/zzzzzzerg";

  public static void start(Context ctx, String flurryKey, String localyticsKey)
  {
    Log.d(tag, "Starting Localytics");
    if (localytics == null)
    {
      localytics = new LocalyticsSession(ctx, localyticsKey);
    }

    localytics.open();
    localytics.upload();

    Log.d(tag, "Starting Flurry");
    FlurryAgent.onStartSession(ctx, flurryKey);
    FlurryAgent.setLogEvents(true);

    Log.d(tag, "Analytics started");
  }

  public static void stop(Context ctx)
  {
    Log.d(tag, "Stopping Flurry");
    FlurryAgent.onEndSession(ctx);

    Log.d(tag, "Analytics stopped");
  }

  public static void pause()
  {
    Log.d(tag, "Pausing localytics");
    localytics.close();
    localytics.upload();

    Log.d(tag, "Analytics paused");
  }

  public static void resume()
  {
    Log.d(tag, "Resuming localytics");
    localytics.open();

    Log.d(tag, "Analytics resumed");
  }

  public static void logEvent(String eventId)
  {
    FlurryAgent.logEvent(eventId, null);
    localytics.tagEvent(eventId);
  }

  public static void logEvent(String eventId, String params)
  {
    Map<String, String> m = new HashMap<String, String>();
    m.put("params", params);

    FlurryAgent.logEvent(eventId, m);
    localytics.tagEvent(eventId, m);
  }

  public static void onPageView()
  {
    FlurryAgent.onPageView();
  }

  public static void setLogEnabled(boolean logEnabled)
  {
    FlurryAgent.setLogEnabled(logEnabled);
  }

}
