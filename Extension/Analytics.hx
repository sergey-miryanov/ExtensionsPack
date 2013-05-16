package ;


#if android
import nme.JNI;
typedef Analytics = AnalyticsAndroid;
#else
typedef Analytics = AnalyticsImpl;
#end

class AnalyticsImpl
{
  public static function logEvent(eventId : String, params : Dynamic = null)
  {
    trace(["logEvent", eventId, params]);
  }
  public static function onPageView():Void
  {
    trace(["onPageView"]);
  }
  public static function setLogEnabled(logEnabled : Bool)
  {
    trace(["setLogEnabled"]);
  }
}

#if android
class AnalyticsAndroid
{
  private static var _logEvent : Dynamic = null;
  private static var _logEventMap : Dynamic = null;
  private static var _setLogEnabled : Dynamic = null;
  private static var _onPageView : Dynamic = null;

  public static function logEvent(eventId : String, params : Dynamic = null)
  {
    if(_logEvent == null)
    {
      _logEvent = nme.JNI.createStaticMethod("ru/zzzzzzerg/Analytics",
          "logEvent",
          "(Ljava/lang/String;)V");
    }
    if(_logEventMap == null)
    {
      _logEventMap = nme.JNI.createStaticMethod("ru/zzzzzzerg/Analytics",
          "logEvent",
          "(Ljava/lang/String;Ljava/lang/String;)V");
    }

    if(params == null)
    {
      trace(["logEvent", eventId]);
      _logEvent(eventId);
    }
    else
    {
      trace(["logEventMap", eventId, params]);
      _logEventMap(eventId, Std.string(params));
    }
  }

  public static function onPageView():Void
  {
    if (_onPageView == null)
    {
      _onPageView = nme.JNI.createStaticMethod("ru/zzzzzzerg/Analytics",
          "onPageView", "()V");
    }

    _onPageView();
  }


  public static function setLogEnabled(logEnabled : Bool)
  {
    if(_setLogEnabled == null)
    {
      _setLogEnabled = nme.JNI.createStaticMethod("ru/zzzzzzerg/Analytics",
          "setDebugEnabled", "(Z)V",
          true);
    }

    var params = new Array<Dynamic>();
    params.push(logEnabled);

    _setLogEnabled(params);
  }
}
#end
