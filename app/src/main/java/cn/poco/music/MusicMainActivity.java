package cn.poco.music;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class MusicMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        makeBitmap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                makeBitmap();
            }
        },1000);
    }

    private void makeBitmap(){
        WaveBitmapFactory waveBitmapFactory = new WaveBitmapFactory(1000,200);
//        InputStream is = getResources().openRawResource(R.raw.huanlesong);
//        File
        long start = System.currentTimeMillis();
        Log.i("1", "start: "+start);
        String out = Environment.getExternalStorageDirectory() + "/hls.mp3";
//        String out = Environment.getExternalStorageDirectory() +"/mywav.wav";
//       VideoUtils.changeToAac(path,out);
//        Log.i("1", "changeToAac: "+(System.currentTimeMillis()-start));
        waveBitmapFactory.setSoundFilePath(out);
        Log.i("1", "setSoundFilePath: "+(System.currentTimeMillis()-start));
        waveBitmapFactory.setWaveLineSpan(10);
        Log.i("1", "setWaveLineSpan: "+(System.currentTimeMillis()-start));
        waveBitmapFactory.draw();
        Log.i("1", "draw: "+(System.currentTimeMillis()-start));
        Bitmap bitmap = waveBitmapFactory.getBitmap();
        WaveBitmapFactory.saveBitmap(bitmap,Environment.getExternalStorageDirectory() + "/testpcmjoint.png");
        Log.i("1", "saveBitmap: "+(System.currentTimeMillis()-start));
    }
}
