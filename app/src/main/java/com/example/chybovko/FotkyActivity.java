package com.example.chybovko;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chybovko.databinding.ActivityFotkyBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FotkyActivity extends AppCompatActivity {
    private ImageView targetImageView;
    private TypFotky typFotky;
    private ActivityFotkyBinding viewBinding;
    private final ActivityResultLauncher<String[]> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        // Zpracování udělení/odmítnutí povolení
                        boolean permissionGranted = true;
                        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                            if (Arrays.asList(REQUIRED_PERMISSIONS).contains(entry.getKey()) && !entry.getValue()) {
                                permissionGranted = false;
                                break;
                            }
                        }
                        if (!permissionGranted) {
                            Toast.makeText(getBaseContext(),
                                    "Permission request denied",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startCamera();
                        }
                    }
            );
    private ImageCapture imageCapture = null;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        viewBinding = ActivityFotkyBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Žádost o povolení kamery
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions();
        }

        // Nastavení prvního typu fotky
        typFotky = TypFotky.CELEK;
        targetImageView = viewBinding.celekImageView;
        Toast.makeText(this, "Teď budeš fotit celek", Toast.LENGTH_SHORT).show();

        // Nastavení typů fotek a zmenšenin po kliknutí na karty
        viewBinding.celekCardView.setOnClickListener(v -> {
            typFotky = TypFotky.CELEK;
            targetImageView = viewBinding.celekImageView;
            Toast.makeText(this, "Teď budeš fotit celek", Toast.LENGTH_SHORT).show();
        });
        viewBinding.detailCardView.setOnClickListener(v -> {
            typFotky = TypFotky.DETAIL;
            targetImageView = viewBinding.detailImageView;
            Toast.makeText(this, "Teď budeš fotit detail", Toast.LENGTH_SHORT).show();
        });
        viewBinding.vykresCardView.setOnClickListener(v -> {
            typFotky = TypFotky.VYKRES;
            targetImageView = viewBinding.vykresImageView;
            Toast.makeText(this, "Teď budeš fotit výkres", Toast.LENGTH_SHORT).show();
        });
        viewBinding.znackaCardView.setOnClickListener(v -> {
            typFotky = TypFotky.ZNACKA;
            targetImageView = viewBinding.znackaImageView;
            Toast.makeText(this, "Teď budeš fotit značku", Toast.LENGTH_SHORT).show();
        });

        // Nastavení posluchačů pro tlačítka pořízení fotografie a videa
        viewBinding.vyfotitButton.setOnClickListener(v -> takePhoto(targetImageView));
        viewBinding.predchoziButton.setOnClickListener(v -> bezNaPredchozi());
        viewBinding.dalsiButton.setOnClickListener(v -> bezNaDalsi());

        cameraExecutor = Executors.newSingleThreadExecutor();

    }

    private void takePhoto(ImageView targetImageView) {
        // Získání stabilní reference na ImageCapture
        ImageCapture imageCapture = this.imageCapture;
        if (imageCapture == null) return;

        ContentResolver resolver = getContentResolver();

        // Nastavení názvu souboru
        String nazevFotky = MainActivity.getChyboveHlaseni().getIncident();
        if (typFotky.equals(TypFotky.VYKRES)) {
            nazevFotky += "_vykres";
        } else if (typFotky.equals(TypFotky.CELEK)) {
            nazevFotky += "_celek";
        } else if (typFotky.equals(TypFotky.ZNACKA)) {
            nazevFotky += "_znacka";
        } else if (typFotky.equals(TypFotky.DETAIL)) {
            nazevFotky += "_detail";
        }

        // Ověření, zda soubor již existuje
        Uri existingUri = null;
        String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = new String[]{nazevFotky + ".jpg"};

        try (Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                selection,
                selectionArgs,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                existingUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            }
        }

        if (existingUri != null) {
            // Pokud existuje, smažte starý soubor
            resolver.delete(existingUri, null, null);
        }

        // Vytvoření položky pro MediaStore
        ImageCapture.OutputFileOptions outputOptions = ziskejVystupniSouborFotky(nazevFotky, resolver);

        // Nastavení posluchače pro pořizování snímků
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
                    }

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Uri savedUri = output.getSavedUri();

                        if (savedUri != null) {
                            // Nastavení cesty k fotce na chybovku
                            if (typFotky.equals(TypFotky.VYKRES)) {
                                MainActivity.getChyboveHlaseni().setCestaVykres(savedUri);
                            } else if (typFotky.equals(TypFotky.CELEK)) {
                                MainActivity.getChyboveHlaseni().setCestaCelek(savedUri);
                            } else if (typFotky.equals(TypFotky.ZNACKA)) {
                                MainActivity.getChyboveHlaseni().setCestaZnacka(savedUri);
                            } else if (typFotky.equals(TypFotky.DETAIL)) {
                                MainActivity.getChyboveHlaseni().setCestaDetail(savedUri);
                            }

                            try {
                                // Načtení obrázku jako bitmapy
                                InputStream inputStream = resolver.openInputStream(savedUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                                // Zmenšení obrázku
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                                        bitmap,
                                        targetImageView.getWidth(), // Šířka ImageView
                                        targetImageView.getHeight(), // Výška ImageView
                                        true
                                );

                                // Nastavení obrázku na ImageView
                                runOnUiThread(() -> targetImageView.setImageBitmap(scaledBitmap));

                            } catch (IOException e) {
                                Log.e(TAG, "Error loading or scaling image: " + e.getMessage(), e);
                            }
                        }
                        String msg = "Photo capture succeeded: " + output.getSavedUri();
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                    }
                }
        );
    }

    private static ImageCapture.OutputFileOptions ziskejVystupniSouborFotky(String nazevFotky, ContentResolver resolver) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, nazevFotky);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        // Vytvoření výstupních možností obsahujících soubor a metadata
        return new ImageCapture.OutputFileOptions
                .Builder(resolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();
    }

    private void startCamera() {
        // Získání instance ProcessCameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Spojení životního cyklu kamery s životním cyklem aktivity
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Nastavení Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());

                // Výběr zadní kamery jako výchozí
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Odpojíme všechny stávající use-cases před novým připojením
                cameraProvider.unbindAll();

                // Připojení use-cases ke kameře
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Selhání při získávání instance ProcessCameraProvider", e);
                Thread.currentThread().interrupt(); // Zajistí ukončení vlákna v případě přerušení
            } catch (Exception exc) {
                Log.e(TAG, "Use case binding failed", exc);
            }
        }, ContextCompat.getMainExecutor(this));
        imageCapture = new ImageCapture.Builder().build();
    }


    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        activityResultLauncher.launch(REQUIRED_PERMISSIONS);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    // Zpracování výsledku požadavku na povolení
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Konstanty
    private static final String TAG = "Fotky Chybovka";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS;

    static {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS = new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        } else {
            REQUIRED_PERMISSIONS = new String[]{
                    android.Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
        }
    }

    private void bezNaPredchozi() {
        Intent intent = new Intent(this, IdentifikaceActivity.class);
        startActivity(intent);
    }

    private void bezNaDalsi() {
        Intent intent = new Intent(this, ReseniActivity.class);
        startActivity(intent);
    }
}