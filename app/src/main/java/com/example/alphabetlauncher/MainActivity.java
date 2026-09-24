package com.example.alphabetlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * Main Activity of Alphabet Launcher.
 */
public class MainActivity
        extends Activity
        implements AlphabetBarView.Listener {

    private static final String PREFS_NAME = "alphabet_launcher_prefs";
    private static final String KEY_FAVOURITES = "favourites";

    /*
     * Theme color scheme.
     */
    private boolean isNightMode;
    private int bgColor;
    private int cardBgColor;
    private int textPrimaryColor;
    private int textSecondaryColor;
    private int searchBgColor;

    /*
     * Main horizontal layout.
     */
    private LinearLayout root;

    /*
     * Main content area.
     */
    private LinearLayout content;

    /*
     * Application list container.
     */
    private LinearLayout appList;

    /*
     * Search input field.
     */
    private EditText searchInput;

    /*
     * Date TextView.
     */
    private TextView dateView;

    /*
     * Alphabet bar.
     */
    private AlphabetBarView alphabetBar;

    /*
     * Complete application list.
     */
    private List<AppInfo> allApps = new ArrayList<>();

    /*
     * Applications grouped by letter.
     */
    private Map<Character, List<AppInfo>> groupedApps;

    /*
     * Favorite package names.
     */
    private Set<String> favouritePackages = new HashSet<>();

    /*
     * Handler used for updating date.
     */
    private final Handler clockHandler = new Handler(Looper.getMainLooper());

    /*
     * Updates date every second.
     */
    private final Runnable clockUpdater = new Runnable() {
        @Override
        public void run() {
            updateDate();
            clockHandler.postDelayed(this, 1000);
        }
    };

    /**
     * Convert dp to pixels.
     */
    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Detect system dark/light theme.
         */
        int uiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        isNightMode = (uiMode == Configuration.UI_MODE_NIGHT_YES);

        bgColor = isNightMode ? 0xFF121216 : 0xFFFAFAFC;
        cardBgColor = isNightMode ? 0xFF1C1C24 : 0xFFF0F0F5;
        textPrimaryColor = isNightMode ? 0xFFF0F0F5 : 0xFF18181C;
        textSecondaryColor = isNightMode ? 0xFF8E8E98 : 0xFF6E6E78;
        searchBgColor = isNightMode ? 0xFF1C1C24 : 0xFFEBEBF0;

        getWindow().setStatusBarColor(bgColor);
        getWindow().setNavigationBarColor(bgColor);

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (!isNightMode) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        /*
         * Load all installed apps and saved favourites.
         */
        loadAppsOnce();
        loadFavourites();

        /*
         * Create interface.
         */
        buildUi();

        /*
         * Show home screen.
         */
        showHome();
    }

    /**
     * Loads applications only once.
     */
    private void loadAppsOnce() {
        allApps = AppRepository.loadLaunchableApps(this);
        groupedApps = AppRepository.groupByLetter(allApps);
    }

    /**
     * Load favourite app packages from SharedPreferences.
     */
    private void loadFavourites() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> saved = prefs.getStringSet(KEY_FAVOURITES, null);
        favouritePackages = new HashSet<>();
        if (saved != null && !saved.isEmpty()) {
            favouritePackages.addAll(saved);
        } else {
            // Default first 6 apps as favourites
            int count = Math.min(6, allApps.size());
            for (int i = 0; i < count; i++) {
                favouritePackages.add(allApps.get(i).packageName);
            }
            saveFavourites();
        }
    }

    /**
     * Save favourite app packages to SharedPreferences.
     */
    private void saveFavourites() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_FAVOURITES, favouritePackages)
                .apply();
    }

    /**
     * Toggle favourite status for an app.
     */
    private void toggleFavourite(String packageName) {
        if (favouritePackages.contains(packageName)) {
            favouritePackages.remove(packageName);
            Toast.makeText(this, "Removed from Favourites", Toast.LENGTH_SHORT).show();
        } else {
            favouritePackages.add(packageName);
            Toast.makeText(this, "Added to Favourites", Toast.LENGTH_SHORT).show();
        }
        saveFavourites();
        showHome();
    }

    /**
     * Build the root interface with Edge-to-Edge WindowInsets support.
     */
    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(bgColor);

        /*
         * Handle system window insets for status/navigation bar spacing.
         */
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(16), dp(12), dp(16));

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
        );

        root.addView(content, contentParams);

        alphabetBar = new AlphabetBarView(this, this);
        alphabetBar.setDarkMode(isNightMode);
        alphabetBar.setActiveLetters(groupedApps.keySet());

        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                dp(64), LinearLayout.LayoutParams.MATCH_PARENT
        );

        root.addView(alphabetBar, barParams);

        setContentView(root);
    }

    /**
     * Displays the home screen.
     */
    private void showHome() {
        content.removeAllViews();

        // ------------------------------------------
        // CLOCK
        // ------------------------------------------
        TextClock clock = new TextClock(this);
        clock.setFormat12Hour("h:mm");
        clock.setFormat24Hour("HH:mm");
        clock.setTextColor(textPrimaryColor);
        clock.setTextSize(58);
        clock.setTypeface(Typeface.DEFAULT_BOLD);

        content.addView(
                clock,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        // ------------------------------------------
        // DATE
        // ------------------------------------------
        dateView = new TextView(this);
        dateView.setTextColor(textSecondaryColor);
        dateView.setTextSize(15);

        content.addView(
                dateView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(32)
                )
        );

        // ------------------------------------------
        // SEARCH BAR
        // ------------------------------------------
        content.addView(createSearchBar());

        // ------------------------------------------
        // FAVOURITES TITLE
        // ------------------------------------------
        TextView title = new TextView(this);
        title.setText("Favourites");
        title.setTextColor(textPrimaryColor);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(12), 0, dp(8));

        content.addView(title);

        // ------------------------------------------
        // FAVOURITES APP LIST
        // ------------------------------------------
        appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);

        showHomeContent();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(appList);

        content.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );

        updateDate();
    }

    /**
     * Fill home app list with user favourite apps.
     */
    private void showHomeContent() {
        if (appList == null) return;
        appList.removeAllViews();

        List<AppInfo> favs = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (favouritePackages.contains(app.packageName)) {
                favs.add(app);
            }
        }

        if (favs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No favourite apps pinned yet.\nLong-press any app to pin it here!");
            empty.setTextColor(textSecondaryColor);
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, dp(24));
            appList.addView(empty);
        } else {
            for (AppInfo app : favs) {
                addAppRow(appList, app);
            }
        }
    }

    /**
     * Create interactive search bar layout.
     */
    private View createSearchBar() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dp(14), dp(8), dp(14), dp(8));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(searchBgColor);
        bg.setCornerRadius(dp(16));
        container.setBackground(bg);

        TextView icon = new TextView(this);
        icon.setText("🔍");
        icon.setTextSize(14);
        icon.setGravity(Gravity.CENTER);
        icon.setPadding(0, 0, dp(8), 0);
        container.addView(icon);

        searchInput = new EditText(this);
        searchInput.setHint("Search apps...");
        searchInput.setHintTextColor(textSecondaryColor);
        searchInput.setTextColor(textPrimaryColor);
        searchInput.setTextSize(15);
        searchInput.setBackground(null);
        searchInput.setSingleLine(true);
        searchInput.setGravity(Gravity.CENTER_VERTICAL);
        searchInput.setPadding(dp(2), 0, dp(2), 0);

        TextView clearBtn = new TextView(this);
        clearBtn.setText("✕");
        clearBtn.setTextColor(textSecondaryColor);
        clearBtn.setTextSize(15);
        clearBtn.setGravity(Gravity.CENTER);
        clearBtn.setPadding(dp(8), 0, 0, 0);
        clearBtn.setVisibility(View.GONE);
        clearBtn.setOnClickListener(v -> searchInput.setText(""));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    clearBtn.setVisibility(View.GONE);
                    showHomeContent();
                } else {
                    clearBtn.setVisibility(View.VISIBLE);
                    showSearchResults(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        container.addView(
                searchInput,
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        );

        container.addView(clearBtn);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
        );
        lp.setMargins(0, dp(8), 0, dp(12));
        container.setLayoutParams(lp);

        return container;
    }

    /**
     * Display live search results across all apps.
     */
    private void showSearchResults(String query) {
        if (appList == null) return;
        appList.removeAllViews();

        String lower = query.toLowerCase();
        List<AppInfo> matches = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (app.label.toLowerCase().contains(lower) || app.packageName.toLowerCase().contains(lower)) {
                matches.add(app);
            }
        }

        if (matches.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No apps found");
            empty.setTextColor(textSecondaryColor);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(32), 0, dp(32));
            appList.addView(empty);
        } else {
            for (AppInfo app : matches) {
                addAppRow(appList, app);
            }
        }
    }

    /**
     * Displays apps belonging to a selected letter.
     */
    private void showLetter(char letter) {
        content.removeAllViews();

        TextView header = new TextView(this);
        header.setText(String.valueOf(letter));
        header.setTextColor(textPrimaryColor);
        header.setTextSize(44);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setPadding(0, dp(16), 0, dp(8));

        content.addView(
                header,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        List<AppInfo> apps = groupedApps.get(letter);

        appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);

        if (apps == null || apps.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No apps");
            empty.setTextColor(textSecondaryColor);
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER_VERTICAL);

            appList.addView(
                    empty,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(72)
                    )
            );
        } else {
            for (AppInfo app : apps) {
                addAppRow(appList, app);
            }
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(appList);

        content.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );
    }

    /**
     * Adds one application row with press scaling and long-press options.
     */
    private void addAppRow(LinearLayout parent, AppInfo app) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(8), dp(14), dp(8));

        row.setBackground(makeRoundedBackground());

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.icon);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        row.addView(
                icon,
                new LinearLayout.LayoutParams(dp(44), dp(44))
        );

        TextView label = new TextView(this);
        label.setText(app.label);
        label.setTextColor(textPrimaryColor);
        label.setTextSize(16);
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setPadding(dp(14), 0, 0, 0);

        row.addView(
                label,
                new LinearLayout.LayoutParams(0, dp(56), 1f)
        );

        if (favouritePackages.contains(app.packageName)) {
            TextView favBadge = new TextView(this);
            favBadge.setText("★");
            favBadge.setTextColor(textSecondaryColor);
            favBadge.setTextSize(14);
            favBadge.setPadding(dp(4), 0, dp(4), 0);
            row.addView(favBadge);
        }

        row.setClickable(true);
        row.setFocusable(true);

        row.setOnClickListener(v -> launchApp(app));

        row.setOnLongClickListener(v -> {
            showAppOptions(app);
            return true;
        });

        row.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(64)
        );
        rowParams.setMargins(0, 0, 0, dp(8));

        parent.addView(row, rowParams);
    }

    /**
     * Display options for a long-pressed application.
     */
    private void showAppOptions(AppInfo app) {
        boolean isFav = favouritePackages.contains(app.packageName);
        String favAction = isFav ? "Unpin from Favourites" : "Pin to Favourites";

        String[] options = {favAction, "App Info"};

        new AlertDialog.Builder(this)
                .setTitle(app.label)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        toggleFavourite(app.packageName);
                    } else if (which == 1) {
                        openAppDetails(app.packageName);
                    }
                })
                .show();
    }

    /**
     * Open system settings details for an app.
     */
    private void openAppDetails(String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open app info", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates rounded app-row background with ripple effect.
     */
    private RippleDrawable makeRoundedBackground() {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(cardBgColor);
        shape.setCornerRadius(dp(18));

        ColorStateList rippleColor = ColorStateList.valueOf(
                isNightMode ? 0x22FFFFFF : 0x11000000
        );

        return new RippleDrawable(rippleColor, shape, null);
    }

    /**
     * Launch selected application.
     */
    private void launchApp(AppInfo app) {
        PackageManagerHelper.launch(this, app.packageName);
    }

    @Override
    public void onLetterChanged(char letter) {
        showLetter(letter);
    }

    @Override
    public void onDragStarted() {
        if (searchInput != null) {
            searchInput.clearFocus();
        }
    }

    @Override
    public void onDragFinished() {
        showHome();
    }

    @Override
    public void onStarClicked() {
        showHome();
    }

    /**
     * Updates the date.
     */
    private void updateDate() {
        if (dateView == null) return;
        SimpleDateFormat format = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        dateView.setText(format.format(new Date()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        clockHandler.post(clockUpdater);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockUpdater);
    }

    private static final class PackageManagerHelper {
        static void launch(Context context, String packageName) {
            try {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                } else {
                    Toast.makeText(context, "Unable to open app", Toast.LENGTH_SHORT).show();
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Unable to open app", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
