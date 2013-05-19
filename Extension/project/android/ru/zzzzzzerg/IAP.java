package ru.zzzzzzerg;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.util.Base64;
import android.text.TextUtils;
import android.content.IntentSender.SendIntentException;

import com.android.vending.billing.IInAppBillingService;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import java.util.ArrayList;

import org.haxe.nme.HaxeObject;
import org.haxe.nme.GameActivity;

public class IAP
{
  // Billing response codes
  public static final int BILLING_RESPONSE_RESULT_OK = 0;
  public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
  public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
  public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
  public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
  public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
  public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
  public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

  // IAB Helper error codes
  public static final int IABHELPER_ERROR_BASE = -1000;
  public static final int IABHELPER_REMOTE_EXCEPTION = -1001;
  public static final int IABHELPER_BAD_RESPONSE = -1002;
  public static final int IABHELPER_VERIFICATION_FAILED = -1003;
  public static final int IABHELPER_SEND_INTENT_FAILED = -1004;
  public static final int IABHELPER_USER_CANCELLED = -1005;
  public static final int IABHELPER_UNKNOWN_PURCHASE_RESPONSE = -1006;
  public static final int IABHELPER_MISSING_TOKEN = -1007;
  public static final int IABHELPER_UNKNOWN_ERROR = -1008;
  public static final int IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE = -1009;
  public static final int IABHELPER_INVALID_CONSUMPTION = -1010;

  // Keys for the responses from InAppBillingService
  public static final String RESPONSE_CODE = "RESPONSE_CODE";
  public static final String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
  public static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
  public static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
  public static final String RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
  public static final String RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
  public static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
  public static final String RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
  public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

  // Item types
  public static final String ITEM_TYPE_INAPP = "inapp";
  public static final String ITEM_TYPE_SUBS = "subs";

  // some fields on the getSkuDetails response bundle
  public static final String GET_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";
  public static final String GET_SKU_DETAILS_ITEM_TYPE_LIST = "ITEM_TYPE_LIST";

  private static final String KEY_FACTORY_ALGORITHM = "RSA";
  private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

  private static String tag = "IAP:ru/zzzzzzerg";
  private static String license = "";

  public static IInAppBillingService iapService;
  public static ServiceConnection iapServiceConnection;
  public static boolean setupDone;
  public static HaxeObject buyCallback;
  public static int requestCode;

  public static String packageName;

  public static void createService(Context ctx, String licensePublicKey)
  {
    iapService = null;
    iapServiceConnection = null;
    packageName = ctx.getPackageName();
    license = licensePublicKey;
    setupDone = false;
    buyCallback = null;

    iapServiceConnection = new ServiceConnection()
    {
      @Override
      public void onServiceDisconnected(ComponentName name)
      {
        iapService = null;
        Log.i(tag, "Billing service disconnected");
      }

      @Override
      public void onServiceConnected(ComponentName name, IBinder service)
      {
        iapService = IInAppBillingService.Stub.asInterface(service);
        Log.i(tag, "Billing service connected");

        setupDone = true;

        try
        {
          int response = iapService.isBillingSupported(3, packageName,
              ITEM_TYPE_INAPP);
          if(response != BILLING_RESPONSE_RESULT_OK)
          {
            Log.i(tag, "Billing is not supported: " + response);
            iapService = null;
          }
          else
          {
            Log.i(tag, "Billing supported");
          }
        }
        catch(RemoteException e)
        {
          e.printStackTrace();

          iapService = null;
        }
      }
    };

    Log.d(tag, "Create service intent");
    Intent serviceIntent = new Intent(
        "com.android.vending.billing.InAppBillingService.BIND");

    if(!ctx.getPackageManager().queryIntentServices(serviceIntent, 0).isEmpty())
    {
      Log.d(tag, "Bind service");
      ctx.bindService(serviceIntent,
          iapServiceConnection,
          Context.BIND_AUTO_CREATE);
    }
    else
    {
      Log.i(tag, "No service for intent");
      setupDone = true;
    }
  }

  public static void destroyService(Context ctx)
  {
    if(iapServiceConnection != null)
    {
      Log.d(tag, "Unbind service");
      ctx.unbindService(iapServiceConnection);
      iapServiceConnection = null;
    }
  }

