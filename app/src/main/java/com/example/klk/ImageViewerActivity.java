package com.example.klk;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import android.graphics.drawable.Drawable;

/**
 * Activity para visualizar imágenes en pantalla completa
 * Principios aplicados:
 * - SRP (Single Responsibility): Solo se encarga de mostrar imágenes
 * - KISS: Implementación simple y directa
 * - YAGNI: Solo incluye funcionalidad esencial (zoom básico con pinch)
 */
public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_SENDER_NAME = "sender_name";

    private ImageView imageView;
    private ImageButton buttonClose;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        initializeViews();
        setupFullscreenMode();
        loadImage();
        setupListeners();
    }

    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        imageView = findViewById(R.id.imageViewFullscreen);
        buttonClose = findViewById(R.id.buttonCloseImage);
        progressBar = findViewById(R.id.progressBarImage);
    }

    /**
     * Configura el modo pantalla completa inmersivo
     */
    private void setupFullscreenMode() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * Carga la imagen desde la URL proporcionada
     */
    private void loadImage() {
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Usar Glide con listener para manejar estados de carga
        Glide.with(this)
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model,
                                               Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ImageViewerActivity.this,
                                "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        buttonClose.setOnClickListener(v -> finish());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setupFullscreenMode();
        }
    }
}

