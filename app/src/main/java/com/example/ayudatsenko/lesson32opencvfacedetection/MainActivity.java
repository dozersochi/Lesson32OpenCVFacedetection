package com.example.ayudatsenko.lesson32opencvfacedetection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

// класс применяет интерфейс из opencv и сразу же переопределяет 3 метода
// onCameraViewStarted onCameraViewStopped и onCameraFrame
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    // объявляем тег для дебага если не пойдет загрузка модуля opencv
    public static String TAG="Main Activity";

    JavaCameraView javaCameraView;

    // прописываем коллбэклоадер для библиотеки. Это основная реализация
    // LoaderCallback интерфейса, сразу переопределем метод onManagerConnected
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    // определяем класс n-мерного массива для работы с изображением с камеры
    Mat mRgba;

    // подключаем библиотеки нативного С++ которые сделали
    // и базовую opencv_java для нормального линка по натив, т.к. в 4.2. не линкуется без этого
    static {
        System.loadLibrary("opencv_java");
        System.loadLibrary("MyLibs");
    };


    // основной метод
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);

        // ставим на вьюшку через SurfaceView
        javaCameraView.setVisibility(SurfaceView.VISIBLE);

        // вешаем на нее listener
        javaCameraView.setCvCameraViewListener(this);
    }

    // переопределяем метод на паузу(отключаем воспроизведение при паузе приложения)
    @Override
    protected void onPause(){
        super.onPause();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    // переопределяем метод на уничтожение(отключаем воспроизведение при уничтожении)
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    //переопределяем метод на старт/возобновление
    @Override
    protected void onResume(){
        super.onResume();

        // проверяем инициализирован ли openCV
        // если да(true), то ставим наш коллбэк из п.26
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV loaded sucsess!");
            mLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }

        // если нет то запускаем заново инициализацию методов opencv
        else {
            Log.d(TAG, "OpenCV NOT Loaded!");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        }

    }

    //переопределяем метод старта view
    @Override
    public void onCameraViewStarted(int width, int height) {
        // инициализируем Mat значениями
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    //переопределяем метод стопа view
    @Override
    public void onCameraViewStopped() {
        // распускаем Mat...
        mRgba.release();
    }

    //переопределяем метод получения с камеры, и работаем с изображением
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();

        // вызываем наш нативный метод класса по поиску лиц/глаз
        OpenCV.faceDetection(mRgba.getNativeObjAddr());

        // отдаем результат
        return mRgba;
    }
}
