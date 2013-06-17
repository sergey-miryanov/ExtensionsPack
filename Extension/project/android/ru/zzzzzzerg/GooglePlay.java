package ru.zzzzzzerg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.opengl.GLSurfaceView;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClient.Builder;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.achievement.OnAchievementsLoadedListener;
import com.google.android.gms.games.achievement.AchievementBuffer;

import org.haxe.nme.HaxeObject;
import org.haxe.nme.GameActivity;

public class GooglePlay
{
  public static GamesClient gamesClient = null;
  public static int result = ConnectionResult.DEVELOPER_ERROR;
  public static HaxeObject connectionCallback = null;

  public static int GOOGLE_PLAY_SIGN_IN_REQUEST = 20202;
  public static int GOOGLE_PLAY_SHOW_ACHIEVEMENTS_REQUEST = 20203;

  static void handleException(Exception e, String where)
  {
    if(connectionCallback != null)
    {
      connectionCallback.call("onException", new Object[] {e.toString(), where});
    }
    else
    {
      Log.i("trace", "Exception at " + where + ": " + e.toString());
    }
  }

  public static void start(Context ctx)
  {
    gamesClient = new GamesClient.Builder(ctx,
        new GooglePlayCallback(),
        new GooglePlayCallback())
      .setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
      .setScopes(Scopes.GAMES)
      .create();
  }

  public static void stop()
  {
    if(gamesClient != null)
    {
      gamesClient.disconnect();
      gamesClient = null;
    }
  }

  public static void signIn(HaxeObject callback)
  {
    try
    {
      connectionCallback = callback;
      callback.call("initErrorTable", new Object[] {
        ConnectionResult.DEVELOPER_ERROR,
        ConnectionResult.INTERNAL_ERROR,
        ConnectionResult.INVALID_ACCOUNT,
        ConnectionResult.LICENSE_CHECK_FAILED,
        ConnectionResult.NETWORK_ERROR,
        ConnectionResult.RESOLUTION_REQUIRED,
        ConnectionResult.SERVICE_DISABLED,
        ConnectionResult.SERVICE_INVALID,
        ConnectionResult.SERVICE_MISSING,
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
        ConnectionResult.SIGN_IN_REQUIRED,
        ConnectionResult.SUCCESS});

      if(!gamesClient.isConnected() && !gamesClient.isConnecting())
      {
        Log.i("trace", "Connecting to GamesClient");

        // not sure should we run gamesClient.connect in thread
        // but unlock and increment achievement sould
        GLSurfaceView view = GameActivity.getMainView();
        GameActivity.getInstance().runOnUiThread(new Runnable(){
          public void run() {
            gamesClient.connect();
          }});
      }
      else if(gamesClient.isConnecting())
      {
        Log.i("trace", "Already connecting");
      }
      else if(gamesClient.isConnected())
      {
        Log.i("trace", "Already connected");
        connectionEstablished();
      }
    }
    catch(Exception e)
    {
      handleException(e, "signIn");
    }
  }

  public static void signOut()
  {
    if(gamesClient != null && gamesClient.isConnected())
    {
      gamesClient.signOut(); // FIXME: sign out with listener
      if(connectionCallback != null)
      {
        connectionCallback.call("signedOut", new Object[]{});
      }
    }
  }

  public static void unlockAchievement(final String achievementId)
  {
    GLSurfaceView view = GameActivity.getMainView();
    GameActivity.getInstance().runOnUiThread(new Runnable(){
      public void run() {
        try
        {
          Log.i("trace", "Unlocking achievement: " + achievementId);
          if(gamesClient != null && gamesClient.isConnected())
          {
            gamesClient.unlockAchievement(achievementId);
            Log.i("trace", "Achievement unlocked: " + achievementId);
          }
        }
        catch(Exception e)
        {
          handleException(e, "unlockAchievement");
        }
      }});
  }

  public static void incrementAchievement(final String achievementId, final int steps)
  {
    GLSurfaceView view = GameActivity.getMainView();
    GameActivity.getInstance().runOnUiThread(new Runnable(){
      public void run() {
        try
        {
          Log.i("trace", "Incrementing achievement: " + achievementId + " with " + steps);
          if(gamesClient != null && gamesClient.isConnected())
          {
            gamesClient.incrementAchievement(achievementId, steps);
            Log.i("trace", "Achievement incremented: " + achievementId);
          }
        }
        catch(Exception e)
        {
          handleException(e, "incrementAchievement");
        }
      }});
  }

  public static void showAchievements()
  {
    try
    {
      if(gamesClient != null && gamesClient.isConnected())
      {
        Intent intent = gamesClient.getAchievementsIntent();
        GameActivity.getInstance().startActivityForResult(intent,
            GOOGLE_PLAY_SHOW_ACHIEVEMENTS_REQUEST);

        Log.i("trace", "Starting activity for show achievements");
      }
    }
    catch(Exception e)
    {
      handleException(e, "showAchievements");
    }
  }


  public static boolean handleActivityResult(int rc, int resultCode,
      Intent data)
  {
    Log.i("trace", "handleActivityResult: " + rc + " " + resultCode);
    if(rc == GOOGLE_PLAY_SIGN_IN_REQUEST)
    {
      connectionEstablished();
      return true;
    }
    else if(rc == GOOGLE_PLAY_SHOW_ACHIEVEMENTS_REQUEST)
    {
      Log.i("trace", "Activity for show achievements handled");
      return true;
    }

    return false;
  }
  public static void connectionEstablished()
  {
    Log.d("trace", "Connection established");
    if(connectionCallback == null)
    {
      Log.d("trace", "Connection established, but connection callback is null");
    }
    else
    {
      connectionCallback.call("signedIn", new Object[] {});
    }

    if(gamesClient != null && gamesClient.isConnected())
    {
      gamesClient.loadAchievements(new GooglePlayCallback());
    }
  }
}

class GooglePlayCallback implements
      ConnectionCallbacks,
      OnConnectionFailedListener,
      OnAchievementsLoadedListener
{
  public void onConnected(Bundle hint)
  {
    Log.i("trace", "GooglePlayCallback.onConnected");
    GooglePlay.connectionEstablished();
  }
  public void onDisconnected()
  {
    Log.i("trace", "GooglePlayCallback.onDisconnected");
    GooglePlay.result = ConnectionResult.SUCCESS;
  }
  public void onConnectionFailed(ConnectionResult result)
  {
    try
    {
      GooglePlay.result = result.getErrorCode();
      if(GooglePlay.result == ConnectionResult.SIGN_IN_REQUIRED)
      {
        Log.i("trace", "GooglePlayCallback: SignIn Required");
        result.startResolutionForResult(GameActivity.getInstance(),
            GooglePlay.GOOGLE_PLAY_SIGN_IN_REQUEST);
      }
      else
      {
        Log.i("trace", "GooglePlayCallback.onConnectionFailed: " + GooglePlay.result);
      }
    }
    catch(Exception e)
    {
      Log.i("trace", "GooglePlayCallback.onConnectionFailed: " + e.toString());
      if(GooglePlay.connectionCallback != null)
      {
        GooglePlay.connectionCallback.call("onException",
            new Object[] {e.toString(), "onConnectionFailed"});
      }
    }
  }
  public void onAchievementsLoaded(int statusCode, AchievementBuffer buffer)
  {
    Log.i("trace", "GooglePlayCallback.onAchievementsLoaded: " + statusCode);
  }
}
