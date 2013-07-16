package ;

import msignal.Signal;

#if android
import nme.JNI;
typedef GooglePlay = GooglePlayImpl;
#else
typedef GooglePlay = GooglePlayFallback;
#end

class GooglePlayCallback
{
  static var gamesClientErrors : Map<Int, String> = null;
  static var appStateClientErrors : Map<Int, String> = null;

  public var signedIn : Signal1<String>;
  public var stateLoaded : Signal2<Int, String>;
  public var stateNotFound : Signal1<Int>;

  public function new()
  {
    if(gamesClientErrors == null)
    {
      gamesClientErrors = new Map();
    }
    if(appStateClientErrors == null)
    {
      appStateClientErrors = new Map();
    }

    signedIn = new Signal1();
    stateLoaded  = new Signal2();
    stateNotFound = new Signal1();
  }

  public function initGamesClientErrors(DEVELOPER_ERROR : Int,
      INTERNAL_ERROR : Int,
      INVALID_ACCOUNT : Int,
      LICENSE_CHECK_FAILED : Int,
      NETWORK_ERROR : Int,
      RESOLUTION_REQUIRED : Int,
      SERVICE_DISABLED : Int,
      SERVICE_INVALID : Int,
      SERVICE_MISSING : Int,
      SERVICE_VERSION_UPDATE_REQUIRED : Int,
      SIGN_IN_REQUIRED : Int,
      SUCCESS : Int)
  {
    gamesClientErrors = new Map();
    gamesClientErrors.set(DEVELOPER_ERROR, "Developer Error");
    gamesClientErrors.set(INTERNAL_ERROR, "Internal Error");
    gamesClientErrors.set(INVALID_ACCOUNT, "Invalid Account");
    gamesClientErrors.set(LICENSE_CHECK_FAILED, "License Check Failed");
    gamesClientErrors.set(NETWORK_ERROR, "Network Error");
    gamesClientErrors.set(RESOLUTION_REQUIRED, "Resolution Required");
    gamesClientErrors.set(SERVICE_DISABLED, "Service Disabled");
    gamesClientErrors.set(SERVICE_INVALID, "Service Invalid");
    gamesClientErrors.set(SERVICE_MISSING, "Service Missing");
    gamesClientErrors.set(SERVICE_VERSION_UPDATE_REQUIRED, "Service Version Update Required");
    gamesClientErrors.set(SIGN_IN_REQUIRED, "Sign in Required");
    gamesClientErrors.set(SUCCESS, "Success");
  }

  public function initAppStateClientErrors(
        STATUS_CLIENT_RECONNECT_REQUIRED : Int,
        STATUS_DEVELOPER_ERROR : Int,
        STATUS_INTERNAL_ERROR : Int,
        STATUS_NETWORK_ERROR_NO_DATA : Int,
        STATUS_NETWORK_ERROR_OPERATION_DEFERRED : Int,
        STATUS_NETWORK_ERROR_OPERATION_FAILED : Int,
        STATUS_NETWORK_ERROR_STALE_DATA : Int,
        STATUS_OK : Int,
        STATUS_STATE_KEY_LIMIT_EXCEEDED : Int,
        STATUS_STATE_KEY_NOT_FOUND : Int,
        STATUS_WRITE_OUT_OF_DATE_VERSION : Int,
        STATUS_WRITE_SIZE_EXCEEDED : Int)
  {
    appStateClientErrors = new Map();
    appStateClientErrors.set(STATUS_CLIENT_RECONNECT_REQUIRED, "AppState Client Reconnect Required");
    appStateClientErrors.set(STATUS_DEVELOPER_ERROR, "AppState Developer Error");
    appStateClientErrors.set(STATUS_INTERNAL_ERROR, "AppState Internal Error");
    appStateClientErrors.set(STATUS_NETWORK_ERROR_NO_DATA, "AppState Network Error: No Data");
    appStateClientErrors.set(STATUS_NETWORK_ERROR_OPERATION_DEFERRED, "AppState Network Error: Operation Deferred");
    appStateClientErrors.set(STATUS_NETWORK_ERROR_OPERATION_FAILED, "AppState Network Error: Operation Failed");
    appStateClientErrors.set(STATUS_NETWORK_ERROR_STALE_DATA, "AppState Network Error: Stale Data");
    appStateClientErrors.set(STATUS_OK, "AppState Ok");
    appStateClientErrors.set(STATUS_STATE_KEY_LIMIT_EXCEEDED, "AppState Key Limit Exceeded");
    appStateClientErrors.set(STATUS_STATE_KEY_NOT_FOUND, "AppState Key Not Found");
    appStateClientErrors.set(STATUS_WRITE_OUT_OF_DATE_VERSION, "AppState Write Out Of Date Version");
    appStateClientErrors.set(STATUS_WRITE_SIZE_EXCEEDED, "AppState Write Size Exceeded");
  }

  public function onWarning(msg : String, where : String)
  {
    trace([where, msg]);
  }

  public function onError(what : String, code : Int, where : String)
  {
    if(what == "GAMES_CLIENT")
    {
      trace(["Error", where, gamesClientErrors.get(code)]);
    }
    else if(what == "APP_STATE_CLIENT")
    {
      trace(["Error", where, appStateClientErrors.get(code)]);
    }
    else
    {
      trace(["Error from unknown", what, where]);
    }
  }

  public function onException(msg : String, where : String)
  {
    trace(["Exception", where, msg]);
  }

