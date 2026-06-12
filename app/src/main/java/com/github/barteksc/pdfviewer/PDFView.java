package com.github.barteksc.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class PDFView extends ScrollView {

    private final LinearLayout container;
    private final int pageSpacingPx;
    private RenderTask currentTask;
    private Uri pendingUri;

    public PDFView(Context context) {
        this(context, null);
    }

    public PDFView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PDFView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        pageSpacingPx = (int) (8 * getResources().getDisplayMetrics().density);
        setBackgroundColor(Color.WHITE);
        setFillViewport(true);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pageSpacingPx, pageSpacingPx, pageSpacingPx, pageSpacingPx);
        addView(container, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public Configurator fromUri(Uri uri) {
        return new Configurator(uri);
    }

    private void loadFromUri(Uri uri) {
        pendingUri = uri;
        if (getWidth() == 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (pendingUri != null) {
                        loadFromUri(pendingUri);
                    }
                }
            });
            return;
        }

        if (currentTask != null) {
            currentTask.cancel(true);
        }

        clearContent();
        currentTask = new RenderTask(uri);
        currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void clearContent() {
        container.removeAllViews();
    }

    private void showError() {
        clearContent();

        TextView errorView = new TextView(getContext());
        errorView.setTextColor(Color.DKGRAY);
        errorView.setPadding(pageSpacingPx, pageSpacingPx, pageSpacingPx, pageSpacingPx);
        errorView.setText("No se pudo abrir el PDF.");
        container.addView(errorView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onDetachedFromWindow() {
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
        super.onDetachedFromWindow();
    }

    public final class Configurator {
        private final Uri uri;

        private Configurator(Uri uri) {
            this.uri = uri;
        }

        public void load() {
            loadFromUri(uri);
        }
    }

    private final class RenderTask extends AsyncTask<Void, Bitmap, Exception> {
        private final Uri uri;
        private ParcelFileDescriptor fileDescriptor;
        private PdfRenderer renderer;

        private RenderTask(Uri uri) {
            this.uri = uri;
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            try {
                fileDescriptor = openDescriptor(uri);
                if (fileDescriptor == null) {
                    return new IOException("Unable to open PDF file descriptor");
                }

                renderer = new PdfRenderer(fileDescriptor);
                int availableWidth = Math.max(getWidth() - getPaddingLeft() - getPaddingRight() - (pageSpacingPx * 2), 1);

                for (int index = 0; index < renderer.getPageCount() && !isCancelled(); index++) {
                    PdfRenderer.Page page = renderer.openPage(index);
                    int pageWidth = Math.max(page.getWidth(), 1);
                    int pageHeight = Math.max(page.getHeight(), 1);
                    int bitmapWidth = availableWidth;
                    int bitmapHeight = Math.max((bitmapWidth * pageHeight) / pageWidth, 1);

                    Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();

                    publishProgress(bitmap);
                }

                return null;
            } catch (Exception e) {
                return e;
            } finally {
                closeRenderer();
            }
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            if (values == null || values.length == 0 || isCancelled()) {
                return;
            }

            ImageView imageView = new ImageView(getContext());
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageBitmap(values[0]);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = pageSpacingPx;
            container.addView(imageView, params);
        }

        @Override
        protected void onPostExecute(Exception error) {
            currentTask = null;
            if (error != null || container.getChildCount() == 0) {
                showError();
            }
        }

        @Override
        protected void onCancelled() {
            currentTask = null;
            closeRenderer();
        }

        private ParcelFileDescriptor openDescriptor(Uri uri) throws IOException {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getContext().getContentResolver().openFileDescriptor(uri, "r");
            }

            File file = new File(uri.getPath());
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        }

        private void closeRenderer() {
            if (renderer != null) {
                renderer.close();
                renderer = null;
            }

            if (fileDescriptor != null) {
                try {
                    fileDescriptor.close();
                } catch (IOException ignored) {
                }
                fileDescriptor = null;
            }
        }
    }
}
