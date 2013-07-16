package ru.zzzzzzerg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.opengl.GLSurfaceView;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.achievement.OnAchievementsLoadedListener;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievement;

import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.AppStateBuffer;
import com.google.android.gms.appstate.AppState;
import com.google.android.gms.appstate.OnStateListLoadedListener;
import com.google.android.gms.appstate.OnStateDeletedListener;
import com.google.android.gms.appstate.OnStateLoadedListener;

import org.haxe.nme.HaxeObject;
import org.haxe.nme.GameActivity;

public class GooglePlay
{
  public static GamesClient gamesClient = null;
  public static AppStateClient appStateClient = null;

  public static int result = ConnectionResult.DEVELOPER_ERROR;
  public static HaxeObject connectionCallback = null;

  public static int GOOGLE_PLAY_SIGN_IN_REQUEST = 20201;
  public static int GOOGLE_PLAY_APP_STATE_SIGN_IN_REQUEST = 20202;
  public static int GOOGLE_PLAY_SHOW_ACHIEVEMENTS_REQUEST = 20203;
  public static int GOOGLE_PLAY_SHOW_LEADERBOARD_REQUEST = 20204;

  public static void handleException(Exception e, String where)
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

  public static void gamesClientError(int code, String where)
  {
    if(connectionCallback != null)
    {
      connectionCallback.call("onError", new Object[] {"GAMES_CLIENT", code, where});
    }
    else
    {
      Log.i("trace", "Error at " + where + " with code = " + code);
    }
  }
  public static void appStateClientError(int code, String where)
  {
    if(connectionCallback != null)
    {
      connectionCallback.call("onError", new Object[] {"APP_STATE_CLIENT", code, where});
    }
    else
    {
      Log.i("trace", "Error at " + where + " with code = " + code);
    }
  }

  public static void start(Context ctx)
  {
    gamesClient = new GamesClient.Builder(ctx,
        new GooglePlayCallback("GAMES_CLIENT"),
        new GooglePlayCallback("GAMES_CLIENT"))
      .setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
      .setScopes(Scopes.GAMES)
      .create();

    appStateClient = new AppStateClient.Builder(ctx,
        new GooglePlayCallback("APP_STATE_CLIENT"),
        new GooglePlayCallback("APP_STATE_CLIENT"))
      .setScopes(Scopes.APP_STATE)
      .create();
  }

  public static void stop()
  {
    if(gamesClient != null)
    {
      gamesClient.disconnect();
      gamesClient = null;
    }
    if(appStateClient != null)
    {
      appStateClient.disconnect();
      appStateClient = null;
    }
  }

