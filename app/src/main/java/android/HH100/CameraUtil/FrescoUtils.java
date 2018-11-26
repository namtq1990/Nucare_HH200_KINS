package android.HH100.CameraUtil;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class FrescoUtils {

    FrescoUtils() {}

    public static class FrescoInner {

        FrescoInner() {}

        String mUrl;
        ResizeOptions mResizeOptions;

        /**
         * 适配AutoLayout的辅助方法.
         * 将控件的width(dp), height(dp)传入resize(int, int)，即可将图片向下缩放(DownScale)到与控件一样大.
         *
         * @param widthDp  控件的width(dp)
         * @param heightDp 控件的height(dp)
         * @return FrescoInner
         */
        public FrescoInner resize(int widthDp, int heightDp) {
            mResizeOptions = new ResizeOptions(CommonUtils.dp2px(widthDp), CommonUtils.dp2px(heightDp));
            return this;
        }

        public void into(SimpleDraweeView v) {
            if (TextUtils.isEmpty(mUrl)) {
                v.setImageURI(Uri.EMPTY);
                return;
            }

            PipelineDraweeControllerBuilder controllerBuilder = Fresco.newDraweeControllerBuilder();

            ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(mUrl));

            if (mResizeOptions != null) {
                requestBuilder.setResizeOptions(mResizeOptions);
            } else

            /**
             * 对控件的宽和高在XML中均指定为wrap_content的支持.
             */
                if (v.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT && v.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    controllerBuilder.setControllerListener(new BaseControllerListener<ImageInfo>() {

                        void setDraweeSize(ImageInfo imageInfo) {
                            if (imageInfo == null) return;
                            ViewGroup.LayoutParams params = v.getLayoutParams();
                            //拿到图片的宽高，给控件的LayoutParams设进去
                            params.width = imageInfo.getWidth();
                            params.height = imageInfo.getHeight();
                            v.setLayoutParams(params);
                        }

                        @Override
                        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                            setDraweeSize(imageInfo);
                        }

                        @Override
                        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                            setDraweeSize(imageInfo);
                        }
                    });
                } else

                /**
                 * 对控件的宽和高之中的一项在XML中指定为wrap_content，另外一项为固定大小的支持.
                 * 同时适用于widthOrHeightFixed()被调用的情况.
                 */
                    if (v.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT || v.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        controllerBuilder.setControllerListener(new BaseControllerListener<ImageInfo>() {

                            void makeSizeFixed(ImageInfo imageInfo) {
                                if (imageInfo == null) return;
                                //计算图片的宽高比，给Drawee设置AspectRatio，让Drawee自适应设为wrap_content的那一个维度
                                v.setAspectRatio(((float) imageInfo.getWidth()) / ((float) imageInfo
                                        .getHeight()));
                            }

                            @Override
                            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                                makeSizeFixed(imageInfo);
                            }

                            @Override
                            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                                makeSizeFixed(imageInfo);
                            }
                        });
                    }

            v.setController(controllerBuilder.setImageRequest(requestBuilder.build()).build());
        }
    }

    public static FrescoInner load(String url) {
        FrescoInner frescoInner = new FrescoInner();
        frescoInner.mUrl = url;
        return frescoInner;
    }
}
