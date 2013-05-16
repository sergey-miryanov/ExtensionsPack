package ;

import haxe.Json;

#if android
import nme.JNI;
typedef IAP = IAPAndroid;
#else
typedef IAP = IAPCpp;
#end

#if android
class IAPAndroid
{
  private static var _getItems : Dynamic = null;
  private static var _purchaseItem : Dynamic = null;
  private static var _consumeItem : Dynamic = null;
  private static var _getPurchases : Dynamic = null;

  public static function getItems(productList : ProductListBase)
  {
    if(_getItems == null)
    {
      _getItems = nme.JNI.createStaticMethod("ru/zzzzzzerg/IAP",
          "getItems", "(Lorg/haxe/nme/HaxeObject;)V");
    }

    _getItems(productList);
  }

  public static function purchaseItem(purchase : PurchaseBase)
  {
    if(_purchaseItem == null)
    {
      _purchaseItem = nme.JNI.createStaticMethod("ru/zzzzzzerg/IAP",
          "purchaseItem", "(Ljava/lang/String;ILorg/haxe/nme/HaxeObject;)V");
    }

    _purchaseItem(purchase.sku, purchase.getRequestCode(),
        purchase);
  }

  public static function consumeItem(purchase : PurchaseBase)
  {
    if(_consumeItem == null)
    {
      _consumeItem = nme.JNI.createStaticMethod("ru/zzzzzzerg/IAP",
          "consumeItem", "(Ljava/lang/String;Ljava/lang/String;Lorg/haxe/nme/HaxeObject;)V");
    }

    if(purchase.token != null)
      _consumeItem(purchase.sku, purchase.token, purchase);
    else
    {
      trace(["FIXME: purchase token for consume item is null",
          purchase.sku, purchase.item]);
    }
  }

  public static function getPurchases(productList : ProductListBase)
  {
    if(_getPurchases == null)
    {
      _getPurchases = nme.JNI.createStaticMethod("ru/zzzzzzerg/IAP",
          "getPurchases", "(Lorg/haxe/nme/HaxeObject;)V");
    }

    _getPurchases(productList);
  }
}
#end

class IAPCpp
{
  public static function getItems()
  {
    trace(["getItems"]);
  }
}

typedef ProductStruct = {
  title : String,
  price : String,
  type : String,
  description : String,
  productId : String,
};

typedef PurchaseStruct = {
  developerPayload : String,
  orderId : String,
  productId : String,
  purchaseToken : String,
  purchaseTime : String,
  purchaseState : String,
  packageName : String,
};

class Callback
{
  public function new()
  {
  }

  public function onError(response : Int, where : String)
  {
    trace(["onError", response, where, IAPErrorMessage.get(response)]);
  }

  public function onWarning(msg : String, where : String)
  {
    trace(["onWarning", where, msg]);
  }

  public function onException(msg : String, where : String)
  {
    trace(["onException", where, msg]);
  }
}

class ProductListBase extends Callback
{
  public var _products : Array<ProductStruct>;

  public function new()
  {
    super();

    _products = new Array();
  }

  public function addProduct(jsonString : String)
  {
    var p : ProductStruct = Json.parse(jsonString);
    _products.push(p);
  }

  public function finish()
  {
    trace(["finish", "implement me"]);
  }
}

class PurchaseBase extends Callback
{
  public var sku : String;
  public var token : String;
  public var item : PurchaseStruct;

  public function new(sku : String)
  {
    super();

    this.sku = sku;
    this.token = null;
  }

  public function getRequestCode() : Int
  {
    return 110100;
  }

  public function purchased(jsonString : String)
  {
    item = Json.parse(jsonString);
    token = item.purchaseToken;
  }

  public function canceled(response : Int, msg : String)
  {
    trace(["canceled", response, msg]);
  }

  public function consumed(consumedSku : String)
  {
    if(sku != consumedSku)
    {
      trace(["consumed SKU mismatch stored SKU"]);
    }
  }

  public function finish()
  {
    trace(["finish", "implement me"]);
  }
}


class IAPErrorMessage
{
  static var errors : Map<Int, String> = null;

  public static function get(error : Int) : String
  {
    initErrors();

    if(errors.exists(error))
      return Std.string(error) + ":" + errors.get(error);
    else
      return Std.string(error) + ":Unknown";
  }

  public static function initErrors()
  {
    if(errors == null)
    {
      errors = new Map();

      errors.set(0, "OK");
      errors.set(1, "User cancel");
      errors.set(2, "Unknown");
      errors.set(3, "Billing Unavailable");
      errors.set(4, "Item Unavailable");
      errors.set(5, "Developer Error");
      errors.set(6, "Error");
      errors.set(7, "Item Already Owned");
      errors.set(8, "Item Not Owned");

      errors.set(-1001, "Remote exception during initialization");
      errors.set(-1002, "Bad response received");
      errors.set(-1003, "Purchase signature verification failed");
      errors.set(-1004, "Send intent failed");
      errors.set(-1005, "User cancelled");
      errors.set(-1006, "Unknown purchase response");
      errors.set(-1007, "Missing token");
      errors.set(-1008, "Unknown error");
      errors.set(-1009, "Subscriptions not available");
      errors.set(-1010, "Invalid consumption attempt");
    }
  }
}