  public static void signIn(HaxeObject callback)
  {
    try
    {
      connectionCallback = callback;
      callback.call("initGamesClientErrors", new Object[] {
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
      callback.call("initAppStateClientErrors", new Object[] {
        AppStateClient.STATUS_CLIENT_RECONNECT_REQUIRED,
        AppStateClient.STATUS_DEVELOPER_ERROR,
        AppStateClient.STATUS_INTERNAL_ERROR,
        AppStateClient.STATUS_NETWORK_ERROR_NO_DATA,
        AppStateClient.STATUS_NETWORK_ERROR_OPERATION_DEFERRED,
        AppStateClient.STATUS_NETWORK_ERROR_OPERATION_FAILED,
        AppStateClient.STATUS_NETWORK_ERROR_STALE_DATA,
        AppStateClient.STATUS_OK,
        AppStateClient.STATUS_STATE_KEY_LIMIT_EXCEEDED,
        AppStateClient.STATUS_STATE_KEY_NOT_FOUND,
        AppStateClient.STATUS_WRITE_OUT_OF_DATE_VERSION,
        AppStateClient.STATUS_WRITE_SIZE_EXCEEDED});

      callback.call("initAchievementStates", new Object[] {
        Achievement.STATE_UNLOCKED,
        Achievement.STATE_REVEALED,
        Achievement.STATE_HIDDEN,});
      callback.call("initAchievementTypes", new Object[] {
        Achievement.TYPE_STANDARD,
        Achievement.TYPE_INCREMENTAL,});

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
        Log.i("trace", "GamesClient already connecting");
      }
      else if(gamesClient.isConnected())
      {
        Log.i("trace", "GamesClient already connected");
        connectionEstablished("GAMES_CLIENT");
      }

      if(!appStateClient.isConnected() && !appStateClient.isConnected())
      {
        Log.i("trace", "Connecting to AppStateClient");

        GameActivity.getInstance().runOnUiThread(new Runnable(){
          public void run() {
            appStateClient.connect();
          }});
      }
      else if(appStateClient.isConnecting())
      {
        Log.i("trace", "AppStateClient already connecting");
      }
      else if(appStateClient.isConnected())
      {
        Log.i("trace", "AppStateClient already connected");
        connectionEstablished("APP_STATE_CLIENT");
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
        connectionCallback.call("signedOut", new Object[]{"GAMES_CLIENT"});
      }
    }
    if(appStateClient != null && appStateClient.isConnected())
    {
      appStateClient.signOut();
      if(connectionCallback != null)
      {
        connectionCallback.call("signedOut", new Object[] {"APP_STATE_CLIENT"});
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

  public static void submitScore(String leaderboardId, long score)
  {
    try
    {
      if(gamesClient != null && gamesClient.isConnected())
      {
        gamesClient.submitScore(leaderboardId, score);
        Log.i("trace", "Submit score " + score + " to " + leaderboardId);
      }
    }
    catch(Exception e)
    {
      handleException(e, "submitScore");
    }
  }

  public static void showLeaderboard(String leaderboardId)
  {
    try
    {
      if(gamesClient != null && gamesClient.isConnected())
      {
        Intent intent = gamesClient.getLeaderboardIntent(leaderboardId);
        GameActivity.getInstance().startActivityForResult(intent,
            GOOGLE_PLAY_SHOW_LEADERBOARD_REQUEST);

        Log.i("trace", "Starting activity for show leaderboard");
      }
    }
    catch(Exception e)
    {
      handleException(e, "showLeaderboard");
    }
  }

  public static void loadState(int stateKey)
  {
    try
    {
      if(appStateClient != null && appStateClient.isConnected())
      {
        appStateClient.loadState(new GooglePlayCallback("APP_STATE_CLIENT"),
            stateKey);

        Log.i("trace", "Loading state: " + stateKey);
      }
    }
    catch(Exception e)
    {
      handleException(e, "loadState");
    }
  }

  public static void updateState(int stateKey, String data)
  {
    try
    {
      if(appStateClient != null && appStateClient.isConnected())
      {
        appStateClient.updateState(stateKey, data.getBytes());

        Log.i("trace", "Updating state: " + stateKey);
      }
    }
    catch(Exception e)
    {
      handleException(e, "updateState");
    }
  }

  public static void resolveState(int stateKey, String version, String data)
  {
    try
    {
      if(appStateClient != null && appStateClient.isConnected())
      {
        appStateClient.resolveState(new GooglePlayCallback("APP_STATE_CLIENT"),
              stateKey, version, data.getBytes());

        Log.i("trace", "Resolving state: " + stateKey + " version: " + version);
      }
    }
    catch(Exception e)
    {
      handleException(e, "resolveState");
    }
  }

  public static void deleteState(int stateKey)
  {
    try
    {
      if(appStateClient != null && appStateClient.isConnected())
      {
        appStateClient.deleteState(new GooglePlayCallback("APP_STATE_CLIENT"),
            stateKey);

        Log.i("trace", "Deleting state: " + stateKey);
      }
    }
    catch(Exception e)
    {
      handleException(e, "deleteState");
    }
  }

  public static boolean handleActivityResult(int rc, int resultCode,
      Intent data)
  {
    Log.i("trace", "handleActivityResult: " + rc + " " + resultCode);
    if(rc == GOOGLE_PLAY_SIGN_IN_REQUEST)
    {
      connectionEstablished("GAMES_CLIENT");
      return true;
    }
    else if(rc == GOOGLE_PLAY_APP_STATE_SIGN_IN_REQUEST)
    {
      connectionEstablished("APP_STATE_CLIENT");
      return true;
    }
    else if(rc == GOOGLE_PLAY_SHOW_ACHIEVEMENTS_REQUEST)
    {
      Log.i("trace", "Activity for show achievements handled");
      return true;
    }
    else if(rc == GOOGLE_PLAY_SHOW_LEADERBOARD_REQUEST)
    {
      Log.i("trace", "Activity for show leaderboard handled");
      return true;
    }

    return false;
  }
  public static void connectionEstablished(String what)
  {
    Log.d("trace", "Connection established");
    if(connectionCallback == null)
    {
      Log.d("trace", "Connection established, but connection callback is null");
    }
    else
    {
      connectionCallback.call("onSignedIn", new Object[] {what});
    }

    if(what == "GAMES_CLIENT" && gamesClient != null && gamesClient.isConnected())
    {
      gamesClient.loadAchievements(new GooglePlayCallback("GAMES_CLIENT"));
    }
    if(what == "APP_STATE_CLIENT" && appStateClient != null && appStateClient.isConnected())
    {
      appStateClient.listStates(new GooglePlayCallback("APP_STATE_CLIENT"));
    }
  }
}

class GooglePlayCallback implements
      ConnectionCallbacks,
      OnConnectionFailedListener,
      OnAchievementsLoadedListener,
      OnStateListLoadedListener,
      OnStateDeletedListener,
      OnStateLoadedListener
{
  String what;

  GooglePlayCallback(String what)
  {
    super();
    this.what = what;
  }

  public void onConnected(Bundle hint)
  {
    Log.i("trace", what + ": GooglePlayCallback.onConnected");
    GooglePlay.connectionEstablished(what);
  }

  public void onDisconnected()
  {
    Log.i("trace", what + ": GooglePlayCallback.onDisconnected");
    GooglePlay.result = ConnectionResult.SUCCESS;
  }

  public void onConnectionFailed(ConnectionResult result)
  {
    try
    {
      GooglePlay.result = result.getErrorCode();
      if(GooglePlay.result == ConnectionResult.SIGN_IN_REQUIRED)
      {
        Log.i("trace", what + ": GooglePlayCallback: SignIn Required");
        if(what == "GAMES_CLIENT")
        {
          result.startResolutionForResult(GameActivity.getInstance(),
              GooglePlay.GOOGLE_PLAY_SIGN_IN_REQUEST);
        }
        else if(what == "APP_STATE_CLIENT")
        {
          result.startResolutionForResult(GameActivity.getInstance(),
              GooglePlay.GOOGLE_PLAY_APP_STATE_SIGN_IN_REQUEST);
        }
      }
      else
      {
        Log.i("trace", what + ": GooglePlayCallback.onConnectionFailed: " + GooglePlay.result);
      }
    }
    catch(Exception e)
    {
      Log.i("trace", what + ": GooglePlayCallback.onConnectionFailed: " + e.toString());
      if(GooglePlay.connectionCallback != null)
      {
        GooglePlay.connectionCallback.call("onException",
            new Object[] {e.toString(), "onConnectionFailed"});
      }
    }
  }

  public void onAchievementsLoaded(int statusCode, AchievementBuffer buffer)
  {
    Log.i("trace", what + ": GooglePlayCallback.onAchievementsLoaded: " + statusCode);
    for(Achievement a : buffer)
    {
      String id = a.getAchievementId();
      int state = a.getState();
      int type = a.getType();

      GooglePlay.connectionCallback.call("addAchievement",
          new Object[] {id, state, type});
    }

    GooglePlay.connectionCallback.call("onAchievementsLoaded", new Object[]{});
  }

  public void onStateListLoaded(int statusCode, AppStateBuffer buffer)
  {
    Log.i("trace", what + ": GooglePlayCallback.onStateListLoaded: " + statusCode);
    for(AppState s : buffer)
    {
      String version = s.getLocalVersion();
      int key = s.getKey();

      GooglePlay.connectionCallback.call("addAppState",
          new Object[] {key, version});
    }
  }

  public void onStateDeleted(int statusCode, int stateKey)
  {
    Log.i("trace", what + ": GooglePlayCallback.onStateDeleted: " +
        statusCode + " key = " + stateKey);
  }

  public void onStateConflict(int stateKey, String resolvedVersion,
      byte[] localData, byte[] serverData)
  {
    try
    {
      Log.i("trace", what + ": GooglePlayCallback.onStateConflict: key = " +
          stateKey + " version = " + resolvedVersion);

      String localString = new String(localData);
      String serverString = new String(serverData);

      GooglePlay.connectionCallback.call("stateConflict",
          new Object[] {stateKey, resolvedVersion, localString, serverString});
    }
    catch(Exception e)
    {
      GooglePlay.handleException(e, "onStateConflict");
    }
  }

  public void onStateLoaded(int statusCode, int stateKey, byte[] localData)
  {
    try
    {
      Log.i("trace", what + ": GooglePlayCallback.onStateLoaded: " +
          statusCode + " key = " + stateKey);

      if(statusCode == AppStateClient.STATUS_OK)
      {
        String data = new String(localData);
        GooglePlay.connectionCallback.call("onStateLoaded",
            new Object[] {stateKey, data});
      }
      else if(statusCode == AppStateClient.STATUS_NETWORK_ERROR_STALE_DATA)
      {
        Log.i("trace", "Load possible out-of-sync cached data");
        String data = new String(localData);
        GooglePlay.connectionCallback.call("onStateLoaded",
            new Object[] {stateKey, data});
      }
      else if(statusCode == AppStateClient.STATUS_STATE_KEY_NOT_FOUND)
      {
        GooglePlay.connectionCallback.call("onStateNotFound",
            new Object[] {stateKey});
      }
      else
      {
        GooglePlay.appStateClientError(statusCode, "onStateLoaded");
      }
    }
    catch(Exception e)
    {
      GooglePlay.handleException(e, "onStateLoaded");
    }
  }
}
