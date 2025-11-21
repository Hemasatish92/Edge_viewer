
#include <jni.h>
#include <vector>
#include <android/log.h>
#include <mutex>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

static vector<uint8_t> outBuf;
static mutex mtx;
static int mode = 1;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativeBridge_setMode(JNIEnv*, jclass, jint m) {
    mode = m;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativeBridge_processFrame(JNIEnv* env, jclass, jbyteArray arr, jint w, jint h) {
    jbyte* data = env->GetByteArrayElements(arr, nullptr);
    if (!data) return;

    Mat yuv(h + h/2, w, CV_8UC1, (uchar*)data);
    Mat bgr;
    cvtColor(yuv, bgr, COLOR_YUV2BGR_NV21);

    Mat rgba;
    if (mode == 1) {
        Mat g;
        cvtColor(bgr, g, COLOR_BGR2GRAY);
        Canny(g, g, 50, 150);
        cvtColor(g, rgba, COLOR_GRAY2RGBA);
    } else {
        cvtColor(bgr, rgba, COLOR_BGR2RGBA);
    }

    lock_guard<mutex> lock(mtx);
    outBuf.assign(rgba.data, rgba.data + rgba.total() * 4);

    env->ReleaseByteArrayElements(arr, data, JNI_ABORT);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeviewer_NativeBridge_getProcessedBuffer(JNIEnv* env, jclass) {
    lock_guard<mutex> lock(mtx);
    jbyteArray a = env->NewByteArray(outBuf.size());
    env->SetByteArrayRegion(a, 0, outBuf.size(), (jbyte*)outBuf.data());
    return a;
}
