package ;

import org.flixel.FlxState;
import org.flixel.FlxSprite;
import org.flixel.FlxG;

import nme.Lib;

import GooglePlay;

class TestGooglePlayState extends FlxState
{
  var _signIn : FlxSprite;
  var _signOut : FlxSprite;

  var _unlockAchievement1 : FlxSprite;
  var _unlockAchievement2 : FlxSprite;
  var _unlockAchievement3 : FlxSprite;
  var _unlockAchievement4 : FlxSprite;
  var _unlockAchievement5 : FlxSprite;

  var _showAchievements : FlxSprite;

  var _submitScore : FlxSprite;
  var _showLeaderboard : FlxSprite;

  var _updateState : FlxSprite;
  var _loadStae : FlxSprite;
  var _deleteState : FlxSprite;

  static var achievement_first_farm : String = "YOUR_ACHIEVEMENT_ID_1";
  static var achievement_cabbage_master : String = "YOUR_ACHIEVEMENT_ID_2";
  static var achievement_kill_rabbits : String = "YOUR_ACHIEVEMENT_ID_3";
  static var achievement_soul_keeper : String = "YOUR_ACHIEVEMENT_ID_4";
  static var achievement_garden_defender : String = "YOUR_ACHIEVEMENT_ID_5";

  static var leaderboard : String = "YOUR_LEADERBOARD_ID";

  static var stateKey : Int = 1;
  static var exampleState : String = "12";

  var googlePlay : GooglePlayHandler = null;

  override public function create() : Void
  {
    super.create();
    FlxG.mouse.show();

    _signIn = new FlxSprite(50, 50);
    _signIn.makeGraphic(50, 50, 0xffff0077);

    _signOut = new FlxSprite(120, 50);
    _signOut.makeGraphic(50, 50, 0xffff4400);

    _unlockAchievement1 = new FlxSprite(50, 190);
    _unlockAchievement1.makeGraphic(50, 50, 0xff22ff22);
    _unlockAchievement2 = new FlxSprite(120, 190);
    _unlockAchievement2.makeGraphic(50, 50, 0xff22ff22);
    _unlockAchievement3 = new FlxSprite(190, 190);
    _unlockAchievement3.makeGraphic(50, 50, 0xff22ff22);
    _unlockAchievement4 = new FlxSprite(260, 190);
    _unlockAchievement4.makeGraphic(50, 50, 0xff22ff22);
    _unlockAchievement5 = new FlxSprite(330, 190);
    _unlockAchievement5.makeGraphic(50, 50, 0xff22ff22);

    _showAchievements = new FlxSprite(260, 50);
    _showAchievements.makeGraphic(50, 50, 0xffff0099);

    _submitScore = new FlxSprite(50, 260);
    _submitScore.makeGraphic(50, 50, 0xff9922ff);
    _showLeaderboard = new FlxSprite(120, 260);
    _showLeaderboard.makeGraphic(50, 50, 0xff9922ff);

    _updateState = new FlxSprite(50, 330);
    _updateState.makeGraphic(50, 50, 0xff6633dd);
    _loadStae = new FlxSprite(120, 330);
    _loadStae.makeGraphic(50, 50, 0xff3366dd);
    _deleteState = new FlxSprite(190, 330);
    _deleteState.makeGraphic(50, 50, 0xffdd6633);

    googlePlay = new GooglePlayHandler(_signIn);

    add(_signIn);
    add(_signOut);

    add(_unlockAchievement1);
    add(_unlockAchievement2);
    add(_unlockAchievement3);
    add(_unlockAchievement4);
    add(_unlockAchievement5);

    add(_showAchievements);

    add(_submitScore);
    add(_showLeaderboard);

    add(_updateState);
    add(_loadStae);
    add(_deleteState);
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
      if(_signIn.overlapsPoint(FlxG.mouse))
      {
        googlePlay.signIn();
      }
      else if(_signOut.overlapsPoint(FlxG.mouse))
      {
        googlePlay.signOut();
      }
      else if(_unlockAchievement1.overlapsPoint(FlxG.mouse))
      {
        googlePlay.incrementAchievement(achievement_first_farm, 1);
      }
      else if(_unlockAchievement2.overlapsPoint(FlxG.mouse))
      {
        googlePlay.incrementAchievement(achievement_cabbage_master, 1);
      }
      else if(_unlockAchievement3.overlapsPoint(FlxG.mouse))
      {
        googlePlay.unlockAchievement(achievement_kill_rabbits);
      }
      else if(_unlockAchievement4.overlapsPoint(FlxG.mouse))
      {
        googlePlay.unlockAchievement(achievement_soul_keeper);
      }
      else if(_unlockAchievement5.overlapsPoint(FlxG.mouse))
      {
        googlePlay.unlockAchievement(achievement_garden_defender);
      }
      else if(_showAchievements.overlapsPoint(FlxG.mouse))
      {
        googlePlay.showAchievements();
      }
      else if(_submitScore.overlapsPoint(FlxG.mouse))
      {
        googlePlay.submitScore(leaderboard, Std.random(100000));
      }
      else if(_showLeaderboard.overlapsPoint(FlxG.mouse))
      {
        googlePlay.showLeaderboard(leaderboard);
      }
      else if(_updateState.overlapsPoint(FlxG.mouse))
      {
        googlePlay.updateState(stateKey, exampleState);
      }
      else if(_loadStae.overlapsPoint(FlxG.mouse))
      {
        googlePlay.loadState(stateKey);
      }
      else if(_deleteState.overlapsPoint(FlxG.mouse))
      {
        googlePlay.deleteState(stateKey);
      }
    }
  }
}

class GooglePlayHandler extends GooglePlay
{
  var s : FlxSprite;

  public function new(s : FlxSprite)
  {
    super();
    this.s = s;
  }

  override public function signedIn(what : String)
  {
    super.signedIn(what);
    s.makeGraphic(50, 50, 0xff0044ff);
  }
  override public function signedOut(what : String)
  {
    super.signedOut(what);
    s.makeGraphic(50, 50, 0xffff0077);
  }
}
