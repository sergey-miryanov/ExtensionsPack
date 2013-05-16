package ru.zzzzzzerg;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import org.haxe.nme.GameActivity;

public class InternalStorage
{
  public static String readStorage (String filename)
  {
    StringBuffer fileContent = new StringBuffer ("");
    try
    {
      GameActivity ga = GameActivity.getInstance ();

      File filePath = ga.getBaseContext ().getFileStreamPath (filename);
      if (filePath.exists ())
      {
        FileInputStream toRead = ga.openFileInput (filename);
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = toRead.read (buffer)) != -1)
        {
          fileContent.append (new String (buffer, 0, length));
        }
        toRead.close ();
      }

      return fileContent.toString ();
    }
    catch (IOException e)
    {
      return fileContent.toString ();
    }
  }

  public static void writeStorage (String filename, String data)
  {
    try
    {
      GameActivity ga = GameActivity.getInstance ();
      FileOutputStream toWrite = ga.openFileOutput (filename, Context.MODE_PRIVATE);
      toWrite.write (data.getBytes ());
      toWrite.close ();
    }
    catch (IOException e)
    {
    }
  }
}
