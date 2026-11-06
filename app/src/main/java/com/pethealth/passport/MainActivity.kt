package com.pethealth.passport

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.pethealth.passport.databinding.ActivityMainBinding
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.util.AppLocale
import com.pethealth.passport.util.Dates
import com.pethealth.passport.util.PetQr
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val app by lazy { application as PetHealthApp }

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { showQrResult(it) }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(app.container.settings.language)
            )
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        setSupportActionBar(binding.toolbar)
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        val topLevel = setOf(
            R.id.homeFragment,
            R.id.petListFragment,
            R.id.communityFragment,
            R.id.moreFragment
        )
        val appBarConfiguration = AppBarConfiguration(topLevel)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.visibility =
                if (destination.id == R.id.homeFragment) View.GONE else View.VISIBLE
        }

        handleNotificationIntent(savedInstanceState)
        requestNotificationPermission()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            // Toolbar sits below the status bar/cutout, content above the keyboard.
            view.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = ime.bottom
            )
            // Let the bottom navigation's surface color fill the gesture/nav bar area.
            binding.bottomNav.updatePadding(
                bottom = if (ime.bottom > 0) 0 else bars.bottom
            )
            windowInsets
        }
    }

    fun startQrScan() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt(getString(R.string.qr_scan_prompt))
            .setBeepEnabled(false)
            .setOrientationLocked(false)
            .setBarcodeImageEnabled(false)
        runCatching { scanLauncher.launch(options) }
            .onFailure { toast(getString(R.string.qr_error_camera)) }
    }

    private fun showQrResult(raw: String) {
        val profile = PetQr.decode(raw)
        if (profile == null) {
            toast(getString(R.string.qr_invalid))
            return
        }
        lifecycleScope.launch {
            val pet = app.container.petRepository.getPet(profile.id)
            if (pet != null) {
                navController.navigate(R.id.petDetailFragment, bundleOf("petId" to pet.id))
            } else {
                val message = buildString {
                    appendLine("${getString(R.string.pet_name)}: ${profile.name}")
                    appendLine("${getString(R.string.pet_species)}: ${profile.species}")
                    if (profile.breed.isNotBlank()) appendLine("${getString(R.string.pet_breed)}: ${profile.breed}")
                    if (profile.ownerName.isNotBlank()) appendLine("${getString(R.string.qr_owner_label)}: ${profile.ownerName}")
                    if (profile.ownerPhone.isNotBlank()) appendLine("${getString(R.string.qr_phone_label)}: ${profile.ownerPhone}")
                    if (profile.latestVaccine.isNotBlank()) {
                        appendLine("${getString(R.string.qr_vaccine_label)}: ${profile.latestVaccine}")
                    }
                    profile.nextDue?.let {
                        append("${getString(R.string.vaccination_next_due)}: ${Dates.format(it)}")
                    }
                }
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.qr_not_found)
                    .setMessage(message.trim())
                    .setPositiveButton(R.string.common_close, null)
                    .show()
            }
        }
    }

    private fun handleNotificationIntent(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) return
        val petId = intent.getLongExtra(EXTRA_PET_ID, 0L)
        if (petId > 0) {
            intent.removeExtra(EXTRA_PET_ID)
            binding.root.post {
                runCatching {
                    navController.navigate(R.id.petDetailFragment, bundleOf("petId" to petId))
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            app.container.settings.reminderEnabled
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    companion object {
        const val EXTRA_PET_ID = "pet_id"
    }
}