  public static void getItems(HaxeObject callback)
  {
    try
    {
      if(!setupDone || iapService == null)
      {
        callback.call("onWarning", new Object[] {"Service is not ready", "getItems"});
        return;
      }

      Log.d(tag, "binded service");

      ArrayList skuList = new ArrayList();
      skuList.add("android.test.purchased");
      skuList.add("android.test.canceled");
      skuList.add("android.test.refunded");
      skuList.add("android.test.item_unavailable");

      Log.d(tag, "create query bundle");
      Bundle query = new Bundle();
      query.putStringArrayList("ITEM_ID_LIST", skuList);

      Log.d(tag, "get sku details: " + packageName);
      Bundle details = iapService.getSkuDetails(3, packageName,
          ITEM_TYPE_INAPP, query);
      if(!details.containsKey(RESPONSE_GET_SKU_DETAILS_LIST))
      {
        int response = getResponseCode(details);
        if(response != BILLING_RESPONSE_RESULT_OK)
        {
          callback.call("onError", new Object[] {response, "getItems"});
        }
        else
        {
          callback.call("onWarning", new Object[] {"No detail list", "getItems"});
        }

        return;
      }

      Log.d(tag, "got sku details");
      ArrayList<String> res = details.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST);
      for(String r : res)
      {
        callback.call("addProduct", new Object[] {r});
      }
    }
    catch(RemoteException e)
    {
      callback.call("onException", new Object[] {e.toString(), "getItems"});
    }
    finally
    {
      callback.call("finish", new Object[] {});
    }
  }

  public static void getPurchases(HaxeObject callback)
  {
    if(!setupDone || iapService == null)
    {
      callback.call("onWarning", new Object[] {"Service is not ready", "getPurchases"});
      return;
    }

    try
    {
      String continueToken = null;
      do
      {
        Bundle items = iapService.getPurchases(3, packageName, ITEM_TYPE_INAPP,
            continueToken);
        int response = getResponseCode(items);
        if(response != BILLING_RESPONSE_RESULT_OK)
        {
          callback.call("onError", new Object[] {response, "getPurchases"});
          return;
        }

        if(!items.containsKey(RESPONSE_INAPP_ITEM_LIST)
            || !items.containsKey(RESPONSE_INAPP_PURCHASE_DATA_LIST)
            || !items.containsKey(RESPONSE_INAPP_SIGNATURE_LIST))
        {
          callback.call("onWarning", new Object[] {"Bundle doesn't contains required fields",
            "getPurchases"});
          return;
        }

        ArrayList<String> skus = items.getStringArrayList(RESPONSE_INAPP_ITEM_LIST);
        ArrayList<String> purchases = items.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
        ArrayList<String> signatures = items.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST);

        for(int i = 0, cnt = purchases.size(); i < cnt; ++i)
        {
          String data = purchases.get(i);
          String sku = skus.get(i);
          String signature = signatures.get(i);

          if(verify(license, data, signature, callback))
          {
            callback.call("addPurchase", new Object[] {data});
          }
          else
          {
            callback.call("onWarning",
                new Object[] {"Purchase signature verification failed", "getPurchases"});
          }
        }

        continueToken = items.getString(INAPP_CONTINUATION_TOKEN);
      }
      while(!TextUtils.isEmpty(continueToken));
    }
    catch(Exception e)
    {
      callback.call("onException", new Object[] {e.toString(), "getPurchases"});
    }
    finally
    {
      callback.call("finish", new Object[]{});
    }
  }

  public static void consumeItem(String sku, String token, HaxeObject callback)
  {
    try
    {
      if(!setupDone || iapService == null)
      {
        callback.call("onWarning", new Object[] {"Service is not ready", "consumeItem"});
        return;
      }

      int response = iapService.consumePurchase(3, packageName, token);
      if(response == BILLING_RESPONSE_RESULT_OK)
      {
        callback.call("consumed", new Object[] {sku});
      }
      else
      {
        callback.call("onError", new Object[] {response, "consumeItem"});
      }
    }
    catch(RemoteException e)
    {
      callback.call("onException", new Object[] {e.toString(), "consumeItem"});
    }
  }

  public static void purchaseItem(String sku, int rc,
      HaxeObject callback)
  {
    try
    {
      if(!setupDone || iapService == null)
      {
        callback.call("onWarning", new Object[] {"Service is not ready", "purchaseItem"});
        return;
      }

      if(buyCallback != null)
      {
        callback.call("onWarning", new Object[] {"Purchase item is not end", "purchaseItem"});
        return;
      }

      Log.d(tag, "getBuyIntent for " + sku);
      Bundle bundle = iapService.getBuyIntent(3, packageName, sku,
          ITEM_TYPE_INAPP, "");
      int response = getResponseCode(bundle);
      if(response != BILLING_RESPONSE_RESULT_OK)
      {
        callback.call("onError", new Object[] {response, "purchaseItem"});

        return;
      }

      buyCallback = callback;
      requestCode = rc;

      PendingIntent intent = bundle.getParcelable(RESPONSE_BUY_INTENT);
      Log.d(tag, "Start intent for " + sku + " with request code " + requestCode);

      Activity activity = GameActivity.getInstance();
      activity.startIntentSenderForResult(intent.getIntentSender(),
          requestCode, new Intent(),
          Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
      Log.d(tag, "Intent for " + sku + " with request code " +
          requestCode + "started");
    }
    catch(Exception e)
    {
      callback.call("onException", new Object[] {e.toString(), "purchaseItem"});
      if(buyCallback != null)
      {
        buyCallback.call("finish", new Object[]{});
      }
      buyCallback = null;
    }
  }

  public static boolean handleActivityResult(int rc, int resultCode,
      Intent data)
  {
    Log.d(tag, "handleActivityResult");
    if(rc != requestCode)
    {
      return false;
    }

    if(buyCallback == null)
    {
      return false;
    }

    try
    {
      if(data == null)
      {
        String msg = "Null data for purchase activity result";
        buyCallback.call("onWarning", new Object[] {msg, "handleActivityResult"});
        return true;
      }

      int response = getResponseCode(data.getExtras());
      if(resultCode == Activity.RESULT_OK && response == BILLING_RESPONSE_RESULT_OK)
      {
        String purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
        String signature = data.getStringExtra(RESPONSE_INAPP_SIGNATURE);

        if(purchaseData == null || signature == null)
        {
          String d = data.getExtras().toString();
          buyCallback.call("onWarning",
              new Object[] {"Data or Signature is null: " + d,
              "handleActivityResult"});
        }
        else if(verify(license, purchaseData, signature, buyCallback))
        {
          buyCallback.call("purchased", new Object[] {purchaseData});
        }
      }
      else if(resultCode == Activity.RESULT_OK)
      {
        buyCallback.call("onError", new Object[] {response, "handleActivityResult"});
      }
      else if(resultCode == Activity.RESULT_CANCELED)
      {
        buyCallback.call("canceled", new Object[] {response});
      }
      else
      {
        buyCallback.call("onError", new Object[] {resultCode, "handleActivityResult"});
      }

      return true;
    }
    finally
    {
      if(buyCallback != null)
      {
        buyCallback.call("finish", new Object[]{});
      }
      buyCallback = null;
    }
  }

  static boolean verify(String publicKey, String data, String signature,
      HaxeObject callback)
  {
    if(!TextUtils.isEmpty(signature))
    {
      try
      {
        byte[] decodedKey = Base64.decode(publicKey, Base64.DEFAULT);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));

        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(key);
        sig.update(data.getBytes());

        if(!sig.verify(Base64.decode(signature, Base64.DEFAULT)))
        {
          callback.call("onWarning",
              new Object[] {"Signature verification failed", "verify"});
          return false;
        }
      }
      catch(Exception e)
      {
        callback.call("onException",
            new Object[] {e.toString(), "verify"});
        return false;
      }
    }

    return true;
  }

  // Workaround to bug where sometimes response codes come as Long instead of Integer
  static int getResponseCode(Bundle b)
  {
    Object o = b.get(RESPONSE_CODE);
    if (o == null)
    {
      Log.d(tag, "Bundle with null response code, assuming OK (known issue)");
      return BILLING_RESPONSE_RESULT_OK;
    }
    else if (o instanceof Integer)
    {
      return ((Integer)o).intValue();
    }
    else if (o instanceof Long)
    {
      return (int)((Long)o).longValue();
    }
    else
    {
      Log.d(tag, "Unexpected type for bundle response code.");
      Log.d(tag, o.getClass().getName());
      return BILLING_RESPONSE_RESULT_ERROR;
    }
  }

}
