package ru.zzzzzzerg;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import ru.zzzzzzerg.IAP;
import ru.zzzzzzerg.Analytics;

public class MyActivity extends org.haxe.nme.GameActivity
{

  protected void onCreate(Bundle state)
  {
    super.onCreate(state);

    Analytics.start(getApplicationContext(),
        "FLURRY_APPLICATION_KEY",
        "LOCALYTICS_APPLICATION_KEY");

    String license = "YOU_BASE_64_PUBLIC_KEY";
    IAP.createService(this, license);
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    IAP.destroyService(this);
  }

  @Override
  protected void onStop()
  {
    Analytics.stop(this);
    super.onStop();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    Analytics.resume();
  }

  @Override
  protected void onPause()
  {
    Analytics.pause();
    super.onPause();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if(!IAP.handleActivityResult(requestCode, resultCode, data))
    {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
