package ;

#if android
import nme.JNI;
typedef GooglePlay = GooglePlayImpl;
#else
typedef GooglePlay = GooglePlayFallback;
#end

#if android
class GooglePlayImpl
{
  static var _signIn : Dynamic = null;
  static var _signOut : Dynamic = null;
  static var _unlockAchievement : Dynamic = null;
  static var _incrementAchievement : Dynamic = null;
  static var _showAchievements : Dynamic = null;

  public static function signIn(_callback : GooglePlayConnectionHandler)
  {
    if(_signIn == null)
    {
      _signIn = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "signIn", "(Lorg/haxe/nme/HaxeObject;)V");
    }

    _signIn(_callback);
  }

  public static function signOut()
  {
    if(_signOut == null)
    {
      _signOut = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "signOut", "()V");
    }

    _signOut();
  }

  public static function unlockAchievement(achievementId : String)
  {
    if(_unlockAchievement == null)
    {
      _unlockAchievement = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "unlockAchievement", "(Ljava/lang/String;)V");
    }

    _unlockAchievement(achievementId);
  }
  public static function incrementAchievement(achievementId : String, steps : Int)
  {
    if(_incrementAchievement == null)
    {
      _incrementAchievement = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "incrementAchievement", "(Ljava/lang/String;I)V");
    }

    _incrementAchievement(achievementId, steps);
  }

  public static function showAchievements()
  {
    if(_showAchievements == null)
    {
      _showAchievements = nme.JNI.createStaticMethod("ru/zzzzzzerg/GooglePlay",
          "showAchievements", "()V");
    }

    _showAchievements();
  }
}
#end

class GooglePlayFallback
{
  public static function signIn(_callback : GooglePlayConnectionHandler)
  {
    trace(["Not implemented", "signIn"]);
  }
  public static function signOut()
  {
    trace(["Not implemented", "signOut"]);
  }
  public static function unlockAchievement(achievementId : String)
  {
    trace(["Not implemented", "unlockAchievement", achievementId]);
  }
  public static function incrementAchievement(achievementId : String, steps : Int)
  {
    trace(["Not implemented", "incrementAchievement", achievementId, steps]);
  }
  public static function showAchievements()
  {
    trace(["Not implemented", "showAchievements"]);
  }
}

class GooglePlayCallback
{
  static var errors : Map<Int, String> = null;

  public function new()
  {
    if(errors == null)
    {
      errors = new Map();
    }
  }

  public function initErrorTable(DEVELOPER_ERROR : Int,
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
    errors = new Map();
    errors.set(DEVELOPER_ERROR, "Developer Error");
    errors.set(INTERNAL_ERROR, "Internal Error");
    errors.set(INVALID_ACCOUNT, "Invalid Account");
    errors.set(LICENSE_CHECK_FAILED, "License Check Failed");
    errors.set(NETWORK_ERROR, "Network Error");
    errors.set(RESOLUTION_REQUIRED, "Resolution Required");
    errors.set(SERVICE_DISABLED, "Service Disabled");
    errors.set(SERVICE_INVALID, "Service Invalid");
    errors.set(SERVICE_MISSING, "Service Missing");
    errors.set(SERVICE_VERSION_UPDATE_REQUIRED, "Service Version Update Required");
    errors.set(SIGN_IN_REQUIRED, "Sign in Required");
    errors.set(SUCCESS, "Success");
  }

  public function onWarning(msg : String, where : String)
  {
    trace([where, msg]);
  }
  public function onException(msg : String, where : String)
  {
    trace(["Exception", where, msg]);
  }
}

class GooglePlayConnectionHandler extends GooglePlayCallback
{
  public function new()
  {
    super();
  }

  public function signedIn()
  {
    trace(["SignedIn"]);
  }
  public function signedOut()
  {
    trace(["SignedOut"]);
  }
}
