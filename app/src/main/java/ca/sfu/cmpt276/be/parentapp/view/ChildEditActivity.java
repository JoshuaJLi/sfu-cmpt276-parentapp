package ca.sfu.cmpt276.be.parentapp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.transition.platform.MaterialArcMotion;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.Objects;

import ca.sfu.cmpt276.be.parentapp.R;
import ca.sfu.cmpt276.be.parentapp.controller.ChildManager;
import ca.sfu.cmpt276.be.parentapp.controller.ImageManager;
import ca.sfu.cmpt276.be.parentapp.model.Child;

/**
 * ChildEditActivity manages the creation and edit of a single child in the app.
 */
public class ChildEditActivity extends AppCompatActivity{
    private static final String EXTRA_CHILD_LOCATION = "childLocation";
    private static final String EXTRA_DO_EDIT = "doEdit";

    private boolean doEdit;
    private boolean didUserEdit = false;
    private int childPosition;
    private Child currentChild;
    private Uri newPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupAnimation();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_child);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.text_editChild);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getGalleryExtraction();
        getExtras();

        setUpPortrait();
        setupTextWatcher();
    }

    @Override
    public void onBackPressed() {
        generateBackWarnDialog();
    }

    @Override
    public boolean onSupportNavigateUp() {
        generateBackWarnDialog();
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_delete_appbar, menu);
        MenuItem deleteOverflow = menu.findItem(R.id.item_delete);
        if (!doEdit) {
            deleteOverflow.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_save) {
            checkForSaving();
        }

        if (item.getItemId() == R.id.item_delete) {
            generateDeleteWarnDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void deleteChild() {
        ChildManager childManager = new ChildManager();
        childManager.remove(childPosition);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, ChildEditActivity.class);
    }

    public static Intent makeIntent(Context context, int childPosition) {
        Intent editChildIntent = new Intent(context, ChildEditActivity.class);

        editChildIntent.putExtra(EXTRA_CHILD_LOCATION, childPosition);
        editChildIntent.putExtra(EXTRA_DO_EDIT, true);

        return editChildIntent;
    }

    private void setUpPortrait() {
        ImageView childPortrait = findViewById(R.id.image_child_portrait);
        ImageManager imageManager = new ImageManager();
        childPortrait.setImageBitmap(imageManager.getPortrait(ChildEditActivity.this, currentChild.getId()));
    }

    private void checkForSaving() {
        EditText childNameEditText = findViewById(R.id.field_child_name);
        String newName = childNameEditText.getText().toString();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the child.", Toast.LENGTH_SHORT).show();
        } else {
            save(newName);
            finish();
        }
    }

    private void save(String newName) {
        ChildManager childManager = new ChildManager();
        if (doEdit) {
            childManager.edit(childPosition, newName);
        } else {
            currentChild.setName(newName);
            childManager.add(currentChild);
        }

        if (didUserEdit && newPhoto != null) {
            ImageManager imageManager = new ImageManager();
            try {
                imageManager.savePortrait(ChildEditActivity.this, newPhoto, currentChild.getId());
            } catch (IOException e) {
                e.printStackTrace();
                generateWarningDialog();
            }
        }
    }

    private void deleteAndExit() {
        ImageManager imageManager = new ImageManager();
        if (doEdit) {
            deleteExistingChild();
        }
        imageManager.deletePortrait(ChildEditActivity.this, currentChild.getId());
        finish();
    }

    private void deleteExistingChild() {
        ChildManager childManager = new ChildManager();
        childManager.remove(childPosition);
    }

    private void getExtras() {
        ChildManager childManager = new ChildManager();
        Intent intent = getIntent();
        doEdit = intent.getBooleanExtra(EXTRA_DO_EDIT, false);
        childPosition = intent.getIntExtra(EXTRA_CHILD_LOCATION, -1);

        if (doEdit) {
            EditText childNameEditText = findViewById(R.id.field_child_name);
            String childName = childManager.get(childPosition).getName();
            childNameEditText.setText(childName);
            currentChild = childManager.get(childPosition);
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.text_addChild);
            currentChild = new Child(null);
        }
    }

    private void getGalleryExtraction() {
        Button changeImage = findViewById(R.id.button_add_image);
        ActivityResultLauncher<String> getContent;

        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                            prepareImage(result);
                    }
                });

        changeImage.setOnClickListener(view -> getContent.launch("image/*"));
    }

    private void prepareImage(Uri image) {
        ImageView childPortrait = findViewById(R.id.image_child_portrait);
        childPortrait.setImageURI(image);
        newPhoto = image;
        didUserEdit = true;
    }

    private void setupTextWatcher() {
        TextView childField = findViewById(R.id.field_child_name);
        childField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                didUserEdit = true;
            }
        });
    }

    private void generateWarningDialog() {
        new MaterialAlertDialogBuilder(ChildEditActivity.this)
                .setTitle("Error")
                .setMessage("Unable to save image")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void generateDeleteWarnDialog() {
        new MaterialAlertDialogBuilder(ChildEditActivity.this)
                .setTitle("Deleting Child")
                .setMessage("Are you sure you want to do this?")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteAndExit())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void generateBackWarnDialog() {
        if (didUserEdit) {
            new MaterialAlertDialogBuilder(ChildEditActivity.this)
                    .setTitle("Discard Changes")
                    .setMessage("Changes will not be saved, are you sure?")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> exitWithBack())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            exitWithBack();
        }
    }

    private void exitWithBack() {
        super.onBackPressed();
    }

    private void setupAnimation() {
        findViewById(android.R.id.content).setTransitionName("shared_container");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.addTarget(android.R.id.content);
        transform.setAllContainerColors(MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface));
        transform.setFitMode(MaterialContainerTransform.FIT_MODE_AUTO);
        transform.setDuration(666);
        transform.setPathMotion(new MaterialArcMotion());
        transform.setInterpolator(new FastOutSlowInInterpolator());
        MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface);
        transform.setScrimColor(Color.TRANSPARENT);
        getWindow().setSharedElementEnterTransition(transform);

    }
}
