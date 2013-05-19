package ;

import org.flixel.FlxState;
import org.flixel.FlxSprite;
import org.flixel.FlxG;
import org.flixel.tweens.FlxTween;
import org.flixel.tweens.misc.VarTween;
import org.flixel.tweens.util.Ease;

import nme.Lib;

import IAP;

class ExampleState extends FlxState
{
  var _getItems : FlxSprite;
  var _purchaseItem : FlxSprite;
  var _consumeItem : FlxSprite;
  var _getPurchases : FlxSprite;
  var _purchaseCanceledItem : FlxSprite;
  var _purchaseRefundedItem : FlxSprite;
  var _purchaseUnavailableItem : FlxSprite;

  var _getItemsHandler : ProductList;
  var _purchaseItemHandler : PurchaseItem;
  var _purchaseCanceledItemHandler : PurchaseItem;
  var _purchaseRefundedItemHandler : PurchaseItem;
  var _purchaseUnavailableItemHandler : PurchaseItem;

  var _flash : FlxSprite;

  override public function create() : Void
  {
    _getItemsHandler = null;
    _purchaseItemHandler = null;
    _purchaseCanceledItemHandler = null;

    _getItems = new FlxSprite(50, 50);
    _getItems.makeGraphic(50, 50, 0xffff7700);

    _purchaseItem = new FlxSprite(150, 150);
    _purchaseItem.makeGraphic(50, 50, 0xffff0077);

    _consumeItem = new FlxSprite(220, 150);
    _consumeItem.makeGraphic(50, 50, 0xff00ff77);

    _getPurchases = new FlxSprite(150, 220);
    _getPurchases.makeGraphic(50, 50, 0xff0077ff);

    _purchaseCanceledItem = new FlxSprite(150, 300);
    _purchaseCanceledItem.makeGraphic(50, 50, 0xffff4499);

    _purchaseRefundedItem = new FlxSprite(220, 300);
    _purchaseRefundedItem.makeGraphic(50, 50, 0xff9944ff);

    _purchaseUnavailableItem = new FlxSprite(290, 300);
    _purchaseUnavailableItem.makeGraphic(50, 50, 0xff44ff99);

    _flash = new FlxSprite(350, 50);
    _flash.makeGraphic(50, 50, 0xff7700ff);

    var tw2 = new VarTween (null, FlxTween.PINGPONG);
    tw2.tween (_flash, "alpha", 0.4, 0.6, Ease.quadIn);
    addTween(tw2);


    add(_getItems);
    add(_purchaseItem);
    add(_consumeItem);
    add(_getPurchases);
    add(_purchaseCanceledItem);
    add(_purchaseRefundedItem);
    add(_purchaseUnavailableItem);

    add(_flash);

    FlxG.mouse.transparentMouse();
  }

  override public function update() : Void
  {
    super.update();
    if(FlxG.keys.justReleased("ESCAPE"))
    {
      Lib.exit();
      return;
    }

    if(FlxG.mouse.justReleased())
    {
      if(_getItems.overlapsPoint(FlxG.mouse))
      {
        trace("getItems->");
        var getItems = new ProductList();
        IAP.getItems(getItems);
        trace("<-getItems");
      }
      else if(_purchaseItem.overlapsPoint(FlxG.mouse))
      {
        trace("purchaseItem->");
        _purchaseItemHandler = new PurchaseItem("android.test.purchased",
            _flash);
        IAP.purchaseItem(_purchaseItemHandler);
        trace("<-purchaseItem");
      }
      else if(_consumeItem.overlapsPoint(FlxG.mouse))
      {
        trace("consumeAll->");
        var consumeAll = new ConsumeAll();
        IAP.getPurchases(consumeAll);
        trace("<-consumeAll");
      }
      else if(_getPurchases.overlapsPoint(FlxG.mouse))
      {
        trace("getPurchases->");
        var getPurchases = new PurchasesList();
        IAP.getPurchases(getPurchases);
        trace("<-getPurchases");
      }
      else if(_purchaseCanceledItem.overlapsPoint(FlxG.mouse))
      {
        trace("purchaseCanceledItem->");
        _purchaseCanceledItemHandler = new PurchaseItem("android.item.canceled",
            _flash);
        IAP.purchaseItem(_purchaseCanceledItemHandler);
        trace("<-purchaseCanceledItem");
      }
      else if(_purchaseRefundedItem.overlapsPoint(FlxG.mouse))
      {
        trace("purchaseRefundedItem->");
        _purchaseRefundedItemHandler = new PurchaseItem("android.item.refunded",
            _flash);
        IAP.purchaseItem(_purchaseRefundedItemHandler);
        trace("<-purchaseRefundedItem");
      }
      else if(_purchaseUnavailableItem.overlapsPoint(FlxG.mouse))
      {
        trace("purchaseUnavailableItem->");
        _purchaseUnavailableItemHandler = new PurchaseItem("android.item.item_unavailable",
            _flash);
        IAP.purchaseItem(_purchaseUnavailableItemHandler);
        trace("<-purchaseUnavailableItem");
      }
    }
  }
}

class ProductList extends ProductListBase
{
  public function new()
  {
    super();
  }

  override public function finish()
  {
    trace("Print Products List");
    for (p in products)
    {
      trace([p]);
    }
  }
}

class PurchasesList extends PurchasesListBase
{
  public function new()
  {
    super();
  }

  override public function finish()
  {
    trace("Print Purchases List");
    for (p in items)
    {
      trace([p]);
    }
  }
}

class ConsumeAll extends PurchasesListBase
{
  public function new()
  {
    super();
  }

  override public function finish()
  {
    var item = items.shift();
    var consumeItems = new ConsumeItem(item, items);
    IAP.consumeItem(consumeItems);
  }
}

class ConsumeItem extends PurchaseBase
{
  var _items : Array<PurchaseInfo>;
  public function new(itemToConsume, items)
  {
    super(itemToConsume.productId);
    token = itemToConsume.purchaseToken;
    item = itemToConsume;

    _items = items;
  }

  override public function finish()
  {
    var item = _items.shift();
    var consumeItems = new ConsumeItem(item, _items);
    IAP.consumeItem(consumeItems);
  }
}

class PurchaseItem extends PurchaseBase
{
  var _flash : FlxSprite;
  public function new(sku : String, flash : FlxSprite)
  {
    super(sku);

    _flash = flash;
  }

  override public function purchased(jsonString : String)
  {
    super.purchased(jsonString);
    trace(item);

    _flash.makeGraphic(50, 50, 0xffff0044);
  }

  override public function consumed(consumedSku : String)
  {
    super.consumed(consumedSku);

    _flash.makeGraphic(50, 50, 0xff4400ff);
  }

  override public function finish()
  {
    trace("Purchase of item " + sku + " finished");
  }

  override public function onError(response: Int, where : String)
  {
    trace([response, IAPErrorMessage.get(response), where, item]);
  }
}
