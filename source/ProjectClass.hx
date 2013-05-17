package;

import nme.Lib;
import org.flixel.FlxGame;

class ProjectClass extends FlxGame
{
  public function new()
  {
    var stageWidth:Int = Lib.current.stage.stageWidth;
    var stageHeight:Int = Lib.current.stage.stageHeight;

    var ratio : Float = 1.0;
    var w = Std.int (stageWidth / ratio);
    var h = Std.int (stageHeight / ratio);

    super(w, h, ExampleState, ratio);
  }
}