  public function onSignedIn(what : String)
  {
    trace(["SignedIn", what]);
    signedIn.dispatch(what);
  }
  public function signedOut(what : String)
  {
    trace(["SignedOut", what]);
  }

  public function addAppState(key : Int, version : String)
  {
    trace(["app state", key, version]);
  }

  public function onStateLoaded(key : Int, data : String)
  {
    trace(["StateLoaded", key, data]);
    stateLoaded.dispatch(key, data);
  }

  public function onStateNotFound(key : Int)
  {
    trace(["StateNotFound", key]);
    stateNotFound.dispatch(key);
  }

  public function stateConflict(key : Int, resolvedVersion : String,
      localData : String, serverData : String)
  {
    trace(["state conflict", key, resolvedVersion, localData, serverData]);
  }

}

#if android
class GooglePlayImpl extends GooglePlayCallback
{
  static var _signIn : Dynamic = null;
  static var _signOut : Dynamic = null;
  static var _unlockAchievement : Dynamic = null;
  static var _incrementAchievement : Dynamic = null;
  static var _showAchievements : Dynamic = null;
  static var _submitScore : Dynamic = null;
  static var _showLeaderboard : Dynamic = null;

  static var _loadState : Dynamic = null;
  static var _updateState : Dynamic = null;
  static var _resolveState : Dynamic = null;
  static var _deleteState : Dynamic = null;

  var _available : Bool;

  public function new()
  {
    super();

    _available = false;
  }

  public function isAvailable()
  {
    return _available;
  }

  public function signIn()
  {
    if(_signIn == null)
    {
      _signIn = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "signIn", "(Lorg/haxe/nme/HaxeObject;)V");
    }

    _signIn(this);
    _available = true;
  }

  public function signOut()
  {
    if(_signOut == null)
    {
      _signOut = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "signOut", "()V");
    }

    _signOut();
  }

  public function unlockAchievement(achievementId : String)
  {
    if(_unlockAchievement == null)
    {
      _unlockAchievement = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "unlockAchievement", "(Ljava/lang/String;)V");
    }

    _unlockAchievement(achievementId);
  }
  public function incrementAchievement(achievementId : String, steps : Int)
  {
    if(_incrementAchievement == null)
    {
      _incrementAchievement = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "incrementAchievement", "(Ljava/lang/String;I)V");
    }

    _incrementAchievement(achievementId, steps);
  }

  public function showAchievements()
  {
    if(_showAchievements == null)
    {
      _showAchievements = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "showAchievements", "()V");
    }

    _showAchievements();
  }

  public function submitScore(leaderboardId : String, score : Int)
  {
    if(_submitScore == null)
    {
      _submitScore = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "submitScore", "(Ljava/lang/String;J)V");
    }

    _submitScore(leaderboardId, score);
  }

  public function showLeaderboard(leaderboardId : String)
  {
    if(_showLeaderboard == null)
    {
      _showLeaderboard = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "showLeaderboard", "(Ljava/lang/String;)V");
    }

    _showLeaderboard(leaderboardId);
  }

  public function loadState(stateKey : Int)
  {
    if(_loadState == null)
    {
      _loadState = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "loadState", "(I)V");
    }

    _loadState(stateKey);
  }

  public function updateState(stateKey : Int, data : String)
  {
    if(_updateState == null)
    {
      _updateState = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "updateState", "(ILjava/lang/String;)V");
    }

    _updateState(stateKey, data);
  }

  public function resolveState(stateKey : Int, version : String, data : String)
  {
    if(_resolveState == null)
    {
      _resolveState = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "resolveState", "(ILjava/lang/String;Ljava/lang/String;)V");
    }

    _resolveState(stateKey, version, data);
  }

  public function deleteState(stateKey : Int)
  {
    if(_deleteState == null)
    {
      _deleteState = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "deleteState", "(I)V");
    }

    _deleteState(stateKey);
  }
}
#end

class GooglePlayFallback extends GooglePlayCallback
{
  public function new()
  {
    super();
  }

  public function isAvailable()
  {
    return false;
  }

  public function signIn()
  {
    onSignedIn("GAMES_CLIENT");
    onSignedIn("APP_STATE_CLIENT");
  }
  public function signOut()
  {
    trace(["Not implemented", "signOut"]);
  }
  public function unlockAchievement(achievementId : String)
  {
    trace(["Not implemented", "unlockAchievement", achievementId]);
  }
  public function incrementAchievement(achievementId : String, steps : Int)
  {
    trace(["Not implemented", "incrementAchievement", achievementId, steps]);
  }
  public function showAchievements()
  {
    trace(["Not implemented", "showAchievements"]);
  }
  public function submitScore(leaderboardId : String, score : Int)
  {
    trace(["Not implemented", "submitScore", leaderboardId, score]);
  }
  public function showLeaderboard(leaderboardId : String)
  {
    trace(["Not implemented", "showLeaderboard", leaderboardId]);
  }

  public function loadState(stateKey : Int)
  {
    onStateLoaded(stateKey, "");
  }

  public function updateState(stateKey : Int, data : String)
  {
    trace(["Not implemented", "updateState", stateKey, data]);
  }

  public function resolveState(stateKey : Int, version : String, data : String)
  {
    trace(["Not implemented", "resolveState", stateKey, version, data]);
  }

  public function deleteState(stateKey : Int)
  {
    trace(["Not implemented", "deleteState", stateKey]);
  }
}
