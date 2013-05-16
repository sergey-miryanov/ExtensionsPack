package ;

import nme.JNI;

class InternalStorage
{
  private static var jniReadStorage : Dynamic = null;
  private static var jniWriteStorage : Dynamic = null;

  public static function readStorage (filename : String) : String
  {
    if (jniReadStorage == null)
    {
      jniReadStorage = JNI.createStaticMethod ("ru/zzzzzzerg/InternalStorage",
          "readStorage",
          "(Ljava/lang/String;)Ljava/lang/String;");
    }

    return jniReadStorage (filename);
  }

  public static function writeStorage (filename : String, data : String) : Void
  {
    if (jniWriteStorage == null)
    {
      jniWriteStorage = JNI.createStaticMethod ("ru/zzzzzzerg/InternalStorage",
          "writeStorage",
          "(Ljava/lang/String;Ljava/lang/String;)V");
    }

    jniWriteStorage (filename, data);
  }

}
